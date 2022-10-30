/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.jdo.spi.persistence.support.sqlstore.impl;

import com.sun.jdo.api.persistence.support.ConnectionFactory;
import com.sun.jdo.api.persistence.support.JDOException;
import com.sun.jdo.api.persistence.support.JDOUnsupportedOptionException;
import com.sun.jdo.api.persistence.support.JDOUserException;
import com.sun.jdo.api.persistence.support.PersistenceManager;
import com.sun.jdo.api.persistence.support.PersistenceManagerFactory;
import com.sun.jdo.spi.persistence.support.sqlstore.RuntimeVersion;
import com.sun.jdo.spi.persistence.support.sqlstore.ejb.EJBHelper;

import java.io.PrintWriter;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.persistence.common.I18NHelper;

/**
 * @author  Marina Vatkina 2000
 */
public class PersistenceManagerFactoryImpl implements PersistenceManagerFactory {

    private static final int NOT_SET = 0;
    private static final int SET_AS_CONNECTIONFACTORY = 1;
    private static final int SET_AS_DATASOURCE = 2;

    /**
     * PersistenceManagerFactory properties
     */
    private String URL;
    private String userName;
    private char[] password;
    private String driverName;
    private ConnectionFactory connectionFactory;
    private Object dataSource;
    private SimpleJndiName connectionFactoryName;
    private String identifier;
    private int connectionMaxPool;
    private int connectionMinPool;
    private int connectionMsInterval;
    private int connectionLoginTimeout;
    private int connectionMsWait;
    private int txIsolation = -1;
    private transient PrintWriter connectionLogWriter; //We do not expect instance of this class to be serialized. Marking a non serializable member as transient to make findbugs happy.
    private boolean optimistic = true;
    private boolean retainValues = true;
    private boolean nontransactionalRead = true;
    private final boolean ignoreCache = false;
    private int queryTimeout;
    private int updateTimeout;
    private int maxPool;
    private int minPool;
    private boolean supersedeDeletedInstance = true;
    private boolean requireCopyObjectId = true;
    private boolean requireTrackedSCO = true;

    /** flag for ConnectionFactory setup
     */
    private int providedConnectionFactory;


    /**
     * Reference to the internal PersistenceManagerFactory implementation
     * to deligate actual stuff
     */
    private transient SQLPersistenceManagerFactory pmFactory;

    /**
     * I18N message handler
     */
    private final static ResourceBundle messages = I18NHelper.loadBundle(
        PersistenceManagerFactoryImpl.class);

    /**
     * Creates new <code>PersistenceManagerFactoryImpl</code> without any user info
     */
    public PersistenceManagerFactoryImpl() {
        EJBHelper.setPersistenceManagerFactoryDefaults(this);
    }

    /**
     * Creates new <code>PersistenceManagerFactoryImpl</code> with user info
     * @param URL        connection URL
     * @param userName    database user
     * @param password    database user password
     * @param driverName    driver name
     */
    public PersistenceManagerFactoryImpl(
        String URL,
        String userName,
        char[] password,
        String driverName)
    {
        EJBHelper.setPersistenceManagerFactoryDefaults(this);

        this.URL = URL;
        this.userName = userName;
        this.password = password;
        this.driverName = driverName;

    }


    /**
     * Sets database user
     *
     * @param userName database user
     */
    @Override
    public void setConnectionUserName(String userName) {
        assertNotConfigured();
        this.userName = userName;
    }


    /**
     * Returns database user name
     *
     * @return current database user name
     */
    @Override
    public String getConnectionUserName() {
        if (connectionFactory != null) {
            return connectionFactory.getUserName();
        }

        return userName;
    }


    /**
     * Sets database user password
     *
     * @param password database user password
     */
    @Override
    public void setConnectionPassword(char[] password) {
        assertNotConfigured();
        this.password = password;
    }


    /**
     * Sets JDBC connection URL
     *
     * @param URL connection URL
     */
    @Override
    public void setConnectionURL(String URL) {
        assertNotConfigured();
        this.URL = URL;
    }


    /**
     * Returns connection URL
     *
     * @return connection URL
     */
    @Override
    public String getConnectionURL() {
        if (connectionFactory != null) {
            return connectionFactory.getURL();
        }

        return URL;

    }


    /**
     * Sets JDBC driver name
     *
     * @param driverName JDBC driver name
     */
    @Override
    public void setConnectionDriverName(String driverName) {
        assertNotConfigured();
        this.driverName = driverName;
    }


    /**
     * Returns JDBC driver name
     *
     * @return driver name
     */
    @Override
    public String getConnectionDriverName() {
        if (connectionFactory != null) {
            return connectionFactory.getDriverName();
        }
        return driverName;

    }


    /**
     * Sets ConnectionFactory that can be one of two types:
     * <a href="ConnectionFactory.html">ConnectionFactory</a> or javax.sql.DataSource
     *
     * @param connectionFactory as java.lang.Object
     */
    @Override
    public void setConnectionFactory(Object connectionFactory) {
        assertNotConfigured();
        if (connectionFactory == null) {
            this.connectionFactory = null;
            this.dataSource = null;
            providedConnectionFactory = NOT_SET;

        } else {
            if (EJBHelper.isManaged() || (connectionFactory instanceof DataSource)) {
                this.dataSource = connectionFactory;
                providedConnectionFactory = SET_AS_DATASOURCE;
            } else if (connectionFactory instanceof ConnectionFactory) {
                this.connectionFactory = (ConnectionFactory) connectionFactory;
                providedConnectionFactory = SET_AS_CONNECTIONFACTORY;
            } else {
                throw new JDOUserException(I18NHelper.getMessage(messages, "persistencemanagerfactoryimpl.wrongtype")); // NOI18N
            }
        }
    }


    /**
     * Returns ConnectionFactory
     *
     * @return ConnectionFactory
     */
    @Override
    public Object getConnectionFactory() {
        if (dataSource != null) {
            return dataSource;
        }

        return connectionFactory;
    }


    /**
     * Sets ConnectionFactory name
     *
     * @param connectionFactoryName ConnectionFactory name
     */
    @Override
    public void setConnectionFactoryName(SimpleJndiName connectionFactoryName) {
        assertNotConfigured();
        this.connectionFactoryName = connectionFactoryName;
    }


    /**
     * Returns ConnectionFactory name
     *
     * @return ConnectionFactoryName
     */
    @Override
    public SimpleJndiName getConnectionFactoryName() {
        return connectionFactoryName;
    }


    /**
     * Sets Identifier. An identifier is a string that user can use to identify
     * the PersistenceManagerFactory in a given environment. Identifier can be
     * particularly useful in an environment where multiple
     * PersistenceManagerFactories are initialized in a system.
     *
     * @param identifier
     */
    @Override
    public void setIdentifier(String identifier) {
        assertNotConfigured();
        this.identifier = identifier;
    }


    /**
     * Gets Identifier. An identifier is a string that user can use to identify
     * the PersistenceManagerFactory in a given environment. Identifier can be
     * particularly useful in an environment where multiple
     * PersistenceManagerFactories are initialized in a system.
     *
     * @return identifier
     */
    @Override
    public String getIdentifier() {
        return identifier;
    }


    /**
     * Sets maximum number of connections in the connection pool
     *
     * @param MaxPool maximum number of connections
     */
    @Override
    public void setConnectionMaxPool(int MaxPool) {
        assertNotConfigured();
        this.connectionMaxPool = MaxPool;
    }


    /**
     * Returns maximum number of connections in the connection pool
     *
     * @return connectionMaxPool
     */
    @Override
    public int getConnectionMaxPool() {
        if (connectionFactory != null) {
            return connectionFactory.getMaxPool();
        }

        return connectionMaxPool;
    }


    /**
     * Sets minimum number of connections in the connection pool
     *
     * @param MinPool minimum number of connections
     */

    @Override
    public void setConnectionMinPool(int MinPool) {
        assertNotConfigured();
        this.connectionMinPool = MinPool;
    }


    /**
     * Returns minimum number of connections in the connection pool
     *
     * @return connectionMinPool
     */
    @Override
    public int getConnectionMinPool() {
        if (connectionFactory != null) {
            return connectionFactory.getMinPool();
        }
        return connectionMinPool;
    }


    /**
     * Sets the number of milliseconds to wait for an available connection
     * from the connection pool before throwing an exception
     *
     * @param MsWait number in milliseconds
     */

    @Override
    public void setConnectionMsWait(int MsWait) {
        assertNotConfigured();
        this.connectionMsWait = MsWait;
    }


    /**
     * Returns the number of milliseconds to wait for an available connection
     * from the connection pool before throwing an exception
     *
     * @return number in milliseconds
     */
    @Override
    public int getConnectionMsWait() {
        if (connectionFactory != null) {
            return connectionFactory.getMsWait();
        }

        return connectionMsWait;
    }


    /**
     * Returns maximum number of PersistenceManager instances in the pool
     *
     * @return maxPool
     */
    @Override
    public int getMaxPool() {
        return maxPool;
    }


    /**
     * Sets maximum number of PersistenceManager instances in the pool
     *
     * @param MaxPool maximum number of PersistenceManager instances
     */
    @Override
    public void setMaxPool(int MaxPool) {
        assertNotConfigured();
        this.maxPool = MaxPool;
    }


    /**
     * Returns minimum number of PersistenceManager instances in the pool
     *
     * @return minPool
     */
    @Override
    public int getMinPool() {
        return minPool;
    }


    /**
     * Sets minimum number of PersistenceManager instances in the pool
     *
     * @param MinPool minimum number of PersistenceManager instances
     */
    @Override
    public void setMinPool(int MinPool) {
        assertNotConfigured();
        this.minPool = MinPool;
    }


    /**
     * Sets the amount of time, in milliseconds, between the connection
     * manager's attempts to get a pooled connection.
     *
     * @param MsInterval the interval between attempts to get a database
     *            connection, in milliseconds.
     */
    @Override
    public void setConnectionMsInterval(int MsInterval) {
        assertNotConfigured();
        this.connectionMsInterval = MsInterval;
    }


    /**
     * Returns the amount of time, in milliseconds, between the connection
     * manager's attempts to get a pooled connection.
     *
     * @return the length of the interval between tries in milliseconds
     */
    @Override
    public int getConnectionMsInterval() {
        if (connectionFactory != null) {
            return connectionFactory.getMsInterval();
        }

        return connectionMsInterval;
    }


    /**
     * Sets the number of seconds to wait for a new connection to be
     * established to the data source
     *
     * @param LoginTimeout wait time in seconds
     */

    @Override
    public void setConnectionLoginTimeout(int LoginTimeout) {
        assertNotConfigured();
        this.connectionLoginTimeout = LoginTimeout;
    }


    /**
     * Returns the number of seconds to wait for a new connection to be
     * established to the data source
     *
     * @return wait time in seconds
     */
    @Override
    public int getConnectionLoginTimeout() {
        if (connectionFactory == null) {
            return connectionLoginTimeout;
        }
        return connectionFactory.getLoginTimeout();
    }


    /**
     * Sets the LogWriter to which messages should be sent
     *
     * @param pw LogWriter
     */
    @Override
    public void setConnectionLogWriter(PrintWriter pw) {
        assertNotConfigured();
        this.connectionLogWriter = pw;
    }


    /**
     * Returns the LogWriter to which messages should be sent
     *
     * @return LogWriter
     */
    @Override
    public PrintWriter getConnectionLogWriter() {
        return connectionLogWriter;
    }


    /**
     * Sets the queryTimeout for all PersistenceManagers
     *
     * @param timeout the timout to be set
     */
    public void setQueryTimeout(String timeout) {
        setQueryTimeout(Integer.parseInt(timeout));
    }


    /**
     * Sets the number of seconds to wait for a query statement
     * to execute in the datastore associated with this PersistenceManagerFactory.
     *
     * @param timeout new timout value in seconds; zero means unlimited
     */
    @Override
    public void setQueryTimeout(int timeout) {
        assertNotConfigured();
        this.queryTimeout = timeout;
    }


    /**
     * Gets the number of seconds to wait for a query statement
     * to execute in the datastore associated with this PersistenceManagerFactory.
     *
     * @return timout value in seconds; zero means unlimited
     */
    @Override
    public int getQueryTimeout() {
        return queryTimeout;
    }


    /**
     * Sets the updateTimeout for all PersistenceManagers
     *
     * @param timeout the timout to be set
     */
    public void setUpdateTimeout(String timeout) {
        setUpdateTimeout(Integer.parseInt(timeout));
    }


    /**
     * Sets the number of seconds to wait for an update statement
     * to execute in the datastore associated with this PersistenceManagerFactory.
     *
     * @param timeout new timout value in seconds; zero means unlimited
     */
    @Override
    public void setUpdateTimeout(int timeout) {
        assertNotConfigured();
        this.updateTimeout = timeout;
    }


    /**
     * Gets the number of seconds to wait for an update statement
     * to execute in the datastore associated with this PersistenceManagerFactory.
     *
     * @return timout value in seconds; zero means unlimited
     */
    @Override
    public int getUpdateTimeout() {
        return updateTimeout;
    }


    /**
     * Sets transaction isolation level for all connections of this PersistenceManagerFactory.
     * All validation is done by java.sql.Connection itself, so e.g. while Oracle
     * will not allow to set solation level to TRANSACTION_REPEATABLE_READ, this method
     * does not have any explicit restrictions
     *
     * @param level - one of the java.sql.Connection.TRANSACTION_* isolation values
     */
    @Override
    public void setConnectionTransactionIsolation(int level) {
        assertNotConfigured();
        txIsolation = level;
    }


    /**
     * Returns current transaction isolation level for connections of this
     * PersistenceManagerFactory.
     *
     * @return the current transaction isolation mode value as java.sql.Connection.TRANSACTION_*
     */
    @Override
    public int getConnectionTransactionIsolation() {
        if (connectionFactory != null) {
            return connectionFactory.getTransactionIsolation();
        }

        return txIsolation;
    }


    /**
     * Sets the optimistic flag for all PersistenceManagers
     *
     * @param flag String optimistic flag
     */
    public void setOptimistic(String flag) {
        setOptimistic(Boolean.parseBoolean(flag));
    }


    /**
     * Sets the optimistic flag for all PersistenceManagers
     *
     * @param flag boolean optimistic flag
     */
    @Override
    public void setOptimistic(boolean flag) {
        assertNotConfigured();
        optimistic = flag;

        // Adjust depending flags
        if (flag) {
            nontransactionalRead = flag;
        }
    }


    /**
     * Returns the boolean value of the optimistic flag for all PersistenceManagers
     *
     * @return boolean optimistic flag
     */
    @Override
    public boolean getOptimistic() {
        return optimistic;
    }


    /**
     * Sets the RetainValues flag for all PersistenceManagers
     *
     * @param flag String RetainValues flag
     */
    public void setRetainValues(String flag) {
        setRetainValues(Boolean.parseBoolean(flag));
    }


    /**
     * Sets flag that will not cause the eviction of persistent instances after transaction
     * completion.
     *
     * @param flag boolean flag passed
     */
    @Override
    public void setRetainValues(boolean flag) {
        assertNotConfigured();
        retainValues = flag;

        // Adjust depending flags
        if (flag) {
            nontransactionalRead = flag;
        }
    }


    /**
     * Returns the boolean value for the flag that will not cause the eviction of persistent
     * instances after transaction completion.
     *
     * @return boolean setting for the flag
     */
    @Override
    public boolean getRetainValues() {
        return retainValues;
    }


    /**
     * Sets the NontransactionalRead flag for all PersistenceManagers
     *
     * @param flag String NontransactionalRead flag
     */
    public void setNontransactionalRead(String flag) {
        setNontransactionalRead(Boolean.parseBoolean(flag));
    }


    /**
     * Sets the flag that allows non-transactional instances to be managed in the cache.
     *
     * @param flag boolean flag passed
     */
    @Override
    public void setNontransactionalRead(boolean flag) {
        assertNotConfigured();
        nontransactionalRead = flag;

        // Adjust depending flags
        if (flag == false) {
            retainValues = flag;
            optimistic = flag;
        }
    }


    /**
     * Returns the boolean value for the flag that allows non-transactional instances
     * to be managed in the cache.
     *
     * @return boolean setting for the flag
     */
    @Override
    public boolean getNontransactionalRead() {
        return nontransactionalRead;
    }


    /**
     * Sets the IgnoreCache flag for all PersistenceManagers
     *
     * @param flag String IgnoreCache flag
     */
    public void setIgnoreCache(String flag) {
        setIgnoreCache(Boolean.parseBoolean(flag));
    }


    /**
     * Sets the flag that allows the user to request that queries be optimized to return
     * approximate results by ignoring changed values in the cache.
     *
     * @param flag boolean flag passed
     */
    @Override
    public void setIgnoreCache(boolean flag) {
        assertNotConfigured();
        throw new JDOUnsupportedOptionException(
            I18NHelper.getMessage(messages, "persistencemanagerfactoryimpl.notsupported")); // NOI18N
        // ignoreCache = flag;
    }


    /**
     * Returns the boolean value for the flag that allows the user to request that queries
     * be optimized to return approximate results by ignoring changed values in the cache.
     *
     * @return boolean setting for the flag
     */
    @Override
    public boolean getIgnoreCache() {
        return ignoreCache;
    }


    /**
     * Returns non-operational properties to be available to the application via a Properties
     * instance.
     *
     * @return Properties object
     */
    @Override
    public Properties getProperties() {
        if (pmFactory != null) {
            return pmFactory.getProperties();
        }
        return RuntimeVersion.getVendorProperties("/com/sun/jdo/spi/persistence/support/sqlstore/sys.properties"); // NOI18N
    }


    /**
     * Creates new <a href="PersistenceManager.html">PersistenceManager</a> without extra info
     *
     * @return the persistence manager
     */
    @Override
    public PersistenceManager getPersistenceManager() {
        return getPersistenceManager(null, null);
    }


    /**
     * Returns the boolean value of the supersedeDeletedInstance flag
     * for all PersistenceManagers. If set to true, deleted instances are
     * allowed to be replaced with persistent-new instances with the equal
     * Object Id.
     *
     * @return boolean supersedeDeletedInstance flag
     */
    @Override
    public boolean getSupersedeDeletedInstance() {
        return supersedeDeletedInstance;
    }


    /**
     * Sets the supersedeDeletedInstance flag for all PersistenceManagers.
     *
     * @param flag boolean supersedeDeletedInstance flag
     */
    @Override
    public void setSupersedeDeletedInstance(boolean flag) {
        assertNotConfigured();
        supersedeDeletedInstance = flag;
    }


    /**
     * Returns the default value of the requireCopyObjectId flag
     * for this PersistenceManagerFactoryImpl. If set to false, the PersistenceManager
     * will not create a copy of an ObjectId for
     * <code>PersistenceManager.getObjectId(Object pc)</code>
     * and <code>PersistenceManager.getObjectById(Object oid)</code> requests.
     *
     * @see PersistenceManager#getObjectId(Object pc)
     * @see PersistenceManager#getObjectById(Object oid)
     * @return boolean requireCopyObjectId flag
     */
    @Override
    public boolean getRequireCopyObjectId() {
        return requireCopyObjectId;
    }


    /**
     * Sets the default value of the requireCopyObjectId flag.
     * If set to false, by default a PersistenceManager will not create a copy of
     * an ObjectId for <code>PersistenceManager.getObjectId(Object pc)</code>
     * and <code>PersistenceManager.getObjectById(Object oid)</code> requests.
     *
     * @see PersistenceManager#getObjectId(Object pc)
     * @see PersistenceManager#getObjectById(Object oid)
     * @param flag boolean requireCopyObjectId flag
     */
    @Override
    public void setRequireCopyObjectId(boolean flag) {
        assertNotConfigured();
        requireCopyObjectId = flag;
    }


    /**
     * Returns the boolean value of the requireTrackedSCO flag.
     * If set to false, by default the PersistenceManager will not create
     * tracked SCO instances for new persistent instances at commit with
     * retainValues set to true and while retrieving data from a datastore.
     *
     * @return boolean requireTrackedSCO flag
     */
    @Override
    public boolean getRequireTrackedSCO() {
        return requireTrackedSCO;
    }


    /**
     * Sets the requireTrackedSCO flag for this PersistenceManagerFactory.
     * If set to false, by default the PersistenceManager will not create tracked
     * SCO instances for new persistent instances at commit with retainValues set to true
     * and while retrieving data from a datastore.
     *
     * @param flag boolean requireTrackedSCO flag
     */
    @Override
    public void setRequireTrackedSCO(boolean flag) {
        assertNotConfigured();
        requireTrackedSCO = flag;
    }


    /**
     * Creates new <a href="PersistenceManager.html">PersistenceManager</a> with specific
     * username and password. Used to call ConnectionFactory.getConnection(String, String)
     *
     * @param username datasource user
     * @param passwd datasource user password
     * @return the persistence manager
     */
    @Override
    public PersistenceManager getPersistenceManager(String username, char[] passwd) {
        synchronized (this) {

            if (pmFactory == null) {
                // Nothing there yet. Check and create
                if (providedConnectionFactory == NOT_SET) {

                    if (connectionFactoryName == null) {

                        // Validate that MsWait/MsInterval are correct
                        assertConnectionWait();

                        // only PMFactory was configured
                        // Create a default ConnectionFactoryImpl and
                        // set all the parameters. With 1st connection
                        // which happens during configuration od SqlStore
                        // the actaul connectionManager will be created
                        connectionFactory = new ConnectionFactoryImpl();

                        connectionFactory.setURL(URL);
                        connectionFactory.setUserName(userName);
                        connectionFactory.setPassword(password);
                        connectionFactory.setDriverName(driverName);
                        connectionFactory.setMinPool(connectionMinPool);
                        connectionFactory.setMaxPool(connectionMaxPool);

                        // MsWait MUST be set BEFORE MsInterval
                        connectionFactory.setMsWait(this.connectionMsWait);
                        connectionFactory.setMsInterval(this.connectionMsInterval);
                        connectionFactory.setLogWriter(this.connectionLogWriter);
                        connectionFactory.setLoginTimeout(this.connectionLoginTimeout);
                        if (txIsolation > 0) {
                            connectionFactory.setTransactionIsolation(txIsolation);
                        }
                    } else {
                        // Do JNDI lookup
                        try {
                            InitialContext ctx = new InitialContext(); // NOI18N
                            Object o = ctx.lookup(connectionFactoryName.toString());
                            if (EJBHelper.isManaged() || (o instanceof DataSource)) {
                                dataSource = o;
                            } else if (o instanceof ConnectionFactory) {
                                connectionFactory = (ConnectionFactory) o;
                            } else {
                                throw new JDOUserException(
                                    I18NHelper.getMessage(messages, "persistencemanagerfactoryimpl.wrongtype")); // NOI18N
                            }

                        } catch (JDOException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new JDOUserException(
                                I18NHelper.getMessage(messages, "persistencemanagerfactoryimpl.lookup"), e); // NOI18N
                        }
                    }
                }

                // If identifier is not yet set, set it to name of connection factory
                if (getIdentifier() == null) {
                    setIdentifier(getConnectionFactoryName().toString());
                }
                // create new
                pmFactory = new SQLPersistenceManagerFactory(this);
                // Check EJBHelper
                pmFactory = (SQLPersistenceManagerFactory) EJBHelper
                    .replaceInternalPersistenceManagerFactory(pmFactory);

            }
        } // end synchronized (this)

        // Should not be called
        if (username != null && connectionFactory != null) {
            throw new JDOUnsupportedOptionException(
                I18NHelper.getMessage(messages, "persistencemanagerfactoryimpl.notsupported")); // NOI18N
        }

        return pmFactory.getPersistenceManager(username, passwd);

    }


    /**
     * Sets default value of a known boolean property.
     *
     * @param name the name of the property to be set.
     * @param value the default boolean value.
     */
    public void setBooleanProperty(String name, boolean value) {
        // These if-else statements will be replaced by the JDO implementation...
        if (name.equals("optimistic")) { // NOI18N
            setOptimistic(value);

        } else if (name.equals("retainValues")) { // NOI18N
            setRetainValues(value);

        } else if (name.equals("nontransactionalRead")) { // NOI18N
            setNontransactionalRead(value);

        } else if (name.equals("ignoreCache")) { // NOI18N
            setIgnoreCache(value);

        } else if (name.equals("supersedeDeletedInstance")) { // NOI18N
            setSupersedeDeletedInstance(value);

        } else if (name.equals("requireCopyObjectId")) { // NOI18N
            setRequireCopyObjectId(value);

        } else if (name.equals("requireTrackedSCO")) { // NOI18N
            setRequireTrackedSCO(value);

        } // else ignore it.

    }


    /**
     * Determines whether obj is a PersistenceManagerFactoryImpl with the same configuration
     *
     * @param obj The possibly null object to check.
     * @return true if obj is equal to this PersistenceManagerFactoryImpl; false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof PersistenceManagerFactoryImpl)) {
            return false;
        }
        PersistenceManagerFactoryImpl pmf = (PersistenceManagerFactoryImpl) obj;

        if (pmf.providedConnectionFactory == this.providedConnectionFactory) {
            if (pmf.providedConnectionFactory == SET_AS_CONNECTIONFACTORY) {
                return (pmf.connectionFactory.equals(this.connectionFactory) && equalBooleanProperties(pmf));

            } else if (pmf.providedConnectionFactory == SET_AS_DATASOURCE) {
                return (pmf.dataSource.equals(this.dataSource) && equalBooleanProperties(pmf));

            } else if (pmf.connectionFactoryName != null) {
                return (pmf.connectionFactoryName.equals(this.connectionFactoryName) && equalBooleanProperties(pmf));

            }
            return (pmf.URL.equals(this.URL) && pmf.userName.equals(this.userName) && pmf.password.equals(this.password)
                && pmf.driverName.equals(this.driverName) && equalBooleanProperties(pmf));
        }
        return false;
    }


    /**
     * Computes the hash code of this PersistenceManagerFactory.
     *
     * @return A hash code of the owning PersistenceManagerFactory as an int.
     */
    @Override
    public int hashCode() {
        if (providedConnectionFactory == SET_AS_CONNECTIONFACTORY) {
            return connectionFactory.hashCode();
        } else if (providedConnectionFactory == SET_AS_DATASOURCE) {
            return dataSource.hashCode();
        } else if (connectionFactoryName != null) {
            return connectionFactoryName.hashCode();
        }
        return URL.hashCode() + userName.hashCode() + password.hashCode() + driverName.hashCode();
    }


    /**
     * INTERNAL
     * Asserts that change to the property is allowed
     */
    private void assertNotConfigured() {
        if (pmFactory != null) {
            throw new JDOUnsupportedOptionException(
                I18NHelper.getMessage(messages, "persistencemanagerfactoryimpl.configured")); // NOI18N
        }
    }


    /**
     * INTERNAL
     * Asserts that MsWait and MsInterval are properly configured
     */
    private void assertConnectionWait() {
        if (connectionMsWait < 0) {
            throw new JDOUserException(I18NHelper.getMessage(messages, "connection.connectionmanager.mswaitvalue")); // NOI18N
        } else if (connectionMsInterval < 0 || connectionMsInterval > connectionMsWait
            || (connectionMsWait > 0 && connectionMsInterval == 0)) {
            throw new JDOUserException(I18NHelper.getMessage(messages, "connection.connectionmanager.msintervalvalue")); // NOI18N
        }
    }

    /**
     * Compares boolean setting on 2 PersistenceManagerFactory instances.
     *
     * @param pmf the PersistenceManagerFactory instance to compare with this instance.
     */
    private boolean equalBooleanProperties(PersistenceManagerFactory pmf) {
        return (pmf.getOptimistic() == optimistic &&
            pmf.getRetainValues() == retainValues &&
            pmf.getNontransactionalRead() == nontransactionalRead &&
            pmf.getIgnoreCache() == ignoreCache &&
            pmf.getSupersedeDeletedInstance() == supersedeDeletedInstance &&
            pmf.getRequireCopyObjectId() == requireCopyObjectId &&
            pmf.getRequireTrackedSCO() == requireTrackedSCO
            );

    }
}
