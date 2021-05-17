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

package com.sun.appserv.web.taglibs.cache;

/**
 * Class responsible for caching and expiring the execution result of a JSP
 * fragment.
 */
public class CacheEntry
{
    public static final int NO_TIMEOUT = -1;

    String content;
    volatile long expireTime;

    /**
     * Constructs a CacheEntry using the response string to be
     * cached and the timeout after which the entry will expire
     */
    public CacheEntry(String response, int timeout) {
        content = response;
        computeExpireTime(timeout);
    }

    /**
     * set the real expire time
     * @param expireTime in milli seconds
     */
    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    /**
     * Gets the cached content.
     *
     * @return The cached content
     */
    public String getContent() {
        return this.content;
    }

    /**
     * compute when this entry to be expired based on timeout relative to
     * current time.
     * @param timeout in seconds
     */
    public void computeExpireTime(int timeout) {
        // timeout is relative to current time
        this.expireTime = (timeout == NO_TIMEOUT) ? timeout :
                          System.currentTimeMillis() + (timeout * 1000L);
    }

    /**
     * is this response still valid?
     */
    public boolean isValid() {
        return (expireTime > System.currentTimeMillis() ||
                expireTime == NO_TIMEOUT);
    }

    /**
     * clear the contents
     */
    public void clear() {
        content = null;
        expireTime = 0L;
    }
}
