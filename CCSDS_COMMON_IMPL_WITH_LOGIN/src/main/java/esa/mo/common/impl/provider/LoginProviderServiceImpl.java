/* ----------------------------------------------------------------------------
 * Copyright (C) 2015      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : ESA NanoSat MO Framework
 * ----------------------------------------------------------------------------
 * Licensed under the European Space Agency Public License, Version 2.0
 * You may not use this file except in compliance with the License.
 *
 * Except as expressly set forth in this License, the Software is provided to
 * You on an "as is" basis and without warranties of any kind, including without
 * limitation merchantability, fitness for a particular purpose, absence of
 * defects or errors, accuracy or non-infringement of intellectual property rights.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 * ----------------------------------------------------------------------------
 */
package esa.mo.common.impl.provider;

import esa.mo.com.impl.util.COMServicesProvider;
import esa.mo.com.impl.util.HelperArchive;
import esa.mo.common.impl.util.LoginServiceSecurityUtils;
import esa.mo.helpertools.connections.ConnectionProvider;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.ccsds.moims.mo.com.COMHelper;
import org.ccsds.moims.mo.com.COMService;
import org.ccsds.moims.mo.com.structures.ObjectId;
import org.ccsds.moims.mo.com.structures.ObjectKey;
import org.ccsds.moims.mo.com.structures.ObjectType;
import org.ccsds.moims.mo.common.CommonHelper;
import org.ccsds.moims.mo.common.login.LoginHelper;
import org.ccsds.moims.mo.common.login.body.HandoverResponse;
import org.ccsds.moims.mo.common.login.body.LoginResponse;
import org.ccsds.moims.mo.common.login.provider.LoginInheritanceSkeleton;
import org.ccsds.moims.mo.common.login.structures.Profile;
import org.ccsds.moims.mo.common.login.structures.ProfileList;
import org.ccsds.moims.mo.mal.MALContextFactory;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALHelper;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.MALStandardError;
import org.ccsds.moims.mo.mal.provider.MALInteraction;
import org.ccsds.moims.mo.mal.provider.MALProvider;
import org.ccsds.moims.mo.mal.structures.Blob;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.IdentifierList;
import org.ccsds.moims.mo.mal.structures.LongList;

/**
 *
 */
public class LoginProviderServiceImpl extends LoginInheritanceSkeleton {

    protected static COMService service;

    private MALProvider loginServiceProvider;
    private boolean initialiased = false;
    private boolean running = false;
    private final ConnectionProvider connection = new ConnectionProvider();
    private COMServicesProvider comServices; 
    private Long loginInstanceId;
    private Long loginEventId;
    private Subject currentUser;
      
    // load shiro configuration
    private final Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
    private final SecurityManager securityManager = factory.getInstance();
    
    /**
     * creates the MAL objects, the publisher used to create updates and starts
     * the publishing thread
     *
     * @param comServices
     * @throws MALException On initialisation error.
     */
    public synchronized void init(COMServicesProvider comServices) throws MALException {
        if (!initialiased) {
            if (MALContextFactory.lookupArea(MALHelper.MAL_AREA_NAME, MALHelper.MAL_AREA_VERSION) == null) {
                MALHelper.init(MALContextFactory.getElementFactoryRegistry());
            }

            if (MALContextFactory.lookupArea(COMHelper.COM_AREA_NAME, COMHelper.COM_AREA_VERSION) == null) {
                COMHelper.deepInit(MALContextFactory.getElementFactoryRegistry());
            }

            if (MALContextFactory.lookupArea(CommonHelper.COMMON_AREA_NAME, CommonHelper.COMMON_AREA_VERSION) == null) {
                CommonHelper.init(MALContextFactory.getElementFactoryRegistry());
            }

            try {
                LoginHelper.init(MALContextFactory.getElementFactoryRegistry());
            } catch (MALException ex) {
                // nothing to be done..
            }

        }

        // shut down old service transport
        if (null != loginServiceProvider) {
            connection.closeAll();
        }
        
        // set the SecurityManager
        SecurityUtils.setSecurityManager(securityManager);
        
        // get the currently executing user
        this.currentUser = SecurityUtils.getSubject();

        service = LoginHelper.LOGIN_SERVICE;
        loginServiceProvider = connection.startService(LoginHelper.LOGIN_SERVICE_NAME.toString(), 
                LoginHelper.LOGIN_SERVICE, false, this);
        this.comServices = comServices;

        running = true;
        initialiased = true;
        Logger.getLogger(LoginProviderServiceImpl.class.getName()).info("Login service READY");
    }

    /**
     * Closes all running threads and releases the MAL resources.
     */
    public void close() {
        try {
            if (null != loginServiceProvider) {
                loginServiceProvider.close();
            }

            connection.closeAll();
            running = false;
        } catch (MALException ex) {
            Logger.getLogger(LoginProviderServiceImpl.class.getName()).log(Level.WARNING, 
                    "Exception during close down of the provider {0}", ex);
        }
    }

    @Override
    public LoginResponse login(Profile prfl, String string, MALInteraction mali) throws MALInteractionException, MALException {
        
        // 3.3.7.2.a
        if (prfl == null) {
            throw new IllegalArgumentException("profile argument must not be null");
        }
        
        Identifier inputUsername = prfl.getUsername(); 
        Long inputRole = prfl.getRole();
        
        // 3.3.7.2.b
        if (inputUsername.toString().equals("*") || inputUsername.toString().isEmpty()) {
            throw new MALInteractionException(new MALStandardError(COMHelper.INVALID_ERROR_NUMBER, null));
        }
        
        // 3.3.7.2.c
        if (inputRole == null) {
            throw new MALInteractionException(new MALStandardError(COMHelper.INVALID_ERROR_NUMBER, null));
        }
        
        LoginResponse response = null;
        
        // login the current user
        if (!this.currentUser.isAuthenticated()) {
            UsernamePasswordToken token = new UsernamePasswordToken(inputUsername.toString(), string);
            //token.setRememberMe(true);
            try {
                this.currentUser.login(token);
                if (this.currentUser.hasRole(String.valueOf(inputRole))) {
                
                    // 3.3.3.b - LoginRole
                    IdentifierList objs = new IdentifierList();
                    objs.add(new Identifier(String.valueOf(inputRole)));
                    LongList roleIds = this.comServices.getArchiveService().store(true, 
                            LoginHelper.LOGINROLE_OBJECT_TYPE,
                            connection.getPrimaryConnectionDetails().getDomain(),
                            HelperArchive.generateArchiveDetailsList(null, 
                                    null,
                                    this.connection.getPrimaryConnectionDetails().getProviderURI()),
                            objs,
                            mali);
        
                    // 3.3.7.2.h - LoginInstance
                    ProfileList profiles = new ProfileList();
                    profiles.add(prfl);
                    LongList loginInstanceIds = this.comServices.getArchiveService().store(true,
                            LoginHelper.LOGININSTANCE_OBJECT_TYPE,
                            connection.getPrimaryConnectionDetails().getDomain(),
                            HelperArchive.generateArchiveDetailsList(roleIds.get(0), // 3.3.3.e, 3.3.7.2.i
                                    null, // 3.3.3.f
                                    this.connection.getPrimaryConnectionDetails().getProviderURI()),
                            profiles,
                            mali);
                    this.loginInstanceId = loginInstanceIds.get(0);
              
                    // 3.3.7.2.j - LoginEvent
                    Long loginEvent = this.comServices.getEventService().generateAndStoreEvent(
                            LoginHelper.LOGINEVENT_OBJECT_TYPE,
                            this.connection.getPrimaryConnectionDetails().getDomain(),
                            null,
                            loginInstanceId, // 3.3.4.d
                            null, // 3.3.4.f
                            mali);
                    this.loginEventId = loginEvent;
                    Blob authId = this.loginServiceProvider.getBrokerAuthenticationId(); // 3.3.7.2.k
                    response = new LoginResponse(authId, loginInstanceId); // 3.3.7.2.l
                } else {
                    this.currentUser.logout();
                }
            } catch (UnknownAccountException uae) {
                // 3.3.7.2.e - unknown user
                throw new MALInteractionException(new MALStandardError(MALHelper.UNKNOWN_ERROR_NUMBER, null));
            } catch (IncorrectCredentialsException ice) {
                // 3.3.7.2.e - username, password are not correct
                throw new MALInteractionException(new MALStandardError(MALHelper.UNKNOWN_ERROR_NUMBER, null));
            } catch (ConcurrentAccessException cae) {
                // 3.3.7.2.f - username and role are currently in use
                throw new MALInteractionException(new MALStandardError(COMHelper.DUPLICATE_ERROR_NUMBER, null));
            } catch (ExcessiveAttemptsException eae) {
                // 3.3.7.2.g - too many attempts to login
                throw new MALInteractionException(new MALStandardError(MALHelper.TOO_MANY_ERROR_NUMBER, null));
            } catch (AuthenticationException ae) {
                //unexpected condition?  error?
            }
        }        
        return response; 
    }

    @Override
    public void logout(MALInteraction mali) throws MALInteractionException, MALException {
        
        // logout the user
        this.currentUser.logout();
        
        ObjectKey key = new ObjectKey(
                this.connection.getPrimaryConnectionDetails().getDomain(), 
                this.loginEventId);
        ObjectType type = LoginHelper.LOGINEVENT_OBJECT_TYPE;
        ObjectId objId = new ObjectId(type, key);

        // 3.3.8.2.b - LogoutEvent
        Long logoutEvent = this.comServices.getEventService().generateAndStoreEvent(
                LoginHelper.LOGOUTEVENT_OBJECT_TYPE,
                connection.getPrimaryConnectionDetails().getDomain(),
                null,
                this.loginInstanceId, // 3.3.4.e
                objId, // 3.3.4.g
                mali);
    }

    @Override
    public LongList listRoles(Identifier idntfr, String string, MALInteraction mali) throws MALInteractionException, MALException {
        
        // 3.3.9.2.a
        if (idntfr == null) {
            throw new IllegalArgumentException("username argument must not be null");
        }
        
        // 3.3.9.2.b
        if (idntfr.toString().equals("*") || idntfr.toString().isEmpty()) {
            throw new MALInteractionException(new MALStandardError(COMHelper.INVALID_ERROR_NUMBER, null));
        }
        
        if (LoginServiceSecurityUtils.isUser(idntfr.getValue(), string)) {
            LongList roles = LoginServiceSecurityUtils.getRoles(idntfr.getValue());
            return roles;
        }
        return null;
    }

    @Override
    public HandoverResponse handover(Profile prfl, String string, MALInteraction mali) throws MALInteractionException, MALException {
        
        // 3.3.10.2.a
        if (prfl == null) {
            throw new IllegalArgumentException("profile argument must not be null");
        }
        
        Identifier username = prfl.getUsername(); 
        Long role = prfl.getRole();
        
        // 3.3.10.2.b
        if (username == null || username.toString().equals("*") || username.toString().isEmpty()) {
            throw new MALInteractionException(new MALStandardError(COMHelper.INVALID_ERROR_NUMBER, null));
        }
        
        // 3.3.10.2.c
        if (role == null) {
            throw new MALInteractionException(new MALStandardError(COMHelper.INVALID_ERROR_NUMBER, null));
        }
        
        Blob authId = this.loginServiceProvider.getBrokerAuthenticationId();
        HandoverResponse response = null;
        
        if (this.currentUser.isAuthenticated()
                && LoginServiceSecurityUtils.isUser(username.getValue(), string)) {
            // check if this is a change of role for the current user
            if (this.currentUser.getPrincipals().getPrimaryPrincipal().equals(username.toString())) {
                if (this.currentUser.hasRole(String.valueOf(role))) {
                    IdentifierList objs = new IdentifierList();
                    objs.add(new Identifier(String.valueOf(role)));
                    LongList roleIds = this.comServices.getArchiveService().store(true,
                            LoginHelper.LOGINROLE_OBJECT_TYPE,
                            connection.getPrimaryConnectionDetails().getDomain(),
                            HelperArchive.generateArchiveDetailsList(null,
                                    null,
                                    this.connection.getPrimaryConnectionDetails().getProviderURI()),
                            objs,
                            mali);
                    response = new HandoverResponse(authId, this.loginInstanceId);
                } else {
                    return null;
                }
            } else if (LoginServiceSecurityUtils.hasRole(username.getValue(), String.valueOf(role))) {
                this.currentUser.runAs(new SimplePrincipalCollection(username.getValue(),
                        IniSecurityManagerFactory.INI_REALM_NAME));

                IdentifierList objs = new IdentifierList();
                objs.add(new Identifier(String.valueOf(role)));
                LongList roleIds = this.comServices.getArchiveService().store(true,
                        LoginHelper.LOGINROLE_OBJECT_TYPE,
                        connection.getPrimaryConnectionDetails().getDomain(),
                        HelperArchive.generateArchiveDetailsList(null,
                                null,
                                this.connection.getPrimaryConnectionDetails().getProviderURI()),
                        objs,
                        mali);
                
                ObjectKey key = new ObjectKey(
                        this.connection.getPrimaryConnectionDetails().getDomain(),
                        this.loginInstanceId);
                ObjectType type = LoginHelper.LOGININSTANCE_OBJECT_TYPE;
                ObjectId objId = new ObjectId(type, key);       
                
                // 3.3.10.2.i - LoginInstance
                ProfileList profiles = new ProfileList();
                profiles.add(prfl);
                LongList loginInstanceIds = this.comServices.getArchiveService().store(true,
                        LoginHelper.LOGININSTANCE_OBJECT_TYPE,
                        connection.getPrimaryConnectionDetails().getDomain(),
                        HelperArchive.generateArchiveDetailsList(roleIds.get(0), // 3.3.10.2.j
                                objId, // 3.3.10.2.k
                                this.connection.getPrimaryConnectionDetails().getProviderURI()),
                        profiles,
                        mali);
                response = new HandoverResponse(authId, loginInstanceIds.get(0)); // 3.3.10.2.l, 3.3.10.2.m
            }
        }
        return response;
    }
    
}
