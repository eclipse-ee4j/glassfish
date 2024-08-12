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

package com.sun.jdo.spi.persistence.utility;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A weak HashSet. An element stored in the WeakHashSet might be garbage collected,
 * if there is no strong reference to this element.
 */
public class WeakHashSet
    extends HashSet
{
    /**
     * Helps to detect garbage collected values.
     */
    ReferenceQueue queue = new ReferenceQueue();

    /**
     * Returns an iterator over the elements in this set.  The elements
     * are returned in no particular order.
     *
     * @return an Iterator over the elements in this set.
     */
    public Iterator iterator ()
    {
        // remove garbage collected elements
        processQueue();

        // get an iterator of the superclass WeakHashSet
        final Iterator i = super.iterator();

        return new Iterator () {
            public boolean hasNext ()
            {
                return i.hasNext();
            }

            public Object next ()
            {
                // unwrap the element
                return getReferenceObject((WeakReference)i.next());
            }

            public void remove ()
            {
                // remove the element from the HashSet
                i.remove();
            }
        };
    }

    /**
     * Returns <code>true</code> if this set contains the specified element.
     *
     * @param o element whose presence in this set is to be tested.
     * @return <code>true</code> if this set contains the specified element.
     */
    public boolean contains (Object o)
    {
        return super.contains(WeakElement.create(o));
    }

    /**
     * Adds the specified element to this set if it is not already
     * present.
     *
     * @param o element to be added to this set.
     * @return <code>true</code> if the set did not already contain the specified
     * element.
     */
    public boolean add (Object o)
    {
        processQueue();
        return super.add(WeakElement.create(o, this.queue));
    }

    /**
     * Removes the given element from this set if it is present.
     *
     * @param o object to be removed from this set, if present.
     * @return <code>true</code> if the set contained the specified element.
     */
    public boolean remove (Object o)
    {
        boolean ret = super.remove(WeakElement.create(o));
        processQueue();
        return ret;
    }

    /**
     * A convenience method to return the object held by the
     * weak reference or <code>null</code> if it does not exist.
     */
    private final Object getReferenceObject (WeakReference ref)
    {
        return ((ref != null) ? ref.get() : null);
    }

    /**
     * Removes all garbage collected values with their keys from the map.
     * Since we don't know how much the ReferenceQueue.poll() operation
     * costs, we should call it only in the put() method.
     */
    private final void processQueue ()
    {
        WeakElement wv = null;

        while  ((wv = (WeakElement)this.queue.poll()) != null)
        {
            super.remove(wv);
        }
    }

    /**
     * A WeakHashSet stores objects of class WeakElement.
     * A WeakElement wraps the element that should be stored in the WeakHashSet.
     * WeakElement inherits from java.lang.ref.WeakReference.
     * It redefines equals and hashCode which delegate to the corresponding methods
     * of the wrapped element.
     */
    static private class WeakElement
        extends WeakReference
    {
        private int hash;    /* Hashcode of key, stored here since the key
                               may be tossed by the GC */

        private WeakElement (Object o)
        {
            super(o);
            hash = o.hashCode();
        }

        private WeakElement (Object o, ReferenceQueue q)
        {
            super(o, q);
            hash = o.hashCode();
        }

        private static WeakElement create (Object o)
        {
            return (o == null) ? null : new WeakElement(o);
        }

        private static WeakElement create (Object o, ReferenceQueue q)
        {
            return (o == null) ? null : new WeakElement(o, q);
        }

        /* A WeakElement is equal to another WeakElement iff they both refer to objects
               that are, in turn, equal according to their own equals methods */
        public boolean equals (Object o)
        {
            if (this == o)
                return true;
            if (!(o instanceof WeakElement))
                return false;
            Object t = this.get();
            Object u = ((WeakElement)o).get();
            if ((t == null) || (u == null))
                return false;
            if (t == u)
                return true;
            return t.equals(u);
        }

        public int hashCode ()
        {
            return hash;
        }

    }

}

