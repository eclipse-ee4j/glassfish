/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.tools.classmodel;

import static org.junit.Assert.*;

import org.junit.Test;


public class UtilitiesTest {

  @Test
  public void testSortInhabitants() {
    StringBuilder sb = new StringBuilder();
    sb.append("class=service1,index=contract1:name1,index=annotation1\n");
    sb.append("class=service2,index=contract1,index=annotation1\n");
    sb.append("class=service3,index=contract2:name2,index=contract1:name2,index=annotation2,index=annotation1,b=2,a=1\n");
    sb.append("class=service4,a=1\n");
    String expected = sb.toString();
    String output = Utilities.sortInhabitantsDescriptor(testCase(), false);
    assertEquals(expected, output);
  }

  @Test
  public void testSortInhabitantsAndContents() {
    StringBuilder sb = new StringBuilder();
    sb.append("class=service1,index=annotation1,index=contract1:name1\n");
    sb.append("class=service2,index=annotation1,index=contract1\n");
    sb.append("class=service3,index=annotation1,index=annotation2,index=contract1:name2,index=contract2:name2,a=1,b=2\n");
    sb.append("class=service4,a=1\n");
    String expected = sb.toString();
    String output = Utilities.sortInhabitantsDescriptor(testCase(), true);
    assertEquals(expected, output);
  }
  
  @Test
  public void testEmptySort() {
    String output = Utilities.sortInhabitantsDescriptor("", true);
    assertEquals("", output);
  }
  
  private String testCase() {
    StringBuilder sb = new StringBuilder();
    sb.append("# comment\n");
    sb.append("class=service2,index=contract1,index=annotation1\n");
    sb.append("class=service1,index=contract1:name1,index=annotation1\n");
    sb.append("class=service4,a=1\n");
    sb.append("class=service3,index=contract2:name2,index=contract1:name2,index=annotation2,index=annotation1,b=2,a=1\n");
    return sb.toString();
  }

}
