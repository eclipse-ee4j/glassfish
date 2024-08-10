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

import com.sun.enterprise.security.ssl.SSLUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.ChoiceCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;

import org.glassfish.internal.api.Globals;

/**
 * This implementation of LoginDialog If these are not set, then it queries the user in the command window.
 *
 * @author Harish Prabandham
 */
public final class TextLoginDialog implements LoginDialog {

    private static Logger _logger = null;

    static {
        _logger = SecurityLoggerInfo.getLogger();
    }
    private String username = null;
    private char[] password = null;
    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(TextLoginDialog.class);

    public TextLoginDialog(Callback[] callbacks) {
        try {
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof NameCallback) {
                    NameCallback nc = (NameCallback) callbacks[i];
                    System.err.print(nc.getPrompt());
                    if (nc.getDefaultName() != null) {
                        System.err.print("[" + nc.getDefaultName() + "]: ");
                    } else {
                        System.err.print(": ");
                    }

                    System.err.flush();
                    username = (new BufferedReader(new InputStreamReader(System.in))).readLine();
                    if ((nc.getDefaultName() != null) && ((username == null) || (username.trim().length() == 0))) {
                        username = nc.getDefaultName();
                    }
                    nc.setName(username);

                } else if (callbacks[i] instanceof PasswordCallback) {
                    PasswordCallback pc = (PasswordCallback) callbacks[i];
                    char[] passwd = null;
                    Object consoleObj = null;
                    Method readPasswordMethod = null;
                    try {
                        Method consoleMethod = System.class.getMethod("console");
                        consoleObj = consoleMethod.invoke(null);
                        readPasswordMethod = consoleObj.getClass().getMethod("readPassword", String.class,
                            Array.newInstance(Object.class, 1).getClass());
                    } catch (Exception ex) {
                    }

                    if (consoleObj != null && readPasswordMethod != null) {
                        passwd = (char[]) readPasswordMethod.invoke(consoleObj, "%s", new Object[] { pc.getPrompt() });
                    } else {
                        System.err.print(pc.getPrompt());
                        System.err.flush();
                        passwd = readPassword(System.in);
                    }
                    if (passwd != null) {
                        pc.setPassword(passwd);
                        Arrays.fill(passwd, ' ');
                    }
                } else if (callbacks[i] instanceof ChoiceCallback) {
                    ChoiceCallback cc = (ChoiceCallback) callbacks[i];
                    /* Get the keystore password to see if the user is
                     * authorized to see the list of certificates
                     */
                    String lbl = (localStrings.getLocalString("enterprise.security.keystore", "Enter the KeyStore Password "));
                    SSLUtils sslUtils = Globals.get(SSLUtils.class);
                    System.out.println(lbl + " : (max 3 tries)");
                    int cnt = 0;
                    for (cnt = 0; cnt < 3; cnt++) {
                        // Let the user try putting password thrice
                        System.out.println(lbl + " : ");
                        String s = (new BufferedReader(new InputStreamReader(System.in))).readLine();
                        if (s != null) {
                            char[] kp = s.toCharArray();
                            if (sslUtils.verifyMasterPassword(kp)) {
                                break;
                            } else {
                                String errmessage = localStrings.getLocalString("enterprise.security.IncorrectKeystorePassword",
                                    "Incorrect Keystore Password");
                                System.err.println(errmessage);
                            }
                            Arrays.fill(kp, ' ');
                        }
                    }
                    if (cnt >= 3) {
                        cc.setSelectedIndex(-1);
                    } else {
                        System.err.println(cc.getPrompt());
                        System.err.flush();
                        String[] choices = cc.getChoices();
                        for (int j = 0; j < choices.length; j++) {
                            System.err.print("[" + j + "] ");
                            System.err.println(choices[j]);
                        }
                        String line = (new BufferedReader(new InputStreamReader(System.in))).readLine();

                        if (line != null) {
                            int sel = Integer.parseInt(line);
                            cc.setSelectedIndex(sel);
                        }
                    }
                }
            }
        } catch (Exception e) {
            _logger.log(Level.SEVERE, SecurityLoggerInfo.usernamePasswordEnteringSecurityError, e);
        }

    }

    /**
     * @return The username of the user.
     */
    @Override
    public String getUserName() {
        return username;
    }

    /**
     * @return The password of the user in plain text...
     */
    @Override
    public final char[] getPassword() {
        return (password == null) ? null : Arrays.copyOf(password, password.length);
    }

    private static char[] readPassword(InputStream in) throws IOException {
        char[] lineBuffer;
        char[] buf;
        int i;

        buf = lineBuffer = new char[128];
        int room = buf.length;
        int offset = 0;
        int c;

        loop: while (true) {
            switch (c = in.read()) {
            case -1:
            case '\n':
                break loop;

            case '\r':
                int c2 = in.read();
                if ((c2 != '\n') && (c2 != -1)) {
                    if (!(in instanceof PushbackInputStream)) {
                        in = new PushbackInputStream(in);
                    }
                    ((PushbackInputStream) in).unread(c2);
                } else {
                    break loop;

                }
            default:
                if (--room < 0) {
                    buf = new char[offset + 128];
                    room = buf.length - offset - 1;
                    System.arraycopy(lineBuffer, 0, buf, 0, offset);
                    Arrays.fill(lineBuffer, ' ');
                    lineBuffer = buf;
                }
                buf[offset++] = (char) c;
                break;
            }
        }

        if (offset == 0) {
            return null;
        }

        char[] ret = new char[offset];
        System.arraycopy(buf, 0, ret, 0, offset);
        Arrays.fill(buf, ' ');

        return ret;
    }
}
