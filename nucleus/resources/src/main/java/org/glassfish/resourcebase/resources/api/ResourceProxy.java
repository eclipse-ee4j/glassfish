/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resourcebase.resources.api;

import com.sun.enterprise.config.serverbeans.Resource;
import org.glassfish.api.naming.NamingObjectProxy;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import javax.naming.Context;
import javax.naming.NamingException;



/**
 * Represents the proxy object for a resource. Proxy will be bound in jndi
 * during startup and the actual <i>resource</i> will be deployed during
 * first lookup
 *
 * @author Jagadish Ramu
 */
@Service
@PerLookup
public class ResourceProxy implements NamingObjectProxy.InitializationNamingObjectProxy {

    @Inject
    private Provider<org.glassfish.resourcebase.resources.util.ResourceManagerFactory> resourceManagerFactoryProvider;

    @Inject
    private org.glassfish.resourcebase.resources.naming.ResourceNamingService namingService;

    private Resource resource = null;
    private Object result = null;
    private org.glassfish.resourcebase.resources.api.ResourceInfo resourceInfo = null;

    public Object create(Context ic) throws NamingException {
        //this is a per-lookup object and once we have the resource,
        //we remove the proxy and bind the resource (ref) with same jndi-name
        //hence block synchronization is fine as it blocks only callers
        //of this particular resource and also only for first time (initialization)
        synchronized(this){
            try{
                if(result == null){
                    getResourceDeployer(resource).deployResource(resource, resourceInfo.getApplicationName(),
                            resourceInfo.getModuleName());
                }
                result = namingService.lookup(resourceInfo, resourceInfo.getName(), ic.getEnvironment());
            }catch(Exception e){
                throwResourceNotFoundException(e, resourceInfo);
            }
        }
        return result;
    }

    /**
     * Set the resource config bean instance
     * @param resource config bean
     */
    public void setResource(Resource resource){
        this.resource = resource;
    }


    /**
     * Name by which the proxy (or the resource) will be bound in JNDI
     * @param resourceInfo resource-info
     */
    public void setResourceInfo(org.glassfish.resourcebase.resources.api.ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

    protected Object throwResourceNotFoundException(Exception e, org.glassfish.resourcebase.resources.api.ResourceInfo resourceInfo) throws NamingException {
        NamingException ne = new NamingException("Unable to lookup resource : " + resourceInfo);
        ne.initCause(e);
        throw ne;
    }

    /**
     * Given a <i>resource</i> instance, appropriate deployer will be provided
     *
     * @param resource resource instance
     * @return ResourceDeployer
     */
    protected org.glassfish.resourcebase.resources.api.ResourceDeployer getResourceDeployer(Object resource){
        return resourceManagerFactoryProvider.get().getResourceDeployer(resource);
    }
}
