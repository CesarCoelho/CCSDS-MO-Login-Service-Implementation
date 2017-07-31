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
package esa.mo.nmf.ctt.utils;

import esa.mo.nmf.ctt.services.common.LoginConsumerPanel;
import javax.swing.JTabbedPane;
import org.ccsds.moims.mo.common.directory.structures.ProviderSummary;

/**
 *
 * @author Cesar Coelho
 */
public class ProviderTabPanelLogin extends ProviderTabPanel {
    
    final JTabbedPane tabs = super.getTabs(); //ProviderTabPanel needs getTabs()
    
    /**
     * Creates a new tab for a Provider and populates it.
     *
     * @param provider
     */
    public ProviderTabPanelLogin(final ProviderSummary provider) {
        super(provider);
    }

    @Override
    public void insertServicesTabs() {
        // To do: Insert the Login service Consumer tab here!
        // Check how it is done inside the startTabs() method for exmaples
        if (this.services.getCommonServices() != null) {
            if (this.services.getCommonServices().getLoginService() != null) {
                LoginConsumerPanel panel = new LoginConsumerPanel(this.services.getCommonServices().getLoginService());
                int count = tabs.getTabCount();
                tabs.insertTab("Login service", null, panel, "Login Tab", count);
                while (!panel.isAuthenticated()) {
                    panel.repaint();
                    if (panel.isAuthenticated()) {
                        startTabs();
                        return;
                    }
                }
                
            }
        }
    }
    
    
}
