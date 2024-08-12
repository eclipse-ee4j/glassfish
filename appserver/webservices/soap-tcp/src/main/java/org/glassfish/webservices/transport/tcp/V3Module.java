/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.webservices.transport.tcp;

import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.tcp.server.TCPAdapter;
import com.sun.xml.ws.transport.tcp.server.WSTCPDelegate;
import com.sun.xml.ws.transport.tcp.server.WSTCPModule;
import com.sun.xml.ws.transport.tcp.servicechannel.ServiceChannelWSImpl;

import java.util.List;

import javax.xml.namespace.QName;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.ejb.spi.WSEjbEndpointRegistry;
import org.glassfish.webservices.WebServiceDeploymentListener;
import org.glassfish.webservices.WebServiceEjbEndpointRegistry;
import org.glassfish.webservices.WebServicesDeployer;
import org.xml.sax.EntityResolver;

/**
 *
 * @author oleksiys
 */
public class V3Module extends WSTCPModule {
    private final WSTCPDelegate delegate;

    V3Module() {
        WSTCPModule.setInstance(this);

        WebServicesDeployer.getDeploymentNotifier().
            addListener(new WebServiceDeploymentListener() {

            @Override
            public void onDeployed(WebServiceEndpoint endpoint) {
                if (endpoint.getWebComponentImpl() != null) {
                    endpoint.getWebComponentImpl().setLoadOnStartUp(0);
                }
            }

            @Override
            public void onUndeployed(WebServiceEndpoint endpoint) {
            }
        });

        AppServRegistry.getInstance();
        delegate = new WSTCPDelegate();
        delegate.setCustomWSRegistry(WSTCPAdapterRegistryImpl.getInstance());
    }

    @Override
    public void register(String contextPath, List<TCPAdapter> adapters) {
        delegate.registerAdapters(contextPath, adapters);
    }

    @Override
    public void free(String contextPath, List<TCPAdapter> adapters) {
        delegate.freeAdapters(contextPath, adapters);
    }

    @Override
    public int getPort() {
        return -1;
    }

    public WSTCPDelegate getDelegate() {
        return delegate;
    }

    @Override
    public WSEndpoint<ServiceChannelWSImpl> createServiceChannelEndpoint() {
        Class<ServiceChannelWSImpl> serviceEndpointClass = ServiceChannelWSImpl.class;
        final QName serviceName = WSEndpoint.getDefaultServiceName(ServiceChannelWSImpl.class);
        final QName portName = WSEndpoint.getDefaultPortName(serviceName, ServiceChannelWSImpl.class);
        final BindingID bindingId = BindingID.parse(ServiceChannelWSImpl.class);
        final WSBinding binding = bindingId.createBinding();

//        final Invoker inv= (new InstanceResolverImpl(serviceEndpointClass)).createInvoker();

        return WSEndpoint.create(serviceEndpointClass, false,
                    null,
                    serviceName, portName, Container.NONE, binding,
                    null, null, (EntityResolver) null, true);
    }


    public static WebServiceEjbEndpointRegistry getWSEjbEndpointRegistry() {
        return (WebServiceEjbEndpointRegistry) org.glassfish.internal.api.Globals.getDefaultHabitat().getService(
                    WSEjbEndpointRegistry.class);
    }

    public static InvocationManager getInvocationManager() {
        return (InvocationManager) org.glassfish.internal.api.Globals.getDefaultHabitat().getService(
                    InvocationManager.class);
    }
}
