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
 * PersistenceCapable.java
 *
 * Created on February 25, 2000
 */
 
package com.sun.jdo.api.persistence.support;

/**
 *
 * @author Craig Russell
 * @version 0.1
 */

/**
 * A class that can be managed by a JDO implementation.
 *
 * <P>Every class whose instances can be managed by a JDO PersistenceManager must 
 * implement the PersistenceCapable interface.
 *
 * <P>This interface defines methods that allow the implementation to manage
 * the instances.  It also defines methods that allow a JDO aware
 * application to examine the runtime state of instances.  For example,
 * an application can discover whether the instance is persistent, transactional,
 * dirty, new, or deleted; and to get its associated
 * PersistenceManager if it has one.
 *
 * <P>In the Reference Implementation, the JDO Enhancer modifies the class
 * to implement PersistenceCapable prior to loading the class into the runtime
 * environment.  The Reference Enhancer also adds code to implement the
 * methods defined by PersistenceCapable.
 *
 * <P>The PersistenceCapable interface is designed to avoid name conflicts
 * in the scope of user-defined classes.  All of its declared method
 * names are prefixed with 'jdo'.
 */
public interface PersistenceCapable
{
    /** Return the associated PersistenceManager if there is one.
   * Transactional and persistent instances return the associated
   * PersistenceManager.  
   *
   * <P>Transient non-transactional instances return null.
     *<P>
   * @return the PersistenceManager associated with this instance.
   */
    PersistenceManager jdoGetPersistenceManager();
    
    /** Explicitly mark this instance and this field dirty.
     * Normally, PersistenceCapable classes are able to detect changes made
     * to their fields.  However, if a reference to an Array is given to a
     * method outside the class, and the Array is modified, then the
     * persistent instance is not aware of the change.  This API allows the
     * application to notify the instance that a change was made to a field.
     *
     * <P>Transient instances ignore this method.
     *<P>
     * @param fieldName the name of the field to be marked dirty.
     */
    void jdoMakeDirty(String fieldName);
    
    /** Return a copy of the JDO identity associated with this instance.
     *
     * <P>Persistent instances of PersistenceCapable classes have a JDO identity
     * managed by the PersistenceManager.  This method returns a copy of the
     * ObjectId that represents the JDO identity.  
     * 
     * <P>Transient instances return null.
     *
     * <P>The ObjectId may be serialized
     * and later restored, and used with a PersistenceManager from the same JDO
     * implementation to locate a persistent instance with the same data store
     * identity.
     *
     * <P>If the JDO identity is managed by the application, then the ObjectId may
     * be used with a PersistenceManager from any JDO implementation that supports
     * the PersistenceCapable class.
     *
     * <P>If the JDO identity is not managed by the application or the data store,
     * then the ObjectId returned is only valid within the current transaction.
     *<P>
     * @see PersistenceManager#getObjectId(Object pc)
     * @see PersistenceManager#getObjectById(Object oid)
     * @return a copy of the ObjectId of this instance.
     */
    Object jdoGetObjectId();
    
    /** Tests whether this object is dirty.
     *
     * Instances that have been modified, deleted, or newly 
     * made persistent in the current transaction return true.
     *
     *<P>Transient instances return false.
     *<P>
     * @see #jdoMakeDirty(String fieldName)
     * @return true if this instance has been modified in the current transaction.
     */
    boolean jdoIsDirty();

    /** Tests whether this object is transactional.
     *
     * Instances that respect transaction boundaries return true.  These instances
     * include transient instances made transactional as a result of being the
     * target of a makeTransactional method call; newly made persistent or deleted
     * persistent instances; persistent instances read in data store
     * transactions; and persistent instances modified in optimistic transactions.
     *
     *<P>Transient instances return false.
     *<P>
     * @return true if this instance is transactional.
     */
    boolean jdoIsTransactional();

    /** Tests whether this object is persistent.
     *
     * Instances whose state is stored in the data store return true.
     *
     *<P>Transient instances return false.
     *<P>
     * @see PersistenceManager#makePersistent(Object pc)
     * @return true if this instance is persistent.
     */
    boolean jdoIsPersistent();

    /** Tests whether this object has been newly made persistent.
     *
     * Instances that have been made persistent in the current transaction 
     * return true.
     *
     *<P>Transient instances return false.
     *<P>
     * @see PersistenceManager#makePersistent(Object pc)
     * @return true if this instance was made persistent
     * in the current transaction.
     */
    boolean jdoIsNew();

    /** Tests whether this object has been deleted.
     *
     * Instances that have been deleted in the current transaction return true.
     *
     *<P>Transient instances return false.
     *<P>
     * @see PersistenceManager#deletePersistent(Object pc)
     * @return true if this instance was deleted
     * in the current transaction.
     */
    boolean jdoIsDeleted();
    
    
}
