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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/TestServlet")
public class TestServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("text/plain");
        res.addHeader("Transfer-encoding", "chunked");
        res.addHeader("TE", "trailers");
        res.addHeader("Trailer", "bar1, bar2");

        StringBuilder sb = new StringBuilder();

        final InputStream in = req.getInputStream();
        int b;
        while ((b = in.read()) != -1) {
            sb.append((char) b);
        }

        System.out.println("--> body = " + sb.toString());

        if (req.isTrailerFieldsReady()) {
            Map<String, String> reqTrailerFields = req.getTrailerFields();
            sb.append(reqTrailerFields.get("foo1"));
            sb.append(reqTrailerFields.get("foo2"));
            sb.append(reqTrailerFields.size());
        }

        res.setTrailerFields(new Supplier<Map<String, String>>() {
            @Override
            public Map<String, String> get() {
                Map<String, String> map = new HashMap<>();
                map.put("bar1", "A");
                map.put("bar2", "B");
                System.out.println("--> supplier return: " + map);
                return map;
            }
        });
        PrintWriter writer = res.getWriter();
        writer.write(sb.toString());
    }
}
