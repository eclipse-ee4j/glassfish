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
 * TransactionHelperImpl.java
 *
 * Created on January 17, 2002
 */

package com.sun.jdo.spi.persistence.support.sqlstore.ejb;

import com.sun.jdo.api.persistence.model.ClassLoaderStrategy;
import com.sun.jdo.api.persistence.support.PersistenceManagerFactory;

import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

/** This is an abstract class which is a generic implementation of the
* TransactionHelper interface. Each concrete implementation that extends
* this class is used for information about the distributed
* transaction environment.
*
* The class that extends this class must implement <code>getTransaction</code>
* and <code>getUserTransaction</code> methods and replace any other method
* implementation if it is necessary.
*
* Such class must register itself by a static method at class initialization time.
* For example,
* <blockquote><pre>
* import com.sun.jdo.spi.persistence.support.sqlstore.ejb.*;
* class blackHerringTransactionHelper extends TransactionHelperImpl {
*    static EJBHelper.register(new blackHerringTransactionHelper());
*    ...
* }
* </pre></blockquote>
*/
abstract public class TransactionHelperImpl
    implements TransactionHelper {

    static final String DEFAULT_STRING = "default"; // NOI18N

    /** Set ClassLoaderStrategy to be "reload" in the managed
     * environment.
     */
    static {
        if (System.getProperty(
                ClassLoaderStrategy.PROPERTY_MULTIPLE_CLASS_LOADERS) == null)
            ClassLoaderStrategy.setStrategy(
                    ClassLoaderStrategy.MULTIPLE_CLASS_LOADERS_RELOAD);
    }

    /** Identifies the managed environment behavior.
     * @return true as this implementation represents the managed environment.
     */
    public boolean isManaged() {
        return true;
    }


    /** Identify the Transaction context for the calling thread, and return a
     * Transaction instance that can be used to register synchronizations,
     * and used as the key for HashMaps. The returned Transaction must implement
     * <code>equals()</code> and <code>hashCode()</code> based on the global transaction id.
     * <P>All Transaction instances returned by this method called in the same
     * Transaction context must compare equal and return the same hashCode.
     * The Transaction instance returned will be held as the key to an
     * internal HashMap until the Transaction completes. If there is no transaction
     * associated with the current thread, this method returns null.
     * @return the Transaction instance for the calling thread
     */
    abstract public Transaction getTransaction();

    /** Returns the UserTransaction associated with the calling thread.  If there
     * is no transaction currently in progress, this method returns null.
     * @return the UserTransaction instance for the calling thread
     */
    abstract public UserTransaction getUserTransaction();

    /** Translate local representation of the Transaction Status to
     * jakarta.transaction.Status value if necessary. Otherwise this method
     * should return the value passed to it as an argument.
     * <P>This method is used during afterCompletion callbacks to translate
     * the parameter value passed by the application server to the
     * afterCompletion method.  The return value must be one of:
     * <code>jakarta.transaction.Status.STATUS_COMMITTED</code> or
     * <code>jakarta.transaction.Status.STATUS_ROLLED_BACK</code>.
     * @param   st      local Status value
     * @return the jakarta.transaction.Status value of the status
     */
    public int translateStatus(int st) {
        return st;
    }

    /** Replace newly created instance of PersistenceManagerFactory
     * with the hashed one if it exists. The replacement is necessary only if
     * the JNDI lookup always returns a new instance. Otherwise this method
     * returns the object passed to it as an argument.
     *
     * PersistenceManagerFactory is uniquely identified by
     * ConnectionFactory.hashCode() if ConnectionFactory is
     * not null; otherwise by ConnectionFactoryName.hashCode() if
     * ConnectionFactoryName is not null; otherwise
     * by the combination of URL.hashCode() + userName.hashCode() +
     * password.hashCode() + driverName.hashCode();
     *
     * @param   pmf     PersistenceManagerFactory instance to be replaced
     * @return  the PersistenceManagerFactory known to the runtime
     */
    public PersistenceManagerFactory replaceInternalPersistenceManagerFactory(
    PersistenceManagerFactory pmf) {

    return pmf;
    }

    /** Called at the beginning of the Transaction.beforeCompletion() to
     * register the component with the app server only if necessary.
     * The component argument is an array of Objects.
     * The first element is com.sun.jdo.spi.persistence.support.sqlstore.Transaction
     * object responsible for transaction completion.
     * The second element is com.sun.jdo.api.persistence.support.PersistenceManager
     * object that has been associated with the Transaction context for the
     * calling thread.
     * The third element is jakarta.transaction.Transaction object that has been
     * associated with the given instance of PersistenceManager.
     * The return value is passed unchanged to the postInvoke method.
     *
     * @param   component       an array of Objects
     * @return  implementation-specific Object
     */
    public Object preInvoke(Object component) {
        return null;
    }

    /** Called at the end of the Transaction.beforeCompletion() to
     * de-register the component with the app server if necessary.
     * The parameter is the return value from preInvoke, and can be any
     * Object.
     *
     * @param   im      implementation-specific Object
     */
    public void postInvoke(Object im) {
    }

    /** Called in a managed environment to register internal Synchronization object
    * with the Transaction Synchronization. If available, this registration
    * provides special handling of the registered instance, calling it after
    * all user defined Synchronization instances.
    *
    * @param jta the Transaction instance for the calling thread.
    * @param sync the internal Synchronization instance to register.
    * @throws jakarta.transaction.RollbackException.
    * @throws jakarta.transaction.SystemException
    */
    public void registerSynchronization(Transaction jta, Synchronization sync)
        throws RollbackException, SystemException {

        jta.registerSynchronization(sync);
    }

    /** Called in a managed environment to get a Connection from the application
     * server specific resource. In a non-managed environment returns null as
     * it should not be called.
     * This is a generic implementation for the case of javax.sql.DataSource as
     * the resource type.
     *
     * @param resource the application server specific resource.
     * @param username the resource username. If null, Connection is requested
     * without username and password validation.
     * @param password the password for the resource username.
     * @return a Connection.
     * @throws java.sql.SQLException.
     */
    public java.sql.Connection getConnection(Object resource, String username,
                char[] password) throws java.sql.SQLException {
        java.sql.Connection rc = null;
        if (resource instanceof javax.sql.DataSource) {
            javax.sql.DataSource ds = (javax.sql.DataSource)resource;
            if (username == null) {
                rc = ds.getConnection();
            } else {
                rc = ds.getConnection(username, new String(password));
            }
        }
        return rc;
    }

    /** Called in a managed environment to get a non-transactional Connection
     * from the application server specific resource.
     *
     * @param resource the application server specific resource.
     * @param username the resource username. If null, Connection is requested
     * without username and password validation.
     * @param password the password for the resource username.
     * @return a Connection.
     * @throws java.sql.SQLException.
     */
    abstract public java.sql.Connection getNonTransactionalConnection(
        Object resource, String username, char[] password)
        throws java.sql.SQLException;

    /** Called in a managed environment to access a TransactionManager
     * for managing local transaction boundaries and registering synchronization
     * for call backs during completion of a local transaction.
     *
     * @return jakarta.transaction.TransactionManager
     */
    abstract public TransactionManager getLocalTransactionManager();

    /**
     * This method unwraps given Statement and return the Statement from
     * JDBC driver if possible.
     */
    public java.sql.Statement unwrapStatement(java.sql.Statement stmt) {
        return stmt;
    }

    /**
     * Set environment specific default values for the given PersistenceManagerFactory.
     * In most app servers optimistic and retainValues flags should be false.
     * For any other settings this method should be overritten.
     *
     * @param pmf the PersistenceManagerFactory.
     */
    public void setPersistenceManagerFactoryDefaults(PersistenceManagerFactory pmf) {
        pmf.setOptimistic(false);
        pmf.setRetainValues(false);
    }

    /**
     * Returns name prefix for DDL files extracted from the info instance by the
     * application server specific code.
     *
     * @param info the instance to use for the name generation.
     * @return name prefix as String.
     */
    public String getDDLNamePrefix(Object info) {
        return DEFAULT_STRING;
    }

    /**
     * @inheritDoc
     */
    public void registerApplicationLifeCycleEventListener(
            ApplicationLifeCycleEventListener listener) {

    }

    /**
     * @inheritDoc
     */
    public void notifyApplicationUnloaded(ClassLoader cl) {

    }

}
