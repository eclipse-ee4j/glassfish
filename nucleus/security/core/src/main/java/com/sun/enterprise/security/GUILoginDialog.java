/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security;

import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.ChoiceCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.glassfish.internal.api.Globals;

import com.sun.enterprise.security.ssl.SSLUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * An implementation of a LoginDialog that presents a swing based GUI for querying username and password.
 *
 * @author Harish Prabandham
 * @author Harpreet Singh
 */
public final class GUILoginDialog implements LoginDialog {

    private static final Logger _logger = SecurityLoggerInfo.getLogger();

    private String entity;
    private PassphraseDialog passphraseDialog;
    private CertificateDialog certDialog;
    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(GUILoginDialog.class);

    /**
     */
    public GUILoginDialog() {
        this(localStrings.getLocalString("enterprise.security.defaultEntity", "user"));
    }

    /**
     */
    public GUILoginDialog(String entity) {
        this.entity = entity;
        JFrame f = new JFrame();
        String phrase = localStrings.getLocalString("enterprise.security.loginPhrase", "Login for ");
        passphraseDialog = new PassphraseDialog(f, phrase + entity + ":");
        passphraseDialog.setVisible(true);
    }

    public GUILoginDialog(String entity, Callback[] callbacks) {
        this.entity = entity;
        String phrase = localStrings.getLocalString("enterprise.security.loginPhrase", "Login for ");
        JFrame f = new JFrame();
        passphraseDialog = new PassphraseDialog(f, phrase + entity + ":", callbacks);
        passphraseDialog.setVisible(true);
    }

    /**
     * @return The username of the user.
     */
    @Override
    public String getUserName() {
        return passphraseDialog.username;
    }

    /**
     * @return The password of the user in plain text...
     */
    @Override
    public final char[] getPassword() {
        char[] temp = passphraseDialog.passphrase;
        return (temp == null) ? null : Arrays.copyOf(temp, temp.length);
    }
}

/**
 * Create a popup dialog box to ask for the passphrase.
 */
class PassphraseDialog extends JDialog {
    private NameCallback nameCallback = null;;
    private PasswordCallback passwordCallback = null;
    private ChoiceCallback choiceCallback = null;
    private JTextField userField;
    private JPasswordField passField;
    private JList choiceList;
    private JFrame frame;
    private JButton okButton;
    private JButton cancelButton;
    // buttons for keystore password
    private JButton okForKP;
    private JButton cancelForKP;

    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(PassphraseDialog.class);
    String username = "";
    char[] passphrase = null;

    private JPasswordField keystorePassword;
    private JLabel lbl;
    // parent panel for keystore password
    private JPanel pnl = new JPanel(new GridLayout(2, 0));
    // panel for buttons for keystore password
    private JPanel bpanel = new JPanel(new FlowLayout());
    private JPanel kpPanel = new JPanel(new FlowLayout());
    private final String pnlKeyStorePassword = "Keystore Password Box";
    private final String pnlCertificateList = "Cerificate Chooser";
    // panel for certificate list
    private JPanel pnl2 = new JPanel();

    /**
     * Create a dialog box with a frame and title.
     *
     * @param frame The parent frame.
     * @param title The dialog box title.
     */
    protected PassphraseDialog(JFrame frame, String title) {
        super(frame, title, true);
        this.frame = frame;
        super.dialogInit();
        initbox();
    }

    /**
     * Create a dialog box with a frame and title.
     *
     * @param frame The parent frame.
     * @param title The dialog box title.
     */
    protected PassphraseDialog(JFrame frame, String title, Callback[] callbacks) {

        super(frame, title, true);
        this.frame = frame;
        super.dialogInit();

        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                nameCallback = (NameCallback) callbacks[i];
            } else if (callbacks[i] instanceof PasswordCallback) {
                passwordCallback = (PasswordCallback) callbacks[i];
            } else if (callbacks[i] instanceof ChoiceCallback) {
                choiceCallback = (ChoiceCallback) callbacks[i];
            }
        }
        initbox();
    }

    private void initbox() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        pnl2.setLayout(gridbag);
        getContentPane().setLayout(new CardLayout());
        int gridx = 0;
        int gridy = 0;

        passField = new JPasswordField(20);
        userField = new JTextField(20);
        choiceList = new JList();

        if (nameCallback != null) {
            c.gridx = gridx++;
            c.gridy = gridy;
            c.anchor = GridBagConstraints.CENTER;
            c.insets = new Insets(20, 10, 10, 2);
            JLabel jl = new JLabel(nameCallback.getPrompt() + ": ");
            gridbag.setConstraints(jl, c);
            pnl2.add(jl);
            c.gridx = gridx++;
            c.gridy = gridy++;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(20, 3, 10, 10);
            userField.setText(nameCallback.getDefaultName());
            gridbag.setConstraints(userField, c);
            userField.selectAll();
            pnl2.add(userField);
        }

        // passField.setEchoChar ('*');

        if (passwordCallback != null) {
            gridx = 0;
            c.gridx = gridx++;
            c.gridy = gridy;
            c.anchor = GridBagConstraints.CENTER;
            c.insets = new Insets(20, 10, 10, 2);
            JLabel l = new JLabel(passwordCallback.getPrompt());
            gridbag.setConstraints(l, c);
            pnl2.add(l);
            c.gridx = gridx++;
            c.gridy = gridy++;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(20, 3, 10, 10);
            gridbag.setConstraints(passField, c);
            pnl2.add(passField);
        }
        if (choiceCallback != null) {
            /*
             * For getting the KeyStore Password from the user
             */
            lbl = new JLabel(localStrings.getLocalString("enterprise.security.keystore", "Enter the KeyStore Password "));
            // adding the password field
            keystorePassword = new JPasswordField(20);
            kpPanel.add(lbl);
            kpPanel.add(keystorePassword);
            /* get the keystore password */
            final SSLUtils sslUtils = Globals.get(SSLUtils.class);
            // ok button For keystore password
            okForKP = new JButton(localStrings.getLocalString("enterprise.security.ok", " OK "));
            okForKP.setActionCommand("ok");

            okForKP.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    char[] passKPFromUser = keystorePassword.getPassword();
                    if (sslUtils.verifyMasterPassword(passKPFromUser)) {
                        okForKP.setEnabled(false);
                        cancelForKP.setEnabled(false);
                        keystorePassword.setEditable(false);
                        CardLayout cl = (CardLayout) (getContentPane()).getLayout();
                        cl.show(getContentPane(), pnlCertificateList);
                    } else {
                        String errmessage = localStrings.getLocalString("enterprise.security.IncorrectKeystorePassword",
                            "Incorrect Keystore Password");
                        GUIErrorDialog guierr = new GUIErrorDialog(errmessage);
                        guierr.setVisible(true);
                    }
                    Arrays.fill(passKPFromUser, ' ');
                }
            });

            cancelForKP = new JButton(localStrings.getLocalString("enterprise.security.cancel", "Cancel"));

            cancelForKP.setActionCommand("cancel");
            cancelForKP.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (choiceCallback != null)
                        choiceCallback.setSelectedIndex(-1);
                    frame.dispose();
                }
            });
            bpanel.add(okForKP);
            bpanel.add(cancelForKP);
            pnl.add(kpPanel);
            pnl.add(bpanel);
            // Adding the certificate lists.
            gridx = 0;
            c.gridx = gridx++;
            c.gridy = gridy;
            c.anchor = GridBagConstraints.CENTER;
            c.insets = new Insets(20, 10, 10, 2);
            JLabel l = new JLabel(choiceCallback.getPrompt());
            gridbag.setConstraints(l, c);
            pnl2.add(l);
            c.gridx = gridx++;
            c.gridy = gridy++;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(20, 3, 10, 10);

            String[] choices = choiceCallback.getChoices();
            choiceList.setListData(choices);

            gridbag.setConstraints(choiceList, c);
            pnl2.add(choiceList);
        }

        okButton = new JButton(localStrings.getLocalString("enterprise.security.ok", " OK "));
        // XXX I18N
        okButton.setActionCommand("ok");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                username = userField.getText();
                if (username.trim().length() > 0)
                    nameCallback.setName(username);
                if (passwordCallback != null) {
                    char[] pass = passField.getPassword();
                    //if(passphrase.trim().length() > 0) {
                    passwordCallback.setPassword(pass);
                    //}
                }
                if (choiceCallback != null) {
                    int idx = choiceList.getSelectedIndex();
                    if (idx != -1)
                        choiceCallback.setSelectedIndex(idx);
                }
                frame.dispose();
            }
        }

        );

        cancelButton = new JButton(localStrings.getLocalString("enterprise.security.cancel", "Cancel"));
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (choiceCallback != null) {
                    choiceCallback.setSelectedIndex(-1);
                } else {
                    username = null;
                    if (passphrase != null) {
                        Arrays.fill(passphrase, ' ');
                    }
                    frame.dispose();
                }
            }
        });

        super.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                //System.out.println("IN WINDOW CLOSING");
                //_logger.log(Level.FINE,"IN WINDOW CLOSING");
                // send a fail back
                if (choiceCallback != null)
                    choiceCallback.setSelectedIndex(-1);
                frame.dispose();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(gridbag);
        c.insets = new Insets(5, 0, 5, 15);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(okButton, c);
        buttonPanel.add(okButton);
        c.gridx = 2;
        c.insets = new Insets(5, 15, 5, 0);
        gridbag.setConstraints(cancelButton, c);
        buttonPanel.add(cancelButton);

        c.gridx = 0;
        c.gridy = gridy++;
        c.gridwidth = 2;
        c.insets = new Insets(0, 0, 5, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(buttonPanel, c);
        pnl2.add(buttonPanel);
        getContentPane().add(pnl, pnlKeyStorePassword);
        getContentPane().add(pnl2, pnlCertificateList);
        CardLayout cl = (CardLayout) (getContentPane()).getLayout();
        if (choiceCallback != null) {
            /* first get the password to the keystore */
            cl.show(getContentPane(), pnlKeyStorePassword);
        } else {
            cl.show(getContentPane(), pnlCertificateList);
        }
        pack();
        setSize(getPreferredSize());
    }

}

/**
 * Create a popup dialog box to ask for the passphrase.
 */
class CertificateDialog extends JDialog {
    private JTextField userField;
    private JList certList;
    private JFrame frame;
    private JButton okButton;
    private JButton cancelButton;
    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CertificateDialog.class);
    String username = "";
    char[] passphrase = new char[0];

    /**
     * Create a dialog box with a frame and title.
     *
     * @param frame The parent frame.
     * @param title The dialog box title.
     */
    protected CertificateDialog(JFrame frame, String title) {
        super(frame, title, true);
        this.frame = frame;
        super.dialogInit();
        initbox();
    }

    private void initbox() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        getContentPane().setLayout(gridbag);

        int gridx = 0;
        int gridy = 0;

        String[] list = null;
        /**/
        list = new String[5];
        list[0] = "foo";
        list[1] = "bar";
        list[2] = "abc";
        list[3] = "def";
        list[4] = "ghi";
        /**/

        certList = new JList(list);
        userField = new JTextField(20);

        c.gridx = gridx++;
        c.gridy = gridy;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(20, 10, 10, 2);
        JLabel jl = new JLabel(localStrings.getLocalString("enterprise.security.login.username", "Enter username:"));
        gridbag.setConstraints(jl, c);
        getContentPane().add(jl);
        c.gridx = gridx++;
        c.gridy = gridy++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(20, 3, 10, 10);
        gridbag.setConstraints(userField, c);
        getContentPane().add(userField);

        gridx = 0;
        c.gridx = gridx++;
        c.gridy = gridy;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(20, 10, 10, 2);
        JLabel l = new JLabel(localStrings.getLocalString("enterprise.security.login.password", "Select a certificate:"));
        gridbag.setConstraints(l, c);
        getContentPane().add(l);
        c.gridx = gridx++;
        c.gridy = gridy++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(20, 3, 10, 10);
        gridbag.setConstraints(certList, c);
        getContentPane().add(certList);

        okButton = new JButton(localStrings.getLocalString("enterprise.security.ok", " OK ")); // XXX I18N
        okButton.setActionCommand("ok");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // System.out.println("OK Action");
                //_logger.log(Level.FINE,"OK Action");
                username = userField.getText();
                //int index = certList.getSelectedIndex();

                if ((username.trim().length() > 0) && (passphrase.length > 0)) {
                    setVisible(false);
                }
            }
        });

        cancelButton = new JButton(localStrings.getLocalString("enterprise.security.cancel", "Cancel")); // XXX I18N
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // System.out.println("Cancel Action");
                // _logger.log(Level.FINE,"Cancel Action");
                // username = null;
                // passphrase = null;
                // setVisible(false);
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        });

        super.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                // System.out.println("IN WINDOW CLOSING");
                // _logger.log(Level.FINE,"IN WINDOW CLOSING");
                frame.dispose();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(gridbag);
        c.insets = new Insets(5, 0, 5, 15);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(okButton, c);
        buttonPanel.add(okButton);
        c.gridx = 2;
        c.insets = new Insets(5, 15, 5, 0);
        gridbag.setConstraints(cancelButton, c);
        buttonPanel.add(cancelButton);

        c.gridx = 0;
        c.gridy = gridy++;
        c.gridwidth = 2;
        c.insets = new Insets(0, 0, 5, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(buttonPanel, c);
        getContentPane().add(buttonPanel);

        pack();
        setSize(getPreferredSize());
    }

}
