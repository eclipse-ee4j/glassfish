/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.concurrent.runtime.deployer;

import com.sun.logging.LogDomains;
import org.glassfish.concurrent.runtime.ConcurrentRuntime;
import org.glassfish.enterprise.concurrent.*;
import org.glassfish.resourcebase.resources.api.ResourceInfo;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ConcurrentObjectFactory implements ObjectFactory {

    private static Logger _logger = LogDomains.getLogger(ConcurrentObjectFactory.class, LogDomains.JNDI_LOGGER);

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        Reference ref = (Reference) obj;
        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,"ConcurrentNamingObjectFactory: " + ref +
                    " Name:" + name);
        }
        BaseConfig config = (BaseConfig) ref.get(0).getContent();
        ResourceInfo resourceInfo = (ResourceInfo) ref.get(1).getContent();

        Object instance = null;
        switch(config.getType()) {
            case CONTEXT_SERVICE:
                instance = getContextService((ContextServiceConfig)config, resourceInfo);
                break;
            case MANAGED_EXECUTOR_SERVICE:
                instance = getManagedExecutorService((ManagedExecutorServiceConfig)config, resourceInfo);
                break;
            case MANAGED_SCHEDULED_EXECUTOR_SERVICE:
                instance = getManagedScheduledExecutorService((ManagedScheduledExecutorServiceConfig)config, resourceInfo);
                break;
            case MANAGED_THREAD_FACTORY:
                instance = getManagedThreadFactory((ManagedThreadFactoryConfig)config, resourceInfo);
                break;
            default:
                break;
        }
        return instance;
    }

    private ContextServiceImpl getContextService(ContextServiceConfig config, ResourceInfo resourceInfo) {

        ContextServiceImpl contextService = getRuntime().getContextService(resourceInfo, config);
        return contextService;
    }

    private ManagedThreadFactoryImpl getManagedThreadFactory(ManagedThreadFactoryConfig config, ResourceInfo resourceInfo) {
        ManagedThreadFactoryImpl managedThreadFactory = getRuntime().getManagedThreadFactory(resourceInfo, config);
        return managedThreadFactory;
    }

    private ManagedExecutorServiceAdapter getManagedExecutorService(ManagedExecutorServiceConfig config, ResourceInfo resourceInfo) {
        ManagedExecutorServiceImpl mes = getRuntime().getManagedExecutorService(resourceInfo, config);
        return mes.getAdapter();
    }

    private ManagedScheduledExecutorServiceAdapter getManagedScheduledExecutorService(ManagedScheduledExecutorServiceConfig config, ResourceInfo resourceInfo) {
        ManagedScheduledExecutorServiceImpl mes = getRuntime().getManagedScheduledExecutorService(resourceInfo, config);
        return mes.getAdapter();
    }

    private ConcurrentRuntime getRuntime() {
        return ConcurrentRuntime.getRuntime();
    }
}
