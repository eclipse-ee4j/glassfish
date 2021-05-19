/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.connector.blackbox;

import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import jakarta.resource.ResourceException;
import javax.transaction.xa.XAResource;
import java.io.Serializable;

public class ResourceAdapter implements jakarta.resource.spi.ResourceAdapter, Serializable {

    private BootstrapContext context;
    public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
        this.context = bootstrapContext;
    }

    public void stop() {
        //do nothing
    }

    public void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) throws ResourceException {
        //do nothing
    }

    public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {
        //do nothing
    }

    public XAResource[] getXAResources(ActivationSpec[] activationSpecs) throws ResourceException {
        return null;
    }

    public BootstrapContext getBootstrapContext(){
        return context;
    }
}
