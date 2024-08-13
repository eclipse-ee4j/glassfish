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

package com.sun.appserv.web.cache.filter;

import jakarta.servlet.http.Cookie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/** HttpCacheEntry
 *  Each entry holds cached (HTTP) response:
 *  a) response bytes b) response headers c) expiryTime
 *  d) parameterEncoding used e) entryKey this entry represents,
 *  to match the entry within the hash bucket.
 *
 *  XXX: should implement methods to enable serialization of cached response?
 */
public class HttpCacheEntry {

    public static final int VALUE_NOT_SET = -1;

    int statusCode;

    HashMap<String, ArrayList<String>> responseHeaders = new HashMap<String, ArrayList<String>>();
    HashMap<String, ArrayList<Long>> dateHeaders = new HashMap<String, ArrayList<Long>>();
    ArrayList<Cookie> cookies = new ArrayList<Cookie>();
    String contentType;
    Locale locale;

    int contentLength;

    // XXX: other cacheable response info
    byte[] bytes;

    volatile long expireTime = 0;

    /**
     * set the real expire time
     * @param expireTime in milli seconds
     */
    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    /**
     * compute when this entry to be expired based on timeout relative to
     * current time.
     * @param timeout in seconds
     */
    public void computeExpireTime(int timeout) {

        // timeout is relative to current time
        this.expireTime = (timeout == -1) ? timeout :
                          System.currentTimeMillis() + (timeout * 1000L);
    }

    /**
     * is this response still valid?
     */
    public boolean isValid() {
        return (expireTime > System.currentTimeMillis() || expireTime == -1);
    }

    /**
     * clear the contents
     */
    public void clear() {
        bytes = null;
        responseHeaders = null;
        cookies = null;
    }

    /**
     * get the size
     * @return size of this entry in bytes
     * Note: this is only approximate
     */
    public int getSize() {
        int size = 0;
        if (bytes != null) {
            size = bytes.length;
        }

        // size of response bytes plus headers (each approx 20 chars or 40 bytes)
        return (size + (40 * responseHeaders.size()) );
    }
}
