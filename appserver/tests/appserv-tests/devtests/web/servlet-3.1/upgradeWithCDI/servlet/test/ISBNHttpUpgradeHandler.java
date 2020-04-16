/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.inject.Inject;
import javax.naming.InitialContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.WebConnection;

/**
 * The protocol is as follows:
 * Client:
 *  (a_isbn_to_be_verified CRLF)*| EXIT
 *   and tokens can be separated by " \t\n\r\f".
 * Server:
 *  (a_previous_isbn (true|false) CRLF)*
 */
public class ISBNHttpUpgradeHandler implements HttpUpgradeHandler {
    @Inject
    private ISBNValidator isbnValidator;

    private boolean debug = false;

    private String appName = null;

    public ISBNHttpUpgradeHandler() {
    }

    @Override
    public void init(WebConnection wc) {
        System.out.println("ISBNHttpUpgradeHandler.init");
        try {
            InitialContext initialContext = new InitialContext();
            appName = (String)initialContext.lookup("java:app/AppName");
            if (debug) {
                System.out.println("--> appName: " + appName);
            }

            ServletInputStream input = wc.getInputStream();
            ReadListenerImpl readListener =
                new ReadListenerImpl(appName, input, wc, isbnValidator, debug);
            input.setReadListener(readListener);
            wc.getOutputStream().flush();
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void destroy() {
        if (debug) {
            System.out.println("--> destroy");
        }
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
