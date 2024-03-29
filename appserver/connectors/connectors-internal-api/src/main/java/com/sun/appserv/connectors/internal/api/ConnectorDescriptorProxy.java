/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.appserv.connectors.internal.api;

import com.sun.enterprise.deployment.ConnectorDescriptor;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import javax.naming.Context;
import javax.naming.NamingException;

import org.glassfish.api.naming.NamingObjectProxy;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

@Service
@PerLookup
public class ConnectorDescriptorProxy implements NamingObjectProxy.InitializationNamingObjectProxy{
    @Inject
    private Provider<ConnectorRuntime> connectorRuntimeProvider;

    private ConnectorDescriptor desc;
    private String rarName;
    private SimpleJndiName jndiName;

    public void setRarName(String rarName){
        this.rarName = rarName;
    }

    public String getRarName(){
        return rarName;
    }

    public void setJndiName(SimpleJndiName jndiName){
        this.jndiName = jndiName;
    }

    public SimpleJndiName getJndiName(){
        return jndiName;
    }

    protected ConnectorRuntime getConnectorRuntime() {
        return connectorRuntimeProvider.get();
    }

    @Override
    public synchronized ConnectorDescriptor create(Context ic) throws NamingException {
        //this is a per-lookup object and once we have the descriptor,
        //we remove the proxy and bind the descriptor with same jndi-name
        //hence block synchronization is fine as it blocks only callers
        //of this particular connector descriptor and also only for first time (initialization)
        if(desc == null) {
            try {
                desc = getConnectorRuntime().getConnectorDescriptor(rarName);
                ic.rebind(jndiName.toString(), desc);
            } catch (ConnectorRuntimeException e) {
                NamingException ne = new NamingException(e.getMessage());
                ne.initCause(e);
                throw ne;
            }
        }
        return desc;
    }
}
