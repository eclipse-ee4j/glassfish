/*
 * Copyright (c) 2001, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.loadbalancing.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import jakarta.jms.*;
import java.util.*;
import com.sun.s1peqe.ejb.bmp.enroller.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.ORBLocator;

public class EnrollerClient {

    private SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        EnrollerClient client = new EnrollerClient();
        client.runTestClient(args);
    }


    public void runTestClient(String[] args) {
        try {
            stat.addDescription("Testing loadbalancing app.");
            test01(args);
            testInAppClientContainer();
            stat.printSummary("loadbalancingAppID");
        } catch (Exception ex) {
            System.out.println("Exception in runTestClient: " + ex.toString());
            ex.printStackTrace();
        }
    }


    private void test01(String[] args) {
        String enrollerString = "";
        String queueString = "";
        try {
            if (args.length != 0 && args[0].equals("standalone")) {
                enrollerString = "ejb/MyEnroller";
                queueString = "jms/SampleQueue";
            } else {
                enrollerString = "java:comp/env/ejb/SimpleEnroller";
                queueString = "java:comp/env/jms/SampleQueue";
            }
            Properties env = new Properties();
            env.put("java.naming.factory.initial", "org.glassfish.jndi.cosnaming.CNCtxFactory");
            ORBLocator orbLocator = Globals.getDefaultHabitat().getService(ORBLocator.class);

            env.put("java.naming.corba.orb", orbLocator.getORB());
            // Initialize the Context with JNDI specific properties
            InitialContext ctx = new InitialContext(env);
            System.out.println("looking up ejb/MyStudent using org.glassfish.jndi.cosnaming.CNCtxFactory...");
            Object obj = ctx.lookup("ejb/MyStudent");
            System.out.println("Looked up ejb/MyStudent with CnCtxFactory...");
            StudentHome sH = (StudentHome) PortableRemoteObject.narrow(obj, StudentHome.class);
            Student tiffany = sH.create("111", "Tiffany Moore");
            System.out.println("Created student id 111 for Tiffany Moore");
            Student denise = sH.create("823", "Denise Smith");

            Context initial = new InitialContext();
            System.out.println("Looking up EJB REFs whose jndi name is specified as a corbaname: url ==>");
            System.out.println("Creating new Context 1..");

            System.out.println("Using Context 1, Looking up EJB using corbaname: url with global jndi name ==>");

            System.out.println("Using Context 1, looking up global jndi name ==>");
            Object objRef = initial.lookup("ejb/MyCourse");
            System.out.println("Looked up ejb/MyCourse");
            CourseHome cHome = (CourseHome) PortableRemoteObject.narrow(objRef, CourseHome.class);
            Course power = cHome.create("220", "Power J2EE Programming");

            Object objref = initial.lookup(enrollerString);
            System.out.println("Looked up " + enrollerString);
            EnrollerHome eHome = (EnrollerHome) PortableRemoteObject.narrow(objref, EnrollerHome.class);
            Enroller enroller = eHome.create();
            enroller.enroll("823", "220");
            enroller.enroll("823", "333");
            enroller.enroll("823", "777");
            enroller.enroll("456", "777");
            enroller.enroll("388", "777");

            System.out.println(denise.getName() + ":");
            ArrayList courses = denise.getCourseIds();
            Iterator i = courses.iterator();
            while (i.hasNext()) {
                String courseId = (String) i.next();
                Course course = cHome.findByPrimaryKey(courseId);
                System.out.println(courseId + " " + course.getName());
            }
            System.out.println();

            Course intro = cHome.findByPrimaryKey("777");
            System.out.println(intro.getName() + ":");
            courses = intro.getStudentIds();
            i = courses.iterator();
            while (i.hasNext()) {
                String studentId = (String) i.next();
                Student student = sH.findByPrimaryKey(studentId);
                System.out.println(studentId + " " + student.getName());
            }

            System.out.println("Looking up JMS Resource Refs ==>");
            System.out.println("Creating new Context 2..");
            Context initial1 = new InitialContext();
            jakarta.jms.Queue queue = (jakarta.jms.Queue) initial1.lookup(queueString);
            System.out.println("looked up " + queueString);

            System.out.println("Creating new Context 3...");
            Context initial2 = new InitialContext();
            jakarta.jms.QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) initial2
                .lookup("jms/QCFactory");
            System.out.println("Looked up jms/QCFactory");

            stat.addStatus("load balancing", stat.PASS);
        } catch (Exception ex) {
            stat.addStatus("load balancing", stat.FAIL);
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        }
    }


    private void testInAppClientContainer() {
        System.out.println("Creating new Context ...");
        try {
            InitialContext ctx = new InitialContext();
            Object obj = ctx.lookup("java:comp/InAppClientContainer");
            if (obj == null) {
                stat.addStatus("testInAppClientContainer", stat.FAIL);
                return;
            }
            Boolean result = (Boolean) obj;
            if (!result) {
                stat.addStatus("testInAppClientContainer", stat.FAIL);
            }
            System.out.println("Looked up java:comp/InAppClientContainer :" + result);
            stat.addStatus("testInAppClientContainer", stat.PASS);
        } catch (Exception ex) {
            stat.addStatus("testInAppClientContainer", stat.FAIL);
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        }
    }
}
