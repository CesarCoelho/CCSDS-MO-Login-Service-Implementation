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

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.ccsds.moims.mo.common.login.structures.Profile;
import org.ccsds.moims.mo.mal.structures.Blob;
import org.ccsds.moims.mo.mal.structures.LongList;

/**
 *
 * @author Andreea Pirvulescu
 */
public class LoginServiceSecurityUtils {
    
    private static Subject currentUser;
    
    /**
     * Initialize Security Manager
     */
    public static void initSecurityManager() {
        // load shiro configuration
        Factory<org.apache.shiro.mgt.SecurityManager> factory
                = new IniSecurityManagerFactory("classpath:shiro.ini");
        org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();
        // set the SecurityManager
        SecurityUtils.setSecurityManager(securityManager);        
    }
    
    /**
     * Sets the current executing user
     * 
     * @param subject
     */
    public static void setSubject(Subject subject) {
       currentUser = subject;
    }
    
    /**
     * Returns the current executing user
     * 
     * @return currentUser
     */
    public static Subject getSubject() {
        return currentUser;
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
     * Returns the roles for a certain user
     *
     * @param username
     * @return roles that match user with username
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
                if (hasRole(username, (String) pair.getKey())) {
                    roles.add(Long.valueOf((String) pair.getKey()));
                }
            }
            return roles;
        }
        return null;
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
