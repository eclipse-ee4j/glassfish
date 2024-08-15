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

package com.sun.ejb.containers;

import com.sun.logging.LogDomains;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreException;
import org.glassfish.ha.store.util.SimpleMetadata;

/**
 * A class to checkpoint HA enabled SFSBs as a single transactional unit.
 *
 * @author Mahesh Kannan
 */
public class SFSBTxCheckpointCoordinator {

    private static final Logger _logger =
            LogDomains.getLogger(SFSBTxCheckpointCoordinator.class, LogDomains.EJB_LOGGER);

    private String haStoreType;

    private ArrayList ctxList = new ArrayList();

    SFSBTxCheckpointCoordinator(String haStoreType) {
        this.haStoreType = haStoreType;
    }

    void registerContext(SessionContextImpl ctx) {
        ctxList.add(ctx);
    }

    void doTxCheckpoint() {
        SessionContextImpl[] contexts = (SessionContextImpl[]) ctxList.toArray(
                new SessionContextImpl[ctxList.size()]);
        int size = contexts.length;
        ArrayList<StoreAndBeanState> states = new ArrayList<StoreAndBeanState>(size);

        for (int i = 0; i < size; i++) {
            SessionContextImpl ctx = contexts[i];
            StatefulSessionContainer container =
                    (StatefulSessionContainer) ctx.getContainer();
            SimpleMetadata beanState = container.getSFSBBeanState(ctx);
            if (beanState != null) {
                states.add(new StoreAndBeanState((Serializable) ctx.getInstanceKey(),
                        container.getBackingStore(), beanState, !ctx.existsInStore()));
            }
        }

        if (states.size() > 0) {
            StoreAndBeanState[] beanStates = states.toArray(new StoreAndBeanState[states.size()]);

            try {
                for (StoreAndBeanState st : states) {
                    st.store.save(st.key, st.state, st.isNew);
                }
            } catch (BackingStoreException sfsbEx) {
                _logger.log(Level.WARNING, "Exception during checkpointSave",
                        sfsbEx);
            } catch (Throwable th) {
                _logger.log(Level.WARNING, "Exception during checkpointSave",
                        th);
            }
        }

        for (int i = 0; i < size; i++) {
            SessionContextImpl ctx = contexts[i];
            StatefulSessionContainer container =
                    (StatefulSessionContainer) ctx.getContainer();
            container.txCheckpointCompleted(ctx);
        }
    }

    private static final class StoreAndBeanState {
        Serializable key;
        BackingStore<Serializable, SimpleMetadata> store;
        SimpleMetadata state;
        boolean isNew;

        StoreAndBeanState(Serializable key,
                          BackingStore<Serializable, SimpleMetadata> store, SimpleMetadata state,
                          boolean isNew) {
            this.key = key;
            this.store = store;
            this.state = state;
            this.isNew = isNew;
        }
    }

}
