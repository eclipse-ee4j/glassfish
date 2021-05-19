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

package com.sun.enterprise.deployment;

import java.util.Collection;
import java.util.Set;
import java.util.Vector;

/**
 * I am an ordered collection that does not allow duplicates.
 *
 * @author Danny Coward
 */

public class OrderedSet<T> extends Vector<T> implements Set<T> { // FIXME by srini - can we instead change the usage to be TreeSet based?

    /**
     * Construct an empty collection.
     */
    public OrderedSet() {
    }

    /**
     * Construct an ordered set from the given collection.
     */
    public OrderedSet(Collection<T> c) {
        this();
        this.addAll(c);
    }

    /**
     * Add the given object to the Set if it is not equal (equals()) to
     * an element already in the set.
     */
    public boolean add(T o) {
        if (o != null && !this.contains(o)) {
            return super.add(o);
        }
        return false;
    }

    /**
     * Add all the elements in the given set that are not already
     * in this ordered set.
     */
    public boolean addAll(Collection<? extends T> c) {
        boolean setChanged = false;
        if (c != null) {
            for (T o : c) {
                if (add(o)) {
                    setChanged = true;
                }
            }
        }
        return setChanged;
    }
}
