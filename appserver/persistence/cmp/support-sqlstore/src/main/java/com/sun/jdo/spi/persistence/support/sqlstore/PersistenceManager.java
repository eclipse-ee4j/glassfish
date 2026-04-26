/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse License v. 2.0 are satisfied: GNU General License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.jdo.spi.persistence.support.sqlstore;

import com.sun.jdo.spi.persistence.support.sqlstore.impl.PersistenceManagerWrapper;

/**
 */
public interface PersistenceManager
        extends com.sun.jdo.api.persistence.support.PersistenceManager
{
        PersistenceManagerWrapper getCurrentWrapper();

        Object newInstance(StateManager sm);

        void setStateManager(Object pc, StateManager sm);

        void setFlags(Object pc, byte flags);

        byte getFlags(Object pc);

        StateManager getStateManager(Object pc);

        void setField(Object pc, int fieldNumber, Object value);

        Object getField(Object pc, int fieldNumber);

        void clearFields(Object pc);

        /**
         * Executes the given retrieve descriptor. The result
         * is a collection unless an aggregate query was specified.
         * In most cases the query result is a collection of
         * persistent objects. In case of a projection
         * on a local field the collection holds objects of that
         * type. For aggregate queries the result is a
         * single object, which type was defined by the caller.
         *
         * @param action The retrieve descriptor.
         * @param parameters The input parameters for the query.
         * @return A collection of (persistent) objects unless
         * an aggregate query was specified.
         */
        Object retrieve(RetrieveDesc action, ValueFetcher parameters);

        /**
         * Executes the given retrieve descriptor. The result
         * is a collection unless an aggregate query was specified.
         * In most cases the query result is a collection of
         * persistent objects. In case of a projection
         * on a local field the collection holds objects of that
         * type. For aggregate queries the result is a
         * single object, which type was defined by the caller.
         *
         * @param action The retrieve descriptor.
         * @return A collection of (persistent) objects unless
         * an aggregate query was specified.
         */
        Object retrieve(RetrieveDesc action);

        /**
         * Return a RetrieveDesc given a Class object.
         */
        RetrieveDesc getRetrieveDesc(Class<?> classType);

        /**
         * Return a RetrieveDesc for a foreign field (relationship) given the
         * Class object for the parent class.
         */
        RetrieveDesc getRetrieveDesc(String fieldName, Class<?> classType);

        /**
         * Called by Transaction commit() or rollback()
         * cleans up transactional cache
         * @param        status        jakarta.transaction.Status
         */
        void afterCompletion(int status);

        /**
         * Called by Transaction commit()
         * Loops through transactional cache and calls PersistentStore.updatePersistent()
         * on each instance
         */
        void beforeCompletion();

        /**
         * Called by Query in pessimistic transaction
         * to flush changes to the database
         */
        void internalFlush();

        /**
         * Called by StateManager to register new instance. This method will throw
         * an JDOUserException if throwDuplicateException is true and the object being
         * registered already exists in the pm cache.
         */
        void registerInstance(StateManager sm, Object oid,
             boolean throwDuplicateException, boolean forceRegister);

        /**
         * Called by StateManager to register persistent instance at the rollback if
         * it was removed from the global (weak) cache as the result of the replace
         * operation.
         */
        void registerInstance(StateManager sm, Object oid);

        /**
         * Deregister an instance.
         */
        void deregisterInstance(Object oid);

        /**
         * Deregister an instance with this object Id, only if it holds the same instance.
         */
        void deregisterInstance(Object oid, StateManager sm);

        /**
         * For Transaction to notify PersistenceManager that
         * status is changed
         */
        void notifyStatusChange(boolean isActive);

        /**
         * For Transaction to notify PersistenceManager that
         * optimistic flag is changed
         */
        void notifyOptimistic(boolean optimistic);

        /**
         * Returns true if associated transaction is optimistic
         */
        boolean isOptimisticTransaction();

        /**
         * For Transaction to notify PersistenceManager that
         * optimistic flag is changed
         */
        void notifyNontransactionalRead(boolean nontransactionalRead);

        /**
         * Returns true if nontransactionalRead flag is set to true.
         */
        boolean isNontransactionalRead();

        /**
         * Returns true if associated transaction is active
         */
        boolean isActiveTransaction();

        /**
         * Called by newSCOInstance from the interface or internally
         * by the runtime
         * Will not result in marking field as dirty
         *
         * Returns a new Second Class Object instance of the type specified,
         * @param type Class of the new SCO instance
         * @param owner the owner to notify upon changes
         * @param fieldName the field to notify upon changes
         * @return the object of the class type
         */
        Object newSCOInstanceInternal (Class<?> type, Object owner, String fieldName);

        /**
         * Called by newCollectionInstance from the interface
         * or internally by the runtime
         * Will not result in marking field as dirty
         *
         * @param type Class of the new SCO instance
         * @param owner the owner to notify upon changes
         * @param fieldName the field to notify upon changes
         * @param elementType the element types allowed
         * @param allowNulls true if allowed
         * @param initialSize initial size of the Collection
         * @return the object of the class type
         */
    Object newCollectionInstanceInternal (Class<?> type, Object owner, String fieldName,
                Class<?> elementType, boolean allowNulls, int initialSize);

        /**
         * Serialize field updates
         */
        void acquireFieldUpdateLock();

        /**
         * Allow other threads to update fields
         */
        void releaseFieldUpdateLock();

        /**
     * Acquires a share lock from the persistence manager. This method will
     * put the calling thread to sleep if another thread is holding the exclusive lock.
         */
        void acquireShareLock();

        /**
     * Releases the share lock and notify any thread waiting to get an exclusive lock.
         * Note that every releaseShareLock() call needs to be preceeded by an acquireShareLock() call.
         */
        void releaseShareLock();

        /**
     * Acquires an exclusive lock from the persistence manager. By acquiring an
         * exclusive lock, a thread is guaranteed to have exclusive right to the persistence
     * runtime meaning no other threads can perform any operation in the runtime.
         */
        void acquireExclusiveLock();

        /**
     * Release the exclusive lock and notify any thread waiting to get an exclusive or
         * share lock. Note that every releaseShareLock() call needs to be preceeded by
         * an acquireExclusiveLock() call.
         */
        void releaseExclusiveLock();

    /**
     * Force to close the persistence manager. Called by
     * TransactionImpl.afterCompletion in case of the CMT transaction
     * and the status value passed to the method cannot be resolved.
     */
    void forceClose();

    /**
     * Returns StateManager instance for this Object Id.
     * @param oid the ObjectId to look up.
     * @param pcClass the expected Class type of the new PC instance.
     */
    StateManager findOrCreateStateManager(Object oid, Class<?> pcClass);

    /**
     * Lock cache for getObjectById and result processing synchronization.
     */
    void acquireCacheLock();

    /** Release cache lock.
     */
    void releaseCacheLock();

    /**
     * Looks up the given instance in the Version Consistency cache and
     * if found, populates it from the cached values.
     * @param sm Instance to be looked up in the version consistency cache.
     * If found, it is populated with values from the cache.
     * @return true if the <code>sm</code> was found and populated, false
     * otherwise.
     */
    boolean initializeFromVersionConsistencyCache(StateManager sm);

}
