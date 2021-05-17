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

package com.sun.s1asdev.ejb.bmp.readonly.ejb;

import java.util.ArrayList;
import jakarta.ejb.EJBLocalObject;

public interface StudentLocal extends EJBLocalObject {

  /**
   * Returns the CourseIds that a student is enrolled in.
   * @param studentId primary key of the student object
   * @param courseId primary key of the course object
   *
   */
   public ArrayList getCourseIds();

  /**
   * Returns the Name of a student.
   */
   public String getName();


  /**
   * Sets the Name of a student.
   */
   public void setName(String name, boolean notify);
}
