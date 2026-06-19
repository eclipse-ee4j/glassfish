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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * <p>
 * This class enables a <code>URLConnection</code> to be returned when <code>openConnection(URL)</code> is called which
 * is capable of reading from a <code>T</code> object.
 * </p>
 */
public class CachedURLStreamHandler<T> extends URLStreamHandler {

    /**
     * <p>
     * This constructor stores the given <code>T</code> object, which will be used by the {@link CachedURLConnection} which
     * is created when openConnection is called. It stores this as a <em>WeakReference</em>, so it will be garbage collected
     * if no other "strong" references refer to it!
     * </p>
     */
    public CachedURLStreamHandler(T obj) {
        this.weakRef = new WeakReference<>(obj);
    }

    /**
     * <p>
     * This method creates a new {@link CachedURLConnection} associated with the <code>T</code> object given when the
     * constructor was called.
     * </p>
     */
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new CachedURLConnection<>(url, weakRef);
    }

    private WeakReference<T> weakRef = null;
}
