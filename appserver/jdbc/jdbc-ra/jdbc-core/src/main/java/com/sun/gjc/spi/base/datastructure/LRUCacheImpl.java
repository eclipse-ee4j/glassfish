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

package com.sun.gjc.spi.base.datastructure;

import com.sun.gjc.monitoring.StatementCacheProbeProvider;
import com.sun.gjc.spi.base.CacheObjectKey;
import com.sun.gjc.spi.base.PreparedStatementWrapper;
import com.sun.logging.LogDomains;
import org.glassfish.resourcebase.resources.api.PoolInfo;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Shalini M
 */
public class LRUCacheImpl implements Cache {

    /**
     * Stores the objects for statement caching
     */
    private Map<CacheObjectKey, CacheEntry> list;
    /**
     * Size of the cache
     */
    private int maxSize ;
    protected final static Logger _logger;
    private StatementCacheProbeProvider probeProvider = null;
    private PoolInfo poolInfo;

    static {
        _logger = LogDomains.getLogger(LRUCacheImpl.class, LogDomains.RSR_LOGGER);
    }

    public LRUCacheImpl(PoolInfo poolInfo, int maxSize){
        this.maxSize = maxSize;
        this.poolInfo = poolInfo;
        list = new LinkedHashMap<CacheObjectKey, CacheEntry>();
        try {
            probeProvider = new StatementCacheProbeProvider();
        } catch(Exception ex) {
            //TODO logger
        }
    }

    /**
     * Check if an entry is found for this key object. If found, the entry is
     * put in the result object and back into the list.
     *
     * @param key key whose mapping entry is to be checked.
     * @return result object that contains the key with the entry if not
     * null when
     * (1) object not found in cache
     */
    public Object checkAndUpdateCache(CacheObjectKey key) {
        Object result = null;
        CacheEntry entry = list.get(key);
        if(entry != null) {
            //Cache hit
            result = entry.entryObj;
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("Cache Hit");
            }
            //TODO-SC Busy cache hits?
            probeProvider.statementCacheHitEvent(poolInfo.getName(), poolInfo.getApplicationName(), poolInfo.getModuleName());
        } else {
            //Cache miss
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("Cache Miss");
            }
            probeProvider.statementCacheMissEvent(poolInfo.getName(), poolInfo.getApplicationName(), poolInfo.getModuleName());
        }
        return result;
    }

    /**
     * Add the key and entry value into the cache.
     * @param key key that contains the sql string and its type (PS/CS)
     * @param o entry that is the wrapper of PreparedStatement or
     * CallableStatement
     * @param force If the already existing key is to be overwritten
     */
    public void addToCache(CacheObjectKey key, Object o, boolean force) {
        if(force || !list.containsKey(key)){
            //overwrite or if not already found in cache

            if(list.size() >= maxSize){
                purge();
            }
            CacheEntry entry = new CacheEntry(o);
            list.put(key, entry);
        }
    }

    /**
     * Clears the statement cache
     */
    public void clearCache(){
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("clearing objects in cache");
        }
       list.clear();
    }

    public void flushCache() {
        while(list.size()!=0){
            purge();
        }
    }

    public void purge() {
        Set<Map.Entry<CacheObjectKey, CacheEntry>> entrySet = list.entrySet();
        Iterator entrySetIterator = entrySet.iterator();
//        Iterator keyIterator = list.keySet().iterator();
        while(entrySetIterator.hasNext()){
//            CacheObjectKey key = (CacheObjectKey) entrySetIterator.next();
            Map.Entry<CacheObjectKey, CacheEntry> entryTuple =
                    (Map.Entry<CacheObjectKey, CacheEntry>) entrySetIterator.next();
            CacheEntry entry = entryTuple.getValue();
            try{
                //TODO Move to a more generic Contract and invoke close()
                //PreparedStatementWrapper could implement the contract instead
                PreparedStatementWrapper ps = (PreparedStatementWrapper)entry.entryObj;
                ps.setCached(false);
                ps.close();
            }catch(SQLException e){
                //ignore
            }
            entrySetIterator.remove();
            break;
        }
    }

    // Used only for purging the bad statements.
    public void purge(Object obj) {
        PreparedStatementWrapper tmpPS = (PreparedStatementWrapper) obj;
        Set<Map.Entry<CacheObjectKey, CacheEntry>> entrySet = list.entrySet();
        Iterator entrySetIterator = entrySet.iterator();
//        Iterator keyIterator = list.keySet().iterator();
        while(entrySetIterator.hasNext()){
//            CacheObjectKey key = (CacheObjectKey)keyIterator.next();
//            CacheEntry entry = list.get(key);
            Map.Entry<CacheObjectKey, CacheEntry> entryTuple =
                    (Map.Entry<CacheObjectKey, CacheEntry>) entrySetIterator.next();
            CacheEntry entry = entryTuple.getValue();
            try{
                //TODO Move to a more generic Contract and invoke close()
                //PreparedStatementWrapper could implement the contract instead
                PreparedStatementWrapper ps = (PreparedStatementWrapper)entry.entryObj;
                if(ps.equals(tmpPS)) {
                    //Found the entry in the cache. Remove this entry.
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.log(Level.FINEST, "Purging an entry from cache");
                    }
                    ps.setCached(false);
                    ps.close();
                }
            }catch(SQLException e){
                //ignore
            }
            entrySetIterator.remove();
            break;
        }
    }

    /**
     * Returns the number of entries in the statement cache
     * @return has integer value
     */
    public int getSize() {
       return list.size();
    }

    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Cache object that has an entry. This is used to put inside the
     * statement cache.
     */
    public static class CacheEntry{
        private Object entryObj;

        public CacheEntry(Object o){
            this.entryObj = o;
        }
    }

    /*public Set getObjects(){
        //TODO-SC-DEFER can the set be "type-safe"
        Set set = new HashSet();
        for(CacheEntry entry : list.values()){
            set.add(entry.entryObj);
        }
        return set;
    }*/

    public boolean isSynchronized() {
        return false;
    }
}
