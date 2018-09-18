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

package com.sun.enterprise.resource.pool.datastructure;

import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.resource.pool.ResourceHandler;
import com.sun.logging.LogDomains;

import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory to create appropriate datastructure type for the pool<br>
 *
 * @author Jagadish Ramu
 */
public class DataStructureFactory {
    //TODO synchronize datastructure creation ?
    protected final static Logger _logger = LogDomains.getLogger(DataStructureFactory.class,LogDomains.RSR_LOGGER);

    public static DataStructure getDataStructure(String className, String parameters, int maxPoolSize,
                                                 ResourceHandler handler, String strategyClass) throws PoolingException {
        DataStructure ds;

        if (className != null) {
            if(className.equals(ListDataStructure.class.getName())){
                ds = new ListDataStructure(parameters, maxPoolSize, handler, strategyClass);
            }else if(className.equals(RWLockDataStructure.class.getName())){
                ds = new RWLockDataStructure(parameters, maxPoolSize, handler, strategyClass);
            }else{
                ds = initializeCustomDataStructureInPrivilegedMode(className, parameters, maxPoolSize, handler, strategyClass);
            }
        } else {
            debug("Initializing RWLock DataStructure");
            ds = new RWLockDataStructure(parameters, maxPoolSize, handler, strategyClass);
        }
        return ds;
    }

    private static DataStructure initializeCustomDataStructureInPrivilegedMode(final String className,
                                                                               final String parameters,
                                                                               final int maxPoolSize,
                                                                               final ResourceHandler handler,
                                                                               final String strategyClass)
            throws PoolingException {
        Object result = AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {

                Object result = null;
                try {
                    result = initializeDataStructure(className, parameters, maxPoolSize, handler, strategyClass);
                } catch (Exception e) {
                    _logger.log(Level.WARNING, "pool.datastructure.init.failure", className);
                    _logger.log(Level.WARNING, "pool.datastructure.init.failure.exception", e);
                }
                return result;
            }
        });
        if (result != null) {
            return (DataStructure) result;
        } else {
            throw new PoolingException("Unable to initalize custom DataStructure : " + className);
        }
    }

    private static DataStructure initializeDataStructure(String className, String parameters, int maxPoolSize,
                                                         ResourceHandler handler, String strategyClass) throws Exception {
        DataStructure ds;
        Object[] constructorParameters = new Object[]{parameters, maxPoolSize, handler, strategyClass};

        Class class1 = Thread.currentThread().getContextClassLoader().loadClass(className);
        Constructor constructor = class1.getConstructor(String.class, int.class, ResourceHandler.class, String.class);
        ds = (DataStructure) constructor.newInstance(constructorParameters);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, "Using Pool Data Structure : ", className);
        }
        return ds;
    }

    private static void debug(String debugStatement) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, debugStatement);
        }
    }
}
