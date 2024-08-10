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

package org.glassfish.web.ha.session.management;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.Session;
import org.apache.catalina.session.PersistentManagerBase;
import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreException;
import org.glassfish.ha.store.api.Storeable;
import org.glassfish.web.ha.LogFacade;

/**
 * @author Rajiv Mordani
 */
public abstract class ReplicationManagerBase<T extends Storeable> extends PersistentManagerBase {

    protected BackingStore<String, T> backingStore;
    protected SessionFactory sessionFactory;

    protected static final String name = "ReplicationManagerBase";

    protected Logger _logger = LogFacade.getLogger();

    protected boolean relaxCacheVersionSemantics = false;
    protected boolean disableJreplica = false;

    public BackingStore<String, T> getBackingStore() {
        return this.backingStore;
    }

    public abstract void createBackingStore(String persistenceType, String storeName, Class<T> metadataClass, Map<String, Object> vendorMap);

    public Session createNewSession() {
        return sessionFactory.createSession(this);
    }

    public Session createEmptySession() {
        Session sess = sessionFactory.createSession(this);
        return sess;
    }


    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setBackingStore(BackingStore<String, T> backingStore) {
        this.backingStore = backingStore;
    }

    public void doRemove(String id) {
        try {
            backingStore.remove(id);
        } catch (BackingStoreException e) {
            _logger.warning(LogFacade.FAILED_TO_REMOVE_SESSION);
        }
    }

    public boolean isSessionVersioningSupported() {
        return true;
    }

    public Session findSession(String id, String version) throws IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in findSession: version=" + version);
        }
        if(!this.isSessionIdValid(id) || version == null) {
            return null;
        }
        Session loadedSession = null;
        long requiredVersion = 0L;
        long cachedVersion = -1L;
        try {
            requiredVersion = Long.parseLong(version);
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("Required version " + requiredVersion);
            }
        } catch (NumberFormatException ex) {
             _logger.log(Level.INFO, LogFacade.REQUIRED_VERSION_NFE, ex);
            //deliberately do nothing
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("findSession:requiredVersion=" + requiredVersion);
        }
        Session cachedSession = sessions.get(id);
        if(cachedSession != null) {
            cachedVersion = cachedSession.getVersion();
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("findSession:cachedVersion=" + cachedVersion);
        }
        //if version match return cached session else purge it from cache
        //if relaxCacheVersionSemantics is set true then we return the
        //cached version even if it is greater than the required version
        if(cachedVersion == requiredVersion || (isRelaxCacheVersionSemantics() && (cachedVersion > requiredVersion))) {
            return cachedSession;
        } else {
            //if relaxCacheVersionSemantics - we do not remove because even
            //though stale we might return it as the best we can do
            if(cachedVersion < requiredVersion && (!isRelaxCacheVersionSemantics())) {
                this.removeSessionFromManagerCache(cachedSession);
                cachedSession = null;
                cachedVersion = -1L;
            }
        }
        // See if the Session is in the Store
        if(requiredVersion != -1L) {
            loadedSession = swapIn(id, version);
        } else {
            loadedSession = swapIn(id);
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("findSession:swappedInSession=" + loadedSession);
        }

        if(loadedSession == null || loadedSession.getVersion() < cachedVersion) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicationManagerBase>>findSession:returning cached version:" + cachedVersion);
            }
            return cachedSession;
        }
        if(loadedSession.getVersion() < requiredVersion && (!isRelaxCacheVersionSemantics())) {
            loadedSession = null;
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationManagerBase>>findSession:returning:" + loadedSession);
        }
        return (loadedSession);

    }

    /** should relax cache version semantics be applied */
    public boolean isRelaxCacheVersionSemantics() {
        return relaxCacheVersionSemantics;
    }

    /**
     * set the relaxCacheVersionSemantics
     * @param value
     */
    public void setRelaxCacheVersionSemantics(boolean value) {
        relaxCacheVersionSemantics = value;
    }


    public void removeSessionFromManagerCache(Session session) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in " + this.getClass().getName() + ">>removeSessionFromManagerCache:session = " + session);
        }
        if(session == null) {
            return;
        }
        Session removed = null;
        removed = sessions.remove(session.getIdInternal());
        if (removed != null && _logger.isLoggable(Level.FINE)){
            _logger.fine("Remove from manager cache id=" + session.getId());
        }
    }

    public void setDisableJreplica(boolean disableJreplica) {
        this.disableJreplica = disableJreplica;
    }

    public boolean isDisableJreplica() {
        return  this.disableJreplica;
    }



    public abstract void doValveSave(Session session);

    public abstract String getReplicaFromPredictor(String sessionId, String oldJreplicaValue);
}
