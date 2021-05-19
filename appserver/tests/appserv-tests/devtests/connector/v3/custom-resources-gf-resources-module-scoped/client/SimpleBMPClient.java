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

package com.sun.s1asdev.jdbc.CustomResourceFactories.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.CustomResourceFactories.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.CustomResourceFactories.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    private static final String testSuite = "Custom Resource Factories Test - ";
    public static void main(String[] args)
        throws Exception {

         SimpleReporterAdapter stat = new SimpleReporterAdapter();

        stat.addDescription(testSuite);

    InitialContext ic = new InitialContext();
    Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
    javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

    SimpleBMP simpleBMP = simpleBMPHome.create();

        String test = args[0];

        if(test.equalsIgnoreCase("javabean")){
            if ( simpleBMP.testJavaBean(args[1]) ) {
                stat.addStatus(testSuite+" Java Bean Factory : ", stat.PASS);
            } else {
                stat.addStatus(testSuite+" Java Bean Factory : ", stat.FAIL);
            }
        }else if(test.equalsIgnoreCase("primitivesandstring")){

            if ( simpleBMP.testPrimitives(args[1], args[2], args[3]) ) {
                stat.addStatus(testSuite+" Primitives And String Factory : ", stat.PASS);
            } else {
                stat.addStatus(testSuite+" Primitives And String Factory : ", stat.FAIL);
            }

        }else if(test.equalsIgnoreCase("properties")){
            Properties properties = new Properties();
            for (int i=1; i<args.length-1;i++){
                properties.put(args[i],args[i+1]);
                i++;
            }

            if ( simpleBMP.testProperties(properties, args[args.length-1])) {
                stat.addStatus(testSuite+" Properties : ", stat.PASS);
            } else {
                stat.addStatus(testSuite+" Properties : ", stat.FAIL);
            }
        }else if(test.equalsIgnoreCase("url")){
            if ( simpleBMP.testURL(args[1], args[2])) {
                stat.addStatus(testSuite+" URL : ", stat.PASS);
            } else {
                stat.addStatus(testSuite+" URL : ", stat.FAIL);
            }
        }



    stat.printSummary();
    }
}
