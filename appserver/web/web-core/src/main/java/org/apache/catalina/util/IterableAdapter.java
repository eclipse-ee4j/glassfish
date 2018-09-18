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

package org.apache.catalina.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Adapter class which wraps an <tt>Iterable</tt> over an
 * <tt>Enumeration</tt>, to support foreach-style iteration over the
 * <tt>Enumeration</tt>.
 */
public final class IterableAdapter<T> implements Iterable<T> {

    // The Enumeration over which to iterate
    private Enumeration<T> en;

    /**
     * Constructor
     *
     * @param en the Enumeration over which to iterate
     */
    public IterableAdapter(Enumeration<T> en) {
        this.en = en;
    }

    public Iterator<T> iterator() {

        return new Iterator<T>() {

            public boolean hasNext() {
                return en.hasMoreElements();
            }

            public T next() {
                return en.nextElement();
            }

            public void remove() {
                throw new UnsupportedOperationException(
                    "remove not supported");
            }
        };
    }
}
