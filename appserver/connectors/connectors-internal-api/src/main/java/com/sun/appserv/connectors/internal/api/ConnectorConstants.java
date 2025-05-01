/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package com.sun.appserv.connectors.internal.api;

import com.sun.enterprise.util.SystemPropertyConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.glassfish.resourcebase.resources.api.ResourceConstants;


/**
 * This interface contains all the constants referenced and used in the
 * connector module.
 * As a design principal all the constants needs to be placed here.
 * This will enable tracking all the constants easily.
 */
public interface ConnectorConstants extends ResourceConstants {

    /**
     * Represents the connector container module name / type
     */
    String CONNECTOR_MODULE = "connector";

     /**
     *  JAXR  system resource adapter name.
     */
    String JAXR_RA_NAME = "jaxr-ra";

    /**
     *  JDBC datasource  system resource adapter name.
     */
    String JDBCDATASOURCE_RA_NAME = "__ds_jdbc_ra";

    /**
     *  JDBC connectionpool datasource  system resource adapter name.
     */
    String JDBCCONNECTIONPOOLDATASOURCE_RA_NAME = "__cp_jdbc_ra";

    /**
     *  JDBC XA datasource  system resource adapter name.
     */
    String JDBCXA_RA_NAME = "__xa_jdbc_ra";

    /**
     *  JDBC Driver Manager system resource adapter name.
     */
    String JDBCDRIVER_RA_NAME = "__dm_jdbc_ra";

    /**
     *  JMS datasource  system resource adapter name.
     */
    String DEFAULT_JMS_ADAPTER = "jmsra";

    /**
     * List of jdbc system resource adapter names
     */
    List<String> jdbcSystemRarNames = Collections.unmodifiableList(
            Arrays.asList(
                JDBCDATASOURCE_RA_NAME,
                JDBCCONNECTIONPOOLDATASOURCE_RA_NAME,
                JDBCXA_RA_NAME,
                JDBCDRIVER_RA_NAME
            ));


    /**
     * List of system resource adapter names
     */
    List<String> systemRarNames = Collections.unmodifiableList(
            Arrays.asList(
                JAXR_RA_NAME,
                JDBCDATASOURCE_RA_NAME,
                JDBCCONNECTIONPOOLDATASOURCE_RA_NAME,
                JDBCXA_RA_NAME,
                JDBCDRIVER_RA_NAME,
                DEFAULT_JMS_ADAPTER
            ));

    /**
     * Indicates the list of system-rars for which connector connection pools can be created
     */
    List<String> systemRarsAllowingPoolCreation = Collections.unmodifiableList(
               Arrays.asList(
                       DEFAULT_JMS_ADAPTER,
                       JAXR_RA_NAME
               ));

    /**
     * delimiter used in hidden CLIs when name-value pairs are returned in response
     */
    String HIDDEN_CLI_NAME_VALUE_PAIR_DELIMITER="=";


    /**
     *  Reserver JNDI context under which sub contexts for default resources
     *  and all connector connection pools are created
     *  Subcontext for connector descriptors bounding is also done under
     *  this context.
     */
    String RESERVE_PREFIX = "__SYSTEM";

    /**
     * Sub context for binding connector descriptors.
     */
    String DD_PREFIX= RESERVE_PREFIX+"/descriptors/";

    /**
     * Token used for generation of poolname pertaining to sun-ra.xml.
     * Generated pool name will be
     * rarName+POOLNAME_APPENDER+connectionDefName+SUN_RA_POOL.
     * SUNRA connector connections pools are are named and bound after
     * this name. Pool object will be bound under POOLS_JNDINAME_PREFIX
     * subcontext. To lookup a pool the jndi name should be
     * POOLS_JNDINAME_PREFIX/rarName+POOLNAME_APPENDER+connectionDefName
     * +SUN_RA_POOL
     */
    String SUN_RA_POOL = "sunRAPool";
    String ADMINISTERED_OBJECT_FACTORY =
        "com.sun.enterprise.resource.naming.AdministeredObjectFactory";

    /**
     * Meta char for mapping the security for connection pools
     */
    String SECURITYMAPMETACHAR="*";

    /**
     * Token used for default poolname generation. Generated pool name will be
     * rarName+POOLNAME_APPENDER+connectionDefName.Default connector connections
     * pools are are named and bound after this name. Pool object will be bound
     * under POOLS_JNDINAME_PREFIX subcontext. To lookup a pool the jndi name
     * should be
     * POOLS_JNDINAME_PREFIX/rarName+POOLNAME_APPENDER+connectionDefName
     */
    String POOLNAME_APPENDER="#";

    /**
     * Token used for default connector resource generation.Generated connector
     * resource  name and JNDI names will be
     * RESOURCE_JNDINAME_PREFIX+rarName+RESOURCENAME_APPENDER+connectionDefName
     * This name should be used to lookup connector resource.
     */
    String RESOURCENAME_APPENDER="#";

    /**
     * resource-adapter archive extension name
     */
    String RAR_EXTENSION=".rar";


    /**
     * represents the monitoring-service level element name
     */
    String MONITORING_CONNECTOR_SERVICE_MODULE_NAME = "connector-service";
    String MONITORING_JMS_SERVICE_MODULE_NAME = "jms-service";

    /**
     * represents the monitoring-service hierarchy elements <br>
     * eg: server.connector-service.&lt;RA-NAME&gt;.work-management<br>
     */
    String MONITORING_CONNECTOR_SERVICE = "connector-service";
    String MONITORING_JMS_SERVICE = "jms-service";
    String MONITORING_WORK_MANAGEMENT = "work-management";
    String MONITORING_CONNECTION_FACTORIES = "connection-factories";
    String MONITORING_SEPARATOR = "/";


    /**
     *  Reserved sub-context where pool objets are bound with generated names.
     */
    String POOLS_JNDINAME_PREFIX=RESERVE_PREFIX+"/pools/";

    /**
     *  Reserved sub-context where connector resource objects are bound with
     *  generated names.
     */
    String RESOURCE_JNDINAME_PREFIX=RESERVE_PREFIX+"/resource/";
    String USERGROUPDISTINGUISHER="#";
    String CAUTION_MESSAGE="Please add the following permissions to the " +
            "server.policy file and restart the appserver.";

    /**
     * Property name for distinguishing the transaction exceptions
     * propagation capability.
     */
    String THROW_TRANSACTED_EXCEPTIONS_PROP
        = "resourceadapter.throw.transacted.exceptions";

    /**
     * System Property value for distinguishing the transaction exceptions
     * propagation capability.
     */
    String sysThrowExcp
        = System.getProperty(THROW_TRANSACTED_EXCEPTIONS_PROP);

    /**
     * Property value for distinguishing the transaction exceptions
     * propagation capability.
     */
    boolean THROW_TRANSACTED_EXCEPTIONS
        = sysThrowExcp != null && !(sysThrowExcp.trim().equals("true")) ?
          false : true;

    int DEFAULT_RESOURCE_ADAPTER_SHUTDOWN_TIMEOUT = 30;

    String JAVAX_SQL_DATASOURCE = "javax.sql.DataSource";

    String JAVAX_SQL_CONNECTION_POOL_DATASOURCE = "javax.sql.ConnectionPoolDataSource";

    String JAVAX_SQL_XA_DATASOURCE = "javax.sql.XADataSource";

    String JAVA_SQL_DRIVER = "java.sql.Driver";

   /**
     * Property value for defining NoTransaction transaction-support in
     * a connector-connection-pool
     */
    String NO_TRANSACTION_TX_SUPPORT_STRING = "NoTransaction";

    /**
     * Property value for defining LocalTransaction transaction-support in
     * a connector-connection-pool
     */
    String LOCAL_TRANSACTION_TX_SUPPORT_STRING = "LocalTransaction";

    /**
     * Property value for defining XATransaction transaction-support in
     * a connector-connection-pool
     */
    String XA_TRANSACTION_TX_SUPPORT_STRING = "XATransaction";

    /**
     * Property value defining the NoTransaction transaction-support value
     * as an integer
     */

    int NO_TRANSACTION_INT = 0;
    /**
     * Property value defining the LocalTransaction transaction-support value
     * as an integer
     */

    int LOCAL_TRANSACTION_INT = 1;

    /**
     * Property value defining the XATransaction transaction-support value
     * as an integer
     */
    int XA_TRANSACTION_INT = 2;

    /**
     * Property value defining an undefined transaction-support value
     * as an integer
     */
    int UNDEFINED_TRANSACTION_INT = -1;

    /**
     * Min pool size for JMS connection pools.
     */
    int JMS_POOL_MINSIZE = 1;

    /**
     * Min pool size for JMS connection pools.
     */
    int JMS_POOL_MAXSIZE = 250;

    public enum PoolType {

        ASSOCIATE_WITH_THREAD_POOL, STANDARD_POOL, PARTITIONED_POOL, POOLING_DISABLED
    }

    String PM_JNDI_SUFFIX = "__pm";

    String NON_TX_JNDI_SUFFIX = "__nontx" ;

    /**
     * Name of the JNDI environment property that can be provided so that the
     * <code>ObjectFactory</code> can decide which type of datasource create.
     */
    String JNDI_SUFFIX_PROPERTY = "com.sun.enterprise.connectors.jndisuffix";

    /**
     * Valid values that can be provided to the JNDI property.
     */
    List<String> JNDI_SUFFIX_VALUES = Collections.unmodifiableList(
            Arrays.asList(
                    PM_JNDI_SUFFIX,
                    NON_TX_JNDI_SUFFIX
            ));

    String CCP = "ConnectorConnectionPool";
    String CR  =  "ConnectorResource";
    String AOR = "AdminObjectResource";
    String SEC = "Security";
    String RA = "ResourceAdapter";
    String JDBC = "Jdbc";

    /**
     * @deprecated Duplicates SystemPropertyConstants.INSTALL_ROOT_PROPERTY
     */
    @Deprecated(forRemoval = true, since = "7.0.25")
    String INSTALL_ROOT = SystemPropertyConstants.INSTALL_ROOT_PROPERTY;

    String DB_VENDOR_MAPPING_ROOT = "org.glassfish.connectors.dbVendorMappingRoot";

    // name by which connector's implemenation of message-bean-client-factory service is available.
    // MDB-Container can use this constant to get connector's implementation of the factory
    String CONNECTOR_MESSAGE_BEAN_CLIENT_FACTORY="ConnectorMessageBeanClientFactory";

    String EXPLODED_EMBEDDED_RAR_EXTENSION="_rar";

    String JAVA_BEAN_FACTORY_CLASS = "org.glassfish.resources.custom.factory.JavaBeanFactory";
    String PRIMITIVES_AND_STRING_FACTORY_CLASS =
            "org.glassfish.resources.custom.factory.PrimitivesAndStringFactory";
    String URL_OBJECTS_FACTORY = "org.glassfish.resources.custom.factory.URLObjectFactory";
    String PROPERTIES_FACTORY = "org.glassfish.resources.custom.factory.PropertiesFactory";

    //service-names for the ActiveResourceAdapter contract's implementations
    // service providing inbound support
    String AIRA = "ActiveInboundResourceAdapter";
    // service providing outbound support
    String AORA = "ActiveOutboundResourceAdapter";

    String CLASSLOADING_POLICY_DERIVED_ACCESS = "derived";
    String CLASSLOADING_POLICY_GLOBAL_ACCESS = "global";

    String RAR_VISIBILITY = "rar-visibility";
    String RAR_VISIBILITY_GLOBAL_ACCESS = "*";

    //flag to indicate that all applications have access to all deployed standalone RARs
    String ACCESS_ALL_RARS = "access-all-rars";
    //flag to indiate additional RARs required for an application, apart from the ones referred via app's DD
    String REQUIRED_RARS_FOR_APP_PREFIX="required-rars-for-";

    //flag to indicate that the call to lookup is a proxy's call so that actual object can be returned
    String DYNAMIC_RECONFIGURATION_PROXY_CALL = "com.sun.enterprise.resource.reconfig.proxyCall";

    //flag to enable dynamic-reconfiguration feature for connection pool
    String DYNAMIC_RECONFIGURATION_FLAG = "dynamic-reconfiguration-wait-timeout-in-seconds";

    /**
     * Admin object type.
     */
    String AO = "AdminObject";

    /**
     * Managed connection factory type.
     */
    String MCF = "ManagedConnectionFactory";

    /**
     * Resource adapter type.
     */
    String RAR = "ResourceAdapter";

    /**
     * Message listener type.
     */
    String MSL = "MessageListener";

    /**
     * Reserved sub-context where concurrent objects are bound with generated names.
     */
    String CONCURRENT_JNDINAME_PREFIX = "concurrent/";

}
