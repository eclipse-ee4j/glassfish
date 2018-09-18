/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package samples.jms.soaptojms;



/**
 * This class implements an exception which can wrapped a lower-level exception.
 *
 */
public class ServiceLocatorException extends Exception {
  private Exception exception;

  /**
   * Creates a new ServiceLocatorException wrapping another exception, and with a detail message.
   * @param message the detail message.
   * @param exception the wrapped exception.
   */
  public ServiceLocatorException(String message, Exception exception) {
    super(message);
    this.exception = exception;
    return;
  }

  /**
   * Creates a ServiceLocatorException with the specified detail message.
   * @param message the detail message.
   */
  public ServiceLocatorException(String message) {
    this(message, null);
    return;
  }

  /**
   * Creates a new ServiceLocatorException wrapping another exception, and with no detail message.
   * @param exception the wrapped exception.
   */
  public ServiceLocatorException(Exception exception) {
    this(null, exception);
    return;
  }

  /**
   * Gets the wrapped exception.
   *
   * @return the wrapped exception.
   */
  public Exception getException() {
    return exception;
  }

  /**
   * Retrieves (recursively) the root cause exception.
   *
   * @return the root cause exception.
   */
  public Exception getRootCause() {
    if (exception instanceof ServiceLocatorException) {
      return ((ServiceLocatorException) exception).getRootCause();
    }
    return exception == null ? this : exception;
  }

  public String toString() {
    if (exception instanceof ServiceLocatorException) {
      return ((ServiceLocatorException) exception).toString();
    }
    return exception == null ? super.toString() : exception.toString();
  }
}
