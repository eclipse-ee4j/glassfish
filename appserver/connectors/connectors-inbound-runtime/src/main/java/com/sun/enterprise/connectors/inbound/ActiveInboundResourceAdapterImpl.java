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

package com.sun.enterprise.connectors.inbound;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.connectors.ActiveOutboundResourceAdapter;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.runtime.BeanPoolDescriptor;

import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.ResourceAdapter;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Represents the active (runtime) inbound resource-adapter
 */
@Service(name= ConnectorConstants.AIRA)
@PerLookup
public class ActiveInboundResourceAdapterImpl extends ActiveOutboundResourceAdapter
        implements ActiveInboundResourceAdapter {


    //beanID -> endpoint factory and its activation spec
    private Hashtable<String, MessageEndpointFactoryInfo> factories_;


    /**
     * Creates an active inbound resource adapter. Sets all RA java bean
     * properties and issues a start.
     *
     * @param ra         <code>ResourceAdapter<code> java bean.
     * @param desc       <code>ConnectorDescriptor</code> object.
     * @param moduleName Resource adapter module name.
     * @param jcl        <code>ClassLoader</code> instance.
     * @throws com.sun.appserv.connectors.internal.api.ConnectorRuntimeException
     *          If there is a failure in loading
     *          or starting the resource adapter.
     */
    public void init(ResourceAdapter ra, ConnectorDescriptor desc, String moduleName, ClassLoader jcl)
            throws ConnectorRuntimeException {
        super.init(ra, desc, moduleName, jcl);
        this.factories_ = new Hashtable<String, MessageEndpointFactoryInfo>();
    }

    public ActiveInboundResourceAdapterImpl() {
    }

    /**
     * Destroys default pools and resources. Stops the Resource adapter
     * java bean.
     */
    public void destroy() {
        deactivateEndPoints();
        super.destroy();
    }

    private void deactivateEndPoints() {
        if (resourceadapter_ != null) {
            //deactivateEndpoints as well!
            Iterator<MessageEndpointFactoryInfo> iter = getAllEndpointFactories().iterator();
            while (iter.hasNext()) {
                MessageEndpointFactoryInfo element = iter.next();
                try {
                    this.resourceadapter_.endpointDeactivation(
                            element.getEndpointFactory(), element.getActivationSpec());
                } catch (RuntimeException e) {
                    _logger.warning(e.getMessage());
                    _logger.log(Level.FINE, "Error during endpointDeactivation ", e);
                }
            }
        }
    }

    /**
     * Retrieves the information about all endpoint factories.
     *
     * @return a <code>Collection</code> of <code>MessageEndpointFactory</code>
     *         objects.
     */
    public Collection<MessageEndpointFactoryInfo> getAllEndpointFactories() {
        return factories_.values();
    }


    /**
     * Returns information about endpoint factory.
     *
     * @param id Id of the endpoint factory.
     * @return <code>MessageEndpointFactoryIndo</code> object.
     */
    public MessageEndpointFactoryInfo getEndpointFactoryInfo(String id) {
        return factories_.get(id);
    }

    public void updateMDBRuntimeInfo(EjbMessageBeanDescriptor descriptor_, BeanPoolDescriptor poolDescriptor)
            throws ConnectorRuntimeException {
        //do nothing
    }

    public void validateActivationSpec(ActivationSpec spec) {
        //do nothing
    }

    /*
     * @return A set of Map.Entry that has the bean ID as the key
     *         and the MessageEndpointFactoryInfo as value
     *         A shallow copy only to avoid concurrency issues.
     */
    public Set getAllEndpointFactoryInfo() {
        Hashtable infos = (Hashtable<String, MessageEndpointFactoryInfo>) factories_.clone();
        return infos.entrySet();
    }

    /**
     * {@inheritDoc}
     */
    public boolean handles(ConnectorDescriptor cd, String moduleName) {
        return (cd.getInBoundDefined() && !ConnectorsUtil.isJMSRA(moduleName));
     }


    /**
     * Adds endpoint factory information.
     *
     * @param id   Unique identifier of the endpoint factory.
     * @param info <code>MessageEndpointFactoryInfo</code> object.
     */
    public void addEndpointFactoryInfo(
            String id, MessageEndpointFactoryInfo info) {
        factories_.put(id, info);
    }

    /**
     * Removes information about an endpoint factory
     *
     * @param id Unique identifier of the endpoint factory to be
     *           removed.
     */
    public void removeEndpointFactoryInfo(String id) {
        factories_.remove(id);
    }

}
