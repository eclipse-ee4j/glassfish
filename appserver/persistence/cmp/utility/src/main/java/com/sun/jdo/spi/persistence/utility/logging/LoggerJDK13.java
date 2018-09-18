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
 * LoggerJDK13.java
 *
 * Created on May 15, 2002, 2:00 PM
 */

package com.sun.jdo.spi.persistence.utility.logging;

import java.io.PrintStream;

/** 
 * This class provides an implementation of the 
 * com.sun.jdo.spi.persistence.utility.Logger interface which 
 * subclasses the AbstractLogger and provides an implementation of 
 * its abstract methods which logs to a PrintStream (System.out).
 * Note that this logger doesn't explicitly flush the PrintStream and 
 * depends on the JVM for flushing.
 *
 * @author Rochelle Raccah
 * @version %I%
 */
public class LoggerJDK13 extends AbstractLogger
{
	private static final PrintStream _printStream = System.out;

	/** Creates a new LoggerJDK13.  The supplied class loader or the
	 * loader which loaded this class must be able to load the bundle.
	 * @param loggerName the full domain name of this logger
	 * @param bundleName the bundle name for message translation
	 * @param loader the loader used for looking up the bundle file
	 * and possibly the logging.properties or alternative file
	 */
	public LoggerJDK13 (String loggerName, String bundleName, 
		ClassLoader loader)
	{
		super(loggerName, bundleName, loader);
	}

	private static PrintStream getPrintStream () { return _printStream; }

	/**
	 * Log a message.
	 * <p>
	 * If the logger is currently enabled for the message 
	 * level then the given message, and the exception dump, 
	 * is forwarded to all the
	 * registered output Handler objects.
	 * <p>
	 * @param level The level for this message
	 * @param msg The string message (or a key in the message catalog)
	 * @param thrown The exception to log
	 */
	public synchronized void log (int level, String msg, Throwable thrown)
	{
		if (isLoggable(level))
		{
			logInternal(level, getMessage(msg));
			thrown.printStackTrace(getPrintStream());
		}
	}

	/**
	 * This method does the actual logging.  It is expected that if a 
	 * check for isLoggable is desired for performance reasons, it has 
	 * already been done, as it should not be done here.  This 
	 * implementation uses a print stream for logging.
	 * @param level the level to print
	 * @param message the message to print
	 */
	protected synchronized void logInternal (int level, String message)
	{
		getPrintStream().println(getMessageWithPrefix(level, message));
	}
}
