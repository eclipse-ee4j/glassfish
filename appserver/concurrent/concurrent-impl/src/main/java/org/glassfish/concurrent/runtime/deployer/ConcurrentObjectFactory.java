/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

import jakarta.enterprise.concurrent.ManagedExecutorService;

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.glassfish.concurrent.runtime.deployer.cfg.ContextServiceCfg;
import org.glassfish.concurrent.runtime.deployer.cfg.ManagedExecutorServiceCfg;
import org.glassfish.concurrent.runtime.deployer.cfg.ManagedScheduledExecutorServiceCfg;
import org.glassfish.concurrent.runtime.deployer.cfg.ManagedThreadFactoryCfg;
import org.glassfish.concurro.ContextServiceImpl;
import org.glassfish.concurro.ManagedThreadFactoryImpl;

import static org.glassfish.concurrent.runtime.ConcurrentRuntime.getRuntime;

public class ConcurrentObjectFactory implements ObjectFactory {

    private static final Logger LOG = LogDomains.getLogger(ConcurrentObjectFactory.class, LogDomains.JNDI_LOGGER, false);

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) {
        LOG.log(Level.FINE, "getObjectInstance(obj={0}, name={1}, nameCtx, environment)", new Object[] {obj, name});
        Reference ref = (Reference) obj;
        Object config = ref.get(0).getContent();
        if (config instanceof ContextServiceCfg) {
            return getContextService((ContextServiceCfg) config);
        } else if (config instanceof ManagedThreadFactoryCfg) {
            return getManagedThreadFactory((ManagedThreadFactoryCfg) config);
        } else if (config instanceof ManagedExecutorServiceCfg) {
            return getManagedExecutorService((ManagedExecutorServiceCfg) config);
        } else if (config instanceof ManagedScheduledExecutorServiceCfg) {
            return getManagedScheduledExecutorService((ManagedScheduledExecutorServiceCfg) config);
        } else {
            throw new IllegalArgumentException("Unknown type of " + config);
        }
    }

    private ContextServiceImpl getContextService(ContextServiceCfg config) {
        return getRuntime().getContextService(config);
    }

    private ManagedThreadFactoryImpl getManagedThreadFactory(ManagedThreadFactoryCfg config) {
        return getRuntime().getManagedThreadFactory(config);
    }

    private ManagedExecutorService getManagedExecutorService(ManagedExecutorServiceCfg config) {
        return getRuntime().getManagedExecutorService(config).getAdapter();
    }

    private ManagedExecutorService getManagedScheduledExecutorService(ManagedScheduledExecutorServiceCfg config) {
        return getRuntime().getManagedScheduledExecutorService(config).getAdapter();
    }
}
