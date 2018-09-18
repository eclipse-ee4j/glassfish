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

/*
 * SCOCollection.java
 *
 * created April 3, 2000
 *
 * @author Marina Vatkina
 * @version 1.0
 */

package com.sun.jdo.spi.persistence.support.sqlstore;
import java.util.Collection;

public interface SCOCollection extends java.util.Collection, SCO
{
    /**
     * Resets removed and added lists after flush
     */
    void reset();

    /*
     * Mark the collection as deferred so any updates won't
     * be applied until markDeferred() gets called
     */
    void markDeferred();

    /*
     * Return true is this collection has been marked as deferred.
     * False otherwise.
     */
    boolean isDeferred();

    /*
     * Apply deferred updates if there is any. The given
     * collection c will be added to the underlying collection
     * before the deferred updates are applied.
     */
    void applyDeferredUpdates(Collection c);

    /**
     * Adds object to the Collection without recording
     * the event. Used internaly to initially populate the Collection
     */
    void addInternal(Object o);

    /**
     * Adds objects of the given Collection to this Collection without recording
     * the event. Used internaly to initially populate the Collection
     */
    void addAllInternal(Collection c);

    /**
     * Adds an object to the list without recording changes.
     */
    void addToBaseCollection(Object o);

    /**
     * Removes objects of the given Collection from this Collection without recording
     * the event. Used internaly to remove a collection of elements from this collection.
     */
    void removeAllInternal(Collection c);

    /**
     * Clears Collection without recording
     * the event. Used internaly to clear the Collection
     */
    void clearInternal();


    /**
     * Removes element from the Collection without recording
     * the event. Used internaly to update the Collection
     */
    void removeInternal(Object o);

    /**
     * Returns the Collection of added elements
     *
     * @return Collection of the added elements as java.util.Collection
     */
    Collection getAdded();

    /**
     * Returns the Collection of removed elements
     *
     * @return Collection of the removed elements as java.util.Collection
     */
    Collection getRemoved();

    /**
     * Sets a new owner for the SCO instance that is not owned
     * by any other object.
     *
     * @param owner the new owner
     * @param fieldName as java.lang.String
     * @param elementType the new element type as Class, or null if type
     * is not to be checke.
     * @throws com.sun.jdo.api.persistence.support.JDOUserException if the
     * instance is owned by another owner.
     */
    void setOwner(Object owner, String fieldName, Class elementType);

}
