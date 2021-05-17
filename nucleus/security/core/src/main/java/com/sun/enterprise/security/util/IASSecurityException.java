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

package com.sun.enterprise.security.util;

import java.lang.*;

/**
 * General exception class for iAS security failures.
 *
 * <P>This class takes advantage of the JDK1.4 Throwable objects which
 * can carry the original cause of the exception. This prevents losing
 * the information on what caused the problem to arise.
 *
 * <P>Ideally there should be common top level iAS Exceptions to extend.
 *
 */

public class IASSecurityException extends Exception
{
  private boolean noMsg;

  /**
   * Constructor.
   *
   * @param msg The detail message.
   *
   */
  public IASSecurityException(String msg)
  {
    super(msg);
    noMsg=false;
  }


  /**
   * Constructor.
   *
   * @param msg The detail message.
   * @param cause The cause (which is saved for later retrieval by the
   *    getCause() method).
   *
   */
  public IASSecurityException(String msg, Throwable cause)
  {
    super(msg, cause);
    noMsg=false;
  }


  /**
   * Constructor.
   *
   * @param cause The cause (which is saved for later retrieval by the
   *    getCause() method).
   *
   */
  public IASSecurityException(Throwable cause)
  {
    super(cause);
    noMsg=true;
  }


  /**
   * Returns a description of this exception. If a root cause was included
   * during construction, its message is also included.
   *
   * @return Message containing information about the exception.
   *
   */
  public String getMessage()
  {
    StringBuffer sb=new StringBuffer();
    sb.append(super.getMessage());
    Throwable cause=getCause();

    if (!noMsg && cause!=null) {
      sb.append(" [Cause: ");
      sb.append(cause.toString());
      sb.append("] ");
    }

    return sb.toString();
  }




}
