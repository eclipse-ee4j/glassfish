/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
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

import com.sun.gjc.common.DataSourceObjectBuilder;
import com.sun.gjc.spi.base.AbstractDataSource;
import com.sun.gjc.spi.base.ConnectionHolder;
import com.sun.logging.LogDomains;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConfigProperty;
import jakarta.resource.spi.ConnectionDefinition;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ResourceAllocationException;
import jakarta.resource.spi.security.PasswordCredential;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.sql.DataSource;

import static com.sun.gjc.common.DataSourceSpec.CLASSNAME;
import static com.sun.gjc.common.DataSourceSpec.LOGINTIMEOUT;
import static com.sun.gjc.common.DataSourceSpec.PASSWORD;
import static com.sun.gjc.common.DataSourceSpec.URL;
import static com.sun.gjc.common.DataSourceSpec.USERNAME;
import static com.sun.gjc.util.SecurityUtils.getPasswordCredential;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;

/**
 * Driver Manager <code>ManagedConnectionFactory</code> implementation for
 * Generic JDBC Connector.
 *
 * @author Evani Sai Surya Kiran
 * @version 1.0, 02/07/31
 */
@ConnectionDefinition(
    connectionFactory = DataSource.class,
    connectionFactoryImpl = AbstractDataSource.class,
    connection = Connection.class,
    connectionImpl = ConnectionHolder.class)
public class DMManagedConnectionFactory extends ManagedConnectionFactoryImpl {

    private static Logger _logger = LogDomains.getLogger(DMManagedConnectionFactory.class, LogDomains.RSR_LOGGER);
    private boolean debug = _logger.isLoggable(FINE);

    Properties props;

    /**
     * Creates a new physical connection to the underlying EIS resource manager.
     *
     * @param subject <code>Subject</code> instance passed by the application server
     * @param cxRequestInfo <code>ConnectionRequestInfo</code> which may be created
     * as a result of the invocation <code>getConnection(user, password)</code> on
     * the <code>DataSource</code> object
     *
     * @return <code>ManagedConnection</code> object created
     *
     * @throws ResourceException if there is an error in instantiating the
     * <code>DataSource</code> object used for the creation of the
     * <code>ManagedConnection</code> object
     * @throws SecurityException if there ino <code>PasswordCredential</code> object
     * satisfying this request
     */
    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        logFine("In createManagedConnection");

        if (dataSourceObjectBuilder == null) {
            dataSourceObjectBuilder = new DataSourceObjectBuilder(spec);
        }

        PasswordCredential passwordCredential = getPasswordCredential(this, subject, cxRequestInfo);

        try {
            Class.forName(spec.getDetail(CLASSNAME));
        } catch (ClassNotFoundException cnfe) {
            _logger.log(SEVERE, "jdbc.exc_cnfe", cnfe);
            throw new ResourceException("The driver could not be loaded: " + spec.getDetail(CLASSNAME));
        }

        Connection connection = null;
        ManagedConnectionImpl managedConnectionImpl = null;

        Properties driverProps = new Properties();

        // Will return a set of properties that would have setURL and <url> as objects
        // Get a set of normal case properties
        Map<String, List<String>> properties = dataSourceObjectBuilder.parseDriverProperties(spec, false);
        for (Map.Entry<String, List<String>> entry : properties.entrySet()) {
            String value = "";
            List<String> values = entry.getValue();

            if (!values.isEmpty() && values.size() == 1) {
                value = values.get(0);
            } else if (values.size() > 1) {
                logFine("More than one value for key : " + entry.getKey());
            }

            String parsedKey = getParsedKey(entry.getKey());
            driverProps.put(parsedKey, value);
            if (parsedKey.equalsIgnoreCase("URL")) {
                if (spec.getDetail(URL) == null) {
                    setConnectionURL(value);
                }
            }
        }

        try {
            if (cxRequestInfo != null) {
                driverProps.setProperty("user", passwordCredential.getUserName());
                driverProps.setProperty("password", new String(passwordCredential.getPassword()));
            } else {
                String user = spec.getDetail(USERNAME);
                String password = spec.getDetail(PASSWORD);
                if (user != null) {
                    driverProps.setProperty("user", user);
                }
                if (password != null) {
                    driverProps.setProperty("password", password);
                }
            }

            connection = DriverManager.getConnection(spec.getDetail(URL), driverProps);

        } catch (SQLException sqle) {
            _logger.log(SEVERE, "jdbc.exc_create_mc", sqle);
            throw new ResourceAllocationException("The connection could not be allocated: " + sqle.getMessage());
        }

        try {
            managedConnectionImpl = constructManagedConnection(null, connection, passwordCredential, this);
            validateAndSetIsolation(managedConnectionImpl);
        } finally {
            if (managedConnectionImpl == null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    _logger.log(FINEST, "Exception while closing connection : createManagedConnection" + connection, e);
                }
            }
        }

        return managedConnectionImpl;
    }

    /**
     * Parses the key and removes the "set" string at the beginning of the property.
     *
     * @param key
     * @return
     */
    private String getParsedKey(String key) throws ResourceException {
        String parsedKey = null;
        int indexOfSet = -1;
        try {
            indexOfSet = key.indexOf("set");
        } catch (NullPointerException npe) {
            if (debug) {
                _logger.log(FINE, "jdbc.exc_caught_ign", npe.getMessage());
            }

        }
        if (indexOfSet == 0) {
            // Find the key String
            try {
                parsedKey = key.substring(indexOfSet + 3, key.length()).trim();
            } catch (IndexOutOfBoundsException iobe) {
                if (debug) {
                    _logger.log(FINE, "jdbc.exc_caught_ign", iobe.getMessage());
                }
            }
            if (parsedKey != null && parsedKey.equals("")) {
                throw new ResourceException("Invalid driver properties string - " + "Key cannot be an empty string");
            }
        }

        return parsedKey;
    }

    /**
     * Sets the login timeout.
     *
     * @param loginTimeOut <code>String</code>
     * @see <code>getLoginTimeOut</code>
     */
    @Override
    public void setLoginTimeOut(String loginTimeOut) {
        try {
            DriverManager.setLoginTimeout(Integer.parseInt(loginTimeOut));
            spec.setDetail(LOGINTIMEOUT, loginTimeOut);
        } catch (Exception e) {
            if (debug) {
                _logger.log(FINE, "jdbc.exc_caught_ign", e.getMessage());
            }
        }
    }

    /**
     * Sets the class name of the driver
     *
     * @param className <code>String</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "org.apache.derby.jdbc.ClientDriver")
    @Override
    public void setClassName(String className) {
        spec.setDetail(CLASSNAME, className);
    }

    public void setURL(String url) {
        spec.setDetail(URL, url);
    }

    public String getURL() {
        return spec.getDetail(URL);
    }

    /**
     * Sets the connection url.
     *
     * @param url <code>String</code>
     * @see <code>getConnectionURL</code>
     */
    public void setConnectionURL(String url) {
        spec.setDetail(URL, url);
    }

    /**
     * Gets the connection url.
     *
     * @return url
     * @see <code>setConnectionURL</code>
     */
    public String getConnectionURL() {
        return spec.getDetail(URL);
    }

    @Override
    public Object getDataSource() throws ResourceException {
        return null;
    }

    /**
     * Check if this <code>ManagedConnectionFactory</code> is equal to another
     * <code>ManagedConnectionFactory</code>.
     *
     * @param other <code>ManagedConnectionFactory</code> object for checking
     * equality with
     * @return true if the property sets of both the
     * <code>ManagedConnectionFactory</code> objects are the same false otherwise
     */
    @Override
    public boolean equals(Object other) {
        logFine("In equals");

        /**
         * The check below means that two ManagedConnectionFactory objects are equal if
         * and only if their properties are the same.
         */
        if (other instanceof DMManagedConnectionFactory) {
            DMManagedConnectionFactory otherMCF = (DMManagedConnectionFactory) other;
            return this.spec.equals(otherMCF.spec);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return 31 * 7 + (spec.hashCode());
    }
}
