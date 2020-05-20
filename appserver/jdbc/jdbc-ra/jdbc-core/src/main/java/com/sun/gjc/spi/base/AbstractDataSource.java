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

package com.sun.gjc.spi.base;

import com.sun.appserv.connectors.internal.spi.BadConnectionEventListener;
import com.sun.gjc.spi.ConnectionManagerImplementation;
import com.sun.gjc.spi.ConnectionRequestInfoImpl;
import com.sun.gjc.spi.ManagedConnectionFactoryImpl;
import com.sun.logging.LogDomains;
import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.gjc.util.MethodExecutor;

import javax.naming.Reference;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holds the <code>java.sql.Connection</code> object, which is to be
 * passed to the application program.
 *
 * @author Binod P.G
 * @version 1.0, 02/07/31
 */
public abstract class AbstractDataSource implements javax.sql.DataSource, java.io.Serializable,
        com.sun.appserv.jdbc.DataSource, jakarta.resource.Referenceable {

    protected ManagedConnectionFactoryImpl mcf;
    protected MethodExecutor executor = null;
    private ConnectionManager cm;
    private int loginTimeout;
    private PrintWriter logWriter;
    private String description;
    private Reference reference;

    private ConnectionHolder.ConnectionType conType_;

    protected final static Logger _logger;

    static {
        _logger = LogDomains.getLogger(ManagedConnectionFactoryImpl.class, LogDomains.RSR_LOGGER);
    }

    /**
     * Constructs <code>DataSource</code> object. This is created by the
     * <code>ManagedConnectionFactory</code> object.
     *
     * @param mcf <code>ManagedConnectionFactory</code> object
     *            creating this object.
     * @param cm  <code>ConnectionManager</code> object either associated
     *            with Application server or Resource Adapter.
     */
    public AbstractDataSource(ManagedConnectionFactoryImpl mcf, ConnectionManager cm) {
        this.mcf = mcf;
        executor = new MethodExecutor();
        if (cm == null) {
            this.cm = new ConnectionManagerImplementation();
        } else {
            this.cm = cm;
            conType_ = findConnectionType();
        }
    }

    /**
     * Retrieves the <code> Connection </code> object.
     *
     * @return <code> Connection </code> object.
     * @throws SQLException In case of an error.
     */
    public Connection getConnection() throws SQLException {
        try {
            ConnectionHolder con = (ConnectionHolder)
                    cm.allocateConnection(mcf, null);
            setConnectionType(con);

            return con;
        } catch (ResourceException re) {
            logNonTransientException(re);
            throw new SQLException(re.getMessage(), re);
        }
    }

    /**
     * log the exception if it is a non-transient exception <br>
     * @param re Exception to log
     */
    private void logNonTransientException(ResourceException re) {
        if(!BadConnectionEventListener.POOL_RECONFIGURED_ERROR_CODE.equals(re.getErrorCode())){
            _logger.log(Level.WARNING, "jdbc.exc_get_conn", re.getMessage());
        }
    }

    /**
     * Retrieves the <code> Connection </code> object.
     *
     * @param user User name for the Connection.
     * @param pwd  Password for the Connection.
     * @return <code> Connection </code> object.
     * @throws SQLException In case of an error.
     */
    public Connection getConnection(String user, String pwd) throws SQLException {
        try {
            ConnectionRequestInfoImpl info = new ConnectionRequestInfoImpl(user, pwd.toCharArray());
            ConnectionHolder con = (ConnectionHolder)
                    cm.allocateConnection(mcf, info);
            setConnectionType(con);
            return con;
        } catch (ResourceException re) {
            logNonTransientException(re);
            throw new SQLException(re.getMessage(), re);
        }
    }

    /**
     * Retrieves the actual SQLConnection from the Connection wrapper
     * implementation of SunONE application server. If an actual connection is
     * supplied as argument, then it will be just returned.
     *
     * @param con Connection obtained from <code>Datasource.getConnection()</code>
     * @return <code>java.sql.Connection</code> implementation of the driver.
     * @throws <code>java.sql.SQLException</code>
     *          If connection cannot be obtained.
     */
    public Connection getConnection(Connection con) throws SQLException {

        Connection driverCon = con;
        if (con instanceof com.sun.gjc.spi.base.ConnectionHolder) {
            driverCon = ((com.sun.gjc.spi.base.ConnectionHolder) con).getConnection();
        }

        return driverCon;
    }

    /**
     * Gets a connection that is not in the scope of any transaction. This
     * can be used to save performance overhead incurred on enlisting/delisting
     * each connection got, irrespective of whether its required or not.
     * Note here that this meethod does not fit in the connector contract
     * per se.
     *
     * @return <code>java.sql.Connection</code>
     * @throws <code>java.sql.SQLException</code>
     *          If connection cannot be obtained
     */
    public Connection getNonTxConnection() throws SQLException {
        try {
            ConnectionHolder con = (ConnectionHolder)
                    ((com.sun.appserv.connectors.internal.spi.ConnectionManager)
                            cm).allocateNonTxConnection(mcf, null);
            setConnectionType(con, true);

            return con;
        } catch (ResourceException re) {
            logNonTransientException(re);
            throw new SQLException(re.getMessage(), re);
        }
    }

    /**
     * Gets a connection that is not in the scope of any transaction. This
     * can be used to save performance overhead incurred on enlisting/delisting
     * each connection got, irrespective of whether its required or not.
     * Note here that this meethod does not fit in the connector contract
     * per se.
     *
     * @param user     User name for authenticating the connection
     * @param password Password for authenticating the connection
     * @return <code>java.sql.Connection</code>
     * @throws <code>java.sql.SQLException</code>
     *          If connection cannot be obtained
     */
    public Connection getNonTxConnection(String user, String password) throws SQLException {
        try {
            ConnectionRequestInfoImpl cxReqInfo = new ConnectionRequestInfoImpl(user, password.toCharArray());
            ConnectionHolder con = (ConnectionHolder)
                    ((com.sun.appserv.connectors.internal.spi.ConnectionManager)
                            cm).allocateNonTxConnection(mcf, cxReqInfo);

            setConnectionType(con, true);

            return con;
        } catch (ResourceException re) {
            logNonTransientException(re);
            throw new SQLException(re.getMessage(), re);
        }
    }

    /**
     * Get the login timeout
     *
     * @return login timeout.
     * @throws SQLException If a database error occurs.
     */
    public int getLoginTimeout() throws SQLException {
        return loginTimeout;
    }

    /**
     * Set the login timeout
     *
     * @param loginTimeout Login timeout.
     * @throws SQLException If a database error occurs.
     */
    public void setLoginTimeout(int loginTimeout) throws SQLException {
        this.loginTimeout = loginTimeout;
    }

    /**
     * Get the logwriter object.
     *
     * @return <code> PrintWriter </code> object.
     * @throws SQLException If a database error occurs.
     */
    public PrintWriter getLogWriter() throws SQLException {
        return logWriter;
    }

    /**
     * Set the logwriter on this object.
     *
     * @param logWriter object.
     * @throws SQLException If a database error occurs.
     */
    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        this.logWriter = logWriter;
    }

    /**
     * Retrieves the description.
     *
     * @return Description about the DataSource.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description.
     *
     * @param description Description about the DataSource.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the reference.
     *
     * @return <code>Reference</code>object.
     */
    public Reference getReference() {
        return reference;
    }

    /**
     * Get the reference.
     *
     * @param reference <code>Reference</code> object.
     */
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    private ConnectionHolder.ConnectionType findConnectionType() {
        ConnectionHolder.ConnectionType cmType = ConnectionHolder.ConnectionType.STANDARD;

        if (cm instanceof jakarta.resource.spi.LazyAssociatableConnectionManager) {
            if (!((com.sun.appserv.connectors.internal.spi.ConnectionManager) cm).
                    getJndiName().endsWith(ConnectorConstants.PM_JNDI_SUFFIX)) {
                cmType = ConnectionHolder.ConnectionType.LAZY_ASSOCIATABLE;
            }
        } else if (cm instanceof
                jakarta.resource.spi.LazyEnlistableConnectionManager) {
            if (!((com.sun.appserv.connectors.internal.spi.ConnectionManager) cm).
                    getJndiName().endsWith(ConnectorConstants.PM_JNDI_SUFFIX) &&
                    !((com.sun.appserv.connectors.internal.spi.ConnectionManager) cm).
                            getJndiName().endsWith(ConnectorConstants.NON_TX_JNDI_SUFFIX)) {
                cmType = ConnectionHolder.ConnectionType.LAZY_ENLISTABLE;
            }
        }

        return cmType;
    }

    private void setConnectionType(ConnectionHolder con) {
        this.setConnectionType(con, false);
    }

    private void setConnectionType(ConnectionHolder con, boolean isNonTx) {
        con.setConnectionType(conType_);
        if (conType_ == ConnectionHolder.ConnectionType.LAZY_ASSOCIATABLE &&
                cm instanceof jakarta.resource.spi.LazyAssociatableConnectionManager) {
            con.setLazyAssociatableConnectionManager(
                    (jakarta.resource.spi.LazyAssociatableConnectionManager) cm);
        } else if (conType_ == ConnectionHolder.ConnectionType.LAZY_ENLISTABLE) {
            if (isNonTx) {
                //if this is a getNonTxConnection call on the DataSource, we
                //should not LazyEnlist
                con.setConnectionType(ConnectionHolder.ConnectionType.STANDARD);
            } else if(cm instanceof jakarta.resource.spi.LazyEnlistableConnectionManager) {
                con.setLazyEnlistableConnectionManager(
                        (jakarta.resource.spi.LazyEnlistableConnectionManager) cm);
            }
        }
    }

    /**
     * API to mark a connection as bad. If the application can determine that the connection
     * is bad, using this api, it can notify the resource-adapter which inturn will notify the
     * connection-pool. Connection-pool will drop and create a new connection.
     * eg:
     * <pre>
        com.sun.appserv.jdbc.DataSource ds=
           (com.sun.appserv.jdbc.DataSource)context.lookup("dataSource");
                Connection con = ds.getConnection();
                Statement stmt = null;
                try{
                         stmt = con.createStatement();
                         stmt.executeUpdate("Update");
                }catch(BadConnectionException e){
                        dataSource.markConnectionAsBad(con) //marking it as bad for removal
                }finally{
                        stmt.close();
                        con.close(); //Connection will be destroyed while close or Tx completion
            }
     * </pre>
     *
     * @param conn <code>java.sql.Connection</code>
     */
    public void markConnectionAsBad(Connection conn) {
        if (conn instanceof ConnectionHolder) {
            ConnectionHolder userConn = ((ConnectionHolder) conn);
            userConn.getManagedConnection().markForRemoval(true);
        }
    }
}
