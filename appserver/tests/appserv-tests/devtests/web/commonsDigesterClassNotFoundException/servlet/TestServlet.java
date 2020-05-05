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
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.apache.commons.digester.Digester;
import org.xml.sax.InputSource;
import mypackage.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        Digester digester = new Digester();

        digester.setValidating(false);
        digester.setNamespaceAware(false);

        digester.addObjectCreate("foo", "mypackage.Foo");
        digester.addSetProperties("foo");
        digester.addObjectCreate("foo/bar", "mypackage.Bar");
        digester.addSetProperties("foo/bar");
        digester.addSetNext("foo/bar", "addBar", "mypackage.Bar");

        try {
            Foo foo = (Foo) digester.parse(
                getServletContext().getResourceAsStream("/input.txt"));
            res.getWriter().print(foo.getName());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

}
