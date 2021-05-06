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

package j2eeguide.product;

import java.io.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.rmi.PortableRemoteObject;
import javax.naming.InitialContext;

import j2eeguide.product.*;

public class ProductServlet extends HttpServlet {

   public void doGet (HttpServletRequest req, HttpServletResponse res)
                      throws ServletException, IOException {

      res.setContentType("text/html");
      PrintWriter out = res.getWriter();

      out.println("<html>");
      out.println("<head>");
      out.println("<title>Product Sample App for CMP</title>");
      out.println("</head>");
      out.println("<body>");

       try {

           InitialContext ic = new InitialContext();

           System.out.println("looking up java:comp/env/ejb/MyProduct");
           Object objref = ic.lookup("java:comp/env/ejb/MyProduct");
           System.out.println("lookup ok");

           ProductHome home =
               (ProductHome)PortableRemoteObject.narrow(objref,
                                            ProductHome.class);

           Product duke = home.create("123", "Ceramic Dog", 10.00);
           out.println("<BR>" + duke.getDescription() + ": " + duke.getPrice());
           duke.setPrice(14.00);
           out.println("<BR>" + duke.getDescription() + ": " + duke.getPrice());

           duke = home.create("456", "Wooden Duck", 13.00);
           duke = home.create("999", "Ivory Cat", 19.00);
           duke = home.create("789", "Ivory Cat", 33.00);
           duke = home.create("876", "Chrome Fish", 22.00);

           Product earl = home.findByPrimaryKey("876");
           out.println("<BR>" + earl.getDescription() + ": " + earl.getPrice());

           Collection c = home.findByDescription("Ivory Cat");
           Iterator i = c.iterator();

           while (i.hasNext()) {
              Product product = (Product)i.next();
              String productId = (String)product.getPrimaryKey();
              String description = product.getDescription();
              double price = product.getPrice();
              out.println("<BR>" + productId + ": " + description + " " + price);
           }

           c = home.findInRange(10.00, 20.00);
           i = c.iterator();

           while (i.hasNext()) {
              Product product = (Product)i.next();
              String productId = (String)product.getPrimaryKey();
              double price = product.getPrice();
              out.println("<BR>" + productId + ": " + price);
           }

       } catch (Exception ex) {
           System.err.println("Caught an exception." );
           ex.printStackTrace();
       }

      out.println("</body>");
      out.println("</html>");
   }

}
