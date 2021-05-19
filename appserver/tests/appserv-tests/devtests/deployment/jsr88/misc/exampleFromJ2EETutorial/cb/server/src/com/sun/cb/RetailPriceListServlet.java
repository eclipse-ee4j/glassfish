/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.cb;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

/**
 * This is a simple example of an HTTP Servlet.  It responds to the GET
 * method of the HTTP protocol.
 */
public class RetailPriceListServlet extends HttpServlet {

   public void doGet (HttpServletRequest request,
                       HttpServletResponse response)
   throws ServletException, IOException {

                  HttpSession session = request.getSession();
                         ServletContext context = getServletContext();
      RetailPriceList rpl = new RetailPriceList();
      context.setAttribute("retailPriceList", rpl);
      ShoppingCart cart = new ShoppingCart(rpl);
      session.setAttribute("cart", cart);

      PrintWriter out = response.getWriter();


                  // then write the data of the response
      out.println("<html><body  bgcolor=\"#ffffff\">" +
                  "Reloaded price list." + "</html></body>");
    }
}
