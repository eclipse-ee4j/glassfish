/*
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

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import org.glassfish.api.naming.NamingObjectProxy;
import com.sun.enterprise.deployment.ResourceDescriptor;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.util.ResourceManagerFactory;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingException;
import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: naman
 * Date: 27/8/12
 * Time: 2:51 PM
 * To change this template use File | Settings | File Templates.
 */
@Service
@PerLookup
public class CommonResourceProxy implements NamingObjectProxy.InitializationNamingObjectProxy, Serializable {

    @Inject
    protected transient ServiceLocator serviceLocator;
    protected ResourceDescriptor desc;
    protected String actualResourceName;

    public synchronized Object create(Context ic) throws NamingException {
        if (actualResourceName == null) {

            actualResourceName = ConnectorsUtil.deriveResourceName
                    (desc.getResourceId(), desc.getName(), desc.getResourceType());

            try {
                if (serviceLocator == null) {
                    serviceLocator = Globals.getDefaultHabitat();
                    if (serviceLocator == null) {
                        throw new NamingException("Unable to create resource " +
                                "[" + desc.getName() + " ] as habitat is null");
                    }
                }
                getResourceDeployer(desc).deployResource(desc);
            } catch (Exception e) {
                NamingException ne = new NamingException("Unable to create resource [" + desc.getName() + " ]");
                ne.initCause(e);
                throw ne;
            }
        }
        return ic.lookup(actualResourceName);
    }

    protected ResourceDeployer getResourceDeployer(Object resource) {
        return serviceLocator.<ResourceManagerFactory>getService(ResourceManagerFactory.class).getResourceDeployer(resource);
    }

    public synchronized void setDescriptor(ResourceDescriptor desc) {
        this.desc = desc;
    }
}
