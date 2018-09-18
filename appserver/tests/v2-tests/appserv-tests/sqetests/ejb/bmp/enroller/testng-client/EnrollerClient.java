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

package com.sun.s1peqe.ejb.bmp.enroller.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.util.*;
import com.sun.s1peqe.ejb.bmp.enroller.ejb.*;

import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;


public class EnrollerClient {

    public static void main(String[] args) {
        org.testng.TestNG testng = new org.testng.TestNG();
        testng.setTestClasses(
            new Class[] { com.sun.s1peqe.ejb.bmp.enroller.client.EnrollerClient.class } );
        testng.run();
    }

    StudentHome studentHome = null;
    CourseHome courseHome = null;
    EnrollerHome enrollerHome = null;

    @Configuration(beforeTestClass = true)
    public void initialize() throws Exception {
        InitialContext ic = new InitialContext();

        studentHome = (StudentHome)PortableRemoteObject.narrow(
            ic.lookup("java:comp/env/ejb/SimpleStudent"), StudentHome.class);

        System.out.println("Narrowed StudentHome !!");

        courseHome = (CourseHome)PortableRemoteObject.narrow(
            ic.lookup("java:comp/env/ejb/SimpleCourse"), CourseHome.class);
        System.out.println("Narrowed CourseHome !!");

        enrollerHome = (EnrollerHome)PortableRemoteObject.narrow(
            ic.lookup("java:comp/env/ejb/SimpleEnroller"), EnrollerHome.class);

        System.out.println("Narrowed EnrollerHome !!");
    }

    @Test
    public void test1() throws Exception {

        Student denise = studentHome.create("823", "Denise Smith");

        Course power = courseHome.create("220", "Power J2EE Programming");

        Enroller enroller = enrollerHome.create();
        enroller.enroll("823", "220");
        enroller.enroll("823", "333");
        enroller.enroll("823", "777");
        enroller.enroll("456", "777");
        enroller.enroll("388", "777");

        System.out.println(denise.getName() + ":");
        ArrayList courses = denise.getCourseIds();
        Iterator i = courses.iterator();
        while (i.hasNext()) {
            String courseId = (String)i.next();
            Course course = courseHome.findByPrimaryKey(courseId);
            System.out.println(courseId + " " + course.getName());
        }

        System.out.println();

        Course intro = courseHome.findByPrimaryKey("777");
        System.out.println(intro.getName() + ":");
        courses = intro.getStudentIds();
        i = courses.iterator();
        while (i.hasNext()) {
            String studentId = (String)i.next();
            Student student = studentHome.findByPrimaryKey(studentId);
            System.out.println(studentId + " " + student.getName());
        }
    }
}
