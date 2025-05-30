/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.jdbcruntime.service;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.service.ConnectorAdminServiceUtils;
import com.sun.enterprise.connectors.service.ConnectorAdminServicesFactory;
import com.sun.enterprise.connectors.service.ConnectorConnectionPoolAdminServiceImpl;
import com.sun.enterprise.connectors.service.ConnectorService;
import com.sun.enterprise.connectors.util.DriverLoader;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.naming.NamingException;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.connectors.util.ConnectionPoolObjectsUtils.getValueFromMCF;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.WARNING;

/**
 * Jdbc admin service performs Jdbc related operations for administration.
 *
 * @author shalini
 */
@Service
@Singleton
public class JdbcAdminServiceImpl extends ConnectorService {

    private static final String JDBC40_CONNECTION_VALIDATION = "org.glassfish.api.jdbc.validation.JDBC40ConnectionValidation";
    private static final JdbcAdminServiceImpl jdbcAdminService = new JdbcAdminServiceImpl();

    private final ConnectorConnectionPoolAdminServiceImpl ccPoolAdmService;

    @Inject
    private DriverLoader driverLoader;

    /**
     * Default constructor
     */
    public JdbcAdminServiceImpl() {
        ccPoolAdmService = (ConnectorConnectionPoolAdminServiceImpl) ConnectorAdminServicesFactory
            .getService(ConnectorConstants.CCP);
    }

    public static JdbcAdminServiceImpl getJdbcAdminService() {
        return jdbcAdminService;
    }

    /**
     * Get Validation class names list for the classname that the jdbc connection
     * pool refers to. This is used for custom connection validation.
     *
     * @param className
     * @return all validation class names.
     */
    public Set<String> getValidationClassNames(String className) {
        SortedSet<String> classNames = new TreeSet<>();
        if (className == null) {
            _logger.log(WARNING, "jdbc.admin.service.ds_class_name_null");
            return classNames;
        }
        String dbVendor = driverLoader.getDatabaseVendorName(className);
        if (dbVendor == null) {
            return classNames;
        }
        // Retrieve validation classnames from the properties file based on the retrieved
        // dbvendor name
        Properties validationClassMappings = driverLoader.getValidationClassMappingFile();
        String validationClassName = validationClassMappings.getProperty(dbVendor);
        if (validationClassName != null) {
            classNames.add(validationClassName);
        }
        // If JDBC40 runtime, add the jdbc40 validation classname
        if (detectJDBC40(className)) {
            classNames.add(JDBC40_CONNECTION_VALIDATION);
        }
        return classNames;
    }

    private boolean detectJDBC40(String className) {
        boolean jdbc40 = true;
        ClassLoader commonClassLoader = ConnectorRuntime.getRuntime().getClassLoaderHierarchy().getCommonClassLoader();
        Class cls = null;
        try {
            cls = commonClassLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            if (_logger.isLoggable(FINEST)) {
                _logger.log(FINEST, "jdbc.admin.service.ex_detect_jdbc40");
            }
            return false;
        }
        Method method;
        try {
            method = cls.getMethod("isWrapperFor", Class.class);
            method.invoke(cls.newInstance(), javax.sql.DataSource.class);
        } catch (NoSuchMethodException e) {
            jdbc40 = false;
            if (_logger.isLoggable(FINEST)) {
                _logger.log(FINEST, "jdbc.admin.service.ex_detect_jdbc40");
            }
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof AbstractMethodError) {
                jdbc40 = false;
            }
        } catch (InstantiationException e) {
            if (_logger.isLoggable(FINEST)) {
                _logger.log(FINEST, "jdbc.admin.service.ex_detect_jdbc40");
            }
            jdbc40 = false;
        } catch (IllegalAccessException e) {
            if (_logger.isLoggable(FINEST)) {
                _logger.log(FINEST, "jdbc.admin.service.ex_detect_jdbc40");
            }
            jdbc40 = false;
        }
        return jdbc40;
    }

    /**
     * Get Validation table names list for the database that the jdbc connection
     * pool refers to. This is used for connection validation.
     *
     * @param poolInfo
     * @return all validation table names.
     * @throws ResourceException
     */
    public Set<String> getValidationTableNames(PoolInfo poolInfo) throws ResourceException {
        ManagedConnectionFactory managedConnectionFactory = null;
        ManagedConnection managedConnection = null;
        Connection connection = null;
        try {
            managedConnection = (ManagedConnection) ccPoolAdmService.getUnpooledConnection(poolInfo, null, false);
            managedConnectionFactory = ccPoolAdmService.obtainManagedConnectionFactory(poolInfo);

            if (managedConnection != null) {
                connection = (Connection) managedConnection.getConnection(null, null);
            }
            return getValidationTableNames(connection, getDefaultDatabaseName(poolInfo, managedConnectionFactory));
        } catch (Exception re) {
            _logger.log(WARNING, "pool.get_validation_table_names_failure", re.getMessage());
            throw new ResourceException(re);
        } finally {
            try {
                if (managedConnection != null) {
                    managedConnection.destroy();
                }
            } catch (Exception ex) {
                _logger.log(FINEST, "pool.get_validation_table_names_mc_destroy", poolInfo);
            }
        }
    }

    /**
     * Returns a databaseName that is populated in pool's default DATABASENAME
     *
     * @param poolInfo
     * @param managedConnectionFactory
     * @return
     * @throws javax.naming.NamingException if poolName lookup fails
     */
    private String getDefaultDatabaseName(PoolInfo poolInfo, ManagedConnectionFactory managedConnectionFactory) throws NamingException {
        // All this to get the default user name and principal
        String databaseName = null;
        ConnectorConnectionPool connectorConnectionPool = null;
        try {
            SimpleJndiName jndiNameForPool = ConnectorAdminServiceUtils.getReservePrefixedJNDINameForPool(poolInfo);
            connectorConnectionPool = (ConnectorConnectionPool) _runtime.getResourceNamingService().lookup(poolInfo, jndiNameForPool, null);
        } catch (NamingException ne) {
            throw ne;
        }

        databaseName = ccPoolAdmService.getPropertyValue("DATABASENAME", connectorConnectionPool);

        // To avoid using "" as the default databasename, try to get
        // the databasename from MCF.
        if (databaseName == null || databaseName.trim().equals("")) {
            databaseName = getValueFromMCF("DatabaseName", poolInfo, managedConnectionFactory);
        }

        return databaseName;
    }

    /**
     * Get Validation table names list for the catalog that the jdbc connection pool
     * refers to. This is used for connection validation.
     *
     * @param con
     * @param catalog database name used.
     * @return
     * @throws jakarta.resource.ResourceException
     */
    public static Set<String> getValidationTableNames(java.sql.Connection con, String catalog) throws ResourceException {
        if (catalog.trim().isEmpty()) {
            catalog = null;
        }
        if (con == null) {
            throw new ResourceException("The connection is not valid as " + "the connection is null");
        }
        try {
            DatabaseMetaData metaData = con.getMetaData();
            try (ResultSet rs = metaData.getTables(catalog, null, null, null)) {
                SortedSet<String> tableNames = new TreeSet<>();
                while (rs.next()) {
                    String schemaName = rs.getString(2);
                    String tableName = rs.getString(3);
                    String actualTableName = tableName;
                    if (schemaName != null && !schemaName.equals("")) {
                        actualTableName = schemaName + "." + tableName;
                    }
                    tableNames.add(actualTableName);
                }
                return tableNames;
            }
        } catch (Exception sqle) {
            _logger.log(Level.INFO, "pool.get_validation_table_names");
            throw new ResourceException(sqle);
        }
    }
}
