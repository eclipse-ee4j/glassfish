/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Ordered collection (by the order of additions) that does not allow duplicates.
 *
 * @param <T> type of all items.
 *
 * @author Danny Coward
 */
public class OrderedSet<T> extends ArrayList<T> implements Set<T> {

    private static final long serialVersionUID = 1L;

    /**
     * Construct an empty collection.
     */
    public OrderedSet() {
    }

    /**
     * Construct an ordered set from the given collection.
     */
    public OrderedSet(Collection<T> collection) {
        this.addAll(collection);
    }

    /**
     * Add the given object to the Set if it is not equal (equals()) to
     * an element already in the set.
     */
    @Override
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
    @Override
    public boolean addAll(Collection<? extends T> collection) {
        if (collection == null) {
            return false;
        }
        boolean changed = false;
        for (T o : collection) {
            if (add(o)) {
                changed = true;
            }
        }
        return changed;
    }
}
