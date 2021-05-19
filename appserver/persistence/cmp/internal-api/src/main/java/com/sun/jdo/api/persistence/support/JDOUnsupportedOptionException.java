/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * JDOUnsupportedOptionException.java
 *
 * Created on March 8, 2000, 8:34 AM
 */

package com.sun.jdo.api.persistence.support;

/**
 *
 * @author  Craig Russell
 * @version 0.1
 */
public class JDOUnsupportedOptionException extends JDOUserException {

  /**
   * Creates a new <code>JDOUnsupportedOptionException</code> without detail message.
   */
  public JDOUnsupportedOptionException() {
  }


  /**
   * Constructs a new <code>JDOUnsupportedOptionException</code> with the specified detail message.
   * @param msg the detail message.
   */
  public JDOUnsupportedOptionException(String msg) {
    super(msg);
  }

  /**
   * Constructs a new <code>JDOUnsupportedOptionException</code> with the specified detail message and nested Exception.
   * @param msg the detail message.
   * @param nested the nested <code>Exception</code>.
   */
  public JDOUnsupportedOptionException(String msg, Exception nested) {
    super(msg, nested);
  }
}

