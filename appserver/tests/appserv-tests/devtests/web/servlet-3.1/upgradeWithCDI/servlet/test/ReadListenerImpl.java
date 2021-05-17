/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.StringTokenizer;

import javax.naming.InitialContext;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.WebConnection;

/**
 * The protocol is as follows:
 * Client:
 *  (a_isbn_to_be_verified CRLF)*| EXIT
 *   and tokens can be separated by " \t\n\r\f".
 * Server:
 *  (a_previous_isbn (true|false) CRLF)*
 */
class ReadListenerImpl implements ReadListener {
    private static final String EXIT = "EXIT";
    private static final String DELIMITER = " \t\n\r\f";
    private static final String SPACE = " ";
    private static final String CRLF = "\r\n";

    private String appName = null;
    private ServletInputStream input = null;
    private ServletOutputStream output = null;
    private WebConnection wc = null;
    private ISBNValidator isbnValidator = null;
    private boolean debug;

    private volatile String unprocessedData = "";

    ReadListenerImpl(String aName, ServletInputStream in, WebConnection c,
            ISBNValidator isbnV, boolean d) throws IOException {
        appName = aName;
        input = in;
        wc = c;
        isbnValidator = isbnV;
        debug = d;
        output = wc.getOutputStream();
    }

    @Override
    public void onDataAvailable() throws IOException {
        System.out.println("--> onDataAvailable");

        StringBuilder sb = new StringBuilder(unprocessedData);

        int len = -1;
        byte b[] = new byte[15];
        while (input.isReady()
                && (len = input.read(b)) != -1) {
            String data = new String(b, 0, len);
            if (debug) {
                System.out.println("--> " + data);
            }
            sb.append(data);
        }

        try {
            processData(sb.toString());
        } catch(IOException ioe) {
            throw ioe;
        } catch(RuntimeException re) {
            throw re;
        } catch(Throwable t) {
            throw new IOException(t);
        }
    }

    @Override
    public void onAllDataRead() throws IOException {
        System.out.println("--> onAllDataRead");
        try {
            wc.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onError(final Throwable t) {
        System.out.println("--> onError: " + t);
        t.printStackTrace();

        try {
            wc.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void processData(String data) throws Exception {
        String lastToken = null;
        StringTokenizer tokens = new StringTokenizer(data, DELIMITER, true);
        boolean isExit = false;
        OutputStream output = wc.getOutputStream();
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if (debug) {
                System.out.println("--> token: " + token);
            }
            if (DELIMITER.contains(token)) {
                if (lastToken != null) {
                    if (EXIT.equals(lastToken)) {
                        if (debug) {
                            System.out.println("--> found EXIT");
                        }
                        isExit = true;
                        break;
                    } else {
                        boolean result = isbnValidator.isValid(lastToken);
                        if (debug) {
                           System.out.println("--> " + lastToken + ": " + result);
                        }
                        output.write((lastToken + SPACE + result + CRLF).getBytes());
                        output.flush();
                   }
                }
                lastToken = null;
            } else {
                lastToken = token;
            }
        }

        unprocessedData = ((lastToken != null) ? lastToken : "");

        if (isExit) {
            if (debug) {
                System.out.println("--> WebConnection#close");
            }
            wc.close();
            return;
        }

        // testing checking
        InitialContext initialContext = new InitialContext();
        String aName = (String)initialContext.lookup("java:app/AppName");
        if (!appName.equals(aName)) {
            throw new IllegalStateException();
        }
    }
}
