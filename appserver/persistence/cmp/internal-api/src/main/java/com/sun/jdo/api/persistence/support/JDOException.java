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
 * JDOException.java
 *
 * Created on March 8, 2000, 8:29 AM
 */

package com.sun.jdo.api.persistence.support;

/** This is the root of all JDO Exceptions.  It contains an optional
 * nested Exception and an optional message.
 * @author Craig Russell
 * @version 0.1
 */
public class JDOException extends java.lang.RuntimeException {

  /** This exception was generated because of an exception in the runtime library.
   * @serial the nested Exception
   */
  Exception nested;

  /** This exception may be the result of incorrect parameters supplied
   * to an API.  This is the array from which the user can determine
   * the cause of the problem.
   * The failed Object array is transient because it might contain
   * non-Serializable instances.
   */
  transient Object[] failed;

  /**
   * Creates a new <code>JDOException</code> without detail message.
   */
  public JDOException() {
  }


  /**
   * Constructs a new <code>JDOException</code> with the specified detail message.
   * @param msg the detail message.
   */
  public JDOException(String msg) {
    super(msg);
  }

  /** Constructs a new <code>JDOException</code> with the specified detail message
   * and nested Exception.
   * @param msg the detail message.
   * @param nested the nested <code>Exception</code>.
   */
  public JDOException(String msg, Exception nested) {
    super(msg, nested);
    this.nested = nested;
  }

  /** Constructs a new <code>JDOException</code> with the specified detail message
   * and failed object array.
   * @param msg the detail message.
   * @param failed the failed object array.
   */
  public JDOException(String msg, Object[] failed) {
    super(msg);
    this.failed = failed;
  }

  /** Constructs a new <code>JDOException</code> with the specified detail message,
   * nested exception, and failed object array.
   * @param msg the detail message.
   * @param nested the nested <code>Exception</code>.
   * @param failed the failed object array.
   */
  public JDOException(String msg, Exception nested, Object[] failed) {
    super(msg, nested);
    this.nested = nested;
    this.failed = failed;
  }

  /** The exception may need to add objects to an array of failed objects.
   * @param o    the failed object to add to an array.
   */
  public void addFailedObject(Object o) {
    if (failed == null)
    //Create new
    failed = new Object[] {o};
    else {
    //Extend exisisting
        int len = failed.length;
    Object[] ofailed = failed;
    failed = new Object[len + 1];
    for (int i = 0; i < len; i++)
        failed[i] = ofailed[i];

    failed[len] = o;
    }
  }

  /** The exception may include an array of failed objects.
   * @return the failed object array.
   */
  public Object[] getFailedObjectArray() {
    return failed;
  }

  /** The exception may have been caused by an Exception in the runtime.
   * @return the nested Exception.
   */
  public Exception getNestedException() {
    return nested;
  }

  /** The String representation includes the name of the class,
   * the descriptive comment (if any),
   * the String representation of the nested Exception (if any),
   * and the String representation of the failed Object array (if any).
   * @return the String.
   */
  public String toString() {
    int len = 0;
    if (failed != null) {
        len = failed.length;
    }
    // calculate approximate size of the String to return
    StringBuffer sb = new StringBuffer (100 + 10 * len);
    sb.append (super.toString());
    // include nested exception information
    if (nested != null) {
      sb.append ("\nNestedException: "); //NOI18N
      sb.append (nested.toString());
    }
    // include failed object information
    if (len > 0) {
      sb.append ("\nFailedObjectArray: ["); //NOI18N
      Object ofail = failed[0];
      sb.append (JDOHelper.printObject(ofail));
      for (int i=1; i<len; ++i) {
        sb.append (", "); //NOI18N
        ofail = failed[i];
        sb.append (JDOHelper.printObject(ofail));
      }
      sb.append ("]"); //NOI18N
    }
    return sb.toString();
  }

  /**
   * Prints this <code>JDOException</code> and its backtrace to the
   * standard error output.
   * Prints nested Throwables' stack trace as well.
   */
  public void printStackTrace() {
    printStackTrace(System.err);
  }

  /**
   * Prints this <code>JDOException</code> and its backtrace to the
   * specified print stream.
   * Prints nested Throwable's stack trace as well.
   * @param s <code>PrintStream</code> to use for output
   */
  public void printStackTrace(java.io.PrintStream s) {
    synchronized (s) {
      super.printStackTrace(s);
      if (nested != null) {
        s.println("\nNestedStackTrace: "); //NOI18N
        nested.printStackTrace(s);
      }
    }
  }

  /**
   * Prints this <code>JDOException</code> and its backtrace to the specified
   * print writer.
   * Prints nested Throwable's stack trace as well.
   * @param s <code>PrintWriter</code> to use for output
   */
  public void printStackTrace(java.io.PrintWriter s) {
    synchronized (s) {
      super.printStackTrace(s);
      if (nested != null) {
        s.println("\nNestedStackTrace: "); //NOI18N
        nested.printStackTrace(s);
      }
    }
  }

}

