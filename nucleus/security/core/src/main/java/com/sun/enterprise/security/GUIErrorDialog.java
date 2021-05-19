/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

/*
 * GUIErrorDialog.java
 * An error dialog box used for FailedLogin
 *
 * @author Harpreet Singh
 * @version
 */

package com.sun.enterprise.security;
import javax.swing.*;
import java.awt.event.*;

public class GUIErrorDialog extends javax.swing.JDialog {
    String message;
    /** Creates new form GUIErrorDialog */
    public GUIErrorDialog (String message){
        super (new JFrame (), true);
        this.message = message;
        initComponents ();
        pack ();
    }
    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        okButton = new javax.swing.JButton();
        errorLbl = new javax.swing.JLabel();
        okButton.setAlignmentX (CENTER_ALIGNMENT);
        errorLbl.setAlignmentX (CENTER_ALIGNMENT);
        getContentPane().setLayout (new javax.swing.BoxLayout (getContentPane (),BoxLayout.Y_AXIS));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        okButton.setActionCommand("okButton");
        okButton.setText("OK");
        okButton.addActionListener (new ActionListener (){
                public void actionPerformed (ActionEvent e){
                    dispose ();
                }
            });
        super.addWindowListener (new WindowAdapter (){
                public void windowClosing (WindowEvent we){
                    dispose ();
                }
            });
        errorLbl.setText("Error : "+message);
        getContentPane().add (errorLbl);
        getContentPane().add (okButton);
    }

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {
        setVisible (false);
        dispose ();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton okButton;
    private javax.swing.JLabel errorLbl;
    // End of variables declaration//GEN-END:variables

}
