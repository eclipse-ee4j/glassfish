/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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
import com.sun.appserv.connectors.internal.api.WorkContextHandler;
import com.sun.enterprise.connectors.util.ConnectorDDTransformUtils;
import com.sun.enterprise.connectors.util.ConnectorJavaBeanValidator;
import com.sun.enterprise.connectors.util.SetMethodAction;
import com.sun.enterprise.deployment.AdminObject;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.resource.beans.AdministeredObjectResource;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterAssociation;
import jakarta.resource.spi.ResourceAdapterInternalException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.Reference;

import org.glassfish.connectors.config.ResourceAdapterConfig;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;

/**
 * This class represents a live outbound resource adapter (1.5 compliant) i.e.
 * <p/>
 * A resource adapter is considered active after start()
 * and before stop() is called.
 *
 * @author Binod P G, Sivakumar Thyagarajan
 */
@Service(name= ConnectorConstants.AORA)
@PerLookup
public class ActiveOutboundResourceAdapter extends ActiveResourceAdapterImpl {

    @Inject
    private ConnectorJavaBeanValidator beanValidator;

    protected ResourceAdapter resourceadapter_; // runtime instance

    protected static final Logger _logger = LogDomains.getLogger(ActiveOutboundResourceAdapter.class,
        LogDomains.RSR_LOGGER);

    private final StringManager localStrings = StringManager.getManager(ActiveOutboundResourceAdapter.class);

    protected BootstrapContext bootStrapContextImpl;


    /**
     * Creates an active inbound resource adapter. Sets all RA java bean
     * properties and issues a start.
     *
     * @param ra         <code>ResourceAdapter<code> java bean.
     * @param desc       <code>ConnectorDescriptor</code> object.
     * @param moduleName Resource adapter module name.
     * @param jcl        <code>ClassLoader</code> instance.
     * @throws ConnectorRuntimeException If there is a failure in loading
     *                                   or starting the resource adapter.
     */
    @Override
    public void init(
            ResourceAdapter ra, ConnectorDescriptor desc, String moduleName,
            ClassLoader jcl) throws ConnectorRuntimeException {
        super.init(ra, desc, moduleName, jcl);
        this.resourceadapter_ = ra;
        if (resourceadapter_ != null) {
            try {
                loadRAConfiguration();

                // now the RA bean would have been fully configured (taking into account, resource-adapter-config),
                // validate the RA bean now.
                beanValidator.validateJavaBean(ra, moduleName);

                ConnectorRegistry registry = ConnectorRegistry.getInstance();
                String poolId = null;
                ResourceAdapterConfig raConfig = registry.getResourceAdapterConfig(moduleName_);
                if (raConfig != null) {
                    poolId = raConfig.getThreadPoolIds();
                }
                this.bootStrapContextImpl = new BootstrapContextImpl(poolId, moduleName_, jcl);
                validateWorkContextSupport(desc);

                startResourceAdapter(bootStrapContextImpl);

            } catch (ResourceAdapterInternalException ex) {
                _logger.log(Level.SEVERE, "rardeployment.start_failed", ex);
                String i18nMsg = localStrings.getString("rardeployment.start_failed", ex.getMessage());
                ConnectorRuntimeException cre = new ConnectorRuntimeException(i18nMsg);
                cre.initCause(ex);
                throw cre;
            } catch (Throwable t) {
                _logger.log(Level.SEVERE, "rardeployment.start_failed", t);
                String i18nMsg = localStrings.getString("rardeployment.start_failed", t.getMessage());
                ConnectorRuntimeException cre = new ConnectorRuntimeException(i18nMsg);
                if (t.getCause() != null) {
                    cre.initCause(t.getCause());
                } else {
                    cre.initCause(t);
                }
                throw cre;
            }
        }
    }

    /**
     * check whether the <i>required-work-context</i> list mandated by the resource-adapter
     * is supported by the application server
     * @param desc ConnectorDescriptor
     * @throws ConnectorRuntimeException when unable to support any of the requested work-context type.
     */
    private void validateWorkContextSupport(ConnectorDescriptor desc) throws ConnectorRuntimeException {
        Set<?> workContexts = desc.getRequiredWorkContexts();
        Iterator<?> workContextsIterator = workContexts.iterator();

        WorkContextHandler workContextHandler = connectorRuntime_.getWorkContextHandler();
        workContextHandler.init(moduleName_, jcl_);
        while(workContextsIterator.hasNext()){
            String ic = (String)workContextsIterator.next();
            boolean supported = workContextHandler.isContextSupported(true, ic );
            if(!supported){
                String errorMsg = "Unsupported work context [ "+ ic + " ] ";
                Object params[] = new Object[]{ic, desc.getName()};
                _logger.log(Level.WARNING,"unsupported.work.context", params);
                throw new ConnectorRuntimeException(errorMsg);
            }
        }
    }


    /**
     * called by connector runtime to start the resource-adapter java bean
     * @param bootstrapContext BootstrapContext
     * @throws ResourceAdapterInternalException
     */
    protected void startResourceAdapter(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
        resourceadapter_.start(bootstrapContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handles(ConnectorDescriptor cd, String moduleName) {
        boolean adminObjectsDefined = false;
        Set<AdminObject> adminObjects = cd.getAdminObjects();
        if (adminObjects != null && !adminObjects.isEmpty()) {
            adminObjectsDefined = true;
        }

        /*
        this class can handle Connector 1.5 Spec. compliant RAR that has no inbound artifacts
        criteria for 1.5 RAR :
          * No inbound artifacts
          * Any one of the following conditions hold true :
          *     -> admin object is defined or
          *     -> resource-adapter-class is defined or
          *     -> more than one connection-definition is defined.
        */
        boolean canHandle = false;
        if(!cd.getInBoundDefined()){
            if(cd.getOutBoundDefined() && cd.getOutboundResourceAdapter().getConnectionDefs().size() > 1){
                canHandle = true;
            }else if (adminObjectsDefined){
                canHandle = true;
            }else if(!cd.getResourceAdapterClass().equals("")){
                canHandle = true;
            }
        }
        return canHandle;
    }


    /**
     * Retrieves the resource adapter java bean.
     *
     * @return <code>ResourceAdapter</code>
     */
    @Override
    public ResourceAdapter getResourceAdapter() {
        return this.resourceadapter_;
    }

    /**
     * Does the necessary initial setup. Creates the default pool and
     * resource.
     *
     * @throws ConnectorRuntimeException If there is a failure
     */
    @Override
    public void setup() throws ConnectorRuntimeException {
        if (connectionDefs_ == null || connectionDefs_.length == 0) {
            return;
        }
        if (isServer() && !isSystemRar(moduleName_)) {
            createAllConnectorResources();
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Completed Active Resource adapter setup", moduleName_);
        }
    }

    /**
     * Destroys default pools and resources. Stops the Resource adapter
     * java bean.
     */
    @Override
    public void destroy() {
        //it is possible that a 1.5 ra may not have connection-definition at all
        if ((connectionDefs_ != null) && (connectionDefs_.length != 0)) {
            super.destroy();
        }
        stopResourceAdapter();
    }

    private void stopResourceAdapter() {
        if (resourceadapter_ != null) {
            try {
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("Calling Resource Adapter stop" + this.getModuleName());
                }
                resourceadapter_.stop();
                if(_logger.isLoggable(Level.FINE)){
                    _logger.fine("Resource Adapter stop call of " + this.getModuleName() + " returned successfully");
                    _logger.fine("rar_stop_call_successful");
                }
            } catch (Throwable t) {
                _logger.log(Level.SEVERE, "rardeployment.stop_warning", t);
            } finally {
                //not needed when there is no ResourceAdapter instance (implementation)
                removeProxiesFromRegistry(moduleName_);
            }
        }
    }

    /**
     * Remove all the proxy objects (Work-Manager) from connector registry
     *
     * @param moduleName_ resource-adapter name
     */
    private void removeProxiesFromRegistry(String moduleName_) {
        ConnectorRuntime.getRuntime().removeWorkManagerProxy(moduleName_);
    }

    /**
     * Creates an instance of <code>ManagedConnectionFactory</code>
     * object using the connection pool properties. Also set the
     * <code>ResourceAdapterAssociation</code>
     *
     * @param pool <code>ConnectorConnectionPool</code> properties.
     * @param jcl  <code>ClassLoader</code>
     */
    @Override
    public ManagedConnectionFactory createManagedConnectionFactory(
            ConnectorConnectionPool pool, ClassLoader jcl) {
        ManagedConnectionFactory mcf;
        mcf = super.createManagedConnectionFactory(pool, jcl);

        if (mcf instanceof ResourceAdapterAssociation) {
            try {
                ((ResourceAdapterAssociation) mcf).setResourceAdapter(this.resourceadapter_);
            } catch (ResourceException ex) {
                _logger.log(Level.SEVERE, "rardeployment.assoc_failed", ex);
            }
        }
        return mcf;
    }

    /**
     * Loads RA javabean. This method is protected, so that any system
     * resource adapter can have specific configuration done during the
     * loading.
     *
     * @throws ConnectorRuntimeException if there is a failure.
     */
    protected void loadRAConfiguration() throws ConnectorRuntimeException {
        try {
            ConnectorRegistry registry = ConnectorRegistry.getInstance();
            ResourceAdapterConfig raConfig = registry.getResourceAdapterConfig(moduleName_);
            List<Property> raConfigProps = new ArrayList<>();
            Set<ConnectorConfigProperty> mergedProps = mergeRAConfiguration(raConfig, raConfigProps);
            logMergedProperties(mergedProps);

            SetMethodAction<ConnectorConfigProperty> setMethodAction = new SetMethodAction<>(this.resourceadapter_, mergedProps);
            setMethodAction.run();
        } catch (Exception e) {
            String i18nMsg = localStrings.getString("ccp_adm.wrong_params_for_create", e.getMessage());
            ConnectorRuntimeException cre = new ConnectorRuntimeException(i18nMsg);
            cre.initCause(e);
            throw cre;
        }
    }

    /**
     * merge RA bean configuration with resource-adapter-config properties
     * Union of both.
     * resource-adapter-config properties will override the values of resource-adapter bean's config
     * @param raConfig resource-adapter-config
     * @param raConfigProps resource-adapter bean configuration
     * @return merged set of config properties
     */
    protected Set<ConnectorConfigProperty> mergeRAConfiguration(ResourceAdapterConfig raConfig, List<Property> raConfigProps) {
        Set<ConnectorConfigProperty> mergedProps;
        if (raConfig != null) {
            raConfigProps = raConfig.getProperty();
        }
        mergedProps = ConnectorDDTransformUtils.mergeProps(raConfigProps, getDescriptor().getConfigProperties());
        return mergedProps;
    }

    private void logMergedProperties(Set<ConnectorConfigProperty> mergedProps) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("Passing in the following properties " +
                    "before calling RA.start of " + this.moduleName_);
            StringBuilder b = new StringBuilder();
            for (ConnectorConfigProperty element : mergedProps) {
                b.append("\nName: " + element.getName()
                        + " Value: " + element.getValue());
            }
            _logger.fine(b.toString());
        }
    }

    public BootstrapContext getBootStrapContext() {
        return this.bootStrapContextImpl;
    }

    /**
     * Creates an admin object.
     *
     * @param appName         Name of application, in case of embedded rar.
     * @param connectorName   Module name of the resource adapter.
     * @param adminObjectType Interface name of the admin object.
     * @param props           <code>Properties</code> object containing name/value
     *                        pairs of properties.
     */
    public void addAdminObject(
            String appName,
            String connectorName,
            ResourceInfo resourceInfo,
            String adminObjectType,
            String adminObjectClassName,
            Properties props)
            throws ConnectorRuntimeException {
        if (props == null) {
            // empty properties
            props = new Properties();
        }

        final ConnectorRegistry registry = ConnectorRegistry.getInstance();
        final ConnectorDescriptor desc = registry.getDescriptor(connectorName);
        final AdminObject adminObject;
        // The admin-object can be identified by interface name, class name
        // or the combination of the both names.
        if (adminObjectClassName == null || adminObjectClassName.isBlank()) {
            // get AO through interface name
            List<AdminObject> adminObjects = desc.getAdminObjectsByType(adminObjectType);
            if (adminObjects.size() > 1) {
                String msg = localStrings.getString("aor.could_not_determine_aor_type", adminObjectType);
                throw new ConnectorRuntimeException(msg);
            }
            adminObject = adminObjects.isEmpty() ? null : adminObjects.get(0);
        } else if (adminObjectType == null || adminObjectType.isBlank()) {
            // get AO through class name
            List<AdminObject> adminObjects = desc.getAdminObjectsByClass(adminObjectClassName);
            if (adminObjects.size() > 1) {
                String msg = localStrings.getString("aor.could_not_determine_aor_class", adminObjectClassName);
                throw new ConnectorRuntimeException(msg);
            }
            adminObject = adminObjects.isEmpty() ? null : adminObjects.get(0);
        } else {
            // get AO through interface name and class name
            adminObject = desc.getAdminObject(adminObjectType, adminObjectClassName);
        }
        if (adminObject == null) {
            String msg = localStrings.getString("aor.could_not_determine_aor", adminObjectType, adminObjectClassName);
            throw new ConnectorRuntimeException(msg);
        }

        AdministeredObjectResource aor = new AdministeredObjectResource(resourceInfo);
        aor.initialize(adminObject);
        aor.setResourceAdapter(connectorName);

        ConnectorConfigProperty[] envProps = adminObject.getConfigProperties().toArray(ConnectorConfigProperty[]::new);

        //Add default config properties to aor
        // Override them if same config properties are provided by the user
        for (ConnectorConfigProperty envProp : envProps) {
            String name = envProp.getName();
            String userValue = (String) props.remove(name);
            if (userValue == null) {
                aor.addConfigProperty(envProp);
            } else {
                aor.addConfigProperty(new ConnectorConfigProperty(name, userValue, userValue, envProp.getType()));
            }
        }

        //Add non-default config properties provided by the user to aor
        Iterator<Object> iter = props.keySet().iterator();
        while (iter.hasNext()) {
            String name = (String) iter.next();
            String userValue = props.getProperty(name);
            if (userValue != null) {
                aor.addConfigProperty(new ConnectorConfigProperty(name, userValue, userValue));
            }
        }

        // bind to JNDI namespace
        try {
            Reference ref = aor.createAdminObjectReference();
            connectorRuntime_.getResourceNamingService().publishObject(resourceInfo, ref, true);
        } catch (NamingException ex) {
            String i18nMsg = localStrings.getString("aira.cannot_bind_admin_obj");
            throw new ConnectorRuntimeException(i18nMsg, ex);
        }
    }

}
