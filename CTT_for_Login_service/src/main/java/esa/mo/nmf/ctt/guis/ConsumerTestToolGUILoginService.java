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
package esa.mo.nmf.ctt.guis;

import esa.mo.nmf.ctt.utils.DirectoryConnectionConsumerPanel;
import esa.mo.nmf.ctt.utils.ProviderTabPanel;
import esa.mo.nmf.ctt.utils.ProviderTabPanelLogin;
import java.awt.EventQueue;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.ccsds.moims.mo.common.directory.structures.ProviderSummary;

/**
 * This class provides a simple form for the control of the consumer.
 */
public class ConsumerTestToolGUILoginService extends ConsumerTestToolGUI {

    /**
     * Main command line entry point.
     *
     * @param args the command line arguments
     */
    public static void main(final String args[]) {
        try {
            // Set cross-platform Java L&F (also called "Metal")
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (UnsupportedLookAndFeelException e) {
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
            // handle exception
        }

        final String name = System.getProperty("application.name", "CTT: Consumer Test Tool for Login service");
        final ConsumerTestToolGUILoginService gui = new ConsumerTestToolGUILoginService(name);

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                gui.setVisible(true);
            }
        });
    }
    
    public ConsumerTestToolGUILoginService(final String name) {
        super(name);
        this.insertDirectoryServiceTabWithLogin("");
    }
    
    public final void insertDirectoryServiceTabWithLogin(final String defaultURI) {
        final JTabbedPane tabs = super.getTabs();
        System.out.println("Hellp");
        final DirectoryConnectionConsumerPanel directoryTab = new DirectoryConnectionConsumerPanel(false, connection, tabs) {
            @Override
            public ProviderTabPanel createNewProviderTabPanel(final ProviderSummary providerSummary) {
                return new ProviderTabPanelLogin(providerSummary);
            }
        };

        tabs.insertTab("Communication Settings (Directory)", null,
                directoryTab,
                "Communications Tab (Directory)", tabs.getTabCount());

        directoryTab.setURITextbox(defaultURI);
    }

}
