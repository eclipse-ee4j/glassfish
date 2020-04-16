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

import java.io.*;
import java.net.*;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.SessionCookieConfig;

public class MyListener implements ServletContextListener {

    /*
     * Cookie path.
     */
    protected String COOKIE_PATH = "/";

    /*
     * Cookie domain.
     */
    protected String COOKIE_DOMAIN = ".iplanet.com";

    /*
     * Cookie comment.
     */
    protected String COOKIE_COMMENT
        =  URLEncoder.encode("Sun-Java-System/Application-Server-PE-8.0 Session Tracking Cookie");

    /**
     * Receives notification that the web application initialization
     * process is starting.
     *
     * @param sce The servlet context event
     */
    public void contextInitialized(ServletContextEvent sce) {
        SessionCookieConfig scc =
            sce.getServletContext().getSessionCookieConfig();
        scc.setPath(COOKIE_PATH);
        scc.setDomain(COOKIE_DOMAIN);
        scc.setComment(COOKIE_COMMENT);
        scc.setSecure(true);
        scc.setHttpOnly(true);
    }


    /**
     * Receives notification that the servlet context is about to be shut down.
     *
     * @param sce The servlet context event
     */
    public void contextDestroyed(ServletContextEvent sce) {

        ServletContext sc = sce.getServletContext();
        sc.log("contextDestroyed");

    }
}

