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

package com.sun.s1asdev.ejb.bmp.handle.mix.ejb;

import java.util.ArrayList;
import jakarta.ejb.EJBObject;
import java.rmi.RemoteException;

public interface Enroller extends EJBObject {

 /**
  * Enrolls a Student in a course
  * @param studentId primary key of the student object
  * @param courseId primary key of the course object
  * @exception RemoteException
  */
   public void enroll(String studentId, String courseId)
      throws RemoteException;
 /**
  * Un-Enrolls a Student in a course
  * @param studentId primary key of the student object
  * @param courseId primary key of the course object
  * @exception RemoteException
  */

   public void unEnroll(String studentId, String courseId)
      throws RemoteException;
 /**
  * Deletes a Student
  * @param studentId primary key of the student object
  * @exception RemoteException
  */

   public void deleteStudent(String studentId)
      throws RemoteException;

 /**
  * Deletes a Course
  * @param courseId primary key of the course object
  * @exception RemoteException
  */
   public void deleteCourse(String courseId)
      throws RemoteException;
 /**
  * Returns an Arraylist of StudentsIds enrolled in a course
  * @param courseId primary key of the course object
  * @exception RemoteException
  */

   public ArrayList getStudentIds(String courseId)
      throws RemoteException;
 /**
  * Return an ArrayList of CourseIds that student is enroller in
  * @param studentId primary key of the student object
  * @exception RemoteException
  */

   public ArrayList getCourseIds(String studentId)
      throws RemoteException;

   public String testEnrollerHomeHandle()
       throws RemoteException;

   public String testEnrollerHandle()
       throws RemoteException;

   public String testStudentHomeHandle()
       throws RemoteException;

   public String testStudentHandle(String studentID)
       throws RemoteException;

}
