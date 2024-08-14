/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
 * Transaction.java
 *
 * Created on February 25, 2000
 */

package com.sun.jdo.api.persistence.support;
import jakarta.transaction.Synchronization;

/** The JDO Transaction interface is a sub-interface of the PersistenceManager
 * that deals with options and completion of transactions under user control.
 *
 * <P>Transaction options include whether optimistic concurrency
 * control should be used for the current transaction, and whether values
 * should be retained in JDO instances after transaction completion.
 *
 * <P>Transaction completion methods have the same semantics as jakarta.transaction
 * UserTransaction, and are valid only in the non-managed, non-distributed
 * transaction environment.
 * @author Craig Russell
 * @version 0.1
 */

public interface Transaction
{
    /** Begin a transaction.  The type of transaction is determined by the
   * setting of the Optimistic flag.
   * @see #setOptimistic
   * @see #getOptimistic
   * @throws JDOUserException if a distributed transaction XAResource
   * is assigned to this Transaction
   */
    void begin();

    /** Commit the current transaction.
     */
    void commit();

    /** Roll back the current transaction.
     */
    void rollback();

    /** Returns whether there is a transaction currently active.
     * @return boolean
     */
    boolean isActive();

    /** If true, at commit instances retain their values and the instances
     * transition to persistent-nontransactional.
     * <P>Setting this flag also sets the NontransactionalRead flag.
     * @param retainValues the value of the retainValues property
     */
    void setRetainValues(boolean retainValues);

    /** If true, at commit time instances retain their field values.
     * @return the value of the retainValues property
     */
    boolean getRetainValues();

    /** If true, at rollback instances restore their values and the instances
     * transition to persistent-nontransactional.
     * @param restoreValues the value of the restoreValues property
     */
    void setRestoreValues(boolean restoreValues);

    /** If true, at rollback time instances restore their field values.
     * @return the value of the restoreValues property
     */
    boolean getRestoreValues();

    /** Optimistic transactions do not hold data store locks until commit time.
     * @param optimistic the value of the Optimistic flag.
     */
    void setOptimistic(boolean optimistic);

    /** Optimistic transactions do not hold data store locks until commit time.
     * @return the value of the Optimistic property.
     */
    boolean getOptimistic();

    /** If this flag is set to true, then queries and navigation are allowed
     * without an active transaction
     * @param flag     the value of the nontransactionalRead property.
     */
    void setNontransactionalRead (boolean flag);

    /** If this flag is set to true, then queries and navigation are allowed
     * without an active transaction
     * @return the value of the nontransactionalRead property.
     */
    boolean getNontransactionalRead ();

    /** The user can specify a Synchronization instance to be notified on
     * transaction completions.  The beforeCompletion method is called prior
     * to flushing instances to the data store.
     *
     * <P>The afterCompletion method is called after performing the data store
     * commit operation.
     * @param sync the Synchronization instance to be notified; null for none
     */
    void setSynchronization(Synchronization sync);

    /** The user-specified Synchronization instance for this Transaction instance.
     * @return the user-specified Synchronization instance.
     */
    Synchronization getSynchronization();

   /**
    * Sets the number of seconds to wait for a query statement
    * to execute in the datastore associated with this  Transaction instance
    * @param timeout          new timout value in seconds; zero means unlimited
    */
   void setQueryTimeout (int timeout);

   /**
    * Gets the number of seconds to wait for a query statement
    * to execute in the datastore associated with this  Transaction instance
    * @return      timout value in seconds; zero means unlimited
    */
   int getQueryTimeout ();

   /**
    * Sets the number of seconds to wait for an update statement
    * to execute in the datastore associated with this  Transaction instance
    * @param timeout          new timout value in seconds; zero means unlimited
    */
   void setUpdateTimeout (int timeout);

   /**
    * Gets the number of seconds to wait for an update statement
    * to execute in the datastore associated with this  Transaction instance
    * @return      timout value in seconds; zero means unlimited
    */
   int getUpdateTimeout();

    /** The Tranansaction instance is always associated with exactly one
     * PersistenceManager.
     *
     * @return the PersistenceManager for this Transaction instance
     */
    PersistenceManager getPersistenceManager();
}
