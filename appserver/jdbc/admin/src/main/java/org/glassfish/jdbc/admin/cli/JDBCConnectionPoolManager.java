/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jdbc.admin.cli;

import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.resource.ResourceException;

import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.Properties;

import org.glassfish.api.I18n;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.jdbc.config.JdbcConnectionPool;
import org.glassfish.jdbc.util.JdbcResourcesUtil;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resources.admin.cli.ResourceManager;
import org.glassfish.resources.api.ResourceAttributes;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static org.glassfish.resources.admin.cli.ResourceConstants.ALLOW_NON_COMPONENT_CALLERS;
import static org.glassfish.resources.admin.cli.ResourceConstants.ASSOCIATE_WITH_THREAD;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_CREATION_RETRY_ATTEMPTS;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_LEAK_RECLAIM;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_LEAK_TIMEOUT_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_POOL_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_VALIDATION_METHOD;
import static org.glassfish.resources.admin.cli.ResourceConstants.DATASOURCE_CLASS;
import static org.glassfish.resources.admin.cli.ResourceConstants.DRIVER_CLASSNAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.FAIL_ALL_CONNECTIONS;
import static org.glassfish.resources.admin.cli.ResourceConstants.IDLE_TIME_OUT_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.INIT_SQL;
import static org.glassfish.resources.admin.cli.ResourceConstants.IS_CONNECTION_VALIDATION_REQUIRED;
import static org.glassfish.resources.admin.cli.ResourceConstants.IS_ISOLATION_LEVEL_GUARANTEED;
import static org.glassfish.resources.admin.cli.ResourceConstants.LAZY_CONNECTION_ASSOCIATION;
import static org.glassfish.resources.admin.cli.ResourceConstants.LAZY_CONNECTION_ENLISTMENT;
import static org.glassfish.resources.admin.cli.ResourceConstants.MATCH_CONNECTIONS;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAX_CONNECTION_USAGE_COUNT;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAX_POOL_SIZE;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAX_WAIT_TIME_IN_MILLIS;
import static org.glassfish.resources.admin.cli.ResourceConstants.NON_TRANSACTIONAL_CONNECTIONS;
import static org.glassfish.resources.admin.cli.ResourceConstants.PING;
import static org.glassfish.resources.admin.cli.ResourceConstants.POOLING;
import static org.glassfish.resources.admin.cli.ResourceConstants.POOL_SIZE_QUANTITY;
import static org.glassfish.resources.admin.cli.ResourceConstants.RES_TYPE;
import static org.glassfish.resources.admin.cli.ResourceConstants.SQL_TRACE_LISTENERS;
import static org.glassfish.resources.admin.cli.ResourceConstants.STATEMENT_CACHE_SIZE;
import static org.glassfish.resources.admin.cli.ResourceConstants.STATEMENT_TIMEOUT_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.STEADY_POOL_SIZE;
import static org.glassfish.resources.admin.cli.ResourceConstants.TRANS_ISOLATION_LEVEL;
import static org.glassfish.resources.admin.cli.ResourceConstants.VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.VALIDATION_CLASSNAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.VALIDATION_TABLE_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.WRAP_JDBC_OBJECTS;


/**
 * @author Prashanth Abbagani
 */
@Service(name = ServerTags.JDBC_CONNECTION_POOL)
@I18n("add.resources")
public class JDBCConnectionPoolManager implements ResourceManager {

    private static final String DESCRIPTION = ServerTags.DESCRIPTION;

    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(JDBCConnectionPoolManager.class);

    private String datasourceclassname;
    private String restype;
    private String steadypoolsize = "8";
    private String maxpoolsize = "32";
    private String maxwait = "60000";
    private String poolresize = "2";
    private String idletimeout = "300";
    private String initsql;
    private String isolationlevel;
    private String isisolationguaranteed = Boolean.TRUE.toString();
    private String isconnectvalidatereq = Boolean.FALSE.toString();
    private String validationmethod = "table";
    private String validationtable;
    private String failconnection = Boolean.FALSE.toString();
    private String allownoncomponentcallers = Boolean.FALSE.toString();
    private String nontransactionalconnections = Boolean.FALSE.toString();
    private String validateAtmostOncePeriod = "0";
    private String connectionLeakTimeout = "0";
    private String connectionLeakReclaim = Boolean.FALSE.toString();
    private String connectionCreationRetryAttempts = "0";
    private String connectionCreationRetryInterval = "10";
    private String driverclassname;
    private String sqltracelisteners;
    private String statementTimeout = "-1";
    private String statementcachesize = "0";
    private String lazyConnectionEnlistment = Boolean.FALSE.toString();
    private String lazyConnectionAssociation = Boolean.FALSE.toString();
    private String associateWithThread = Boolean.FALSE.toString();
    private String matchConnections = Boolean.FALSE.toString();
    private String maxConnectionUsageCount = "0";
    private String ping = Boolean.FALSE.toString();
    private String pooling = Boolean.TRUE.toString();
    private String validationclassname;
    private String wrapJDBCObjects = Boolean.TRUE.toString();

    private String description;
    private String jdbcconnectionpoolid;

    @Override
    public String getResourceType() {
        return ServerTags.JDBC_CONNECTION_POOL;
    }

    @Override
    public ResourceStatus create(Resources resources, ResourceAttributes attributes, final Properties properties,
        String target) throws Exception {
        setAttributes(attributes);

        ResourceStatus validationStatus = isValid(resources);
        if(validationStatus.getStatus() == ResourceStatus.FAILURE){
            return validationStatus;
        }

        try {
            ConfigSupport.apply(param -> createResource(param, properties), resources);
        } catch (TransactionFailure tfe) {
            String msg = I18N.getLocalString(
                    "create.jdbc.connection.pool.fail", "JDBC connection pool {0} create failed: {1}",
                    jdbcconnectionpoolid, tfe.getMessage());
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
        String msg = I18N.getLocalString(
                "create.jdbc.connection.pool.success", "JDBC connection pool {0} created successfully",
                jdbcconnectionpoolid);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
    }

    private ResourceStatus isValid(Resources resources){
        ResourceStatus status = new ResourceStatus(ResourceStatus.SUCCESS, "Validation Successful");
        if (jdbcconnectionpoolid == null) {
            String msg = I18N.getLocalString("add.resources.noJdbcConnectionPoolId",
                    "No pool name defined for JDBC Connection pool.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
        // ensure we don't already have one of this name
        for (ResourcePool pool : resources.getResources(ResourcePool.class)) {
            if (pool.getName().equals(jdbcconnectionpoolid)) {
                String msg = I18N.getLocalString("create.jdbc.connection.pool.duplicate",
                        "A resource {0} already exists.", jdbcconnectionpoolid);
                return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
            }
        }

        if ("table".equals(this.validationmethod)
                && Boolean.TRUE.toString().equals(this.isconnectvalidatereq)
                && this.validationtable == null) {
            String msg = I18N.getLocalString("create.jdbc.connection.pool.validationtable_required",
                    "--validationtable is required if --validationmethod=table " +
                            "and --isconnectvalidatereq=true.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }
        return status;
    }

    private JdbcConnectionPool createResource(Resources param, Properties properties) throws PropertyVetoException,
            TransactionFailure {
        JdbcConnectionPool newResource = createConfigBean(param, properties);
        param.getResources().add(newResource);
        return newResource;
    }

    private JdbcConnectionPool createConfigBean(Resources param, Properties properties) throws PropertyVetoException,
            TransactionFailure {
        JdbcConnectionPool newResource = param.createChild(JdbcConnectionPool.class);
        newResource.setWrapJdbcObjects(wrapJDBCObjects);
        if (validationtable != null) {
            newResource.setValidationTableName(validationtable);
        }
        newResource.setValidateAtmostOncePeriodInSeconds(validateAtmostOncePeriod);
        if (isolationlevel != null) {
            newResource.setTransactionIsolationLevel(isolationlevel);
        }
        newResource.setSteadyPoolSize(steadypoolsize);
        if(statementTimeout != null){
            newResource.setStatementTimeoutInSeconds(statementTimeout);
        }
        if (restype != null) {
            newResource.setResType(restype);
        }
        newResource.setPoolResizeQuantity(poolresize);
        newResource.setNonTransactionalConnections(nontransactionalconnections);
        newResource.setMaxWaitTimeInMillis(maxwait);
        newResource.setMaxPoolSize(maxpoolsize);
        if(maxConnectionUsageCount != null){
            newResource.setMaxConnectionUsageCount(maxConnectionUsageCount);
        }
        if(matchConnections != null){
            newResource.setMatchConnections(matchConnections);
        }
        if(lazyConnectionEnlistment != null){
            newResource.setLazyConnectionEnlistment(lazyConnectionEnlistment);
        }
        if(lazyConnectionAssociation != null){
            newResource.setLazyConnectionAssociation(lazyConnectionAssociation);
        }
        newResource.setIsIsolationLevelGuaranteed(isisolationguaranteed);
        newResource.setIsConnectionValidationRequired(isconnectvalidatereq);
        newResource.setIdleTimeoutInSeconds(idletimeout);
        newResource.setFailAllConnections(failconnection);
        if (datasourceclassname != null) {
            newResource.setDatasourceClassname(datasourceclassname);
        }
        newResource.setConnectionValidationMethod(validationmethod);
        if(connectionLeakTimeout != null){
            newResource.setConnectionLeakTimeoutInSeconds(connectionLeakTimeout);
        }
        if(connectionLeakReclaim != null){
            newResource.setConnectionLeakReclaim(connectionLeakReclaim);
        }
        if(connectionCreationRetryInterval != null){
            newResource.setConnectionCreationRetryIntervalInSeconds(connectionCreationRetryInterval);
        }
        if(connectionCreationRetryAttempts != null){
            newResource.setConnectionCreationRetryAttempts(connectionCreationRetryAttempts);
        }
        if(associateWithThread != null){
            newResource.setAssociateWithThread(associateWithThread);
        }
        if(allownoncomponentcallers != null){
            newResource.setAllowNonComponentCallers(allownoncomponentcallers);
        }
        if(statementcachesize != null){
            newResource.setStatementCacheSize(statementcachesize);
        }
        if (validationclassname != null) {
            newResource.setValidationClassname(validationclassname);
        }
        if(initsql != null){
            newResource.setInitSql(initsql);
        }
        if (sqltracelisteners != null) {
            newResource.setSqlTraceListeners(sqltracelisteners);
        }
        if(pooling != null){
            newResource.setPooling(pooling);
        }
        if(ping != null){
            newResource.setPing(ping);
        }
        if (driverclassname != null) {
            newResource.setDriverClassname(driverclassname);
        }
        if (description != null) {
            newResource.setDescription(description);
        }
        newResource.setName(jdbcconnectionpoolid);
        if (properties != null) {
            for (String propertyName : properties.stringPropertyNames()) {
                Property prop = newResource.createChild(Property.class);
                prop.setName(propertyName);
                prop.setValue(properties.getProperty(propertyName));
                newResource.getProperty().add(prop);
            }
        }
        return newResource;
    }

    public void setAttributes(ResourceAttributes attrList) {
        datasourceclassname = attrList.getString(DATASOURCE_CLASS);
        restype = attrList.getString(RES_TYPE);
        steadypoolsize = attrList.getString(STEADY_POOL_SIZE);
        maxpoolsize = attrList.getString(MAX_POOL_SIZE);
        maxwait = attrList.getString(MAX_WAIT_TIME_IN_MILLIS);
        poolresize = attrList.getString(POOL_SIZE_QUANTITY);
        idletimeout = attrList.getString(IDLE_TIME_OUT_IN_SECONDS);
        isolationlevel = attrList.getString(TRANS_ISOLATION_LEVEL);
        isisolationguaranteed = attrList.getString(IS_ISOLATION_LEVEL_GUARANTEED);
        isconnectvalidatereq = attrList.getString(IS_CONNECTION_VALIDATION_REQUIRED);
        validationmethod = attrList.getString(CONNECTION_VALIDATION_METHOD);
        validationtable = attrList.getString(VALIDATION_TABLE_NAME);
        failconnection = attrList.getString(FAIL_ALL_CONNECTIONS);
        allownoncomponentcallers = attrList.getString(ALLOW_NON_COMPONENT_CALLERS);
        nontransactionalconnections = attrList.getString(NON_TRANSACTIONAL_CONNECTIONS);
        validateAtmostOncePeriod = attrList.getString(VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS);
        connectionLeakTimeout = attrList.getString(CONNECTION_LEAK_TIMEOUT_IN_SECONDS);
        connectionLeakReclaim = attrList.getString(CONNECTION_LEAK_RECLAIM);
        connectionCreationRetryAttempts = attrList.getString(CONNECTION_CREATION_RETRY_ATTEMPTS);
        connectionCreationRetryInterval = attrList.getString(CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS);
        statementTimeout = attrList.getString(STATEMENT_TIMEOUT_IN_SECONDS);
        lazyConnectionEnlistment = attrList.getString(LAZY_CONNECTION_ENLISTMENT);
        lazyConnectionAssociation = attrList.getString(LAZY_CONNECTION_ASSOCIATION);
        associateWithThread = attrList.getString(ASSOCIATE_WITH_THREAD);
        matchConnections = attrList.getString(MATCH_CONNECTIONS);
        maxConnectionUsageCount = attrList.getString(MAX_CONNECTION_USAGE_COUNT);
        wrapJDBCObjects = attrList.getString(WRAP_JDBC_OBJECTS);
        description = attrList.getString(DESCRIPTION);
        jdbcconnectionpoolid = attrList.getString(CONNECTION_POOL_NAME);
        statementcachesize = attrList.getString(STATEMENT_CACHE_SIZE);
        validationclassname = attrList.getString(VALIDATION_CLASSNAME);
        initsql = attrList.getString(INIT_SQL);
        sqltracelisteners = attrList.getString(SQL_TRACE_LISTENERS);
        pooling = attrList.getString(POOLING);
        ping = attrList.getString(PING);
        driverclassname = attrList.getString(DRIVER_CLASSNAME);
    }


    public ResourceStatus delete(Iterable<Server> servers, Iterable<Cluster> clusters, final Resources resources,
        final String cascade, final SimpleJndiName poolName) throws Exception {
        if (poolName == null) {
            String msg = I18N.getLocalString("jdbcConnPool.resource.noJndiName",
                    "No id defined for JDBC Connection pool.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        // ensure we already have this resource
        if (!isResourceExists(resources, poolName)) {
            String msg = I18N.getLocalString("delete.jdbc.connection.pool.notfound",
                    "A JDBC connection pool named {0} does not exist.", poolName);
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        try {

            // if cascade=true delete all the resources associated with this pool
            // if cascade=false don't delete this connection pool if a resource is referencing it
            Object obj = deleteAssociatedResources(servers, clusters, resources,
                    Boolean.parseBoolean(cascade), poolName);
            if (obj instanceof Integer &&
                    (Integer) obj == ResourceStatus.FAILURE) {
                String msg = I18N.getLocalString(
                        "delete.jdbc.connection.pool.pool_in_use",
                        "JDBC Connection pool {0} delete failed ", poolName);
                return new ResourceStatus(ResourceStatus.FAILURE, msg);
            }

            // delete jdbc connection pool
            if (ConfigSupport.apply(new SingleConfigCode<Resources>() {
                @Override
                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    JdbcConnectionPool cp = resources.getResourceByName(JdbcConnectionPool.class, poolName);
                    return param.getResources().remove(cp);
                }
            }, resources) == null) {
                String msg = I18N.getLocalString("delete.jdbc.connection.pool.notfound",
                        "A JDBC connection pool named {0} does not exist.", poolName);
                return new ResourceStatus(ResourceStatus.FAILURE, msg);
            }

        } catch (TransactionFailure tfe) {
            String msg = tfe.getMessage() != null ? tfe.getMessage() :
                    I18N.getLocalString("jdbcConnPool.resource.deletionFailed",
                            "JDBC Connection pool {0} delete failed ", poolName);
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            status.setException(tfe);
            return status;
        }

        String msg = I18N.getLocalString("jdbcConnPool.resource.deleteSuccess",
                "JDBC Connection pool {0} deleted successfully", poolName);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
    }

    private boolean isResourceExists(Resources resources, SimpleJndiName poolName) {
        return resources.getResourceByName(JdbcConnectionPool.class, poolName) != null;
    }


    private Object deleteAssociatedResources(final Iterable<Server> servers, final Iterable<Cluster> clusters,
        Resources resources, final boolean cascade, final SimpleJndiName poolName) throws TransactionFailure {
        if (cascade) {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {
                @Override
                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    Collection<BindableResource> referringResources = JdbcResourcesUtil.getResourcesOfPool(param, poolName);
                    for (BindableResource referringResource : referringResources) {
                        // delete resource-refs
                        SimpleJndiName jndiName = SimpleJndiName.of(referringResource.getJndiName());
                        deleteServerResourceRefs(servers, jndiName);
                        deleteClusterResourceRefs(clusters, jndiName);
                        // remove the resource
                        param.getResources().remove(referringResource);
                    }
                    return true; //no-op
                }
            }, resources);
        }else{
            Collection<BindableResource> referringResources = JdbcResourcesUtil.getResourcesOfPool(resources, poolName);
            if(referringResources.size() > 0){
                return ResourceStatus.FAILURE;
            }
        }
        return true; //no-op
    }

    private void deleteServerResourceRefs(Iterable<Server> servers, final SimpleJndiName refName)
            throws TransactionFailure {
        if(servers != null){
            for (Server server : servers) {
                server.deleteResourceRef(refName);
            }
        }
    }

    private void deleteClusterResourceRefs(Iterable<Cluster> clusters, final SimpleJndiName refName)
            throws TransactionFailure {
        if(clusters != null){
            for (Cluster cluster : clusters) {
                cluster.deleteResourceRef(refName);
            }
        }
    }

    @Override
    public Resource createConfigBean(Resources resources, ResourceAttributes attributes, Properties properties,
        boolean validate) throws Exception {
        setAttributes(attributes);
        final ResourceStatus status;
        if (validate) {
            status = isValid(resources);
        } else {
            status = new ResourceStatus(ResourceStatus.SUCCESS, "");
        }
        if (status.getStatus() == ResourceStatus.SUCCESS) {
            return createConfigBean(resources, properties);
        }
        throw new ResourceException(status.getMessage(), status.getException());
    }
}
