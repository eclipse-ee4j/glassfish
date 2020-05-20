/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb32.mdb.ra;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.endpoint.MessageEndpoint;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author David Blevins
 */
public class CommandResourceAdapter implements ResourceAdapter {

    private Map<CommandActivationSpec, ActivatedEndpoint> endpoints = new HashMap<CommandActivationSpec, ActivatedEndpoint>();

    public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
    }

    public void stop() {
    }

    public void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) throws ResourceException {
        final CommandActivationSpec commandActivationSpec = (CommandActivationSpec) activationSpec;
        final ActivatedEndpoint activatedEndpoint = new ActivatedEndpoint(messageEndpointFactory, commandActivationSpec);
        endpoints.put(commandActivationSpec, activatedEndpoint);
        final Thread thread = new Thread(activatedEndpoint);
        thread.setDaemon(true);
        thread.start();
    }

    public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {
        endpoints.remove((CommandActivationSpec) activationSpec);
    }

    public XAResource[] getXAResources(ActivationSpec[] activationSpecs) throws ResourceException {
        return new XAResource[0];
    }

    private static class ActivatedEndpoint implements Runnable {

        private final MessageEndpointFactory factory;
        private final List<Method> commands = new ArrayList<Method>();

        private ActivatedEndpoint(MessageEndpointFactory factory, CommandActivationSpec spec) {
            this.factory = factory;

            final Method[] methods = factory.getEndpointClass().getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Command.class)) {
                    commands.add(method);
                }
            }

        }

        public void run() {
            pause();
            try {
                final MessageEndpoint endpoint = factory.createEndpoint(null);
                try {
                    for (Method method : commands) {
                        endpoint.beforeDelivery(method);
                        try {
                            method.invoke(endpoint);
                        } finally {
                            endpoint.afterDelivery();
                        }
                    }
                } finally {
                    endpoint.release();
                }
            } catch (Throwable e) {
                e.printStackTrace();
                // fail
            }
        }

        /**
         *  Have to wait till the application is fully started
         */
        private static void pause() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }
}
