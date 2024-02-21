/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.ha.store.adapter.cache;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.inject.Inject;

import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreConfiguration;
import org.glassfish.ha.store.api.BackingStoreException;
import org.glassfish.ha.store.api.BackingStoreFactory;
import org.glassfish.ha.store.api.BackingStoreTransaction;
import org.glassfish.ha.store.spi.BackingStoreFactoryRegistry;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Mahesh Kannan
 */
@Service(name = "replicated")
@RunLevel(StartupRunLevel.VAL)
public class ShoalBackingStoreProxy
        implements PostConstruct, BackingStoreFactory {

    @Inject
    ServiceLocator habitat;

    @Inject
    Events events;

    @Override
    public <K extends Serializable, V extends Serializable> BackingStore<K, V> createBackingStore(BackingStoreConfiguration<K, V> conf) throws BackingStoreException {
        try {
            BackingStoreFactory storeFactory = habitat.getService(BackingStoreFactory.class, "shoal-backing-store-factory");
            return storeFactory.createBackingStore(conf);
        } catch (IllegalStateException ex) {
            String msg = "ReplicatedBackingStore requires GMS to be running in the target cluster before the application is deployed. ";
            throw new BackingStoreException("Exception while creating replicated BackingStore. " + msg, ex);
        } catch (Exception ex) {
            throw new BackingStoreException("Exception while creating shoal cache", ex);
        }
    }

    @Override
    public void postConstruct() {
        BackingStoreFactoryRegistry.register("replicated", this);
        Logger.getLogger(ShoalBackingStoreProxy.class.getName()).log(Level.FINE, "Registered SHOAL BackingStore Proxy with persistence-type = replicated");
        EventListener glassfishEventListener = event -> {
            if (event.is(EventTypes.SERVER_SHUTDOWN)) {
                BackingStoreFactoryRegistry.unregister("replicated");
                Logger.getLogger(ShoalBackingStoreProxy.class.getName()).log(Level.FINE, "Unregistered SHOAL BackingStore Proxy with persistence-type = replicated");
            } // else if (event.is(EventTypes.SERVER_READY)) {  }
        };
        events.register(glassfishEventListener);
    }

    @Override
    public BackingStoreTransaction createBackingStoreTransaction() {
        try {
            BackingStoreFactory storeFactory = habitat.getService(BackingStoreFactory.class, "shoal-backing-store-factory");
            return storeFactory.createBackingStoreTransaction();
        } catch (Exception ex) {
            //FIXME avoid runtime exception
            throw new RuntimeException("Exception while creating shoal cache", ex);
        }
    }
}
