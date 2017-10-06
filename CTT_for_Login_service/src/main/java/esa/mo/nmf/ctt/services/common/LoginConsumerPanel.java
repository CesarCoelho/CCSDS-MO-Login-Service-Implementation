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
package esa.mo.nmf.ctt.services.common;

import esa.mo.common.impl.consumer.LoginConsumerServiceImpl;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import org.ccsds.moims.mo.common.login.body.HandoverResponse;
import org.ccsds.moims.mo.common.login.body.LoginResponse;
import org.ccsds.moims.mo.common.login.structures.Profile;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.LongList;

/**
 *
 * @author Andreea Pirvulescu
 */
public class LoginConsumerPanel extends javax.swing.JPanel {

    private final LoginConsumerServiceImpl serviceCommonLogin;
    private final LoginTablePanel loginTable;
    private boolean authenticationStatus = false;
    private boolean handoverSuccess = false;
    public String[] data = new String[3];

    /**
     * Creates new form LoginConsumerPanel
     *
     * @param serviceCommonLogin
     */
    public LoginConsumerPanel(LoginConsumerServiceImpl serviceCommonLogin) {
        initComponents();
        this.serviceCommonLogin = serviceCommonLogin;
        loginTable = new LoginTablePanel(serviceCommonLogin.getCOMServices().getArchiveService());
        this.logoutButton.setEnabled(false);
        this.handoverButton.setEnabled(false);
        this.listRolesScrollPane.setVisible(false);
        this.userTextPane.setVisible(false);
        this.userScrollPane.setVisible(false);
    }

    public boolean isAuthenticated() {
        return authenticationStatus;
    }
    
    public boolean getHandoverResult() {
        return handoverSuccess;
    }
    
    public void setHandoverResult(boolean result) {
        this.handoverSuccess = result;
    }
    
    /**
     * Calls the login operation
     */
    public void checkLoginData() {
        Identifier username = new Identifier(this.data[0]);
        Long role = Long.valueOf(this.data[1]);
        String password = this.data[2];

        Profile profile = new Profile(username, role);

        try {
            LoginResponse loginResponse = this.serviceCommonLogin.getLoginStub().login(profile, password);
            if (loginResponse != null) {
                this.authenticationStatus = true;
                this.userTextPane.setVisible(true);
                this.userScrollPane.setVisible(true);
                this.userTextPane.setText("You are logged in as user " + username + " with role " + role);
            } 
        } catch (MALInteractionException ex) {
            Logger.getLogger(LoginConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(LoginConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (this.isAuthenticated()) {
            this.loginButton.setEnabled(false);
            this.logoutButton.setEnabled(true);
            this.handoverButton.setEnabled(true);
        }
    }
    
    /**
     * Calls the listRoles operation
     */
    public void checkListRolesData() {
        Identifier username = new Identifier(this.data[0]);
        String password = this.data[1];
        
        try {
            LongList roles = this.serviceCommonLogin.getLoginStub().listRoles(username, password);
            if (roles == null) {
                JOptionPane.showMessageDialog(null, "There are no roles for this username and password combination");
            } else {
                this.listRolesScrollPane.setVisible(true);
                String title = "Roles for user " + username + ":";
                DefaultListModel model = new DefaultListModel();
                this.rolesList.setModel(model);
                model.add(0, title);
                for (int i = 1; i <= roles.size(); i++) {
                    model.add(i, roles.get(i-1));           
                }
            }
        } catch (MALInteractionException ex) {
            Logger.getLogger(LoginConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(LoginConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Calls the handover operation
     */
    public void checkHandoverData() {
        Identifier username = new Identifier(this.data[0]);
        Long role = Long.valueOf(this.data[1]);
        String password = this.data[2];

        Profile profile = new Profile(username, role);
        try {
            HandoverResponse handoverResponse = this.serviceCommonLogin.getLoginStub().handover(profile, password);
            if (handoverResponse != null) {
                this.handoverSuccess = true;
                this.userTextPane.setText("You are logged in as user " + username + " with role " + role);
            }
        } catch (MALInteractionException ex) {
            Logger.getLogger(LoginConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(LoginConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        loginButton = new javax.swing.JButton();
        logoutButton = new javax.swing.JButton();
        listRolesButton = new javax.swing.JButton();
        listRolesScrollPane = new javax.swing.JScrollPane();
        rolesList = new javax.swing.JList();
        handoverButton = new javax.swing.JButton();
        userScrollPane = new javax.swing.JScrollPane();
        userTextPane = new javax.swing.JTextPane();

        loginButton.setText("Login");
        loginButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginButtonActionPerformed(evt);
            }
        });

        logoutButton.setText("Logout");
        logoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutButtonActionPerformed(evt);
            }
        });

        listRolesButton.setText("listRoles");
        listRolesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listRolesButtonActionPerformed(evt);
            }
        });

        listRolesScrollPane.setViewportView(rolesList);

        handoverButton.setText("handover");
        handoverButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                handoverButtonActionPerformed(evt);
            }
        });

        userScrollPane.setViewportView(userTextPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(userScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addGap(72, 72, 72)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(loginButton, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(listRolesScrollPane)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(74, 74, 74)
                                .addComponent(logoutButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(listRolesButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(handoverButton)))
                        .addContainerGap(75, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(userScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(loginButton)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(logoutButton)
                        .addComponent(listRolesButton)
                        .addComponent(handoverButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(listRolesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(69, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void loginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed
        // TODO add your handling code here: 
        GetCredentialsFrame lfr = new GetCredentialsFrame(this, "login");
        lfr.setVisible(true);
    }//GEN-LAST:event_loginButtonActionPerformed

    private void logoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutButtonActionPerformed
        
        try {
            this.serviceCommonLogin.getLoginStub().logout();
        } catch (MALInteractionException ex) {
            Logger.getLogger(LoginConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(LoginConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.authenticationStatus = false;
        this.loginButton.setEnabled(true);
        this.logoutButton.setEnabled(false);
        this.handoverButton.setEnabled(false);
        
    }//GEN-LAST:event_logoutButtonActionPerformed

    private void listRolesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listRolesButtonActionPerformed
        // TODO add your handling code here:
        ListRolesFrame lrf = new ListRolesFrame(this);
        lrf.setVisible(true);        
    }//GEN-LAST:event_listRolesButtonActionPerformed

    private void handoverButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_handoverButtonActionPerformed
        // TODO add your handling code here:
        GetCredentialsFrame lfr = new GetCredentialsFrame(this, "handover");
        lfr.setVisible(true);
    }//GEN-LAST:event_handoverButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton handoverButton;
    private javax.swing.JButton listRolesButton;
    private javax.swing.JScrollPane listRolesScrollPane;
    private javax.swing.JButton loginButton;
    private javax.swing.JButton logoutButton;
    private javax.swing.JList rolesList;
    private javax.swing.JScrollPane userScrollPane;
    private javax.swing.JTextPane userTextPane;
    // End of variables declaration//GEN-END:variables
}
