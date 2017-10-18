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
package esa.mo.common.impl.util;

import esa.mo.helpertools.helpers.HelperMisc;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.util.Factory;
import org.ccsds.moims.mo.common.login.structures.Profile;
import org.ccsds.moims.mo.mal.structures.Blob;
import org.ccsds.moims.mo.mal.structures.LongList;

/**
 *
 * @author Andreea Pirvulescu
 */
public class LoginServiceSecurityUtils {
    
    private final static int MAX_USERS_FOR_A_ROLE = 2;
    private static boolean securityManagerHasBeenSet = false;
    private static Map<Long, Integer> rolesAvailability;

    /**
     * Initialize Security Manager
     */
    public static void initSecurityManager() {
        // load shiro configuration
        String config = System.getProperty(HelperMisc.INI_CONFIGURATION);
        if (config == null) {
            config = "classpath:shiro.ini";
        }
        Factory<SecurityManager> factory
                = new IniSecurityManagerFactory(config);
        SecurityManager securityManager = factory.getInstance();
        // set the SecurityManager
        SecurityUtils.setSecurityManager(securityManager);
        securityManagerHasBeenSet = true;
        // initialize the available roles
        assignRolesAvailability();
    }
    
    /**
     * 
     * @return true if the security manager is set; false otherwise
     */
    public static boolean isSecurityManagerSet() {
        return securityManagerHasBeenSet;
    }
    
    /**
     * Returns an instance of IniRealm
     *
     * @return realm
     */
    private static Realm getRealm() {
        RealmSecurityManager securityManager = (RealmSecurityManager) SecurityUtils.getSecurityManager();
        Collection<Realm> realms = securityManager.getRealms();

        for (Realm realm : realms) {
            String name = realm.getClass().getName();
            if (name.equals("org.apache.shiro.realm.text.IniRealm")) {
                return realm;
            }
        }
        return null;
    }
    
    /**
     *
     * @return all active sessions
     */
    private static Collection getActiveSessions() {
        if (securityManagerHasBeenSet) {
            try {
                DefaultSecurityManager securityManager
                        = (DefaultSecurityManager) SecurityUtils.getSecurityManager();
                DefaultSessionManager sessionManager
                        = (DefaultSessionManager) securityManager.getSessionManager();
                Method getActiveSessionsMethod = DefaultSessionManager.class.getDeclaredMethod("getActiveSessions");
                getActiveSessionsMethod.setAccessible(true);
                Collection<Session> activeSessions = (Collection) getActiveSessionsMethod.invoke(sessionManager);
                return activeSessions;
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(LoginServiceSecurityUtils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(LoginServiceSecurityUtils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(LoginServiceSecurityUtils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(LoginServiceSecurityUtils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(LoginServiceSecurityUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    /**
     * Checks for a session with the given authId
     *
     * @param authId
     * @return true if the session exists; false otherwise
     */
    public static boolean hasActiveSession(Blob authId) {
        Collection<Session> activeSessions = getActiveSessions();
        if(activeSessions == null) {
            return false;
        }
        for (Session session : activeSessions) {
            if (session.getAttribute(authId) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the username and password combination exists
     *
     * @param username
     * @param pass
     * @return true if the user exists; false otherwise
     */
    public static boolean isUser(String username, String pass) {      
        Realm realm = getRealm();
        // get the users defined in shiro.ini
        Map allUsers = ((IniRealm) realm).getIni().get("users");
        // service to encrypt pass and check against the stored hash in shiro.ini
        DefaultPasswordService dps = new DefaultPasswordService();
        if (allUsers != null) {
            Iterator it = allUsers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                List<String> infoList = Arrays.asList(pair.getValue().toString().split(","));
                if (pair.getKey().toString().equals(username) && dps.passwordsMatch(pass, infoList.get(0))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the username and role combination exists
     *
     * @param username
     * @param role
     * @return true if user has the role; false otherwise
     */
    public static boolean hasRole(String username, String role) {
        Realm realm = getRealm();
        // get the users defined in shiro.ini
        Map allUsers = ((IniRealm) realm).getIni().get("users");

        if (allUsers != null) {
            Iterator it = allUsers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                List<String> infoList = Arrays.asList(pair.getValue().toString().split("[ *,* ]"));
                if (pair.getKey().toString().equals(username) && infoList.contains(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the roles for a certain user; if the
     * username is "", returns all the possible roles
     * @param username
     * @return roles
     */
    public static LongList getRoles(String username) {
        Realm realm = getRealm();
        LongList roles = new LongList();
        // get the roles defined in shiro.ini
        Map allRoles = ((IniRealm) realm).getIni().get("roles");

        if (allRoles != null) {
            Iterator it = allRoles.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (username.equals("")) {
                    roles.add(Long.valueOf((String) pair.getKey()));
                } else if (hasRole(username, (String) pair.getKey())) {
                    roles.add(Long.valueOf((String) pair.getKey()));
                }
            }
            return roles;
        }
        return null;
    }
    
    /**
     * Initialize roles usage with 0
     */
    public static void assignRolesAvailability() {
        rolesAvailability = new HashMap();
        LongList allRoles = getRoles("");
        for (Long role : allRoles) {
            rolesAvailability.put(role, 0);
        }
    }
    
    /**
     * Checks if the limit of users for a role has been reached
     * 
     * @param role
     * @return 
     */
    public static boolean checkRoleAvailability(Long role) {
        return rolesAvailability.get(role) != MAX_USERS_FOR_A_ROLE;
    }
    
    /**
     * Decreases the role availability by updating the number 
     * of users that logged in with the given role
     * 
     * @param role
     */
    public static void decreaseRoleAvailability(Long role) {
        rolesAvailability.put(role, rolesAvailability.get(role) + 1);
    }
    
    /**
     * Increases the role availability by updating the number 
     * of users that logged out from the given role
     * 
     * @param role
     */
    public static void increaseRoleAvailability(Long role) {
        rolesAvailability.put(role, rolesAvailability.get(role) - 1);
    }
            
    /**
     * Generates an authentication id
     * 
     * @param prfl
     * @return authentication id
     */
    public static Blob generateAuthId(Profile prfl) {
        // authId = username + role
        byte[] value0 = prfl.getUsername().getValue().getBytes((Charset.forName("UTF-16")));
        byte value1 = prfl.getRole().byteValue();
        
        byte[] value = new byte[value0.length + 1];
        System.arraycopy(value0, 0, value, 0, value0.length);
        value[value0.length] = value1;
        Blob authId = new Blob(value);

        return authId;
    }

}
