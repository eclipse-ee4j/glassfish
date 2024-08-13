/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.common;

import java.util.Map;

import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author vbkumarjayanti
 */
@Contract
public interface CNonceCache extends Map<String, NonceInfo> {

    /**
     * @param cnonceCacheSize the cnonceCacheSize to set
     */
    public void setCnonceCacheSize(long cnonceCacheSize);
    /**
     * @param nonceValidity the nonceValidity to set
     */
    public void setNonceValidity(long nonceValidity);

    /**
     * @return the cnonceCacheSize
     */
    public long getCnonceCacheSize();

    /**
     * @return the nonceValidity
     */
    public long getNonceValidity();

    /**
     * Initialize the cache
     */
    public void init(long size, String name, long validity, Map<String, String> props);
    /**
     * destroy
     */
    public void destroy();
}
