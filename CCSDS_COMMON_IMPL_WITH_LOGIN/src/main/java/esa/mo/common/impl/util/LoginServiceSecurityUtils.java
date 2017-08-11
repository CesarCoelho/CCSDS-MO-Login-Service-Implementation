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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.subject.Subject;
import org.ccsds.moims.mo.mal.structures.LongList;

/**
 *
 * @author Andreea Pirvulescu
 */
public class LoginServiceSecurityUtils {
    
    public static Collection getRealmsList() {
        RealmSecurityManager securityManager = (RealmSecurityManager) SecurityUtils.getSecurityManager();
        Collection<Realm> realms = securityManager.getRealms();
        return realms;
    }
    
    public static LongList getRoles(Subject subject) {
        
        LongList roles = new LongList();
        Map allRoles = null;

        if (subject.isAuthenticated()) {
            Collection realmsList = getRealmsList();
            for (Iterator<Realm> iterator = realmsList.iterator(); iterator.hasNext(); ) {
                Realm realm = iterator.next();
                String name = realm.getClass().getName();
                if (name.equals("org.apache.shiro.realm.text.IniRealm")) {
                    allRoles = ((IniRealm) realm).getIni().get("roles");
                }
            }
            
            if (allRoles != null) {
                Iterator it = allRoles.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    if (subject.hasRole((String) pair.getKey())) {
                        roles.add(Long.valueOf((String) pair.getKey()));
                    }       
                }
            }
        }
        return roles;
    }
}
