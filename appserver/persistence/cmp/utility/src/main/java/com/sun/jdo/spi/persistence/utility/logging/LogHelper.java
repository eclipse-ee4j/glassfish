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

package com.sun.jdo.spi.persistence.utility.logging;

/** This class manages the logging facility for JDO components.  It is the
 * class that keeps track of the log factory in use for getting loggers
 * for use in JDO components.
 * <P>
 * This class has no JDK 1.4 dependencies.
 * <P>
 * The log factory is responsible for constructing the loggers and for
 * ensuring that there is only one logger per component.
 *
 * @author  Craig Russell
 * @version 1.0
 */

public class LogHelper {

    /** Flag to tell we are running in JDK 1.4 and can use
     * java.util.logging.Logger implementation.
     */
    protected static boolean jdk14 = isJDK14();

    /** LoggerFactory registered for creating new loggers.
     */
    protected static LoggerFactory loggerFactory = null;

    /** Get a Logger.  This call is delegated to the registered LoggerFactory.
     * If there is no registered LoggerFactory, then initialize one based on
     * whether we are running in JDK 1.4 (or higher).
     * The bundle name and class loader are passed to allow the implementation
     * to properly find and construct the internationalization bundle.
     * This method is synchronized to avoid race conditions where two threads
     * access a component using the same Logger at the same time.
     * @param loggerName the relative name of this logger
     * @param bundleName the fully qualified name of the resource bundle
     * @param loader the class loader used to load the resource bundle, or null
     * @return the logger
     */
    public synchronized static Logger getLogger(String loggerName, String bundleName, ClassLoader loader) {
        // if an implementation has not registered a LoggerFactory, use a standard one.
        if (loggerFactory == null) {
            if (jdk14) {
                loggerFactory = new LoggerFactoryJDK14();
            } else {
                loggerFactory = new LoggerFactoryJDK13();
            }
        }
        return loggerFactory.getLogger(loggerName, bundleName, loader);
    }

    /** Register a LoggerFactory for use in managed environments or
     * for special situations.  This
     * factory will be delegated to for all getLogger requests.
     * @param factory  the LoggerFactory to use for all getLogger requests
     */
    public static void registerLoggerFactory (LoggerFactory factory) {
        loggerFactory = factory;
    }

    /** Check to see if the JDK 1.4 logging environment is available.
     * @return  true if JDK 1.4 logging is available
     */
    public static boolean isJDK14() {
        try {
            Class.forName("java.util.logging.Logger"); //NOI18N
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

}
