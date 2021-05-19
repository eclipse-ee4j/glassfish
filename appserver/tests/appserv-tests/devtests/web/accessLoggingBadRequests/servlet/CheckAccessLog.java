/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class CheckAccessLog extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        String location = req.getParameter("location");
        String[] files = new File(location + "/domains/domain1/logs/access").list();
        if (files != null && files.length == 2) {
            for (int i=0; i<files.length; i++) {
                resp.getWriter().println("File "+files[i]);
                if (files[i].startsWith("server")) {
                    File file = new File(location + "/domains/domain1/logs/access/"+files[i]);
                    try (FileInputStream fis = new FileInputStream(file);
                        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                        ) {
                        String line = br.readLine();
                        resp.getWriter().println("file content "+line);
                        resp.getWriter().println("file length "+file.exists()+" "+file.length());
                        if ((file.length() > 0) && (line.contains("400"))) {
                            resp.getWriter().println("SUCCESS!");
                        }
                    }
                }
            }
        }
    }
}
