/*
 * Copyright (c) 2001, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.jndi.url.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.s1peqe.jndi.url.ejb.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class HTMLReaderClient {

   private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

   public static void main(String[] args) {
       try {
           stat.addDescription("Testing HTMLReader");
           Context initial = new InitialContext();
           System.out.println("looking up objref");
           Object objref = initial.lookup("java:comp/env/ejb/SimpleHTMLReader");

           System.out.println("getting home...");
           HTMLReaderHome home =
               (HTMLReaderHome)PortableRemoteObject.narrow(objref,
                                            HTMLReaderHome.class);

           System.out.println("creating bean...");
           HTMLReader htmlReader = home.create();
           System.out.println("getting contents...");
           StringBuffer contents = htmlReader.getContents();
           System.out.println("The contents of the HTML page follows:\n");
           System.out.print(contents);
           stat.addStatus("HTMLReader Test", stat.PASS);
       } catch (Exception ex) {
           stat.addStatus("HTMLReader Test", stat.FAIL);
           System.err.println("Caught an unexpected exception!");
           System.out.println("check the url being accessed in sun-ejb-jar.xml.");
           ex.printStackTrace();
       }
       stat.printSummary("urlID");
       System.exit(0);
   }
}
