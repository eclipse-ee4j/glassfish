/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.jdbc.cmpsimple.client;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import com.sun.s1asdev.jdbc.cmpsimple.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    
    public static void main(String[] args) {
       
 	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "cmpsimple";
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/BlobTestBean");
            BlobTestHome bhome = (BlobTestHome)
                PortableRemoteObject.narrow(objref, BlobTestHome.class);

            System.out.println("START");

            BlobTest bean = bhome.create(new Integer(100), "FOO");
            System.out.println("Created: " +bean.getPrimaryKey());
            
            System.out.println("Testing new...");
            bean = bhome.findByPrimaryKey(new Integer(100));
            System.out.println(new String(bean.getName()));

            System.out.println("Testing old...");
            bean = bhome.findByPrimaryKey(new Integer(1));
            System.out.println(new String(bean.getName()));

	    stat.addStatus(testSuite + " test : ", stat.PASS);

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
	    stat.addStatus(testSuite +  "test : ", stat.FAIL);
        }

	stat.printSummary();

    }
    
}
