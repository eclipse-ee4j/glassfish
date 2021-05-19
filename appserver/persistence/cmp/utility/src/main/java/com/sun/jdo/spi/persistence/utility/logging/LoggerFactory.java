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

/** This interface provides for isolation between the JDO components that
 * need logging services and the implementation of the service.
 * <P>
 * This interface has no JDK 1.4 dependencies.
 *
 * @author  Craig Russell
 * @version 1.0
 */

public interface LoggerFactory {

    /** Get a Logger.  The class that implements this interface is responsible
     * for creating a logger for the named component.
     * The bundle name and class loader are passed to allow the implementation
     * to properly find and construct the internationalization bundle.
     * @param loggerName the relative name of this logger
     * @param bundleName the fully qualified name of the resource bundle
     * @param loader the class loader used to load the resource bundle, or null
     * @return the logger
     */
    Logger getLogger (String loggerName, String bundleName, ClassLoader loader);
}

