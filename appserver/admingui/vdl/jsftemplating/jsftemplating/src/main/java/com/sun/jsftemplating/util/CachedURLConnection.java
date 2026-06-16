/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

/**
 * <p>
 * This class enables a URL connection to a cached object of type <code>&lt;T&gt;</code>. It is required that a
 * <code>WeakReference</code> to this object by used, allowing GC to occur if no other Strong references are present.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class CachedURLConnection<T> extends URLConnection {

    /**
     * <p>
     * This constructor requires the <code>WeakReference&lt;T&gt;</code> containing the object to be supplied, in addition
     * to the URL.
     * </p>
     */
    public CachedURLConnection(URL url, WeakReference<T> weakRef) {
        super(url);
        if (weakRef == null || weakRef.get() == null) {
            throw new IllegalArgumentException("The weakRef is required in " + "order to create a CachedURLConnection!");
        }
        this.weakRef = weakRef;
    }

    /**
     * <p>
     * This method is overriden to provide access to an InputStream based on the <code>T</code> object.
     * </p>
     */
    @Override
    public InputStream getInputStream() throws IOException {
        try {
            // If the value is (null) a NPE will be thrown here... only
            // should occur if the object has been GC'd.
            byte bytes[] = null;
            T obj = weakRef.get();
            if (obj instanceof byte[]) {
                bytes = (byte[]) obj;
            } else {
                bytes = obj.toString().getBytes();
            }
            return new ByteArrayInputStream(bytes);
        } catch (NullPointerException ex) {
            throw new IOException("Cached object was null!");
        }
    }

    /**
     * <p>
     * This method is required to be overriden, however, no "connection" is needed, so this method does nothing.
     * </p>
     */
    @Override
    public void connect() throws IOException {
        // Do nothing.
    }

    private WeakReference<T> weakRef = null;
}
