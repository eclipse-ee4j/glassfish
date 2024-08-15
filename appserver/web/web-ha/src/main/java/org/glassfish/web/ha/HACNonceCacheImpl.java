/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.web.ha;

import com.sun.enterprise.security.CNonceCacheFactory;
import com.sun.web.security.CNonceCacheImpl;

import jakarta.inject.Inject;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreConfiguration;
import org.glassfish.ha.store.api.BackingStoreException;
import org.glassfish.ha.store.api.BackingStoreFactory;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.security.common.CNonceCache;
import org.glassfish.security.common.NonceInfo;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author vbkumarjayanti
 */
@Service(name="HA-CNonceCache")
@PerLookup
public class HACNonceCacheImpl  implements CNonceCache {

    @Inject
    private ServiceLocator services;

    private CNonceCacheImpl localStore;
    private BackingStore<String, NonceInfo> backingStore = null;
    private String storeName = null;
    private Map<String, String> props;
    private static final String BS_TYPE_REPLICATED ="replicated";
    private static final Logger logger = LogFacade.getLogger();

    public HACNonceCacheImpl() {
    }
   /**
    * @param cnonceCacheSize the cnonceCacheSize to set
    */
    @Override
    public void setCnonceCacheSize(long cnonceCacheSize) {
       localStore.setCnonceCacheSize(cnonceCacheSize);
    }

    /**
     * @param nonceValidity the nonceValidity to set
     */
    @Override
    public void setNonceValidity(long nonceValidity) {
        localStore.setNonceValidity(nonceValidity);
    }

    /**
     * @return the cnonceCacheSize
     */
    @Override
    public long getCnonceCacheSize() {
       return  localStore.getCnonceCacheSize();
    }

    /**
     * @return the nonceValidity
     */
    @Override
    public long getNonceValidity() {
        return localStore.getNonceValidity();
    }

    @Override
    public int size() {
        return localStore.size();
    }

    @Override
    public boolean isEmpty() {
        return localStore.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
       return localStore.containsKey((String)o);
    }

    @Override
    public boolean containsValue(Object o) {
        return localStore.containsValue((NonceInfo)o);
    }

    @Override
    public NonceInfo get(Object o) {
        NonceInfo ret = localStore.get((String)o);
        if (ret == null && backingStore != null) {
            try {
                return backingStore.load((String)o, null);
            } catch (BackingStoreException ex) {
                // TODO exception message
                logger.log(Level.WARNING,null,ex);
            }
        }
        return ret;
    }

    @Override
    public NonceInfo put(String k, NonceInfo v) {
        NonceInfo  ret = localStore.put(k, v);
        if (backingStore == null) {
            return ret;
        }
        try {
            if (removeEldestEntry(null)) {
                backingStore.remove(localStore.getEldestCNonce());
            }
            backingStore.save(k, v, true);
        } catch (BackingStoreException ex) {
            //TODO: EX message
            logger.log(Level.WARNING, null, ex);
        }

        return ret;
    }

    @Override
    public NonceInfo remove(Object o) {
        NonceInfo ret = localStore.remove((String)o);
        if (backingStore == null) {
            return ret;
        }
        try {
            backingStore.remove((String) o);
        } catch (BackingStoreException ex) {
            //TODO: EX message
            logger.log(Level.WARNING, null, ex);
        }
        return ret;
    }

    // we do not need to support the below operation
    @Override
    public void putAll(Map<? extends String, ? extends NonceInfo> map) {
        //localStore.putAll(map);
        throw new UnsupportedOperationException("putAll : Not  Supported");
    }

    @Override
    public void clear() {
        if (backingStore != null) {
            for (String s : keySet()) {
                try {
                    backingStore.remove(s);
                } catch (BackingStoreException ex) {
                    //TODO: EX message
                    logger.log(Level.WARNING, null, ex);
                }
            }
        }
        localStore.clear();
    }

    @Override
    public Set<String> keySet() {
        return localStore.keySet();
    }

    @Override
    public Collection<NonceInfo> values() {
        return localStore.values();
    }

    @Override
    public Set<Entry<String, NonceInfo>> entrySet() {
        return localStore.entrySet();
    }

    protected boolean removeEldestEntry(
            Map.Entry<String, NonceInfo> eldest) {
        // This is called from a sync so keep it simple
        if (size() > getCnonceCacheSize()) {
            return true;
        }
        return false;
    }

    public void postConstruct() {
        localStore = new CNonceCacheImpl();
        try {
            final BackingStoreConfiguration<String, NonceInfo> bsConfig =
                    new BackingStoreConfiguration<String, NonceInfo>();
            bsConfig.setClusterName(props.get(CNonceCacheFactory.CLUSTER_NAME_PROP)).
                    setInstanceName(props.get(CNonceCacheFactory.INSTANCE_NAME_PROP)).
                    setStoreName(storeName).setKeyClazz(String.class)
                    .setValueClazz(NonceInfo.class);
            BackingStoreFactory bsFactory = services.getService(BackingStoreFactory.class, BS_TYPE_REPLICATED);
            backingStore = bsFactory.createBackingStore(bsConfig);
        } catch (BackingStoreException ex) {
            logger.log(Level.WARNING, null, ex);
        }
    }

    @Override
    public void init(long size, String name, long validity, Map<String, String> props) {
        this.storeName = name;
        this.props = props;
        postConstruct();
        localStore.setCnonceCacheSize(size);
        localStore.setNonceValidity(validity);

    }

    @Override
    public void destroy() {
        clear();
        try {
            if (this.backingStore != null) {
                this.backingStore.destroy();
            }
        } catch (Exception ex) {
        }
    }

}
