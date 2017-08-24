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

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ccsds.moims.mo.common.login.structures.Profile;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.accesscontrol.MALAccessControl;
import org.ccsds.moims.mo.mal.accesscontrol.MALCheckErrorException;
import org.ccsds.moims.mo.mal.structures.Blob;
import org.ccsds.moims.mo.mal.transport.MALMessage;

/**
 *
 * @author Andreea Pirvulescu
 */
public class MALAccessControlImpl implements MALAccessControl{
    
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
            System.out.println(malm.getHeader().toString());
            int serviceArea = malm.getHeader().getServiceArea().getValue();
            int service = malm.getHeader().getService().getValue();
            int operation = malm.getHeader().getOperation().getValue();
            if (serviceArea == 3 && service == 2 && operation == 1) {
                System.out.println(malm.getHeader().toString());
                System.out.println("SERVICE AREA " + malm.getHeader().getServiceArea().getValue());
                System.out.println("SERVICE " + malm.getHeader().getService().getValue());
                System.out.println("OPERATION " + malm.getHeader().getOperation().getValue());
                try {
                    Profile prfl = (Profile) malm.getBody().getBodyElement(0, malm);
                    Blob authId = generateAuthId(prfl);
                } catch (MALException ex) {
                    Logger.getLogger(MALAccessControlImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return malm;
    }
    
    /**
     * 
     * @param prfl
     * @return authentication id
     */
    public static Blob generateAuthId(Profile prfl) {
        
        byte[] value0 = prfl.getUsername().getValue().getBytes((Charset.forName("UTF-8")));
        byte value1 = prfl.getRole().byteValue();
        
        byte[] value = new byte[value0.length + 1];
        System.arraycopy(value0, 0, value, 0, value0.length);
        value[value0.length] = value1;
        Blob authId = new Blob(value);
        
        return authId;
    }

}
