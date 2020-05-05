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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/test")
public class TestServlet extends HttpServlet {
    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        ServletContext sc = req.getServletContext();
        ServletOutputStream output = res.getOutputStream();
        print(sc.getResourceAsStream("/abc.txt"), output);

        URL url = sc.getResource("/folder/def.txt");
        print(url.openConnection().getInputStream(), output);
    }

    private void print(InputStream input, OutputStream output) throws IOException {
        byte[] b = new byte[100];
        int len = -1;
        while ((len = input.read(b)) != -1) {
            output.write(b, 0, len);
        }
    }
}
