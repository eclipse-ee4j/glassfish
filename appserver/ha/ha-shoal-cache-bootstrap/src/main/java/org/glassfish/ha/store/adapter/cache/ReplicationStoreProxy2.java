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

package org.glassfish.ha.store.adapter.cache;

import jakarta.inject.Inject;

import java.io.Serializable;

import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.event.Events;
import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreConfiguration;
import org.glassfish.ha.store.api.BackingStoreException;
import org.glassfish.ha.store.api.BackingStoreFactory;
import org.glassfish.ha.store.api.BackingStoreTransaction;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Mahesh Kannan
 */
@Service(name = "replication")
@RunLevel(StartupRunLevel.VAL)
public class ReplicationStoreProxy2
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
        } catch (Exception ex) {
            throw new BackingStoreException("Exception while created shoal cache", ex);
        }
    }

    @Override
    public void postConstruct() {
// TBD:   Delete this proxy once we are certain that no subsystem is using "replication".
//        For now, commented out registering/unregistering "replication" to fix gf 13546.


//        BackingStoreFactoryRegistry.register("replication", this);
//        Logger.getLogger(ReplicationStoreProxy2.class.getName()).log(Level.FINE, "Registered ReplicationStoreProxy with persistence-type = replication");
//        EventListener glassfishEventListener = new EventListener() {
//            @Override
//            public void event(Event event) {
//                if (event.is(EventTypes.SERVER_SHUTDOWN)) {
//                    // BackingStoreFactoryRegistry.unregister("replication");
//                    //Logger.getLogger(ReplicationStoreProxy2.class.getName()).log(Level.FINE, "Unregistered ReplicationStoreProxy with persistence-type = replication");
//                } //else if (event.is(EventTypes.SERVER_READY)) { }
//            }
//        };
//        events.register(glassfishEventListener);
    }

    @Override
    public BackingStoreTransaction createBackingStoreTransaction() {
        try {
            BackingStoreFactory storeFactory = habitat.getService(BackingStoreFactory.class, "shoal-backing-store-factory");
            return storeFactory.createBackingStoreTransaction();
        } catch (Exception ex) {
            //FIXME avoid runtime exception
            throw new RuntimeException("Exception while created shoal cache", ex);
        }
    }
}
