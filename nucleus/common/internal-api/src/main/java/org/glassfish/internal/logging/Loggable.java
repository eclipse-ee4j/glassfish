/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.internal.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Loggable objects are used when there is a need to collect log entry data
 * without actually logging the message until a later time. Type casting is weak
 * for the log parameters provided when constructing. The user must ensure that
 * the arguments associated with a particular log message are of the appropriate
 * type and in the correct order.
 */
public class Loggable {

  private static final String FORMAT_PREFIX = "[{0}:{1}]";

  private String id;
  private Object[] args;
  private Throwable thrown;
  private Logger logger;
  private Level level;

  private static Throwable getThrowable(Object[] args) {
    if (args == null)
      return null;
    int candidateIndex = args.length - 1;
    if (candidateIndex >= 0) {
      Object throwableCandidate = args[candidateIndex];
      if (throwableCandidate instanceof Throwable) {
        return (Throwable) throwableCandidate;
      }
    }
    return null;
  }

  /**
   * Constructor
   * @exclude
   */
  public Loggable(Level level, String id, Object[] args, Logger logger) {
    this.level = level;
    this.id = id;
    this.args = args;
    this.thrown = getThrowable(this.args);
    this.logger = logger;
  }

  /**
   * Log the message.
   */
  public String log() {
    LogRecord rec = new LogRecord(level, getMessage(false,false));
    if (thrown != null) {
      rec.setThrown(thrown);
    }
    logger.log(rec);// [i18n ok]
    return id;
  }

  /**
   * Gets the contents of the message body without appending a stack trace. This
   * is particularly useful when using the value of a loggables message as the
   * value when creating an exception.
   *
   */
  public String getMessageBody() {
    return getMessage(true, false);
  }

  /**
   * Get the message in specified locale.
   */
  private String getMessage(boolean prefix, boolean addTrace) {

    StringBuffer sb = new StringBuffer();

    if (prefix) {
      Object[] preArgs = { getSubSystem(), id };
      sb.append(MessageFormat.format(FORMAT_PREFIX, preArgs));
    }
    sb.append(MessageFormat.format(getBody(), args));

    // if last arg was a throwable and addTrace is set, stick exception on
    // end
    if (addTrace && (thrown != null)) {
      sb.append("\n");
      sb.append(throwable2StackTrace(thrown));
    }

    return sb.toString();
  }

  private Object throwable2StackTrace(Throwable th) {
    ByteArrayOutputStream ostr = new ByteArrayOutputStream();
    th.printStackTrace(new PrintStream(ostr));
    return ostr.toString();
  }

  private String getBody() {
    return logger.getResourceBundle().getString(id);
  }

  private String getSubSystem() {
    return logger.getName();
  }

  /**
   * Get the message in current locale with [subsytem:id] prefix.
   */
  public String getMessage() {
    return getMessage(true, true);
  }

  /**
   * Get the message in current locale, no prefix.
   */
  public String getMessageText() {
    return getMessage(false, true);
  }

  /**
   * Get the message id.
   */
  public String getId() {
    return id;
  }

}
