/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
 * ReplicationWebEventPersistentManager.java
 *
 * Created on November 18, 2005, 3:38 PM
 *
 */

package org.glassfish.web.ha.session.management;

import jakarta.inject.Inject;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.util.Map;
import java.util.logging.Level;

import org.apache.catalina.Session;
import org.glassfish.gms.bootstrap.GMSAdapterService;
import org.glassfish.ha.common.GlassFishHAReplicaPredictor;
import org.glassfish.ha.common.HACookieInfo;
import org.glassfish.ha.common.HACookieManager;
import org.glassfish.ha.common.NoopHAReplicaPredictor;
import org.glassfish.ha.store.api.BackingStoreConfiguration;
import org.glassfish.ha.store.api.BackingStoreException;
import org.glassfish.ha.store.api.BackingStoreFactory;
import org.glassfish.ha.store.api.Storeable;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.web.ha.LogFacade;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author Rajiv Mordani
 */
@Service
@PerLookup
public class ReplicationWebEventPersistentManager<T extends Storeable> extends ReplicationManagerBase<T>
        implements WebEventPersistentManager {

    @Inject
    private ServiceLocator services;

    @Inject
    private GMSAdapterService gmsAdapterService;


    private GlassFishHAReplicaPredictor predictor;

    private String clusterName = "";

    private String instanceName = "";


    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "ReplicationWebEventPersistentManager/1.0";


    /**
     * The descriptive name of this Manager implementation (for logging).
     */
    private static final String name = "ReplicationWebEventPersistentManager";


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Manager implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (this.info);

    }

    /** Creates a new instance of ReplicationWebEventPersistentManager */
    public ReplicationWebEventPersistentManager() {
        super();
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationWebEventPersistentManager created");
        }
    }

    /**
    * called from valve; does the save of session
    *
    * @param session
    *   The session to store
    */
    public void doValveSave(Session session) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("in doValveSave");
        }

            try {
                ReplicationStore replicationStore = (ReplicationStore) this.getStore();
                replicationStore.doValveSave(session);
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine("FINISHED repStore.valveSave");
                }
            } catch (Exception ex) {
                ex.printStackTrace();

                _logger.log(Level.FINE, "exception occurred in doValveSave id=" + session.getIdInternal(),
                                ex);

            }
    }


    //START OF 6364900
    public void postRequestDispatcherProcess(ServletRequest request, ServletResponse response) {
        Session sess = this.getSession(request);

        if(sess != null) {
            doValveSave(sess);
        }
        return;
    }

    private Session getSession(ServletRequest request) {
        jakarta.servlet.http.HttpServletRequest httpReq =
            (jakarta.servlet.http.HttpServletRequest) request;
        jakarta.servlet.http.HttpSession httpSess = httpReq.getSession(false);
        if(httpSess == null) {
            return null;
        }
        String id = httpSess.getId();
        Session sess = null;
        try {
            sess = this.findSession(id);
        } catch (java.io.IOException ex) {}

        return sess;
    }
    //END OF 6364900

    //new code start


    //private static int NUMBER_OF_REQUESTS_BEFORE_FLUSH = 1000;
    //volatile Map<String, String> removedKeysMap = new ConcurrentHashMap<String, String>();
    //private static AtomicInteger requestCounter = new AtomicInteger(0);
    private static int _messageIDCounter = 0;
    //private AtomicBoolean  timeToChange = new AtomicBoolean(false);


    // ------------------------------------------------------------- Properties


    /**
     * Return the descriptive short name of this Manager implementation.
     */
    public String getName() {

        return this.name;

    }

    /**
     * Back up idle sessions.
     * Hercules: modified method we do not want
     * background saves when we are using web-event persistence-frequency
     */
    protected void processMaxIdleBackups() {
        //this is a deliberate no-op for this manager
        return;
    }

    /**
     * Swap idle sessions out to Store if too many are active
     * Hercules: modified method
     */
    protected void processMaxActiveSwaps() {
        //this is a deliberate no-op for this manager
        return;
    }

    /**
     * Swap idle sessions out to Store if they are idle too long.
     */
    protected void processMaxIdleSwaps() {
        //this is a deliberate no-op for this manager
        return;
    }

    public String getReplicaFromPredictor(String sessionId, String oldJreplicaValue) {
        if (isDisableJreplica()) {
            return null;
        }
        HACookieInfo cookieInfo = predictor.makeCookie(gmsAdapterService.getGMSAdapter().getClusterName(), sessionId, oldJreplicaValue);
        HACookieManager.setCurrrent(cookieInfo);
        return cookieInfo.getNewReplicaCookie();
    }


    @Override
    public void createBackingStore(String persistenceType, String storeName, Class<T> metadataClass, Map<String, Object> vendorMap) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("Create backing store invoked with persistence type " + persistenceType + " and store name " + storeName);
        }
        BackingStoreFactory factory = services.getService(BackingStoreFactory.class, persistenceType);
        BackingStoreConfiguration<String, T> conf = new BackingStoreConfiguration<String, T>();

        if(gmsAdapterService.isGmsEnabled()) {
            clusterName = gmsAdapterService.getGMSAdapter().getClusterName();
            instanceName = gmsAdapterService.getGMSAdapter().getModule().getInstanceName();
        }
        conf.setStoreName(storeName)
                .setClusterName(clusterName)
                .setInstanceName(instanceName)
                .setStoreType(persistenceType)
                .setKeyClazz(String.class).setValueClazz(metadataClass)
                .setClassLoader(this.getClass().getClassLoader());
        if (vendorMap != null) {
            conf.getVendorSpecificSettings().putAll(vendorMap);
        }

        try {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("About to create backing store " + conf);
            }
            this.backingStore = factory.createBackingStore(conf);
        } catch (BackingStoreException e) {
            _logger.log(Level.WARNING, LogFacade.COULD_NOT_CREATE_BACKING_STORE, e);
        }
        Object obj = conf.getVendorSpecificSettings().get("key.mapper");
        if (obj != null && obj instanceof GlassFishHAReplicaPredictor) {
            predictor = (GlassFishHAReplicaPredictor)obj;
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicatedManager.keymapper is " + predictor);
            }
        } else {
            predictor = new NoopHAReplicaPredictor();
        }
    }
}
