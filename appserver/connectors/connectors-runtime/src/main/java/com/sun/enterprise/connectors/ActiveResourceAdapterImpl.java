/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.appserv.connectors.spi.ConfigurableTransactionSupport;
import com.sun.appserv.connectors.spi.TransactionSupport;
import com.sun.enterprise.connectors.util.ConnectionPoolObjectsUtils;
import com.sun.enterprise.connectors.util.ConnectorDDTransformUtils;
import com.sun.enterprise.connectors.util.PrintWriterAdapter;
import com.sun.enterprise.connectors.util.SetMethodAction;
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.runtime.connector.ResourceAdapter;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.resource.spi.ManagedConnectionFactory;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.jvnet.hk2.annotations.Service;


/**
 * This class represents the abstraction of a 1.0 compliant rar.
 * It holds the ra.xml (connector decriptor) values, class loader used to
 * to load the Resource adapter class and managed connection factory and
 * module name (rar) to which it belongs.
 * It is also the base class for ActiveOutboundResourceAdapter(a 1.5 compliant
 * outbound rar).
 *
 * @author  Srikanth P, Binod PG
 */

@Service
@PerLookup
public class ActiveResourceAdapterImpl implements ActiveResourceAdapter {

    private static Logger _logger = LogDomains.getLogger(ActiveResourceAdapterImpl.class, LogDomains.RSR_LOGGER);
    private final StringManager localStrings = StringManager.getManager(ActiveResourceAdapterImpl.class);

    @Inject
    protected ServiceLocator locator;

    protected ConnectorDescriptor desc_;
    protected String moduleName_;
    protected ClassLoader jcl_;
    protected ConnectionDefDescriptor[] connectionDefs_;
    protected ConnectorRuntime connectorRuntime_;

    /**
     * Constructor.
     *
     * @param desc       Connector Descriptor. Holds the all ra.xml values
     * @param moduleName Name of the module i.e rar Name. Incase of
     *                   embedded resource adapters its name will be appName#rarName
     * @param jcl        Classloader used to load the ResourceAdapter and managed
     *                   connection factory class.
     *                   values to domain.xml.
     */
    @Override
    public void init(jakarta.resource.spi.ResourceAdapter ra, ConnectorDescriptor desc, String moduleName,
        ClassLoader jcl) throws ConnectorRuntimeException {
        this.desc_ = desc;
        moduleName_ = moduleName;
        jcl_ = jcl;
        connectorRuntime_ = ConnectorRuntime.getRuntime();
        connectionDefs_ = ConnectorDDTransformUtils.getConnectionDefs(desc_);
    }


    public ActiveResourceAdapterImpl() {
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getModuleName() {
        return moduleName_;
    }

    /**
     * It initializes the resource adapter. It also creates the default pools
     * and resources of all the connection definitions.
     *
     * @throws ConnectorRuntimeException This exception is thrown if the
     *                                   ra.xml is invalid or the default pools and resources couldn't
     *                                   be created
     */
    @Override
    public void setup() throws ConnectorRuntimeException {
        if (connectionDefs_ == null || connectionDefs_.length != 1) {
            _logger.log(Level.SEVERE, "rardeployment.invalid_connector_desc", moduleName_);
            String i18nMsg = localStrings.getString("ccp_adm.invalid_connector_desc", moduleName_);
            throw new ConnectorRuntimeException(i18nMsg);
        }
        if (isServer() && !isSystemRar(moduleName_)) {
            createAllConnectorResources();
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Completed Active Resource adapter setup", moduleName_);
        }
    }

    /**
     * Check if the execution environment is appserver runtime or application
     * client container.
     *
     * @return boolean if the environment is appserver runtime
     */
    protected boolean isServer() {
        if (connectorRuntime_.isServer()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates both the default connector connection pools and resources
     *
     * @throws ConnectorRuntimeException when unable to create resources
     */
    protected void createAllConnectorResources() throws ConnectorRuntimeException {
        try {

            if (desc_.getSunDescriptor() != null && desc_.getSunDescriptor().getResourceAdapter() != null) {

                // sun-ra.xml exists
                SimpleJndiName jndiName = desc_.getSunDescriptor().getResourceAdapter().getValue(ResourceAdapter.JNDI_NAME);
                if (jndiName == null || jndiName.isEmpty()) {
                    // jndiName is empty, do not create duplicate pools, use setting in sun-ra.xml
                    createDefaultConnectorConnectionPools(true);
                } else {
                    // jndiName is not empty, so create duplicate pools, both default and sun-ra.xml
                    createSunRAConnectionPool();
                    createDefaultConnectorConnectionPools(false);
                }
            } else {
                // sun-ra.xml doesn't exist, so create default pools
                createDefaultConnectorConnectionPools(false);
            }

            // always create default connector resources
            createDefaultConnectorResources();
        } catch (ConnectorRuntimeException cre) {
            //Connector deployment should _not_ fail if default connector
            //connector pool and resource creation fails.
            _logger.log(Level.SEVERE, "rardeployment.defaultpoolresourcecreation.failed", cre);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Error while trying to create the default connector, " +
                    "connection pool and resource", cre);
            }
        } catch (Exception e) {
            //Connector deployment should _not_ fail if default connector
            //connector pool and resource creation fails.
            _logger.log(Level.SEVERE, "rardeployment.defaultpoolresourcecreation.failed", e);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Error while trying to create the default connector, " +
                    "connection pool and resource", e);
            }
        }
    }

    /**
     * Deletes both the default connector connection pools and resources
     */
    protected void destroyAllConnectorResources() {
        if (!(isSystemRar(moduleName_))) {
            deleteDefaultConnectorResources();
            deleteDefaultConnectorConnectionPools();

            // Added to ensure clean-up of the Sun RA connection pool
            if (desc_.getSunDescriptor() != null &&
                    desc_.getSunDescriptor().getResourceAdapter() != null) {

                // sun-ra.xml exists
                SimpleJndiName jndiName = desc_.getSunDescriptor().getResourceAdapter()
                    .getValue(ResourceAdapter.JNDI_NAME);

                if (jndiName != null && !jndiName.isEmpty()) {
                    deleteSunRAConnectionPool();
                }
            }
        }
    }

    protected boolean isSystemRar(String moduleName) {
        return ConnectorsUtil.belongsToSystemRA(moduleName);
    }

    /**
     * Deletes the default connector connection pools.
     */
    protected void deleteDefaultConnectorConnectionPools() {
        for (ConnectionDefDescriptor aConnectionDefs_ : connectionDefs_) {
            String connectionDefName = aConnectionDefs_.getConnectionFactoryIntf();
            SimpleJndiName resourceJndiName = connectorRuntime_.getDefaultPoolName(moduleName_, connectionDefName);
            try {
                PoolInfo poolInfo = new PoolInfo(resourceJndiName);
                connectorRuntime_.deleteConnectorConnectionPool(poolInfo);
            } catch (ConnectorRuntimeException cre) {
                _logger.log(Level.WARNING, "rar.undeployment.default_pool_delete_fail", resourceJndiName);
            }
        }
    }

    /**
     * Deletes the default connector resources.
     */
    protected void deleteDefaultConnectorResources() {
        for (ConnectionDefDescriptor aConnectionDefs_ : connectionDefs_) {
            String connectionDefName = aConnectionDefs_.getConnectionFactoryIntf();
            SimpleJndiName resourceJndiName = connectorRuntime_.getDefaultResourceName(moduleName_, connectionDefName);
            try {
                ResourceInfo resourceInfo = new ResourceInfo(resourceJndiName);
                connectorRuntime_.deleteConnectorResource(resourceInfo);
            } catch (ConnectorRuntimeException cre) {
                _logger.log(Level.WARNING, "rar.undeployment.default_resource_delete_fail", resourceJndiName);
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "Error while trying to delete the default connector resource", cre);
                }
            }
        }
    }

    /**
     * uninitializes the resource adapter. It also destroys the default pools
     * and resources
     */
    @Override
    public void destroy() {
        if (isServer()) {
            destroyAllConnectorResources();
        }
    }

    /**
     * Returns the Connector descriptor which represents/holds ra.xml
     *
     * @return ConnectorDescriptor Representation of ra.xml.
     */
    @Override
    public ConnectorDescriptor getDescriptor() {
        return desc_;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handles(ConnectorDescriptor cd, String moduleName) {

        boolean canHandle = false;
        boolean adminObjectsDefined = false;
        Set adminObjects = cd.getAdminObjects();
        if (adminObjects != null && adminObjects.size() > 0) {
            adminObjectsDefined = true;
        }

        /*
        this class can handle Connector 1.0 Spec. compliant RAR
        criteria for 1.0 RAR :
          * No inbound artifacts
          * No admin-objects
          * There should be only one connection-definition
          * RA Class should not be present (equivalent to "")
        */
        if(!cd.getInBoundDefined() && !adminObjectsDefined &&
                (cd.getOutBoundDefined() && cd.getOutboundResourceAdapter().getConnectionDefs().size() < 2
                        && "".equals(cd.getResourceAdapterClass()))
                ){
            canHandle = true;
        }
        return canHandle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ManagedConnectionFactory[] createManagedConnectionFactories(
            ConnectorConnectionPool ccp, ClassLoader jcl) {
        throw new UnsupportedOperationException("This operation is not supported");
    }


    /**
     * Creates managed Connection factory instance.
     *
     * @param ccp Connector connection pool which contains the pool properties
     *            and ra.xml values pertaining to managed connection factory
     *            class. These values are used in MCF creation.
     * @param jcl Classloader used to managed connection factory class.
     * @return ManagedConnectionFactory created managed connection factory
     *         instance
     */
    @Override
    public ManagedConnectionFactory createManagedConnectionFactory(
            ConnectorConnectionPool ccp, ClassLoader jcl) {
        final String mcfClass = ccp.getConnectorDescriptorInfo().getManagedConnectionFactoryClass();
        try {
            ManagedConnectionFactory mcf = instantiateMCF(mcfClass, jcl);
            if (mcf instanceof ConfigurableTransactionSupport) {
                TransactionSupport ts = ConnectionPoolObjectsUtils.getTransactionSupport(ccp.getTransactionSupport());
                ((ConfigurableTransactionSupport)mcf).setTransactionSupport(ts);
            }

            SetMethodAction<ConnectorConfigProperty> setMethodAction = new SetMethodAction<>(mcf,
                ccp.getConnectorDescriptorInfo().getMCFConfigProperties());
            setMethodAction.run();
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Created MCF object : ", mcfClass);
            }
            return mcf;
        } catch (ClassNotFoundException Ex) {
            _logger.log(Level.SEVERE, "rardeployment.class_not_found", new Object[]{mcfClass, Ex.getMessage()});
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "rardeployment.class_not_found", Ex);
            }
            return null;
        } catch (InstantiationException Ex) {
            _logger.log(Level.SEVERE, "rardeployment.class_instantiation_error", new Object[]{mcfClass, Ex.getMessage()});
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "rardeployment.class_instantiation_error", Ex);
            }
            return null;
        } catch (IllegalAccessException Ex) {
            _logger.log(Level.SEVERE, "rardeployment.illegalaccess_error", new Object[]{mcfClass, Ex.getMessage()});
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "rardeployment.illegalaccess_error", Ex);
            }
            return null;
        } catch (Exception Ex) {
            _logger.log(Level.SEVERE, "rardeployment.mcfcreation_error", new Object[]{mcfClass, Ex.getMessage()});
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "rardeployment.mcfcreation_error", Ex);
            }
            return null;
        }
    }

    /**
     * sets the logWriter for the MCF being instantiated.<br>
     * Resource Adapter implementer can make use of this logWriter<br>
     *
     * @param mcf ManagedConnectionFactory
     */
    private void setLogWriter(ManagedConnectionFactory mcf) {
        PrintWriterAdapter adapter = new PrintWriterAdapter(ConnectorRuntime.getRuntime().getResourceAdapterLogWriter());
        try {
            mcf.setLogWriter(adapter);
        } catch (Exception e) {
            Object[] params = new Object[]{mcf.getClass().getName(), e.toString()};
            _logger.log(Level.WARNING, "rardeployment.logwriter_error", params);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Unable to set LogWriter for ManagedConnectionFactory : " + mcf.getClass().getName(), e);
            }
        }
    }


    protected ManagedConnectionFactory instantiateMCF(String mcfClassName, ClassLoader loader) throws Exception {
        ManagedConnectionFactory mcf = null;
        Class<?> mcfClass;
        if (jcl_ != null) {
            mcfClass = jcl_.loadClass(mcfClassName);
        } else if (loader != null) {
            mcfClass = loader.loadClass(mcfClassName);

        } else {
            mcfClass = Thread.currentThread().getContextClassLoader().loadClass(mcfClassName);
        }
        mcf = locator.createAndInitialize((Class<ManagedConnectionFactory>)mcfClass);
        setLogWriter(mcf);
        return mcf;
    }


    /**
     * Creates default connector resource
     *
     * @throws ConnectorRuntimeException when unable to create connector resources
     */
    protected void createDefaultConnectorResources()
            throws ConnectorRuntimeException {
        for (ConnectionDefDescriptor descriptor : connectionDefs_) {

            String connectionDefName = descriptor.getConnectionFactoryIntf();
            SimpleJndiName resourceName = connectorRuntime_.getDefaultResourceName(moduleName_, connectionDefName);
            SimpleJndiName poolName = connectorRuntime_.getDefaultPoolName(moduleName_, connectionDefName);

            PoolInfo poolInfo = new PoolInfo(poolName);
            ResourceInfo resourceInfo = new ResourceInfo(resourceName);
            connectorRuntime_.createConnectorResource(resourceInfo, poolInfo, null);
            desc_.addDefaultResourceName(resourceName);

            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Created default connector resource [ " + resourceName + " ] " );
            }
        }
    }

    /**
     * Creates default connector connection pool
     *
     * @param useSunRA whether to use default pool settings or settings in sun-ra.xml
     * @throws ConnectorRuntimeException when unable to create connector connection pools
     */
    protected void createDefaultConnectorConnectionPools(boolean useSunRA)
            throws ConnectorRuntimeException {
        for (ConnectionDefDescriptor descriptor : connectionDefs_) {
            // example: connection-factory-definition-embedraApp#cfd-ra#jakarta.resource.cci.ConnectionFactory
            SimpleJndiName poolName = connectorRuntime_.getDefaultPoolName(moduleName_, descriptor.getConnectionFactoryIntf());
            PoolInfo poolInfo = new PoolInfo(poolName);

            ConnectorDescriptorInfo connectorDescriptorInfo = ConnectorDDTransformUtils
                .getConnectorDescriptorInfo(descriptor);
            connectorDescriptorInfo.setRarName(moduleName_);
            connectorDescriptorInfo.setResourceAdapterClassName(desc_.getResourceAdapterClass());
            ConnectorConnectionPool connectorPoolObj;

            // if useSunRA is true, then create connectorPoolObject using settings from sunRAXML
            if (useSunRA) {
                connectorPoolObj = ConnectionPoolObjectsUtils.createSunRaConnectorPoolObject(poolInfo, desc_, moduleName_);
            } else {
                connectorPoolObj = ConnectionPoolObjectsUtils.createDefaultConnectorPoolObject(poolInfo, moduleName_);
            }

            connectorPoolObj.setConnectorDescriptorInfo(connectorDescriptorInfo);
            connectorRuntime_.createConnectorConnectionPool(connectorPoolObj);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Created default connection pool [ "+ poolInfo + " ] ");
            }
        }
    }

    /**
     * Creates connector connection pool pertaining to sun-ra.xml. This is
     * only for 1.0 complient rars.
     *
     * @throws ConnectorRuntimeException Thrown when pool creation fails.
     */
    private void createSunRAConnectionPool() throws ConnectorRuntimeException {

        SimpleJndiName defaultPoolName = connectorRuntime_.getDefaultPoolName(
                moduleName_, connectionDefs_[0].getConnectionFactoryIntf());

        SimpleJndiName sunRAPoolName = new SimpleJndiName(defaultPoolName + ConnectorConstants.SUN_RA_POOL);
        PoolInfo poolInfo = new PoolInfo(sunRAPoolName);

        ConnectorDescriptorInfo connectorDescriptorInfo =
                ConnectorDDTransformUtils.getConnectorDescriptorInfo(connectionDefs_[0]);
        connectorDescriptorInfo.setRarName(moduleName_);
        connectorDescriptorInfo.setResourceAdapterClassName(desc_.getResourceAdapterClass());
        ConnectorConnectionPool connectorPoolObj =
                ConnectionPoolObjectsUtils.createSunRaConnectorPoolObject(poolInfo, desc_, moduleName_);

        connectorPoolObj.setConnectorDescriptorInfo(connectorDescriptorInfo);
        connectorRuntime_.createConnectorConnectionPool(connectorPoolObj);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Created SUN-RA connection pool:", poolInfo);
        }

        SimpleJndiName jndiName = desc_.getSunDescriptor().getResourceAdapter().getValue(ResourceAdapter.JNDI_NAME);
        ResourceInfo resourceInfo = new ResourceInfo(jndiName);
        connectorRuntime_.createConnectorResource(resourceInfo, poolInfo, null);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Created SUN-RA connector resource : ", resourceInfo);
        }

    }

    /**
     * Added to clean up the connector connection pool pertaining to sun-ra.xml. This is
     * only for 1.0 complient rars.
     */
    private void deleteSunRAConnectionPool() {

        SimpleJndiName defaultPoolName = connectorRuntime_.getDefaultPoolName(moduleName_,
            connectionDefs_[0].getConnectionFactoryIntf());

        SimpleJndiName sunRAPoolName = new SimpleJndiName(defaultPoolName + ConnectorConstants.SUN_RA_POOL);
        PoolInfo poolInfo = new PoolInfo(sunRAPoolName);
        try {
            connectorRuntime_.deleteConnectorConnectionPool(poolInfo);
        } catch (ConnectorRuntimeException cre) {
            _logger.log(Level.WARNING, "rar.undeployment.sun_ra_pool_delete_fail", poolInfo);
        }
    }

    /**
     * Returns the class loader that is used to load the RAR.
     *
     * @return <code>ClassLoader</code> object.
     */
    @Override
    public ClassLoader getClassLoader() {
        return jcl_;
    }

    /**
     * Retrieves the resource adapter java bean.
     *
     * @return <code>ResourceAdapter</code>
     */
    @Override
    public jakarta.resource.spi.ResourceAdapter getResourceAdapter() {
        throw new UnsupportedOperationException("1.0 RA will not have ResourceAdapter bean");
    }

}
