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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.shiro.subject.Subject;
import org.ccsds.moims.mo.common.CommonHelper;
import org.ccsds.moims.mo.common.directory.DirectoryHelper;
import org.ccsds.moims.mo.common.login.LoginHelper;
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
public class MALAccessControlImpl implements MALAccessControl {

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
        
        if (malm == null) {
            throw new IllegalArgumentException("Message argument must not be null"); // 6.2.3.2.4
        }

        if (malm.getHeader() != null) {
            int serviceArea = malm.getHeader().getServiceArea().getValue();
            int service = malm.getHeader().getService().getValue();
            int operation = malm.getHeader().getOperation().getValue();
            
            // Directory service - lookupProvider operation
            if (serviceArea == CommonHelper._COMMON_AREA_NUMBER
                    && service == DirectoryHelper._DIRECTORY_SERVICE_NUMBER
                    && operation == DirectoryHelper.LOOKUPPROVIDER_OP_NUMBER.getValue()) {
                return malm;
            }

            // Login service
            if (serviceArea == CommonHelper._COMMON_AREA_NUMBER
                    && service == LoginHelper.LOGIN_SERVICE_NUMBER.getValue()) {
                // login || handover
                if (operation == LoginHelper._LOGIN_OP_NUMBER
                        || operation == LoginHelper.HANDOVER_OP_NUMBER.getValue()) {
                    try {
                        Profile prfl = (Profile) malm.getBody().getBodyElement(0, malm);
                        newAuthId = LoginServiceSecurityUtils.generateAuthId(prfl);
                        authenticationFlag = true;
                    } catch (MALException ex) {
                        Logger.getLogger(MALAccessControlImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                // logout
                if (operation == LoginHelper._LOGOUT_OP_NUMBER) {
                    authenticationFlag = false;
                    newAuthId = null;
                }
                malm.getHeader().setAuthenticationId(newAuthId);
                return malm;
            }

            if (authenticationFlag && !malm.getHeader().getIsErrorMessage()) {
                Subject subject = LoginServiceSecurityUtils.getSubject();
                if ((newAuthId != null) && (newAuthId.getLength() > 0) && subject.isAuthenticated()) {
                    malm.getHeader().setAuthenticationId(newAuthId);
                    if (subject.isPermitted(serviceArea + ":" + service + ":" + operation)) {
                        return malm;
                    } else {
                        // 6.2.3.2.7
                        throw new MALCheckErrorException(new MALStandardError(MALHelper.AUTHORISATION_FAIL_ERROR_NUMBER,
                                new Union("Failed authorization")), malm.getQoSProperties()); // 3.6.1.8.3.3.c   
                    }
                } else {
                    // 6.2.3.2.7
                    throw new MALCheckErrorException(new MALStandardError(MALHelper.AUTHENTICATION_FAIL_ERROR_NUMBER,
                            new Union("Failed authentication")),
                            malm.getQoSProperties()); // 3.6.1.8.3.3.b
                }
            }
        }
        // 3.6.1.4.a
        malm.getHeader().setIsErrorMessage(true);
        return malm;
    }

}