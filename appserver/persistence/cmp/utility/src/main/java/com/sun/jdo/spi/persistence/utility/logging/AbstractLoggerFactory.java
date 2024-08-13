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
 * AbstractLoggerFactory.java
 *
 * Created on May 13, 2002, 10:15 PM
 */

package com.sun.jdo.spi.persistence.utility.logging;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Rochelle Raccah
 * @version %I%
 */
abstract public class AbstractLoggerFactory implements LoggerFactory
{
    private final static String _domainPrefix = "com.sun.jdo."; //NOI18N

    private final static Map _loggerCache = new HashMap();

    private static final String _bundleName =
        "com.sun.jdo.spi.persistence.utility.logging.Bundle"; // NOI18N


    /** Get the error logger which is used to log things during creation of
     * loggers.
     */
    protected static Logger getErrorLogger ()
    {
        return LogHelper.getLogger("", _bundleName,  // NOI18N
            AbstractLoggerFactory.class.getClassLoader());
    }

    /** Get a Logger.  The class that implements this interface is responsible
     * for creating a logger for the named component.
     * The bundle name and class loader are passed to allow the implementation
     * to properly find and construct the internationalization bundle.
     * @param relativeLoggerName the relative name of this logger
     * @param bundleName the fully qualified name of the resource bundle
     * @param loader the class loader used to load the resource bundle, or null
     * @return the logger
     */
    public synchronized Logger getLogger (String relativeLoggerName,
        String bundleName, ClassLoader loader)
    {
        String absoluteLoggerName = getAbsoluteLoggerName(relativeLoggerName);
        Logger value = (Logger)_loggerCache.get(absoluteLoggerName);

        if (value == null)
        {
            value = createLogger(absoluteLoggerName, bundleName, loader);

            if (value != null)
                _loggerCache.put(absoluteLoggerName, value);
        }

        return value;
    }

    /** Create a new Logger.  Subclasses are responsible for creating a
     * logger for the named component.  The bundle name and class loader
     * are passed to allow the implementation to properly find and
     * construct the internationalization bundle.
     * @param absoluteLoggerName the absolute name of this logger
     * @param bundleName the fully qualified name of the resource bundle
     * @param loader the class loader used to load the resource bundle, or null
     * @return the logger
     */
    abstract protected Logger createLogger (String absoluteLoggerName,
        String bundleName, ClassLoader loader);

    protected String getDomainRoot () { return _domainPrefix; }

    protected String getAbsoluteLoggerName (String relativeLoggerName)
    {
        return (relativeLoggerName.startsWith("java") ?            //NOI18N
            relativeLoggerName : (getDomainRoot() + relativeLoggerName));
    }
}
