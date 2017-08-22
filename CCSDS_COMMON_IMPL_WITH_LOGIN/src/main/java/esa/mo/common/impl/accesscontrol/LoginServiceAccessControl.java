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

import org.ccsds.moims.mo.mal.accesscontrol.MALAccessControl;
import org.ccsds.moims.mo.mal.accesscontrol.MALCheckErrorException;
import org.ccsds.moims.mo.mal.transport.MALMessage;

/**
 *
 * @author Andreea Pirvulescu
 */
public class LoginServiceAccessControl implements MALAccessControl{
    
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
        }
        return malm;
    }

}
