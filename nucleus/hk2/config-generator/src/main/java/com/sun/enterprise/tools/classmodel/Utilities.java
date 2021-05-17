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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jvnet.hk2.component.MultiMap;

/**
 * Utilities
 *
 * @author Jeff Trent
 */
public class Utilities {

  /**
   * Sorts all of the lines in an inhabitants descriptor
   *
   * @param in the input string
   * @param innerSort true if each line in the inhabitants file is sorted as well
   * @return the sorted output string
   */
  public static String sortInhabitantsDescriptor(String in, boolean innerSort) {
    ArrayList<String> lines = new ArrayList<String>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(in.getBytes())));
    String line;
    try {
      while (null != (line = reader.readLine())) {
        if (!line.startsWith("#") && !line.isEmpty()) {
          lines.add(innerSort ? innerSort(line) : line);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Collections.sort(lines);

    StringBuilder sb = new StringBuilder();
    for (String oline : lines) {
      sb.append(oline).append("\n");
    }
    return sb.toString();
  }

  static String innerSort(String line) {
    MultiMap<String, String> mm = split(line);
    StringBuilder sb = new StringBuilder();

    // class
    List<String> vals = mm.remove("class");
    assert(null != vals && 1 == vals.size());
    sb.append("class=").append(vals.iterator().next());

    // indicies
    vals = mm.remove("index");
    if (null != vals && vals.size() > 0) {
      Collections.sort(vals);
      for (String index : vals) {
        sb.append(",index=").append(index);
      }
    }

    // metadata
    vals = new ArrayList<String>(mm.keySet());
    Collections.sort(vals);
    for (String key : vals) {
      List<String> subVals = new ArrayList<String>(mm.get(key));
      Collections.sort(subVals);
      for (String val : subVals) {
        sb.append(",").append(key).append("=").append(val);
      }
    }

    return sb.toString();
  }

  static MultiMap<String, String> split(String value) {
    MultiMap<String, String> result = new MultiMap<String, String>();
    String split[] = value.split(",");
    for (String s : split) {
      String split2[] = s.split("=");
      assert(2 == split2.length);
      result.add(split2[0], split2[1]);
    }
    return result;
  }
}
