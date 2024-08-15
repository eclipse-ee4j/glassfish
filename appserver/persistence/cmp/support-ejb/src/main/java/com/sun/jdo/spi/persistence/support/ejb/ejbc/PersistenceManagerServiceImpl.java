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
 * PersistenceManagerServiceImpl.java
 *
 * Created on January 24, 2002
 */

package com.sun.jdo.spi.persistence.support.ejb.ejbc;

import com.sun.jdo.spi.persistence.support.sqlstore.ejb.CMPHelper;
import com.sun.jdo.spi.persistence.support.sqlstore.ejb.LoggerFactoryiAS;
import com.sun.jdo.spi.persistence.support.sqlstore.ejb.SunContainerHelper;
import com.sun.jdo.spi.persistence.utility.logging.LogHelper;

import org.glassfish.ejb.spi.CMPService;
import org.jvnet.hk2.annotations.Service;

/**
 * This class implements CMPService contract and allows to load Sun specific implementation of the
 * ContainerHelper when loaded.
 */
@Service
public class PersistenceManagerServiceImpl implements CMPService {

    // Initialize the appserver loggers.
    static {
        LogHelper.registerLoggerFactory(new LoggerFactoryiAS());
    }

    // Reference and force the initialization of the Sun specific
    // implementation of the TransactionHelper and the ContainerHelper.
    private static final Class helper = forceInit(SunContainerHelper.class);

    public boolean isReady() {
        // Checks that SunContainerHelper regeistered with CMPHelper
        return CMPHelper.isContainerReady();
    }

    /**
     * Forces the initialization of the class pertaining to the specified
     * <tt>Class</tt> object. This method does nothing if the class is already
     * initialized prior to invocation.
     *
     * @param klass the class for which to force initialization
     * @return <tt>klass</tt>
     */
    private static <T> Class<T> forceInit(Class<T> klass) {
        try {
            Class.forName(klass.getName(), true, klass.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);  // Can't happen
        }
        return klass;
    }

}
