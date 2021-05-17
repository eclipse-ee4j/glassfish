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

public class Constants {

  /**
   * This is the target inhabitants file built.
   * <p>
   * Passed as a system property.
   */
  public static final String PARAM_INHABITANT_TARGET_FILE = "inhabitants.target.file";

  /**
   * This is the source inhabitants file read.
   * <p>
   * Passed as a system property.
   */
  public static final String PARAM_INHABITANT_SOURCE_FILE = "inhabitants.source.file";

  /**
   * This is the source files (jars | directories) to introspect and build a habitat for.
   * <p>
   * Passed as a system property.
   */
  public static final String PARAM_INHABITANTS_SOURCE_FILES = "inhabitants.source.files";

  /**
   * This is the working classpath the introspection machinery will use to resolve
   * referenced contracts and annotations.  <b>Without this you may see a bogus
   * inhabitants file being generated.</b>  The indicator for this is a habitat with
   * only class names and missing indicies.
   * <p>
   * Passed as a system property.
   */
  public static final String PARAM_INHABITANTS_CLASSPATH = "inhabitants.classpath";

  /**
   * Set to true if the inhabitants should be sorted
   * <p>
   * Passed as a system property.
   */
  public static final String PARAM_INHABITANTS_SORTED = "inhabitants.sorted";

  /**
   * This is the optionally provided Advisor for pruning and/or caching the {@link #PARAM_INHABITANTS_CLASSPATH}.
   * <p>
   * Passed as a system property.
   */
  public static final String PARAM_INHABITANTS_CLASSPATH_ADVISOR = "inhabitants.classpath.advisor";
}
