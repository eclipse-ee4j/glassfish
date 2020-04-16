/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestServlet extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        File logFile = new File("mylog4j.log");
        logFile.delete();

        /*
         * Use commons-logging APIs for logging. Message will be logged into
         * the file specified in log4j.properties resource.
         */
        Log log = LogFactory.getLog(this.getClass());
        log.error(log.getClass() + ": This is my test log message");

        FileInputStream fis = new FileInputStream(logFile);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            res.getWriter().println(br.readLine());
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

        logFile.delete();
    }
}
