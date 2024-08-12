/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.webservices;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.Component;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.FiberContextSwitchInterceptor;
import com.sun.xml.ws.api.pipe.ServerTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.ThrowableContainerPropertySet;
import com.sun.xml.ws.api.server.Adapter;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.ServiceDefinition;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.wsdl.OperationDispatcher;

import jakarta.xml.ws.EndpointReference;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.xml.namespace.QName;

import org.glassfish.gmbal.ManagedObjectManager;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author ljungman
 */
public class JAXWSAdapterRegistryTest {

    /**
     * Test of addAdapter method, of class JAXWSAdapterRegistry.
     * http://java.net/jira/browse/GLASSFISH-17836
     * Putting load on freshly-started Glassfish web-app messes up its initialization process
     */
    @Test
    public void testAddAdapter() {
        final String contextRoot = "/cr";
        final String urlPattern = "/up";
        final JAXWSAdapterRegistry registry = JAXWSAdapterRegistry.getInstance();
        int size = 25;
        Thread[] ts = new Thread[size];
        for (int i = 0; i < size; i++) {
            final int j = i;
            ts[i] = new Thread(new Runnable() {

                @Override
                public void run() {
                    registry.addAdapter(contextRoot, urlPattern + j, new A(j));
                }
            });
        }

        for (int i = 0; i < size; i++) {
            ts[i].start();
        }

        for (int i = 0; i < size; i++) {
            try {
                ts[i].join();
            } catch (InterruptedException ex) {
            }
        }

        for (int i = 0; i < size; i++) {
            Adapter adapter = registry.getAdapter(contextRoot, urlPattern + i, urlPattern + i);
            assertNotNull(adapter, "No adapter for '" + contextRoot + urlPattern + i + "'");
            assertEquals(i, ((A) adapter).getX());
        }
    }

    private class A extends Adapter {

        private final int x;

        public A(int x) {
            super(new WSE());
            this.x = x;
        }


        public int getX() {
            return x;
        }


        @Override
        protected Toolkit createToolkit() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private class WSE extends WSEndpoint {

        @NotNull
        @Override
        public Set<Component> getComponents() {
            return new HashSet<>();
        }


        @Override
        public Codec createCodec() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public QName getServiceName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public QName getPortName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public Class getImplementationClass() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public WSBinding getBinding() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public Container getContainer() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public WSDLPort getPort() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public void setExecutor(Executor exctr) {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public void schedule(Packet packet, CompletionCallback cc, FiberContextSwitchInterceptor fcsi) {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public PipeHead createPipeHead() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public void dispose() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public ServiceDefinition getServiceDefinition() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public SEIModel getSEIModel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public PolicyMap getPolicyMap() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public ManagedObjectManager getManagedObjectManager() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public void closeManagedObjectManager() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public ServerTubeAssemblerContext getAssemblerContext() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public EndpointReference getEndpointReference(Class clazz, String address, String wsdlAddress,
            Element... referenceParameters) {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public EndpointReference getEndpointReference(Class clazz, String address, String wsdlAddress, List metadata,
            List referenceParameters) {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public OperationDispatcher getOperationDispatcher() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public Packet createServiceResponseForException(ThrowableContainerPropertySet tcps, Packet packet,
            SOAPVersion soapv, WSDLPort wsdlp, SEIModel seim, WSBinding wsb) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
