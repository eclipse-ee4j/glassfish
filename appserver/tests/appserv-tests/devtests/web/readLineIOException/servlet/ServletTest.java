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

import java.io.IOException;
import java.io.BufferedReader;
import java.io.StringWriter;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.util.Enumeration;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class ServletTest extends HttpServlet {


    public void doPost(HttpServletRequest req,
                      HttpServletResponse resp)
      throws IOException, ServletException {


        BufferedReader reader = req.getReader();

        StringBuffer sb = new StringBuffer();
        for (String line = reader.readLine(); line != null; line = reader.readLine())
        {
            sb.append(line);
        }
        reader.close();

        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        bw.write("PASSED");
        bw.newLine();
        bw.flush();
        bw.close();
        resp.setBufferSize(sw.toString().length());
        resp.setContentLength(sw.toString().length());
        resp.setContentType("text/plain");
        Writer writer = resp.getWriter();
        writer.write(sw.toString());
        writer.flush();
        writer.close();
    }

}
