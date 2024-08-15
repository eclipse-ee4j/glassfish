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

package com.sun.jdo.spi.persistence.support.sqlstore.ejb;

import com.sun.jdo.spi.persistence.utility.logging.LoggerFactoryJDK14;
import com.sun.jdo.spi.persistence.utility.logging.LoggerJDK14;

import java.security.AccessController;
import java.security.PrivilegedAction;


/**
 *
 * @author  Craig Russell
 * @version 1.0
 */

public class LoggerFactoryiAS extends LoggerFactoryJDK14 {

    /** The top level of the logger domain for application server.
     */
    protected String DOMAIN_ROOT = "jakarta.enterprise.resource.jdo."; //NOI18N

    /** Creates new LoggerFactory */
    public LoggerFactoryiAS() {
    }


    protected String getDomainRoot() {
        return DOMAIN_ROOT;
    }

    /** Create a new Logger.  Create a logger for the named component.
     * The bundle name is passed to allow the implementation
     * to properly find and construct the internationalization bundle.
     *
     * This operation is executed as a privileged action to allow
     * permission access for the following operations:
     * ServerLogManager.initializeServerLogger
     *
     * @param absoluteLoggerName the absolute name of this logger
     * @param bundleName the fully qualified name of the resource bundle
     * @return the logger
     */
    protected LoggerJDK14 createLogger (final String absoluteLoggerName,
                                        final String bundleName) {
        return (LoggerJDK14) AccessController.doPrivileged (
            new PrivilegedAction () {
                public Object run () {
                    LoggerJDK14 result = new LoggerJDK14(absoluteLoggerName, bundleName);
                    //Handlers and Formatters will be set in addLogger().
                    //ServerLogManager.initializeServerLogger(result);

                    return result;
                }
            }
        );
    }

    /**
     * This method is a no-op in the Sun ONE Application server.
     */
    protected void configureFileHandler(LoggerJDK14 logger) {
    }

}

