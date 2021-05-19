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

import java.util.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class ProductClient {

   private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

   public static void main(String[] args) {

       try {

           Context initial = new InitialContext();
           Object objref = initial.lookup("java:comp/env/MyProduct");

           ProductHome home =
               (ProductHome)PortableRemoteObject.narrow(objref,
                                            ProductHome.class);
           System.out.println("START");
           stat.addDescription("j2eeguide_product");

           Product duke = home.create("123", "Ceramic Dog", 10.00);
           System.out.println(duke.getDescription() + ": " + duke.getPrice());
           duke.setPrice(14.00);
           System.out.println(duke.getDescription() + ": " + duke.getPrice());

           duke = home.create("456", "Wooden Duck", 13.00);
           duke = home.create("999", "Ivory Cat", 19.00);
           duke = home.create("789", "Ivory Cat", 33.00);
           duke = home.create("876", "Chrome Fish", 22.00);

           Product earl = home.findByPrimaryKey("876");
           System.out.println(earl.getDescription() + ": " + earl.getPrice());

           Collection c = home.findByDescription("Ivory Cat");
           Iterator i = c.iterator();

           while (i.hasNext()) {
              Product product = (Product)i.next();
              String productId = (String)product.getPrimaryKey();
              String description = product.getDescription();
              double price = product.getPrice();
              System.out.println(productId + ": " + description + " " + price);
           }

           c = home.findInRange(10.00, 20.00);
           i = c.iterator();

           while (i.hasNext()) {
              Product product = (Product)i.next();
              String productId = (String)product.getPrimaryKey();
              double price = product.getPrice();
              System.out.println(productId + ": " + price);
           }
           stat.addStatus("ejbclient j2eeguide_product", stat.PASS);
           System.out.println("FINISH");

       } catch (Exception ex) {
           System.err.println("Caught an exception." );
           ex.printStackTrace();
           stat.addStatus("ejbclient j2eeguide_product", stat.FAIL);
       }
       stat.printSummary("j2eeguide_product");
   }
}
