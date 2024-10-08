/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation.
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

package com.sun.gjc.spi;

import com.sun.appserv.connectors.internal.spi.MCFLifecycleListener;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.gjc.common.DataSourceObjectBuilder;
import com.sun.gjc.common.DataSourceSpec;
import com.sun.gjc.monitoring.JdbcStatsProvider;
import com.sun.gjc.util.SQLTraceDelegator;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConfigProperty;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.LazyEnlistableConnectionManager;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterAssociation;
import jakarta.resource.spi.ResourceAllocationException;
import jakarta.resource.spi.ValidatingManagedConnectionFactory;
import jakarta.resource.spi.security.PasswordCredential;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.sql.DataSource;
import javax.sql.PooledConnection;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.jdbc.ConnectionValidation;
import org.glassfish.api.jdbc.SQLTraceListener;
import org.glassfish.api.jdbc.objects.TxIsolationLevel;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.resourcebase.resources.api.PoolInfo;

import static com.sun.gjc.common.DataSourceSpec.CONNECTIONVALIDATIONREQUIRED;
import static com.sun.gjc.common.DataSourceSpec.GUARANTEEISOLATIONLEVEL;
import static com.sun.gjc.common.DataSourceSpec.TRANSACTIONISOLATION;
import static com.sun.gjc.common.DataSourceSpec.VALIDATIONCLASSNAME;
import static com.sun.gjc.common.DataSourceSpec.VALIDATIONMETHOD;
import static com.sun.gjc.util.SecurityUtils.getPasswordCredential;
import static com.sun.gjc.util.SecurityUtils.isPasswordCredentialEqual;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

/**
 * <code>ManagedConnectionFactory</code> implementation for Generic JDBC
 * Connector. This class is extended by the DataSource specific
 * <code>ManagedConnection</code> factories and the
 * <code>ManagedConnectionFactory</code> for the <code>DriverManager</code>.
 *
 * @author Evani Sai Surya Kiran, Aditya Gore
 * @version 1.0, 02/08/03
 */

public abstract class ManagedConnectionFactoryImpl
        implements ManagedConnectionFactory, ValidatingManagedConnectionFactory, MCFLifecycleListener,
        ResourceAdapterAssociation, Serializable, Externalizable {

    private static Logger _logger = LogDomains.getLogger(ManagedConnectionFactoryImpl.class, LogDomains.RSR_LOGGER);
    protected static final StringManager localStrings = StringManager.getManager(DataSourceObjectBuilder.class);

    @Inject
    protected InvocationManager invocationManager;

    protected DataSourceSpec spec = new DataSourceSpec();
    protected transient DataSourceObjectBuilder dataSourceObjectBuilder;
    protected PrintWriter logWriter;
    protected transient ResourceAdapter resourceAdapter;
    protected boolean statementWrapping;
    protected SQLTraceDelegator sqlTraceDelegator;
    protected LazyEnlistableConnectionManager connectionManager;
    protected boolean isLazyConnectionManager;

    private final JdbcObjectsFactory jdbcObjectsFactory = JdbcObjectsFactory.getInstance();
    private int statementCacheSize;
    private String statementCacheType;
    private long statementLeakTimeout;
    private boolean statementLeakReclaim;

    // Jdbc Stats provider that is created
    private JdbcStatsProvider jdbcStatsProvider;

    /**
     * Creates a Connection Factory instance. The <code>ConnectionManager</code>
     * implementation of the resource adapter is used here.
     *
     * @return Generic JDBC Connector implementation of
     * <code>javax.sql.DataSource</code>
     */
    @Override
    public Object createConnectionFactory() {
        logFine("In createConnectionFactory()");
        return jdbcObjectsFactory.getDataSourceInstance(this, null);
    }

    /**
     * Creates a Connection Factory instance. The <code>ConnectionManager</code>
     * implementation of the application server is used here.
     *
     * @param connectionManager <code>ConnectionManager</code> passed by the application
     * server
     * @return Generic JDBC Connector implementation of
     * <code>javax.sql.DataSource</code>
     */
    @Override
    public Object createConnectionFactory(ConnectionManager connectionManager) {
        logFine("In createConnectionFactory(jakarta.resource.spi.ConnectionManager cxManager)");

        DataSource connectionFactory = jdbcObjectsFactory.getDataSourceInstance(this, connectionManager);

        if (connectionManager instanceof LazyEnlistableConnectionManager) {
            this.connectionManager = (LazyEnlistableConnectionManager) connectionManager;
            isLazyConnectionManager = true;
        }

        return connectionFactory;
    }

    /**
     * Creates a new physical connection to the underlying EIS resource manager.
     *
     * @param subject <code>Subject</code> instance passed by the application server
     * @param cxRequestInfo <code>ConnectionRequestInfo</code> which may be created
     * as a result of the invocation <code>getConnection(user, password)</code> on
     * the <code>DataSource</code> object
     *
     * @return <code>ManagedConnection</code> object created
     * @throws ResourceException if there is an error in instantiating the
     * <code>DataSource</code> object used for the creation of the
     * <code>ManagedConnection</code> object
     * @throws SecurityException if there ino <code>PasswordCredential</code> object
     * satisfying this request
     * @throws ResourceException if there is an error in allocating the physical
     * connection
     */
    @Override
    public abstract ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException;

    /**
     * Check if this <code>ManagedConnectionFactoryImpl</code> is equal to another
     * <code>ManagedConnectionFactoryImpl</code>.
     *
     * @param other <code>ManagedConnectionFactoryImpl</code> object for checking
     * equality with
     * @return true if the property sets of both the
     * <code>ManagedConnectionFactoryImpl</code> objects are the same false
     * otherwise
     */
    @Override
    public abstract boolean equals(Object other);

    /**
     * Get the log writer for this <code>ManagedConnectionFactoryImpl</code>
     * instance.
     *
     * @return <code>PrintWriter</code> associated with this
     * <code>ManagedConnectionFactoryImpl</code> instance
     * @see <code>setLogWriter</code>
     */
    @Override
    public java.io.PrintWriter getLogWriter() {
        return logWriter;
    }

    /**
     * Get the <code>ResourceAdapterImpl</code> for this
     * <code>ManagedConnectionFactoryImpl</code> instance.
     *
     * @return <code>ResourceAdapterImpl</code> associated with this
     * <code>ManagedConnectionFactoryImpl</code> instance
     * @see <code>setResourceAdapter</code>
     */
    @Override
    public jakarta.resource.spi.ResourceAdapter getResourceAdapter() {
        logFine("In getResourceAdapter");
        return resourceAdapter;
    }

    /**
     * Returns the hash code for this <code>ManagedConnectionFactoryImpl</code>.
     *
     * @return hash code for this <code>ManagedConnectionFactoryImpl</code>
     */
    @Override
    public int hashCode() {
        logFine("In hashCode");
        return spec.hashCode();
    }

    /**
     * Returns a matched <code>ManagedConnection</code> from the candidate set of
     * <code>ManagedConnection</code> objects.
     *
     * @param connectionSet <code>Set</code> of <code>ManagedConnection</code>
     * objects passed by the application server
     * @param subject passed by the application server for retrieving information
     * required for matching
     * @param cxRequestInfo <code>ConnectionRequestInfo</code> passed by the
     * application server for retrieving information required for matching
     * @return <code>ManagedConnection</code> that is the best match satisfying this
     * request
     * @throws ResourceException if there is an error accessing the
     * <code>Subject</code> parameter or the <code>Set</code> of
     * <code>ManagedConnection</code> objects passed by the application server
     */
    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        logFine("In matchManagedConnections");

        if (connectionSet == null) {
            return null;
        }

        PasswordCredential passwordCredential = getPasswordCredential(this, subject, cxRequestInfo);

        Iterator<ManagedConnectionImpl> iter = connectionSet.iterator();
        ManagedConnectionImpl managedConnectionImpl = null;

        while (iter.hasNext()) {
            try {
                managedConnectionImpl = iter.next();
            } catch (NoSuchElementException nsee) {
                _logger.log(SEVERE, "jdbc.exc_iter");
                throw new ResourceException(nsee.getMessage());
            }
            if (passwordCredential == null && this.equals(managedConnectionImpl.getManagedConnectionFactory())) {
                return managedConnectionImpl;
            }

            if (isPasswordCredentialEqual(passwordCredential, managedConnectionImpl.getPasswordCredential())) {
                return managedConnectionImpl;
            }
        }

        return null;
    }

    /**
     * This method returns a set of invalid <code>ManagedConnection</code> objects
     * chosen from a specified set of <code>ManagedConnection</code> objects.
     *
     * @param connectionSet a set of <code>ManagedConnection</code> objects that
     * need to be validated.
     * @return a set of invalid <code>ManagedConnection</code> objects.
     * @throws ResourceException generic exception.
     */
    @Override
    public Set getInvalidConnections(Set connectionSet) throws ResourceException {
        Iterator iter = connectionSet.iterator();
        Set<ManagedConnectionImpl> invalidConnections = new HashSet<>();
        while (iter.hasNext()) {
            ManagedConnectionImpl managedConnectionImpl = (ManagedConnectionImpl) iter.next();
            try {
                isValid(managedConnectionImpl);
            } catch (ResourceException re) {
                invalidConnections.add(managedConnectionImpl);
                managedConnectionImpl.connectionErrorOccurred(re, null);
                _logger.log(FINE, "jdbc.invalid_connection", re);
            }
        }

        return invalidConnections;
    }

    /**
     * Checks if a <code>ManagedConnection</code> is to be validated or not and
     * validates it or returns.
     *
     * @param managedConnectionImpl <code>ManagedConnection</code> to be validated
     * @throws ResourceException if the connection is not valid or if validation
     * method is not proper
     */
    void isValid(ManagedConnectionImpl managedConnectionImpl) throws ResourceException {
        if (managedConnectionImpl == null || managedConnectionImpl.isTransactionInProgress()) {
            return;
        }

        String conVal = spec.getDetail(CONNECTIONVALIDATIONREQUIRED);

        boolean connectionValidationRequired = (conVal == null) ?
                false :
                Boolean.parseBoolean(conVal.toLowerCase(Locale.getDefault()));
        if (!connectionValidationRequired) {
            return;
        }

        String validationMethod = spec.getDetail(VALIDATIONMETHOD).toLowerCase(Locale.getDefault());

        managedConnectionImpl.checkIfValid();

        /**
         * The above call checks if the actual physical connection is usable or not.
         */
        Connection connection = managedConnectionImpl.getActualConnection();

        if (validationMethod.equals("custom-validation")) {
            isValidByCustomValidation(connection, spec.getDetail(VALIDATIONCLASSNAME));
        } else if (validationMethod.equals("auto-commit")) {
            isValidByAutoCommit(connection);
        } else if (validationMethod.equals("meta-data")) {
            isValidByMetaData(connection);
        } else if (validationMethod.equals("table")) {
            isValidByTableQuery(connection, spec.getDetail(DataSourceSpec.VALIDATIONTABLENAME));
        } else {
            throw new ResourceException("The validation method is not proper");
        }
    }

    /**
     * Checks if a <code>java.sql.Connection</code> is valid or not by doing a
     * custom validation using the validation class name specified.
     *
     * @param connection <code>java.sql.Connection</code> to be validated
     * @throws ResourceException if the connection is not valid
     */
    protected void isValidByCustomValidation(Connection connection, String validationClassName) throws ResourceException {
        boolean isValid = false;
        if (connection == null) {
            throw new ResourceException("The connection is not valid as " + "the connection is null");
        }

        try {
            ConnectionValidation connectionValidation = (ConnectionValidation)
                Thread.currentThread()
                      .getContextClassLoader()
                      .loadClass(validationClassName)
                      .getDeclaredConstructor()
                      .newInstance();

            isValid = connectionValidation.isConnectionValid(connection);
        } catch (Exception e) {
            _logger.log(INFO, "jdbc.exc_custom_validation", validationClassName);
            throw new ResourceException(e);
        }

        if (!isValid) {
            _logger.log(INFO, "jdbc.exc_custom_validation", validationClassName);
            throw new ResourceException("Custom validation detected invalid connection");
        }
    }

    /**
     * Checks if a <code>java.sql.Connection</code> is valid or not by checking its
     * auto commit property.
     *
     * @param connection <code>java.sql.Connection</code> to be validated
     * @throws ResourceException if the connection is not valid
     */
    protected void isValidByAutoCommit(Connection connection) throws ResourceException {
        if (connection == null) {
            throw new ResourceException("The connection is not valid as " + "the connection is null");
        }

        try {
            // Notice that using something like
            // dbCon.setAutoCommit(dbCon.getAutoCommit()) will cause problems with
            // some drivers like sybase
            // We do not validate connections that are already enlisted
            // in a transaction
            // We cycle autocommit to true and false to by-pass drivers that
            // might cache the call to set autocomitt
            // Also notice that some XA data sources will throw and exception if
            // you try to call setAutoCommit, for them this method is not recommended

            boolean autoCommit = connection.getAutoCommit();
            if (autoCommit) {
                connection.setAutoCommit(false);
            } else {
                connection.rollback(); // prevents uncompleted transaction exceptions
                connection.setAutoCommit(true);
            }

            connection.setAutoCommit(autoCommit);

        } catch (Exception sqle) {
            _logger.log(INFO, "jdbc.exc_autocommit_validation");
            throw new ResourceException(sqle);
        }
    }

    /**
     * Checks if a <code>java.sql.Connection</code> is valid or not by checking its
     * meta data.
     *
     * @param connection <code>java.sql.Connection</code> to be validated
     * @throws ResourceException if the connection is not valid
     */
    protected void isValidByMetaData(Connection connection) throws ResourceException {
        if (connection == null) {
            throw new ResourceException("The connection is not valid as the connection is null");
        }

        try {
            connection.getMetaData();
        } catch (Exception sqle) {
            _logger.log(INFO, "jdbc.exc_metadata_validation");
            throw new ResourceException(sqle);
        }
    }

    /**
     * Checks if a <code>java.sql.Connection</code> is valid or not by querying a
     * table.
     *
     * @param connection <code>java.sql.Connection</code> to be validated
     * @param tableName table which should be queried
     * @throws ResourceException if the connection is not valid
     */
    protected void isValidByTableQuery(Connection connection, String tableName) throws ResourceException {
        if (connection == null) {
            throw new ResourceException("The connection is not valid as the connection is null");
        }

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM " + tableName);
            resultSet = preparedStatement.executeQuery();
        } catch (Exception sqle) {
            _logger.log(INFO, "jdbc.exc_table_validation", tableName);
            throw new ResourceException(sqle);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (Exception e1) {
            }

            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (Exception e2) {
            }
        }
    }

    /**
     * Sets the isolation level specified in the <code>ConnectionRequestInfo</code>
     * for the <code>ManagedConnection</code> passed.
     *
     * @param managedConnectionImpl <code>ManagedConnection</code>
     * @throws ResourceException if the isolation property is invalid or if the
     * isolation cannot be set over the connection
     */
    protected void setIsolation(ManagedConnectionImpl managedConnectionImpl) throws ResourceException {
        Connection connection = managedConnectionImpl.getActualConnection();
        if (connection == null) {
            return;
        }

        String tranIsolation = spec.getDetail(TRANSACTIONISOLATION);
        if (tranIsolation != null && !tranIsolation.isEmpty()) {
            int tranIsolationInt = getTransactionIsolationInt(tranIsolation);
            try {
                connection.setTransactionIsolation(tranIsolationInt);
                managedConnectionImpl.setLastTransactionIsolationLevel(tranIsolationInt);
            } catch (SQLException sqle) {
                throw new ResourceException("The transaction isolation could not be set!", sqle);
            }
        }
    }

    /**
     * Resets the isolation level for the <code>ManagedConnection</code> passed. If
     * the transaction level is to be guaranteed to be the same as the one present
     * when this <code>ManagedConnection</code> was created, as specified by the
     * <code>ConnectionRequestInfo</code> passed, it sets the transaction isolation
     * level from the <code>ConnectionRequestInfo</code> passed. Else, it sets it to
     * the transaction isolation passed.
     *
     * @param managedConnectionImpl <code>ManagedConnection</code>
     * @param tranIsol int
     * @throws ResourceException if the isolation property is invalid or if the
     * isolation cannot be set over the connection
     */
    void resetIsolation(ManagedConnectionImpl managedConnectionImpl, int tranIsol) throws ResourceException {
        Connection connection = managedConnectionImpl.getActualConnection();
        if (connection == null) {
            return;
        }

        String transactionIsolation = spec.getDetail(TRANSACTIONISOLATION);
        if (transactionIsolation != null && !transactionIsolation.isEmpty()) {
            String guaranteeIsolationLevel = spec.getDetail(GUARANTEEISOLATIONLEVEL);
            if (guaranteeIsolationLevel != null && !guaranteeIsolationLevel.isEmpty()) {
                boolean guarantee = Boolean.parseBoolean(guaranteeIsolationLevel);
                if (guarantee) {
                    int tranIsolationInt = getTransactionIsolationInt(transactionIsolation);
                    try {
                        if (tranIsolationInt != connection.getTransactionIsolation()) {
                            connection.setTransactionIsolation(tranIsolationInt);
                        }
                    } catch (SQLException sqle) {
                        throw new ResourceException("The isolation level could not be set!", sqle);
                    }
                } else {
                    try {
                        if (tranIsol != connection.getTransactionIsolation()) {
                            connection.setTransactionIsolation(tranIsol);
                        }
                    } catch (SQLException sqle) {
                        throw new ResourceException("The isolation level could not be set!", sqle);
                    }
                }
            }
        }
    }

    private void detectSqlTraceListeners() {
        // Check for sql-trace-listeners attribute.
        String sqlTraceListeners = getSqlTraceListeners();
        String delimiter = ",";

        if (sqlTraceListeners != null && !sqlTraceListeners.equals("null")) {
            sqlTraceDelegator = new SQLTraceDelegator(getPoolName(), invocationManager);
            StringTokenizer st = new StringTokenizer(sqlTraceListeners, delimiter);

            while (st.hasMoreTokens()) {
                String sqlTraceListener = st.nextToken().trim();
                if (!sqlTraceListener.equals("")) {
                    Class listenerClass = null;
                    SQLTraceListener listener = null;
                    Constructor[] constructors = null;
                    Class[] parameterTypes = null;
                    Object[] initargs = null;
                    // Load the listener class
                    try {
                        listenerClass = Thread.currentThread().getContextClassLoader().loadClass(sqlTraceListener);
                    } catch (ClassNotFoundException e) {
                        _logger.log(SEVERE, "Failed to load class: " + sqlTraceListener, e);
                    }
                    Class intf[] = listenerClass.getInterfaces();
                    for (Class interfaceName : intf) {
                        if (interfaceName.getName().equals("org.glassfish.api.jdbc.SQLTraceListener")) {

                            try {

                                constructors = listenerClass.getConstructors();
                                for (Constructor constructor : constructors) {
                                    parameterTypes = constructor.getParameterTypes();
                                    // For now only the no argument constructors are allowed.
                                    // TODO should this be documented?
                                    if (parameterTypes.length == 0) {
                                        listener = (SQLTraceListener) constructor.newInstance(initargs);
                                    }
                                }
                            } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException ex) {
                                _logger.log(SEVERE, "jdbc.sql_trace_listener_exception", ex);
                            }
                            sqlTraceDelegator.registerSQLTraceListener(listener);
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the integer equivalent of the string specifying the transaction
     * isolation.
     *
     * @param tranIsolation string specifying the isolation level
     * @return tranIsolationInt the <code>java.sql.Connection</code> constant for
     * the string specifying the isolation.
     */
    private int getTransactionIsolationInt(String tranIsolation) throws ResourceException {
        try {
            return TxIsolationLevel.byName(tranIsolation).getId();
        } catch (IllegalArgumentException e) {
            throw new ResourceException(e.getMessage(), e);
        }
    }

    /**
     * Common operation performed by all the child MCFs before returning a created mc
     */
    protected void validateAndSetIsolation(ManagedConnectionImpl managedConnectionImpl) throws ResourceException {
        try {
            isValid(managedConnectionImpl);
            setIsolation(managedConnectionImpl);
        } catch (ResourceException e) {
            if (managedConnectionImpl != null) {
                try {
                    managedConnectionImpl.destroy();
                } catch (ResourceException e1) {
                    _logger.log(Level.WARNING, "jdbc.exc_destroy", e1);
                }
            }

            throw new ResourceAllocationException(localStrings.getString("jdbc.exc_destroy", e.getMessage()), e);
        }
    }

    private void detectStatementCachingSupport() {
        String cacheSize = getStatementCacheSize();
        if (cacheSize != null) {
            try {
                statementCacheSize = Integer.parseInt(cacheSize);
                // TODO-SC FINE log-level with Pool Name (if possible)
                if (_logger.isLoggable(FINE)) {
                    _logger.log(FINE, "StatementCaching Size : " + statementCacheSize);
                }
            } catch (NumberFormatException nfe) {
                if (_logger.isLoggable(FINE)) {
                    _logger.fine("Exception while setting StatementCacheSize : " + nfe.getMessage());
                }
                // ignore
            }
        }
    }

    /**
     * Set the log writer for this <code>ManagedConnectionFactoryImpl</code>
     * instance.
     *
     * @param out <code>PrintWriter</code> passed by the application server
     * @see <code>getLogWriter</code>
     */
    @Override
    public void setLogWriter(java.io.PrintWriter out) {
        logWriter = out;
    }

    /**
     * Set the associated <code>ResourceAdapterImpl</code> JavaBean.
     *
     * @param ra <code>ResourceAdapterImpl</code> associated with this
     * <code>ManagedConnectionFactoryImpl</code> instance
     * @see <code>getResourceAdapter</code>
     */
    @Override
    public void setResourceAdapter(jakarta.resource.spi.ResourceAdapter ra) {
        this.resourceAdapter = ra;
    }

    /**
     * Sets the user name
     *
     * @param user <code>String</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "APP")
    public void setUser(String user) {
        spec.setDetail(DataSourceSpec.USERNAME, user);
    }

    /**
     * Gets the user name
     *
     * @return user
     */
    public String getUser() {
        return spec.getDetail(DataSourceSpec.USERNAME);
    }

    /**
     * Sets the password
     *
     * @param passwd <code>String</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "APP")
    public void setPassword(String passwd) {
        spec.setDetail(DataSourceSpec.PASSWORD, passwd);
    }

    /**
     * Gets the password
     *
     * @return passwd
     */
    public String getPassword() {
        return spec.getDetail(DataSourceSpec.PASSWORD);
    }

    /**
     * Sets the class name of the data source
     *
     * @param className <code>String</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "org.apache.derby.jdbc.ClientConnectionPoolDataSource")
    public void setClassName(String className) {
        spec.setDetail(DataSourceSpec.CLASSNAME, className);
    }

    /**
     * Gets the class name of the data source
     *
     * @return className
     */
    public String getClassName() {
        return spec.getDetail(DataSourceSpec.CLASSNAME);
    }

    /**
     * Sets if connection validation is required or not
     *
     * @param conVldReq <code>String</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "false")
    public void setConnectionValidationRequired(String conVldReq) {
        spec.setDetail(DataSourceSpec.CONNECTIONVALIDATIONREQUIRED, conVldReq);
    }

    /**
     * Returns if connection validation is required or not
     *
     * @return connection validation requirement
     */
    public String getConnectionValidationRequired() {
        return spec.getDetail(DataSourceSpec.CONNECTIONVALIDATIONREQUIRED);
    }

    /**
     * Sets the validation method required
     *
     * @param validationMethod <code>String</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "")
    public void setValidationMethod(String validationMethod) {
        spec.setDetail(VALIDATIONMETHOD, validationMethod);
    }

    /**
     * Returns the connection validation method type
     *
     * @return validation method
     */
    public String getValidationMethod() {
        return spec.getDetail(VALIDATIONMETHOD);
    }

    /**
     * Sets the table checked for during validation
     *
     * @param table <code>String</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "")
    public void setValidationTableName(String table) {
        spec.setDetail(DataSourceSpec.VALIDATIONTABLENAME, table);
    }

    /**
     * Returns the table checked for during validation
     *
     * @return table
     */
    public String getValidationTableName() {
        return spec.getDetail(DataSourceSpec.VALIDATIONTABLENAME);
    }

    /**
     * Sets the validation class name checked for during validation
     *
     * @param className <code>String</code>
     */
    public void setValidationClassName(String className) {
        try {
            Class validationClass = Thread.currentThread().getContextClassLoader().loadClass(className);
            boolean isAssignable = ConnectionValidation.class.isAssignableFrom(validationClass);
            if (isAssignable) {
                spec.setDetail(VALIDATIONCLASSNAME, className);
            } else {
                // Validation Failed
                _logger.log(SEVERE, "jdbc.set_custom_validation_class_name_failure", className);
                throw new ResourceException("The Custom validation class name is "
                        + "not valid as it does not implement " + ConnectionValidation.class.getName());
            }
        } catch (ResourceException ex) {
            _logger.log(SEVERE, "jdbc.set_custom_validation_class_name_failure", ex.getMessage());
        } catch (ClassNotFoundException ex) {
            _logger.log(SEVERE, "jdbc.set_custom_validation_class_name_failure", ex.getMessage());
        }
    }

    /**
     * Returns the validation class name checked for during validation
     *
     * @return table
     */
    public String getValidationClassName() {
        return spec.getDetail(VALIDATIONCLASSNAME);
    }

    /**
     * Sets the transaction isolation level
     *
     * @param trnIsolation <code>String</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "")
    public void setTransactionIsolation(String trnIsolation) {
        spec.setDetail(TRANSACTIONISOLATION, trnIsolation);
    }

    /**
     * Returns the transaction isolation level
     *
     * @return transaction isolation level
     */
    public String getTransactionIsolation() {
        return spec.getDetail(TRANSACTIONISOLATION);
    }

    /**
     * Sets if the transaction isolation level is to be guaranteed
     *
     * @param guaranteeIsolation <code>String</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "")
    public void setGuaranteeIsolationLevel(String guaranteeIsolation) {
        spec.setDetail(GUARANTEEISOLATIONLEVEL, guaranteeIsolation);
    }

    /**
     * Returns the transaction isolation level
     *
     * @return isolation level guarantee
     */
    public String getGuaranteeIsolationLevel() {
        return spec.getDetail(GUARANTEEISOLATIONLEVEL);
    }

    protected boolean isEqual(PasswordCredential pc, String user, String password) {

        // if equal get direct connection else
        // get connection with user and password.

        String thisUser = (pc == null) ? null : pc.getUserName();
        char[] passwordArray = (pc == null) ? null : pc.getPassword();
        char[] tmpPasswordArray = (password == null) ? null : password.toCharArray();

        return (isStringEqual(thisUser, user) && Arrays.equals(passwordArray, tmpPasswordArray));

    }

    private boolean isStringEqual(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equals(str2);
    }

    /**
     * Sets the server name.
     *
     * @param serverName <code>String</code>
     * @see <code>getServerName</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "localhost")
    public void setServerName(String serverName) {
        spec.setDetail(DataSourceSpec.SERVERNAME, serverName);
    }

    /**
     * Gets the server name.
     *
     * @return serverName
     * @see <code>setServerName</code>
     */
    public String getServerName() {
        return spec.getDetail(DataSourceSpec.SERVERNAME);
    }

    /**
     * Sets the port number.
     *
     * @param portNumber <code>String</code>
     * @see <code>getPortNumber</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "1527")
    public void setPortNumber(String portNumber) {
        spec.setDetail(DataSourceSpec.PORTNUMBER, portNumber);
    }

    /**
     * Gets the port number.
     *
     * @return portNumber
     * @see <code>setPortNumber</code>
     */
    public String getPortNumber() {
        return spec.getDetail(DataSourceSpec.PORTNUMBER);
    }

    public void setJdbc30DataSource(String booleanValue) {
        spec.setDetail(DataSourceSpec.JDBC30DATASOURCE, booleanValue);
    }

    public String getJdbc30DataSource() {
        return spec.getDetail(DataSourceSpec.JDBC30DATASOURCE);
    }

    /**
     * Sets the database name.
     *
     * @param databaseName <code>String</code>
     * @see <code>getDatabaseName</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "testdb")
    public void setDatabaseName(String databaseName) {
        spec.setDetail(DataSourceSpec.DATABASENAME, databaseName);
    }

    /**
     * Gets the database name.
     *
     * @return databaseName
     * @see <code>setDatabaseName</code>
     */
    public String getDatabaseName() {
        return spec.getDetail(DataSourceSpec.DATABASENAME);
    }

    /**
     * Sets the data source name.
     *
     * @param dsn <code>String</code>
     * @see <code>getDataSourceName</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "")
    public void setDataSourceName(String dsn) {
        spec.setDetail(DataSourceSpec.DATASOURCENAME, dsn);
    }

    /**
     * Gets the data source name.
     *
     * @return dsn
     * @see <code>setDataSourceName</code>
     */
    public String getDataSourceName() {
        return spec.getDetail(DataSourceSpec.DATASOURCENAME);
    }

    /**
     * Set Statement Wrapping value
     *
     * @param wrapping <code>String</code>
     * @see <code> getStatementWrapping </code>
     */
    public void setStatementWrapping(String wrapping) {
        spec.setDetail(DataSourceSpec.STATEMENTWRAPPING, wrapping);
        computeStatementWrappingStatus();
    }

    /**
     * Gets the statement wrapping value
     *
     * @return String representing "true" or "false"
     * @see <code>setStatementWrapping</code>
     */
    public String getStatementWrapping() {
        return spec.getDetail(DataSourceSpec.STATEMENTWRAPPING);
    }

    public void setStatementCacheSize(String value) {
        spec.setDetail(DataSourceSpec.STATEMENTCACHESIZE, value);
        detectStatementCachingSupport();
    }

    public String getStatementCacheSize() {
        return spec.getDetail(DataSourceSpec.STATEMENTCACHESIZE);
    }

    public void setStatementLeakTimeoutInSeconds(String value) {
        spec.setDetail(DataSourceSpec.STATEMENTLEAKTIMEOUTINSECONDS, value);
        detectStatementLeakSupport();
    }

    public String getStatementLeakTimeoutInSeconds() {
        return spec.getDetail(DataSourceSpec.STATEMENTLEAKTIMEOUTINSECONDS);
    }

    public void setStatementLeakReclaim(String value) {
        spec.setDetail(DataSourceSpec.STATEMENTLEAKRECLAIM, value);
    }

    public String getStatementLeakReclaim() {
        return spec.getDetail(DataSourceSpec.STATEMENTLEAKRECLAIM);
    }

    public void setPoolMonitoringSubTreeRoot(String value) {
        spec.setDetail(DataSourceSpec.POOLMONITORINGSUBTREEROOT, value);
    }

    public String getPoolMonitoringSubTreeRoot() {
        return spec.getDetail(DataSourceSpec.POOLMONITORINGSUBTREEROOT);
    }

    public String getApplicationName() {
        return spec.getDetail(DataSourceSpec.APPLICATIONNAME);
    }

    public void setApplicationName(String value) {
        spec.setDetail(DataSourceSpec.APPLICATIONNAME, value);
    }

    public String getModuleName() {
        return spec.getDetail(DataSourceSpec.MODULENAME);
    }

    public void setModuleName(String value) {
        spec.setDetail(DataSourceSpec.MODULENAME, value);
    }

    public SimpleJndiName getPoolName() {
        return SimpleJndiName.of(spec.getDetail(DataSourceSpec.POOLNAME));
    }

    public void setPoolName(SimpleJndiName value) {
        spec.setDetail(DataSourceSpec.POOLNAME, value == null ? null : value.toString());
    }

    public String getStatementCacheType() {
        return spec.getDetail(DataSourceSpec.STATEMENTCACHETYPE);
    }

    public void setStatementCacheType(String statementCacheType) {
        spec.setDetail(DataSourceSpec.STATEMENTCACHETYPE, statementCacheType);
        this.statementCacheType = getStatementCacheType();
        if (this.statementCacheType == null || this.statementCacheType.trim().equals("")) {
            if (_logger.isLoggable(FINE)) {
                _logger.fine(
                    " Default StatementCaching Type : " +
                    localStrings.getString("jdbc.statement-cache.default.datastructure"));
            }
        } else {
            if (_logger.isLoggable(FINE)) {
                _logger.fine("StatementCaching Type : " + this.statementCacheType);
            }
        }
    }

    public String getNumberOfTopQueriesToReport() {
        return spec.getDetail(DataSourceSpec.NUMBEROFTOPQUERIESTOREPORT);
    }

    public void setNumberOfTopQueriesToReport(String numTopQueriesToReport) {
        spec.setDetail(DataSourceSpec.NUMBEROFTOPQUERIESTOREPORT, numTopQueriesToReport);
    }

    public String getTimeToKeepQueriesInMinutes() {
        return spec.getDetail(DataSourceSpec.TIMETOKEEPQUERIESINMINUTES);
    }

    public void setTimeToKeepQueriesInMinutes(String timeToKeepQueries) {
        spec.setDetail(DataSourceSpec.TIMETOKEEPQUERIESINMINUTES, timeToKeepQueries);
    }

    public String getInitSql() {
        return spec.getDetail(DataSourceSpec.INITSQL);
    }

    public void setInitSql(String initSql) {
        // TODO remove case where "null" is checked. Might be a CLI/GUI bug.
        if (initSql != null && !initSql.equalsIgnoreCase("null") && !initSql.equals("")) {
            spec.setDetail(DataSourceSpec.INITSQL, initSql);
        }
    }

    /**
     * Set StatementTimeout value
     *
     * @param timeout <code>String</code>
     * @see <code> getStatementTimeout </code>
     */
    public void setStatementTimeout(String timeout) {
        spec.setDetail(DataSourceSpec.STATEMENTTIMEOUT, timeout);
    }

    /**
     * Gets the StatementTimeout value
     *
     * @return String representing "true" or "false"
     * @see <code>setStatementTimeout</code>
     */
    public String getStatementTimeout() {
        return spec.getDetail(DataSourceSpec.STATEMENTTIMEOUT);
    }

    public String getSqlTraceListeners() {
        return spec.getDetail(DataSourceSpec.SQLTRACELISTENERS);
    }

    public void setSqlTraceListeners(String sqlTraceListeners) {
        if (sqlTraceListeners != null) {
            spec.setDetail(DataSourceSpec.SQLTRACELISTENERS, sqlTraceListeners);
            detectSqlTraceListeners();
        }
    }

    /**
     * Sets the description.
     *
     * @param desc <code>String</code>
     * @see <code>getDescription</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "Derby driver for datasource")
    public void setDescription(String desc) {
        spec.setDetail(DataSourceSpec.DESCRIPTION, desc);
    }

    /**
     * Gets the description.
     *
     * @return desc
     * @see <code>setDescription</code>
     */
    public String getDescription() {
        return spec.getDetail(DataSourceSpec.DESCRIPTION);
    }

    /**
     * Sets the network protocol.
     *
     * @param nwProtocol <code>String</code>
     * @see <code>getNetworkProtocol</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "")
    public void setNetworkProtocol(String nwProtocol) {
        spec.setDetail(DataSourceSpec.NETWORKPROTOCOL, nwProtocol);
    }

    /**
     * Gets the network protocol.
     *
     * @return nwProtocol
     * @see <code>setNetworkProtocol</code>
     */
    public String getNetworkProtocol() {
        return spec.getDetail(DataSourceSpec.NETWORKPROTOCOL);
    }

    /**
     * Sets the role name.
     *
     * @param roleName <code>String</code>
     * @see <code>getRoleName</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "")
    public void setRoleName(String roleName) {
        spec.setDetail(DataSourceSpec.ROLENAME, roleName);
    }

    /**
     * Gets the role name.
     *
     * @return roleName
     * @see <code>setRoleName</code>
     */
    public String getRoleName() {
        return spec.getDetail(DataSourceSpec.ROLENAME);
    }

    /**
     * Sets the login timeout.
     *
     * @param loginTimeOut <code>String</code>
     * @see <code>getLoginTimeOut</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "0")
    public void setLoginTimeOut(String loginTimeOut) {
        spec.setDetail(DataSourceSpec.LOGINTIMEOUT, loginTimeOut);
    }

    /**
     * Gets the login timeout.
     *
     * @return loginTimeout
     * @see <code>setLoginTimeOut</code>
     */
    public String getLoginTimeOut() {
        return spec.getDetail(DataSourceSpec.LOGINTIMEOUT);
    }

    /**
     * Sets the delimiter.
     *
     * @param delim <code>String</code>
     * @see <code>getDelimiter</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "#")
    public void setDelimiter(String delim) {
        spec.setDetail(DataSourceSpec.DELIMITER, delim);
    }

    /**
     * Gets the delimiter.
     *
     * @return delim
     * @see <code>setDelimiter</code>
     */
    public String getDelimiter() {
        return spec.getDetail(DataSourceSpec.DELIMITER);
    }

    public void setEscapeCharacter(String escapeCharacter) {
        spec.setDetail(DataSourceSpec.ESCAPECHARACTER, escapeCharacter);
    }

    public String getEscapeCharacter() {
        return spec.getDetail(DataSourceSpec.ESCAPECHARACTER);
    }

    /**
     * Sets the driver specific properties.
     *
     * @param driverProps <code>String</code>
     * @see <code>getDriverProperties</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "")
    public void setDriverProperties(String driverProps) {
        spec.setDetail(DataSourceSpec.DRIVERPROPERTIES, driverProps);
    }

    /**
     * Gets the driver specific properties.
     *
     * @return driverProps
     * @see <code>setDriverProperties</code>
     */
    public String getDriverProperties() {
        return spec.getDetail(DataSourceSpec.DRIVERPROPERTIES);
    }

    protected PoolInfo getPoolInfo() {
        return new PoolInfo(getPoolName(), getApplicationName(), getModuleName());
    }

    protected ManagedConnectionImpl constructManagedConnection(PooledConnection pc, Connection sqlCon,
            PasswordCredential passCred, ManagedConnectionFactoryImpl mcf) throws ResourceException {
        return new ManagedConnectionImpl(pc, sqlCon, passCred, mcf, getPoolInfo(), statementCacheSize,
                statementCacheType, sqlTraceDelegator, statementLeakTimeout, statementLeakReclaim);
    }

    /**
     * Returns the underlying datasource
     *
     * @return DataSource of jdbc vendor
     * @throws ResourceException
     */
    public Object getDataSource() throws ResourceException {
        if (dataSourceObjectBuilder == null) {
            dataSourceObjectBuilder = new DataSourceObjectBuilder(spec);
        }
        return dataSourceObjectBuilder.constructDataSourceObject();
    }

    protected static final int JVM_OPTION_STATEMENT_WRAPPING_ON = 1;
    protected static final int JVM_OPTION_STATEMENT_WRAPPING_OFF = 0;
    protected static final int JVM_OPTION_STATEMENT_WRAPPING_NOT_SET = -1;

    /**
     * This wrapStatement flag is used to enable disable wrapping the statement
     * objects. This can be enabled by passing jvm option,
     * com.sun.appserv.jdbc.wrapstatement = true. This can be disabled by removing
     * this option or by passing non true value. By default this will be disabled
     */
    private static int wrapStatement = JVM_OPTION_STATEMENT_WRAPPING_NOT_SET;

    static {
        wrapStatement = getStatementWrappingJVMOption();
    }

    /**
     * Gets the Statement Wrapping JVM Option (available in 8.2)<br>
     * Which will be deprecated in future versions.
     *
     * @return int representing the JVM Option value of statement wrapping.
     */
    private static int getStatementWrappingJVMOption() {
        int result = JVM_OPTION_STATEMENT_WRAPPING_NOT_SET;
        String str = System.getProperty("com.sun.appserv.jdbc.wrapJdbcObjects");
        if ("true".equalsIgnoreCase(str)) {
            result = JVM_OPTION_STATEMENT_WRAPPING_ON;
        } else if ("false".equalsIgnoreCase(str)) {
            result = JVM_OPTION_STATEMENT_WRAPPING_OFF;
        }
        return result;
    }

    /**
     * 9.1 has attribute "wrap-statements" which will be overridden when JVM option
     * is specified as "true" or "false" JVM Option will be deprecated in future
     * versions.
     */
    protected void computeStatementWrappingStatus() {

        boolean poolProperty = false;
        String statementWrappingString = getStatementWrapping();
        if (statementWrappingString != null) {
            poolProperty = Boolean.parseBoolean(statementWrappingString);
        }

        if (wrapStatement == JVM_OPTION_STATEMENT_WRAPPING_ON
                || (wrapStatement == JVM_OPTION_STATEMENT_WRAPPING_NOT_SET && poolProperty)) {
            statementWrapping = true;
        } else {
            statementWrapping = false;
        }
    }

    /**
     * Returns whether statement wrapping is enabled or not.<br>
     *
     * @return boolean representing statementwrapping status
     */
    public boolean isStatementWrappingEnabled() {
        return statementWrapping;
    }

    public JdbcObjectsFactory getJdbcObjectsFactory() {
        return jdbcObjectsFactory;
    }

    protected void logFine(String logMessage) {
        _logger.log(FINE, logMessage);
    }

    @Override
    public void mcfCreated() {
        String poolMonitoringSubTreeRoot = getPoolMonitoringSubTreeRoot();
        String sqlTraceListeners = getSqlTraceListeners();

        // Default values used in case sql tracing is OFF
        int sqlTraceCacheSize = 0;
        long timeToKeepQueries = 0;
        if (sqlTraceListeners != null && !sqlTraceListeners.equals("null")) {
            if (getNumberOfTopQueriesToReport() != null && !getNumberOfTopQueriesToReport().equals("null")) {
                // Some value is set for this property
                sqlTraceCacheSize = Integer.parseInt(getNumberOfTopQueriesToReport());
            } else {
                // No property by this name. default to 10 queries
                sqlTraceCacheSize = 10;
            }
            if (getTimeToKeepQueriesInMinutes() != null && !getTimeToKeepQueriesInMinutes().equals("null")) {
                // Time-To-Keep-Queries property has been set
                timeToKeepQueries = Integer.parseInt(getTimeToKeepQueriesInMinutes());
            } else {
                // Default to 5 minutes after which cache is pruned.
                timeToKeepQueries = 5;
                // Time to keep queries is set to 5 minutes because the timer
                // task will keep running till mcf is destroyed even if
                // monitoring is turned OFF.
            }
        }

        _logger.finest("MCF Created");

        if (statementCacheSize > 0 || (sqlTraceListeners != null && !sqlTraceListeners.equals("null"))
                || statementLeakTimeout > 0) {
            jdbcStatsProvider = new JdbcStatsProvider(getPoolName(), getApplicationName(), getModuleName(),
                    sqlTraceCacheSize, timeToKeepQueries);

            // Get the poolname and use it to initialize the stats provider n register
            StatsProviderManager.register("jdbc-connection-pool", PluginPoint.SERVER, poolMonitoringSubTreeRoot,
                    jdbcStatsProvider);

            if (jdbcStatsProvider.getSqlTraceCache() != null) {
                _logger.finest("Scheduling timer task for sql trace caching");
                Timer timer = ((ResourceAdapterImpl) resourceAdapter).getTimer();
                jdbcStatsProvider.getSqlTraceCache().scheduleTimerTask(timer);
            }

            _logger.finest("Registered JDBCRA Stats Provider");
        }
    }

    @Override
    public void mcfDestroyed() {
        _logger.finest("MCF Destroyed");

        if (jdbcStatsProvider != null) {
            if (jdbcStatsProvider.getSqlTraceCache() != null) {
                _logger.finest("Canceling timer task for sql trace caching");
                jdbcStatsProvider.getSqlTraceCache().cancelTimerTask();
            }

            StatsProviderManager.unregister(jdbcStatsProvider);
            jdbcStatsProvider = null;
            _logger.finest("Unregistered JDBCRA Stats Provider");
        }
    }

    private void detectStatementLeakSupport() {
        String stmtLeakTimeout = getStatementLeakTimeoutInSeconds();
        String stmtLeakReclaim = getStatementLeakReclaim();
        if (stmtLeakTimeout != null) {
            statementLeakTimeout = Integer.parseInt(stmtLeakTimeout) * 1000L;
            statementLeakReclaim = Boolean.parseBoolean(stmtLeakReclaim);
            if (_logger.isLoggable(FINE)) {
                _logger.log(FINE, "StatementLeakTimeout in seconds: " + statementLeakTimeout
                        + " & StatementLeakReclaim: " + statementLeakReclaim + " for pool : " + getPoolInfo());
            }
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        resourceAdapter = ResourceAdapterImpl.getInstance();
    }
}
