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
 * ExtentCollection.java
 *
 * Created on April 6, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore;

import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.api.persistence.support.JDOUnsupportedOptionException;
import com.sun.jdo.api.persistence.support.JDOUserException;

import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;

/**
 *
 * @author  Michael Bouschen
 * @version 0.1
 */
public class ExtentCollection
        implements Collection {
    /**
     * The PersistenceManager getExtent is called from
     */
    protected PersistenceManager pm;

    /**
     * This extent collection reperesents the extent of persistenceCapableClass.
     */
    protected Class persistenceCapableClass;

    /**
     * I18N message handler
     */
    private final static ResourceBundle messages = I18NHelper.loadBundle(
           ExtentCollection.class);

    /**
     *
     * @param persistenceCapableClass Class of instances
     * @param subclasses whether to include instances of subclasses
     */
    public ExtentCollection(PersistenceManager pm, Class persistenceCapableClass, boolean subclasses) {
        this.pm = pm;
        this.persistenceCapableClass = persistenceCapableClass;

        // check persistenceCapableClass parameter being null
        if (persistenceCapableClass == null)
            throw new JDOUserException(
                    I18NHelper.getMessage(messages, "jdo.extentcollection.constructor.invalidclass", "null"));// NOI18N
        // check persistence-capable
        if (Model.RUNTIME.getMappingClass(persistenceCapableClass.getName(),
                persistenceCapableClass.getClassLoader()) == null)
            throw new JDOUserException(
                    I18NHelper.getMessage(messages, "jdo.extentcollection.constructor.nonpc", // NOI18N
                            persistenceCapableClass.getName()));

        // subclasses == true is not yet supported
        if (subclasses)
            throw new JDOUnsupportedOptionException(
                    I18NHelper.getMessage(messages, "jdo.extentcollection.constructor.subclasses"));// NOI18N
    }

    /**
     *
     */
    public Class getPersistenceCapableClass() {
        return persistenceCapableClass;
    }

    /**
     *
     */
    public int size() {
        throw new JDOUnsupportedOptionException(
                I18NHelper.getMessage(messages, "jdo.extentcollection.methodnotsupported", "size"));// NOI18N
    }

    /**
     *
     */
    public boolean isEmpty() {
        throw new JDOUnsupportedOptionException(
                I18NHelper.getMessage(messages, "jdo.extentcollection.methodnotsupported", "isEmpty"));// NOI18N
    }

    /**
     *
     */
    public boolean contains(Object o) {
        throw new JDOUnsupportedOptionException(
                I18NHelper.getMessage(messages, "jdo.extentcollection.methodnotsupported", "contains"));// NOI18N
    }

    /**
     *
     */
    public Iterator iterator() {
        RetrieveDesc rd = pm.getRetrieveDesc(persistenceCapableClass);
        return ((Collection)pm.retrieve(rd)).iterator();
    }

    /**
     *
     */
    public Object[] toArray() {
        throw new JDOUnsupportedOptionException(
                I18NHelper.getMessage(messages, "jdo.extentcollection.methodnotsupported", "toArray"));// NOI18N
    }

    /**
     *
     */
    public Object[] toArray(Object a[]) {
        throw new JDOUnsupportedOptionException(
                I18NHelper.getMessage(messages, "jdo.extentcollection.methodnotsupported", "toArray"));// NOI18N
    }

    /**
     * Extent collection is unmodifiable => throw UnsupportedOperationException
     */
    public boolean add(Object o) {
        throw new UnsupportedOperationException(
                I18NHelper.getMessage(messages, "jdo.extentcollection.illegalmodification", // NOI18N
                        persistenceCapableClass.getName()));
    }

    /**
     * Extent collection is unmodifiable => throw UnsupportedOperationException
     */
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(
                I18NHelper.getMessage(messages, "jdo.extentcollection.illegalmodification", // NOI18N
                        persistenceCapableClass.getName()));
    }

    /**
     *
     */
    public boolean containsAll(Collection c) {
        throw new JDOUnsupportedOptionException(
                I18NHelper.getMessage(messages, "jdo.extentcollection.methodnotsupported", "containsAll"));// NOI18N
    }

    /**
     * Extent collection is unmodifiable => throw UnsupportedOperationException
     */
    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException(
                I18NHelper.getMessage(messages, "jdo.extentcollection.illegalmodification", // NOI18N
                        persistenceCapableClass.getName()));
    }

    /**
     * Extent collection is unmodifiable => throw UnsupportedOperationException
     */
    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException(
                I18NHelper.getMessage(messages, "jdo.extentcollection.illegalmodification", // NOI18N
                        persistenceCapableClass.getName()));
    }

    /**
     * Extent collection is unmodifiable => throw UnsupportedOperationException
     */
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException(
                I18NHelper.getMessage(messages, "jdo.extentcollection.illegalmodification", // NOI18N
                        persistenceCapableClass.getName()));
    }

    /**
     * Extent collection is unmodifiable => throw UnsupportedOperationException
     */
    public void clear() {
        throw new UnsupportedOperationException(
                I18NHelper.getMessage(messages, "jdo.extentcollection.illegalmodification", // NOI18N
                        persistenceCapableClass.getName()));
    }

    /**
     * Two extent collections are equal, iff the names of their persistence capable class are equal
     */
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o instanceof ExtentCollection) {
            String otherClassName = ((ExtentCollection) o).persistenceCapableClass.getName();
            return persistenceCapableClass.getName().equals(otherClassName);
        }
        return false;
    }

    /**
     * The hashCode is mapped to the hashCode of the name of the extent collection's persistence capable class
     */
    public int hashCode() {
        return persistenceCapableClass.getName().hashCode();
    }
}
