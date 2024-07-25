/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.module.single.StaticModulesRegistry;
import com.sun.enterprise.security.ssl.SSLUtils;
import com.sun.enterprise.server.pluggable.SecuritySupport;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ChoiceCallback;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.LanguageCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.TextOutputCallback;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * This test is here just to give example code for driving the callback mechanism.
 *
 * @author tjquinn
 */
@DisabledIfSystemProperty(
    named = "java.awt.headless",
    matches = "true",
    disabledReason = "The test cannot work in headless mode and without human interaction, but it is still worth as an example.")
public class DefaultGUICallbackHandlerTest {

    private static final String[] CHOICES = {"First", "Second", "Third", "Fourth", "Fifth", "Sixth", "Seventh"};
    private StaticModulesRegistry registry;

    @BeforeEach
    public void init() {
        registry = new StaticModulesRegistry(getClass().getClassLoader());
        ServiceLocator locator = registry.createServiceLocator(getClass().getSimpleName());
        Globals.setDefaultHabitat(locator);
        DynamicConfiguration config = locator.getService(DynamicConfigurationService.class).createDynamicConfiguration();
        config.addActiveDescriptor(ProcessEnvironment.class);
        config.addActiveDescriptor(SecuritySupport.class);
        config.addActiveDescriptor(SSLUtils.class);
        config.commit();
    }

    @AfterEach
    public void shutdown() {
        if (Globals.getStaticBaseServiceLocator() != null) {
            Globals.getStaticBaseServiceLocator().shutdown();
        }
        if (registry != null) {
            registry.shutdown();
        }
    }

    @Test
    public void testHandle() throws Exception {
        ChoiceCallback choiceCB = new ChoiceCallback(
                    "Choose one",
                    CHOICES,
                    0,
                    false);
        ConfirmationCallback confirmationCB = new ConfirmationCallback(
                    "Decide",
                    ConfirmationCallback.INFORMATION,
                    ConfirmationCallback.OK_CANCEL_OPTION,
                    ConfirmationCallback.OK);

        NameCallback nameCB = new NameCallback("Username", "who");
        PasswordCallback passwordCB = new PasswordCallback("Password", false);
        passwordCB.setPassword("".toCharArray());

        TextInputCallback textInCB = new TextInputCallback("Enter something interesting", "Good stuff to start with...");
        TextOutputCallback textOutCB = new TextOutputCallback(TextOutputCallback.WARNING,
                "Some fascinating text of great interest to the user goes here");
        LanguageCallback langCB = new LanguageCallback();
        Callback[] callbacks = new Callback[] {
            choiceCB, confirmationCB, nameCB, passwordCB, textInCB, textOutCB, langCB
        };

        CallbackHandler ch = new DefaultGUICallbackHandler();
        ch.handle(callbacks);

        if (choiceCB.getSelectedIndexes() == null) {
            System.out.println("ChoiceCallback: nothing selected.");
        } else {
            assertEquals(1, choiceCB.getSelectedIndexes().length, "ChoiceCallback choice");
            final int firstSelected = choiceCB.getSelectedIndexes()[0];
            if (firstSelected == -1) {
                System.out.println("  Selection not made");
            } else {
                assertThat("ChoiceCallback choice", choiceCB.getSelectedIndexes()[0],
                    allOf(greaterThanOrEqualTo(0), lessThan(CHOICES.length)));
            }
        }

        System.out.print("ConfirmationCallback result: ");
        if (confirmationCB.getOptions() == null) {
            System.out.println(confirmationResultToString(confirmationCB.getSelectedIndex()));
        } else {
            System.out.println(confirmationCB.getOptions()[confirmationCB.getSelectedIndex()]);
        }

        assertNotNull(passwordCB.getPassword());
        assertAll(
            () -> assertEquals("who", nameCB.getName(), "NameCallback result"),
            () -> assertEquals("", new String(passwordCB.getPassword()), "PasswordCallback result"),
            () -> assertNull(textInCB.getText(), "TextInputCallback result"),
            () -> assertNull(langCB.getLocale(), "LanguageCallback result")
        );
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
