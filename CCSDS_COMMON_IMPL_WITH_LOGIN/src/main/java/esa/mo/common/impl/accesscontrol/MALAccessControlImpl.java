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
package esa.mo.common.impl.accesscontrol;

import esa.mo.common.impl.util.LoginServiceSecurityUtils;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ccsds.moims.mo.common.login.structures.Profile;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALHelper;
import org.ccsds.moims.mo.mal.MALStandardError;
import org.ccsds.moims.mo.mal.accesscontrol.MALAccessControl;
import org.ccsds.moims.mo.mal.accesscontrol.MALCheckErrorException;
import org.ccsds.moims.mo.mal.structures.Blob;
import org.ccsds.moims.mo.mal.structures.Union;
import org.ccsds.moims.mo.mal.transport.MALMessage;

/**
 *
 * @author Andreea Pirvulescu
 */
public class MALAccessControlImpl implements MALAccessControl{
    
    Boolean authenticationFlag = false;
    Blob newAuthId = null; 
    
    /**
     * 
     * @param malm
     * @return
     * @throws IllegalArgumentException
     * @throws MALCheckErrorException 
     */
    @Override
    public MALMessage check(MALMessage malm) throws IllegalArgumentException, MALCheckErrorException {    
        
        if ((malm != null) && (malm.getHeader() != null)) {
            int serviceArea = malm.getHeader().getServiceArea().getValue();
            int service = malm.getHeader().getService().getValue();
            int operation = malm.getHeader().getOperation().getValue();
            // login service
            if (serviceArea == 3 && service == 2) {
                // login && handover
                if (operation == 1 || operation == 4) {
                    try {
                        Profile prfl = (Profile) malm.getBody().getBodyElement(0, malm);
                        newAuthId = generateAuthId(prfl);
                        malm.getHeader().setAuthenticationId(newAuthId);
                        authenticationFlag = true;
                    } catch (MALException ex) {
                        Logger.getLogger(MALAccessControlImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                // logout && listRoles 
                if (operation == 2 || operation == 3) {
                    authenticationFlag = false;
                    return malm;
                }
            }
            if (authenticationFlag && !malm.getHeader().getIsErrorMessage()) {
                byte[] authId;
                try {
                    authId = malm.getHeader().getAuthenticationId().getValue();                  
                } catch (MALException exc) {
                    throw new MALCheckErrorException(new MALStandardError(MALHelper.AUTHENTICATION_FAIL_ERROR_NUMBER,
                            new Union("Could not get the authentication id: " + exc)),
                            malm.getQoSProperties());
                }
                if ((authId != null) && (authId.length > 0)) {
                    if (!LoginServiceSecurityUtils.hasRole(new String(Arrays.copyOfRange(authId, 0, authId.length - 1),
                            StandardCharsets.UTF_16), String.valueOf(authId[authId.length - 1]))) {
                        throw new MALCheckErrorException(new MALStandardError(MALHelper.AUTHORISATION_FAIL_ERROR_NUMBER,
                                new Union("Failed authorization")), malm.getQoSProperties());
                        
                    }
                } else {
                    malm.getHeader().setAuthenticationId(newAuthId);
                }
            }
        } else {
            throw new IllegalArgumentException("Message argument must not be null"); // 6.2.3.2.4
        }
        return malm;
    }
    
    /**
     * 
     * @param prfl
     * @return authentication id
     */
    public static Blob generateAuthId(Profile prfl) {
        
        byte[] value0 = prfl.getUsername().getValue().getBytes((Charset.forName("UTF-16")));
        byte value1 = prfl.getRole().byteValue();
        
        byte[] value = new byte[value0.length + 1];
        System.arraycopy(value0, 0, value, 0, value0.length);
        value[value0.length] = value1;
        Blob authId = new Blob(value);
        
        return authId;
    }

}
