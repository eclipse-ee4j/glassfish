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

package com.sun.enterprise.resource.naming;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.connectors.ConnectionManagerImpl;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.service.ConnectorAdminServiceUtils;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.resource.DynamicallyReconfigurableResource;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceInfo;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import jakarta.resource.spi.ManagedConnectionFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An object factory to handle creation of Connection Factories
 *
 * @author Tony Ng
 */
public class ConnectorObjectFactory implements ObjectFactory {

    private ConnectorRuntime runtime ;

    private static Logger _logger = LogDomains.getLogger(ConnectorObjectFactory.class, LogDomains.JNDI_LOGGER);
    protected final static StringManager localStrings =
            StringManager.getManager(ConnectorRuntime.class);

    public ConnectorObjectFactory() {
    }

    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable env) throws Exception {

        Reference ref = (Reference) obj;
        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,"ConnectorObjectFactory: " + ref +
                " Name:" + name);
        }
            PoolInfo poolInfo = (PoolInfo) ref.get(0).getContent();
            String moduleName  = (String) ref.get(1).getContent();
            ResourceInfo resourceInfo = (ResourceInfo) ref.get(2).getContent();


        if (getRuntime().isACCRuntime() || getRuntime().isNonACCRuntime()) {
            ConnectorDescriptor connectorDescriptor = null;

            String descriptorJNDIName = ConnectorAdminServiceUtils.
                    getReservePrefixedJNDINameForDescriptor(moduleName);
            Context ic = new InitialContext(env);
            connectorDescriptor = (ConnectorDescriptor) ic.lookup(descriptorJNDIName);
            try {
                getRuntime().createActiveResourceAdapter(connectorDescriptor, moduleName, null);
            } catch (ConnectorRuntimeException e) {
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE,
                            "Failed to look up ConnectorDescriptor from JNDI",
                            moduleName);
                }
                NamingException ne = new NamingException("Failed to look up ConnectorDescriptor from JNDI");
                ne.setRootCause(e);
                throw ne;
            }
        }

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (!getRuntime().checkAccessibility(moduleName, loader)) {
            String msg = localStrings.getString("cof.no_access_to_embedded_rar", moduleName);
            throw new NamingException(msg);
        }

        Object cf = null;
        try {
            ManagedConnectionFactory mcf = getRuntime().obtainManagedConnectionFactory(poolInfo, env);
            if (mcf == null) {
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "Failed to create MCF ", poolInfo);
                }
                throw new ConnectorRuntimeException("Failed to create MCF");
            }

            boolean forceNoLazyAssoc = false;

            String jndiName = name.toString();
            if (jndiName.endsWith(ConnectorConstants.PM_JNDI_SUFFIX)) {
                forceNoLazyAssoc = true;
            }

            String derivedJndiName = ConnectorsUtil.deriveJndiName(jndiName, env);
            ConnectionManagerImpl mgr = (ConnectionManagerImpl)
                    getRuntime().obtainConnectionManager(poolInfo, forceNoLazyAssoc, resourceInfo);
            mgr.setJndiName(derivedJndiName);
            mgr.setRarName(moduleName);

            String logicalName = (String)env.get(GlassfishNamingManager.LOGICAL_NAME);
            if(logicalName != null){
                mgr.setLogicalName(logicalName);
            }

            mgr.initialize();

            cf = mcf.createConnectionFactory(mgr);
            if (cf == null) {
                String msg = localStrings.getString("cof.no.resource.adapter");
                throw new RuntimeException(new ConfigurationException(msg));
            }

            if (getRuntime().isServer() || getRuntime().isEmbedded()) {
                ConnectorRegistry registry = ConnectorRegistry.getInstance();
                if (registry.isTransparentDynamicReconfigPool(poolInfo)) {
                    Resources resources = getRuntime().getResources(poolInfo);
                    ResourcePool resourcePool = null;
                    if (resources != null) {
                        resourcePool = (ResourcePool) ConnectorsUtil.getResourceByName(resources, ResourcePool.class, poolInfo.getName());
                        if (resourcePool != null) {
                            ResourceDeployer deployer = getRuntime().getResourceDeployer(resourcePool);
                            if (deployer != null && deployer.supportsDynamicReconfiguration() &&
                                    ConnectorsUtil.isDynamicReconfigurationEnabled(resourcePool)) {

                                Object o = env.get(ConnectorConstants.DYNAMIC_RECONFIGURATION_PROXY_CALL);
                                if (o == null || Boolean.valueOf(o.toString()).equals(false)) {
                                    //TODO use list ? (even in the ResourceDeployer API)
                                    Class[] classes = deployer.getProxyClassesForDynamicReconfiguration();
                                    Class[] proxyClasses = new Class[classes.length + 1];
                                    for (int i = 0; i < classes.length; i++) {
                                        proxyClasses[i] = classes[i];
                                    }
                                    proxyClasses[proxyClasses.length - 1] = DynamicallyReconfigurableResource.class;

                                    cf = getProxyObject(cf, proxyClasses, resourceInfo);
                                }
                            }
                        }
                    }
                }
            }

            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Connection Factory:" + cf);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cf;
    }

      protected <T> T getProxyObject(final Object actualObject, Class<T>[] ifaces, ResourceInfo resourceInfo) throws Exception {
        InvocationHandler ih = new DynamicResourceReconfigurator(actualObject, resourceInfo);
        return (T) Proxy.newProxyInstance(actualObject.getClass().getClassLoader(), ifaces, ih);
    }

    private ConnectorRuntime getRuntime() {
        if (runtime == null) {
            runtime = ConnectorNamingUtils.getRuntime();
        }
        return runtime;
    }
}
