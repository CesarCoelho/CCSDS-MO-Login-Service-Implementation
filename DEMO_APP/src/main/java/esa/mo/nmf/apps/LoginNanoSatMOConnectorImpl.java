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
package esa.mo.nmf.apps;

import esa.mo.common.impl.provider.LoginProviderServiceImpl;
import esa.mo.nmf.MonitorAndControlNMFAdapter;
import esa.mo.nmf.nanosatmoconnector.NanoSatMOConnectorImpl;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ccsds.moims.mo.mal.MALException;

/**
 * The demo app to take snaps
 */
public class LoginNanoSatMOConnectorImpl extends NanoSatMOConnectorImpl {

    private LoginProviderServiceImpl loginService;

    public LoginNanoSatMOConnectorImpl(MonitorAndControlNMFAdapter mcAdapter) {
        super.init(mcAdapter);
    }

    @Override
    public void initAdditionalServices() {
        loginService = new LoginProviderServiceImpl();
        try {
            loginService.init(comServices);
        } catch (MALException ex) {
            Logger.getLogger(LoginNanoSatMOConnectorImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
