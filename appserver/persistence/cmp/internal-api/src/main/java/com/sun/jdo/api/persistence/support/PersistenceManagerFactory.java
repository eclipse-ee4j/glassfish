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
 * PersistenceManagerFactory.java
 *
 * Created on February 25, 2000
 */

package com.sun.jdo.api.persistence.support;

import java.io.PrintWriter;
import java.util.Properties;

/**
 *
 * @author  Craig Russell
 * @version 0.1
 */

public interface PersistenceManagerFactory extends java.io.Serializable
{
  /**
   * Sets JDBC driver name
   * @param driverName    JDBC driver name
   */
  void setConnectionDriverName (String driverName);

  /**
   * Returns JDBC driver name
   * @return      driver name
   */
  String getConnectionDriverName ();

  /**
   * Sets database user
   * @param userName      database user
   */
  void setConnectionUserName (String userName);

  /**
   * Returns database user name
   * @return      current database user name
   */
  String getConnectionUserName ();

  /**
   * Sets database user password
   * @param password      database user password
   */
  void setConnectionPassword (char[] password);

  /**
   * Sets maximum number of connections in the connection pool
   * @param MaxPool       maximum number of connections
   */
  void setConnectionMaxPool (int MaxPool);

  /**
   * Returns maximum number of connections in the connection pool
   * @return      connectionMaxPool
   */
  int getConnectionMaxPool ();

  /**
   * Sets minimum number of connections in the connection pool
   * @param MinPool       minimum number of connections
   */
  void setConnectionMinPool (int MinPool);

  /**
   * Returns minimum number of connections in the connection pool
   * @return      connectionMinPool
   */
  int getConnectionMinPool ();


  /**
   * Sets maximum number of PersistenceManager instances in the pool
   * @param MaxPool       maximum number of instances
   */
  void setMaxPool (int MaxPool);

  /**
   * Returns maximum number of PersistenceManager instances in the pool
   * @return      maxPool
   */
  int getMaxPool ();

  /**
   * Sets minimum number of PersistenceManager instances in the pool
   * @param MinPool       minimum number of PersistenceManager instances
   */
  void setMinPool (int MinPool);

  /**
   * Returns minimum number of PersistenceManager instances in the pool
   * @return      minPool
   */
  int getMinPool ();

  /**
   * Sets the number of milliseconds to wait for an available connection
   * from the connection pool before throwing an exception
   * @param MsWait        number in milliseconds
   */
  void setConnectionMsWait (int MsWait);

  /**
   * Returns the number of milliseconds to wait for an available connection
   * from the connection pool before throwing an exception
   * @return      number in milliseconds
   */
  int getConnectionMsWait ();

  /**
   * Sets the amount of time, in milliseconds, between the connection
   * manager's attempts to get a pooled connection.
   * @param MsInterval    the interval between attempts to get a database
   *                      connection, in milliseconds.
   *
   */
  void setConnectionMsInterval (int MsInterval);

  /**
   * Returns the amount of time, in milliseconds, between the connection
   * manager's attempts to get a pooled connection.
   * @return      the length of the interval between tries in milliseconds
   */
  int getConnectionMsInterval ();

  /**
   * Sets the number of seconds to wait for a new connection to be
   * established to the data source
   * @param LoginTimeout           wait time in seconds
   */
  void setConnectionLoginTimeout (int LoginTimeout);

  /**
   * Returns the number of seconds to wait for a new connection to be
   * established to the data source
   * @return      wait time in seconds
   */
  int getConnectionLoginTimeout ();

  /**
   * Sets JDBC connection URL
   * @param URL   connection URL
   */
  void setConnectionURL (String URL);

  /**
   * Returns connection URL
   * @return      connection URL
   */
  String getConnectionURL ();

  /**
   * Sets Connection Factory as <a href="ConnectionFactory.html">ConnectionFactory</a>
   * or javax.sql.DataSource
   * @param cf     as java.lang.Object
   */
  void setConnectionFactory (Object cf);

  /**
   * Returns Connection Factory object that can be one of
   * <a href="ConnectionFactory.html">ConnectionFactory</a> or javax.sql.DataSource
   * @return      Connection Factory as java.lang.Object
   */
  Object getConnectionFactory ();

  /**
   * Sets the LogWriter to which messages should be sent
   * @param pw            LogWriter
   */
  void setConnectionLogWriter(PrintWriter pw);

  /**
   * Returns the LogWriter to which messages should be sent
   * @return      LogWriter
   */
  PrintWriter getConnectionLogWriter ();

  /**
   * Sets transaction isolation level for all connections of this PersistenceManagerFactory.
   * All validation is done by java.sql.Connection itself, so e.g. while Oracle
   * will not allow to set solation level to TRANSACTION_REPEATABLE_READ, this method
   * does not have any explicit restrictions
   *
   * @param level - one of the java.sql.Connection.TRANSACTION_* isolation values
   */
  void setConnectionTransactionIsolation (int level);

  /**
   * Returns current transaction isolation level for connections of this PersistenceManagerFactory.
   * @return      the current transaction isolation mode value as java.sql.Connection.TRANSACTION_*
   */
  int getConnectionTransactionIsolation ();

  /**
   * Sets ConnectionFactory name
   * @param connectionFactoryName     ConnectionFactory name
   */
  void setConnectionFactoryName (String connectionFactoryName);

  /**
   * Returns ConnectionFactory name
   * @return      ConnectionFactoryName
   */
  String getConnectionFactoryName ();

  /**
   * Sets Identifier. An Identifier is a string that user can use to identify
   * the PersistenceManagerFacory in a given environment. Identifier can be
   * particularly useful in an environment where multiple
   * PersistenceManagerFacories are initialized in a system.
   * @param identifier
   */
  void setIdentifier(String identifier);

  /**
   * Gets Identifier. An Identifier is a string that user can use to identify
   * the PersistenceManagerFacory in a given environment. Identifier can be
   * particularly useful in an environment where multiple
   * PersistenceManagerFacories are initialized in a system.
   * @return identifier
   */
  String getIdentifier();


  /**
   * Creates new <a href="PersistenceManager.html">PersistenceManager</a> without extra info
   * @return      the persistence manager
   */
  PersistenceManager getPersistenceManager ();

  /**
   * Creates new <a href="PersistenceManager.html">PersistenceManager</a> with specific
   * username and password. Used to call ConnectionFactory.getConnection(String, String)
   * @param       username    datasource user
   * @param       password    datasource user password
   * @return      the persistence manager
   */
  PersistenceManager getPersistenceManager (String username, char[] password);

  /**
   * Sets the optimistic flag for all PersistenceManagers
   * @param flag          boolean optimistic flag
   */
  void setOptimistic (boolean flag);

  /**
   * Returns the boolean value of the optimistic flag for all PersistenceManagers
   * @return      boolean optimistic flag
   */
  boolean getOptimistic ();

  /**
   * Sets flag that will not cause the eviction of persistent instances after transaction completion.
   * @param flag          boolean flag passed
   */
  void setRetainValues (boolean flag);

  /**
   * Returns the boolean value for the flag that will not cause the eviction of persistent
   * instances after transaction completion.
   * @return      boolean setting for the flag
   */
  boolean getRetainValues ();

  /**
   * Sets the flag that allows non-transactional instances to be managed in the cache.
   * @param flag          boolean flag passed
   */
  void setNontransactionalRead (boolean flag);

  /**
   * Returns the boolean value for the flag that allows non-transactional instances
   * to be managed in the cache.
   * @return      boolean setting for the flag
   */
  boolean getNontransactionalRead ();

  /**
   * Sets the flag that allows the user to request that queries be optimized to return
   * approximate results by ignoring changed values in the cache.
   * @param flag          boolean flag passed
   */
  void setIgnoreCache (boolean flag);

  /**
   * Returns the boolean value for the flag that allows the user to request that queries
   * be optimized to return approximate results by ignoring changed values in the cache.
   * @return      boolean setting for the flag
   */
  boolean getIgnoreCache ();

  /**
   * Sets the number of seconds to wait for a query statement
   * to execute in the datastore associated with this PersistenceManagerFactory.
   * @param timeout          new timout value in seconds; zero means unlimited
   */
  void setQueryTimeout (int timeout);

  /**
   * Gets the number of seconds to wait for a query statement
   * to execute in the datastore associated with this PersistenceManagerFactory.
   * @return      timout value in seconds; zero means unlimited
   */
  int getQueryTimeout ();

  /**
   * Sets the number of seconds to wait for an update statement
   * to execute in the datastore associated with this PersistenceManagerFactory.
   * @param timeout          new timout value in seconds; zero means unlimited
   */
  void setUpdateTimeout (int timeout);

  /**
   * Gets the number of seconds to wait for an update statement
   * to execute in the datastore associated with this PersistenceManagerFactory.
   * @return      timout value in seconds; zero means unlimited
   */
  int getUpdateTimeout();


  /**
   * Returns non-operational properties to be available to the application via a Properties instance.
   * @return      Properties object
   */
  Properties getProperties ();

  /**
   * Returns the boolean value of the supersedeDeletedInstance flag
   * for all PersistenceManagers. If set to true, deleted instances are
   * allowed to be replaced with persistent-new instances with the equal
   * Object Id.
   * @return      boolean supersedeDeletedInstance flag
   */
  boolean getSupersedeDeletedInstance ();


  /**
   * Sets the supersedeDeletedInstance flag for all PersistenceManagers.
   * @param flag          boolean supersedeDeletedInstance flag
   */
  void setSupersedeDeletedInstance (boolean flag);

  /**
   * Returns the default value of the requireCopyObjectId flag
   * for this PersistenceManagerFactory. If set to false, the PersistenceManager
   * will not create a copy of an ObjectId for <code>PersistenceManager.getObjectId(Object pc)</code>
   * and <code>PersistenceManager.getObjectById(Object oid)</code> requests.
   *
   * @see PersistenceManager#getObjectId(Object pc)
   * @see PersistenceManager#getObjectById(Object oid)
   * @return      boolean requireCopyObjectId flag
   */
  boolean getRequireCopyObjectId();


  /**
   * Sets the default value of the requireCopyObjectId flag.
   * If set to false, by default a PersistenceManager will not create a copy of
   * an ObjectId for <code>PersistenceManager.getObjectId(Object pc)</code>
   * and <code>PersistenceManager.getObjectById(Object oid)</code> requests.
   *
   * @see PersistenceManager#getObjectId(Object pc)
   * @see PersistenceManager#getObjectById(Object oid)
   * @param flag          boolean requireCopyObjectId flag
   */
  void setRequireCopyObjectId (boolean flag);

  /**
   * Returns the boolean value of the requireTrackedSCO flag
   * for this PersistenceManagerFactory. If set to false, by default the
   * PersistenceManager will not create tracked SCO instances for
   * new persistent instances at commit with retainValues set to true and while
   * retrieving data from a datastore.
   *
   * @return      boolean requireTrackedSCO flag
   */
  boolean getRequireTrackedSCO();

  /**
   * Sets the requireTrackedSCO flag for this PersistenceManagerFactory.
   * If set to false, by default the PersistenceManager will not create tracked
   * SCO instances for new persistent instances at commit with retainValues set to true
   * and while retrieving data from a datastore.
   *
   * @param flag          boolean requireTrackedSCO flag
   */
  void setRequireTrackedSCO (boolean flag);

}

