/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.web.security;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.security.common.CNonceCache;
import org.glassfish.security.common.NonceInfo;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author vbkumarjayanti
 */
@Service(name = "CNonceCache")
@PerLookup
public final class CNonceCacheImpl extends LinkedHashMap<String, NonceInfo> implements CNonceCache {

    /**
     *
     */
    private static final long serialVersionUID = 4657887758539980470L;

    private static final long LOG_SUPPRESS_TIME = 5 * 60 * 1000;

    private long lastLog = 0;

    private static final Logger log = Logger.getLogger(CNonceCacheImpl.class.getName());

    private String eldestCNonce;
    private String storeName;

    /**
     * Maximum number of client nonces to keep in the cache. If not specified, the default value of 1000 is used.
     */
    long cnonceCacheSize = 1000;

    /**
     * How long server nonces are valid for in milliseconds. Defaults to 5 minutes.
     */
    long nonceValidity = 5 * 60 * 1000;

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, NonceInfo> eldest) {
        // This is called from a sync so keep it simple
        long currentTime = System.currentTimeMillis();
        eldestCNonce = eldest.getKey();
        if (size() > getCnonceCacheSize()) {
            if (lastLog < currentTime && currentTime - eldest.getValue().getTimestamp() < getNonceValidity()) {
                // Replay attack is possible
                lastLog = currentTime + LOG_SUPPRESS_TIME;
                log.log(Level.WARNING, "The lastLog was set to {0}", lastLog);
            }
            return true;
        }
        return false;
    }

    /**
     * @return the cnonceCacheSize
     */
    @Override
    public long getCnonceCacheSize() {
        return cnonceCacheSize;
    }

    /**
     * @return the nonceValidity
     */
    @Override
    public long getNonceValidity() {
        return nonceValidity;
    }

    /**
     * @return the eldestCNonce
     */
    public String getEldestCNonce() {
        return eldestCNonce;
    }

    @Override
    public void init(long size, String name, long validity, Map<String, String> props) {
        this.storeName = name;
        this.cnonceCacheSize = size;
        this.nonceValidity = validity;
    }

    @Override
    public void setCnonceCacheSize(long cnonceCacheSize) {
        this.cnonceCacheSize = cnonceCacheSize;
    }

    @Override
    public void setNonceValidity(long nonceValidity) {
        this.nonceValidity = nonceValidity;
    }

    @Override
    public void destroy() {
        clear();
    }

}
