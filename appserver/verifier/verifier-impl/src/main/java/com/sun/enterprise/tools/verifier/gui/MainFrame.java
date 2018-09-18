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

package com.sun.enterprise.tools.verifier.gui;

import javax.swing.*;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import com.sun.enterprise.tools.verifier.StringManagerHelper;
import com.sun.enterprise.tools.verifier.Verifier;

public class MainFrame extends JFrame {


    /**
     * Deploytool gui entry point (acessed via reflection)
     */
    private static MainFrame verifierPanel = null;
    private static boolean exitOnClose = false;
    MainPanel mp = null;

    /**
     * Constructor.
     */
    public MainFrame() {
        this(null);
    }

    public MainFrame(String jarFileName) {
        this(jarFileName, false, null);
    }

    public MainFrame(String jarFileName, boolean exitOnClose,
                     Verifier verifier) {
        super((StringManagerHelper.getLocalStringsManager().getLocalString
                ("com.sun.enterprise.tools.verifier.gui.MainFrame" + // NOI18N
                ".WindowTitle", // NOI18N
                        "Verify Specification Compliance"))); // NOI18N
        setExitOnClose(exitOnClose);

        // 508 compliance for the JFrame
        this.getAccessibleContext().setAccessibleName(StringManagerHelper.getLocalStringsManager()
                .getLocalString("com.sun.enterprise.tools.verifier.gui.MainFrame" + // NOI18N
                ".jfName", // NOI18N
                        "Main Window")); // NOI18N
        this.getAccessibleContext().setAccessibleDescription(StringManagerHelper.getLocalStringsManager()
                .getLocalString("com.sun.enterprise.tools.verifier.gui.MainFrame" + // NOI18N
                ".jfDesc", // NOI18N
                        "This is the main window of the verifier tool")); // NOI18N

        if (exitOnClose) {
            this.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
        }
        Container contentPane = getContentPane();
        mp = new MainPanel(this, jarFileName, verifier);
        contentPane.add(mp);
        JOptionPane.showMessageDialog(this,
                StringManagerHelper.getLocalStringsManager()
                .getLocalString("com.sun.enterprise.tools.verifier.gui.Deprecation", // NOI18N
                        "\nThis GUI has been deprecated. Please use the GUI that comes with NetBeans."), // NOI18N
                "WARNING", JOptionPane.WARNING_MESSAGE); // NOI18N
    }

    public static JFrame getDeploytoolVerifierFrame(File jarFile) {
        if (verifierPanel == null) {
            verifierPanel = new MainFrame();
        } else {
            verifierPanel.getMainPanel().reset();
        }
        if (jarFile != null) {
            verifierPanel.getMainPanel().setJarFilename(
                    jarFile.getAbsolutePath());
        }
        return verifierPanel;
    }


    public MainPanel getMainPanel() {
        return mp;
    }

    public static boolean getExitOnClose() {
        return exitOnClose;
    }

    public static void setExitOnClose(boolean b) {
        exitOnClose = b;
    }
}
