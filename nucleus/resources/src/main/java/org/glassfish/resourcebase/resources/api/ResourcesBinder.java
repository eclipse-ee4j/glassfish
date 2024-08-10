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

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;
import org.glassfish.resourcebase.resources.ResourceLoggingConstansts;
import org.jvnet.hk2.annotations.Service;


/**
 * Binds proxy objects in the jndi namespace for all the resources and pools defined in the
 * configuration. Those objects will delay the instantiation of the actual resources and pools
 * until code looks them up in the naming manager.
 *
 * @author Jerome Dochez, Jagadish Ramu
 */
@Service
public class ResourcesBinder {

    @Inject
    private GlassfishNamingManager manager;

    @LogMessagesResourceBundle
    public static final String LOGMESSAGE_RESOURCE = "org.glassfish.resourcebase.resources.LogMessages";

    @LoggerInfo(subsystem="RESOURCE", description="Nucleus Resource", publish=true)

    public static final String LOGGER = "jakarta.enterprise.resources.api";
    private static final Logger logger = Logger.getLogger(LOGGER, LOGMESSAGE_RESOURCE);

    @Inject
    private Provider<org.glassfish.resourcebase.resources.api.ResourceProxy> resourceProxyProvider;

    @Inject
    private org.glassfish.resourcebase.resources.naming.ResourceNamingService resourceNamingService;

    /**
     * deploy proxy for the resource
     * @param resourceInfo   jndi name with which the resource need to be deployed
     * @param resource config object of the resource
     */
    public void deployResource(org.glassfish.resourcebase.resources.api.ResourceInfo resourceInfo, Resource resource){
        try{
            bindResource(resourceInfo, resource);
        }catch(NamingException ne){
            Object[] params = {resourceInfo, ne};
            logger.log(Level.SEVERE,ResourceLoggingConstansts.BIND_RESOURCE_FAILED, params);
        }
    }

    /**
     * bind proxy for the resource
     * @param resource config object of the resource
     * @param jndiName jndi name with which the resource need to be deployed
     * @throws NamingException
     */
    private void bindResource(org.glassfish.resourcebase.resources.api.ResourceInfo resourceInfo, Resource resource) throws NamingException {
        org.glassfish.resourcebase.resources.api.ResourceProxy proxy = resourceProxyProvider.get();
        proxy.setResource(resource);
        proxy.setResourceInfo(resourceInfo);
        resourceNamingService.publishObject(resourceInfo, proxy, true);
    }
}
