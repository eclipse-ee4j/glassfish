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

/**
 * This servlet, which is the target of the forwarded request, retrieves
 * the Shift-JIS encoded query parameter that was added by the origin servlet.
 */
package test;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.text.Collator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServletTest2 extends HttpServlet {

    Collator japCollator = Collator.getInstance(Locale.JAPANESE);

    public void doPost (HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Retrieve the query param added by the origin servlet
        String japName = req.getParameter("japaneseName");

        PrintWriter out = res.getWriter();

        if (japCollator.compare(japName,"\u3068\u4eba\u6587") == 0){
            out.println("MultiByteValue::PASS");
        } else {
            out.println("MultiByteValue::FAIL");
        }

        out.close();
    }

    public void doGet (HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        doPost (req,res);
    }
}
