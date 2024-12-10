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

package org.glassfish.appclient.client.jws.boot;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.glassfish.embeddable.client.UserError;

/**
 * Displays errors detected after Java Web Start has launched the
 * Java Web Start-aware ACC but before the developer's client has
 * launched.  Such errors would normally go to the Java Web Start trace file
 * or the Java Console (if either is enabled) but by default neither of those
 * is turned on, with the result that the end-user has no information about
 * such errors.  The client would simply never start.
 *
 * @author tjquinn
 */
public class ErrorDisplayDialog {

    private static final String LINE_SEP = System.getProperty("line.separator");

    private static ResourceBundle rb;

    /**
     * Displays a kinder, gentler display for user errors - ones that normally
     * a user could correct.  This is not typically user-fixable in a
     * Java Web Start launch environment, but the nature of the error still
     * does not call for a stack trace.
     *
     * @param ue UserError to be displayed
     * @param rb ResourceBundle from which to retrieve i18n strings
     */
    public static void showUserError(final UserError ue, final ResourceBundle rb) {
        showText(ue.messageForGUIDisplay(), rb);
    }


    /**
     *Displays a dialog box reporting an error, listing the stack trace in
     *a text box and allowing the user to copy the stack trace to the platform's
     *clipboard before dismissing the dialog box.
     *
     *@param t the Throwable error detected and being reported
     *@param rb a ResourceBundle containing localizable strings
     */
    public static void showErrors(Throwable t, ResourceBundle rb) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        t.printStackTrace(ps);
        showText(baos.toString(), rb);
    }

    /**
     * Manages the dialog box to display whatever error information has already
     * been formatted.
     *
     * @param text message to display
     * @param rb ResourceBundle containing the i18n strings
     */
    private static void showText(final String text, final ResourceBundle rb) {

        ErrorDisplayDialog.rb = rb;

        /*
         *The JOptionPane class will accept other Components as elements of
         *the dialog box.  Build a JTextArea containing the stack trace.  Do not
         *let the user edit the text.
         */
        JTextArea stackTraceArea = new JTextArea();
        stackTraceArea.setEditable(false);
        stackTraceArea.setRows(16);

        stackTraceArea.setText(text);

        /*
         *Place the text area inside a scroll pane.
         */
        JScrollPane sp = new JScrollPane(stackTraceArea);

        /*
         *Build a check box for the user to click to copy the error information
         *to the platform's clipboard.
         */
        JCheckBox copyToClipboardCB = new JCheckBox(getString("jwsacc.errorDialog.copyToClipboardLabel"));

        /*
         *Display the dialog box that also contains the text area's scroll
         *pane and the checkbox and then wait for the user to close it.
         */
        boolean copyToClipboard = showDialog(
                getString("jwsacc.errorDialog.mainMessage.1") +
                    LINE_SEP +
                    getString("jwsacc.errorDialog.mainMessage.2"),
                sp,
                copyToClipboardCB
                );

        if (copyToClipboard) {
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection ss = new StringSelection(stackTraceArea.getText());

            try {
                cb.setContents(ss, null);
            }
            catch (Throwable e) {
                /*
                 *If we cannot copy the text to the clipboard, tell the user that
                 *and suggest that he or she manually copy it.
                 */
                showDialog(
                        getString("jwsacc.errorDialog.errorCopyingMessage.1") +
                            LINE_SEP +
                            getString("jwsacc.errorDialog.errorCopyingMessage.2"),
                        sp,
                        null /* no checkbox this time */
                );
            }
        }
    }

    /**
     *Displays a dialog box with the specified main message and stack trace.
     *@param mainMessage the string to display at the top of the dialog box introducing the stack trace
     *@param sp the JScrollPane containing the text area
     *@param copyToClipboardCB the JCheckBox to use - null if this is a report that copying itself failed
     *@return whether the user wants to copy the error text to the clipboard
     */
    private static boolean showDialog(
            String mainMessage,
            JScrollPane sp,
            JCheckBox copyToClipboardCB) {
        String close = getString("jwsacc.errorDialog.closeLabel");
        Object [] displayElements = (copyToClipboardCB == null) ?
                    new Object[] {mainMessage, sp} :
                    new Object[] {mainMessage, sp, copyToClipboardCB};
        JOptionPane pane = new JOptionPane(
                displayElements,
                JOptionPane.ERROR_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                new String[] {close},
                close);

        JDialog dialog = pane.createDialog(null, getString("jwsacc.errorDialog.title"));
        dialog.setResizable(true);
        dialog.setVisible(true);
        boolean result = (copyToClipboardCB == null ? false : copyToClipboardCB.isSelected());
        dialog.dispose();
        return result;
    }

    private static String getString(String key) {
        return rb.getString(key);
    }
}
