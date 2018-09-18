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

package test;

import java.io.*;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

public class MyListener implements ServletContextListener {

    private ServletContext sc = null;

    /**
     * Receives notification that the web application initialization
     * process is starting.
     *
     * @param sce The servlet context event
     */
    public void contextInitialized(ServletContextEvent sce) {
        sc = sce.getServletContext();
        sc.log("contextInitialized");
    }

    /**
     * Receives notification that the servlet context is about to be shut down.
     *
     * @param sce The servlet context event
     */
    public void contextDestroyed(ServletContextEvent sce) {
        sc.log("contextDestroyed");
    }


    @PreDestroy
    public void myPreDestroy() {
        sc.log("contextDestroyed");
        try {
            FileOutputStream fos = new FileOutputStream("/tmp/mytest");
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write("SUCCESS");
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
