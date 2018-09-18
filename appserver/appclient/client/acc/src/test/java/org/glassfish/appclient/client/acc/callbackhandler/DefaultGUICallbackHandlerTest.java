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

package org.glassfish.appclient.client.acc.callbackhandler;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ChoiceCallback;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.LanguageCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tjquinn
 */
public class DefaultGUICallbackHandlerTest {

    public DefaultGUICallbackHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * The following test is here just to give example code for driving
     * the callback mechanism.
     *
     * @throws java.lang.Exception
     */
    @Ignore
    @Test
    public void testHandle() throws Exception {
        run();
    }

    private void run() throws IOException, UnsupportedCallbackException {
        CallbackHandler ch = new DefaultGUICallbackHandler();
        ChoiceCallback choiceCB = new ChoiceCallback(
                    "Choose one",
                    new String[] {
                        "First", "Second", "Third", "Fourth", "Fifth", "Sixth", "Seventh"},
                    0,
                    false);
        ConfirmationCallback confirmationCB = new ConfirmationCallback(
                    "Decide",
                    ConfirmationCallback.INFORMATION,
                    ConfirmationCallback.OK_CANCEL_OPTION,
                    ConfirmationCallback.OK);

        NameCallback nameCB = new NameCallback("Username", "who");
        PasswordCallback passwordCB = new PasswordCallback("Password", false);
        TextInputCallback textInCB = new TextInputCallback("Enter something interesting", "Good stuff to start with...");
        TextOutputCallback textOutCB = new TextOutputCallback(TextOutputCallback.WARNING,
                "Some fascinating text of great interest to the user goes here");
        LanguageCallback langCB = new LanguageCallback();
        Callback [] callbacks = new Callback[] {
            choiceCB, confirmationCB, nameCB, passwordCB, textInCB, textOutCB, langCB
        };

        ch.handle(callbacks);

        System.out.println("ChoiceCallback choice(s):");
        for (int index : choiceCB.getSelectedIndexes()) {
            if (index > 0) {
                System.out.println("  " + choiceCB.getChoices()[index]);
            } else {
                System.out.println("  Selection not made");
            }
        }


        System.out.print("ConfirmationCallback result: ");
        if (confirmationCB.getOptions() == null) {
            System.out.println(confirmationResultToString(confirmationCB.getSelectedIndex()));
        } else {
            System.out.println(confirmationCB.getOptions()[confirmationCB.getSelectedIndex()]);
        }

        System.out.println("NameCallback result: " + nameCB.getName());
        System.out.println("PasswordCallback result: " + new String(passwordCB.getPassword()));
        System.out.println("TextInputCallback result: " + textInCB.getText());
        System.out.println("LanguageCallback result: " + langCB.getLocale().getDisplayName());
    }

    private String confirmationResultToString(int result) {
        if (result == ConfirmationCallback.OK) {
            return "OK";
        }
        if (result == ConfirmationCallback.NO) {
            return "NO";
        }
        if (result == ConfirmationCallback.YES) {
            return "YES";
        }
        if (result == ConfirmationCallback.CANCEL) {
            return "CANCEL";
        }
        return "???";
    }}
