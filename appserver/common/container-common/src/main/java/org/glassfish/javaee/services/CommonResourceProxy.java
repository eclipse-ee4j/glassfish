/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

package org.glassfish.javaee.services;

import com.sun.enterprise.deployment.ResourceDescriptor;

import jakarta.inject.Inject;

import java.io.Serializable;
import java.lang.System.Logger;

import javax.naming.Context;
import javax.naming.NamingException;

import org.glassfish.api.naming.NamingObjectProxy;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.util.ResourceManagerFactory;
import org.jvnet.hk2.annotations.Service;

import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.deriveResourceName;
import static java.lang.System.Logger.Level.DEBUG;

/**
 * @author naman 2012
 */
@Service
@PerLookup
public class CommonResourceProxy implements NamingObjectProxy.InitializationNamingObjectProxy, Serializable {
    private static final Logger LOG = System.getLogger(CommonResourceProxy.class.getName());

    @Inject
    protected transient ServiceLocator serviceLocator;
    protected ResourceDescriptor desc;
    protected SimpleJndiName actualResourceName;

    @Override
    public synchronized <T> T create(Context context) throws NamingException {
        if (actualResourceName == null) {
            actualResourceName = deriveResourceName(desc.getResourceId(), desc.getJndiName(), desc.getResourceType());
            LOG.log(DEBUG, "Deploying resource for actualResourceName={0} and descriptor.jndiName={1}",
                actualResourceName, desc.getJndiName());
            try {
                if (serviceLocator == null) {
                    serviceLocator = Globals.getDefaultHabitat();
                    if (serviceLocator == null) {
                        throw new NamingException(
                            "Unable to create resource " + "[" + desc.getJndiName() + " ] as habitat is null");
                    }
                }
                getResourceDeployer(desc).deployResource(desc);
            } catch (Exception e) {
                NamingException ne = new NamingException("Unable to create resource [" + desc.getJndiName() + " ]");
                ne.initCause(e);
                throw ne;
            }
        }

        return (T) context.lookup(actualResourceName.toString());
    }


    protected ResourceDeployer getResourceDeployer(Object resource) {
        return serviceLocator.<ResourceManagerFactory> getService(ResourceManagerFactory.class)
            .getResourceDeployer(resource);
    }


    public synchronized void setDescriptor(ResourceDescriptor desc) {
        this.desc = desc;
    }


    @Override
    public String toString() {
        return super.toString() + "[actualResourceName=" + actualResourceName + ", desc.jndiName="
            + (desc == null ? null : desc.getJndiName()) + ']';
    }
}
