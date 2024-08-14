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

package com.sun.enterprise.web;


import com.sun.enterprise.web.session.PersistenceType;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.Context;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.web.LogFacade;

/**
  * @author Rajiv Mordani
  */

public class PersistenceStrategyBuilderFactory {


    private static final Logger _logger = LogFacade.getLogger();

    ServiceLocator services;


    /**
     * Constructor.
     */
    public PersistenceStrategyBuilderFactory(
            ServerConfigLookup serverConfigLookup, ServiceLocator services) {

        this.services = services;
    }


    /**
     * creates the correct implementation of PersistenceStrategyBuilder
     * if an invalid combination is input; an error is logged
     * and MemoryStrategyBuilder is returned
     */
    public PersistenceStrategyBuilder createPersistenceStrategyBuilder(
            String persistenceType, String frequency, String scope,
            Context ctx) {
        String resolvedPersistenceFrequency = null;
        String resolvedPersistenceScope = null;

        if (persistenceType.equalsIgnoreCase(PersistenceType.MEMORY.getType()) ||
                persistenceType.equalsIgnoreCase(PersistenceType.FILE.getType()) ||
                persistenceType.equalsIgnoreCase(PersistenceType.COOKIE.getType())) {
            // Deliberately leaving frequency & scope null
        } else {
            resolvedPersistenceFrequency = frequency;
            resolvedPersistenceScope = scope;
        }

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, LogFacade.CREATE_PERSISTENCE_STRATEGY_BUILDER_INFO,
                    new Object[] {persistenceType, resolvedPersistenceFrequency, resolvedPersistenceScope});
        }

        PersistenceStrategyBuilder builder = services.getService(PersistenceStrategyBuilder.class, persistenceType);
        if (builder == null) {
            builder = new MemoryStrategyBuilder();
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, LogFacade.PERSISTENT_STRATEGY_BUILDER_NOT_FOUND, persistenceType);
            }
        } else {
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST, LogFacade.CREATE_PERSISTENCE_STRATEGY_BUILDER_CLASS_NAME, builder.getClass());
                }

              builder.setPersistenceFrequency(frequency);
              builder.setPersistenceScope(scope);
              builder.setPassedInPersistenceType(persistenceType);
          }
        return builder;
    }

    /**
     * returns the application id for the module
     *
     * @param ctx the context
     */

    public String getApplicationId(Context ctx) {
        if (ctx instanceof WebModule) {
            return ((WebModule)ctx).getID();
        } else {
            return ctx.getName();
        }
    }

}
