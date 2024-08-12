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

package org.glassfish.web.ha.strategy.builder;

import com.sun.enterprise.container.common.spi.util.JavaEEIOUtils;
import com.sun.enterprise.web.BasePersistenceStrategyBuilder;
import com.sun.enterprise.web.ServerConfigLookup;
import com.sun.enterprise.web.WebModule;

import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;
import org.glassfish.ha.store.api.Storeable;
import org.glassfish.ha.store.util.SimpleMetadata;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.web.deployment.runtime.SessionManager;
import org.glassfish.web.ha.session.management.CompositeMetadata;
import org.glassfish.web.ha.session.management.FullSessionFactory;
import org.glassfish.web.ha.session.management.HASessionStoreValve;
import org.glassfish.web.ha.session.management.ModifiedAttributeSessionFactory;
import org.glassfish.web.ha.session.management.ModifiedSessionFactory;
import org.glassfish.web.ha.session.management.ReplicationAttributeStore;
import org.glassfish.web.ha.session.management.ReplicationStore;
import org.glassfish.web.ha.session.management.ReplicationWebEventPersistentManager;
import org.glassfish.web.ha.session.management.SessionFactory;
import org.glassfish.web.valve.GlassFishValve;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Rajiv Mordani
 */

@Service(name="replicated")
@PerLookup
public class ReplicatedWebMethodSessionStrategyBuilder extends BasePersistenceStrategyBuilder {
    @Inject
    private ReplicationWebEventPersistentManager rwepMgr;

    @Inject
    private JavaEEIOUtils ioUtils;

    public ReplicatedWebMethodSessionStrategyBuilder() {
        super();
    }

    public void initializePersistenceStrategy(
            Context ctx,
            SessionManager smBean,
            ServerConfigLookup serverConfigLookup)
    {

        super.initializePersistenceStrategy(ctx, smBean, serverConfigLookup);
        //super.setPassedInPersistenceType("replicated");
        if (this.getPersistenceScope().equals("session")) {
            setupReplicationWebEventPersistentManager(SimpleMetadata.class,
                    new FullSessionFactory(),
                    new ReplicationStore(ioUtils),
                    ctx, serverConfigLookup);
        } else if (this.getPersistenceScope().equals("modified-session")) {
            setupReplicationWebEventPersistentManager(SimpleMetadata.class,
                    new ModifiedSessionFactory(),
                    new ReplicationStore(ioUtils),
                    ctx, serverConfigLookup);
        } else if (this.getPersistenceScope().equals("modified-attribute")) {
            setupReplicationWebEventPersistentManager(CompositeMetadata.class,
                    new ModifiedAttributeSessionFactory(),
                    new ReplicationAttributeStore(ioUtils),
                    ctx, serverConfigLookup);
        } else {
            throw new IllegalArgumentException(this.getPersistenceScope());
        }

        HASessionStoreValve haValve = new HASessionStoreValve();
        StandardContext stdCtx = (StandardContext) ctx;
        stdCtx.addValve((GlassFishValve)haValve);

    }

    private <T extends Storeable> void setupReplicationWebEventPersistentManager(
            Class<T> metadataClass, SessionFactory sessionFactory, ReplicationStore store,
            Context ctx, ServerConfigLookup serverConfigLookup) {

        Map<String, Object> vendorMap = new HashMap<String, Object>();
        boolean asyncReplicationValue = serverConfigLookup.getAsyncReplicationFromConfig((WebModule)ctx);
        vendorMap.put("async.replication", asyncReplicationValue);
        vendorMap.put("broadcast.remove.expired", false);
        vendorMap.put("value.class.is.thread.safe", true);
        ReplicationWebEventPersistentManager<T> rwepMgr = getReplicationWebEventPersistentManager();
        rwepMgr.setSessionFactory(sessionFactory);
        rwepMgr.createBackingStore(this.getPassedInPersistenceType(), ctx.getPath(), metadataClass, vendorMap);

        boolean disableJreplica = serverConfigLookup.getDisableJreplicaFromConfig();
        rwepMgr.setMaxActiveSessions(maxSessions);
        rwepMgr.setMaxIdleBackup(0);
        rwepMgr.setRelaxCacheVersionSemantics(relaxCacheVersionSemantics);
        rwepMgr.setStore(store);
        rwepMgr.setDisableJreplica(disableJreplica);

        ctx.setManager(rwepMgr);
        if(!((StandardContext)ctx).isSessionTimeoutOveridden()) {
            rwepMgr.setMaxInactiveInterval(sessionMaxInactiveInterval);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Storeable>  ReplicationWebEventPersistentManager<T> getReplicationWebEventPersistentManager() {

        return rwepMgr;
    }
}
