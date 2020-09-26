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

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionListener;
import jakarta.servlet.http.HttpSessionEvent;

public class MyHttpSessionListener implements HttpSessionListener {

    static final String KEY = "successHttpSessionListener";

    /**
     * Receives notification that a session has been created.
     *
     * @param hse The HttpSessionEvent
     */
    public void sessionCreated(HttpSessionEvent hse) {
        // Do nothing
    }

    /**
     * Receives notification that a session is about to be invalidated.
     *
     * @param hse The HttpSessionEvent
     */
    public void sessionDestroyed(HttpSessionEvent hse) {


        HttpSession session = hse.getSession();
        ServletContext sc = session.getServletContext();
        try {
            System.out.println("HSL.sessionDestroyed: " + Thread.currentThread().getContextClassLoader());
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass("test.MyObject");
            System.out.println("HSL.sessionDestroyed clazz: " + clazz);
            sc.setAttribute(KEY, clazz);
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
