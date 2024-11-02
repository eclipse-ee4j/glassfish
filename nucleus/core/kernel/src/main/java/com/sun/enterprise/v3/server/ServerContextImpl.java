/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.server;

import com.sun.enterprise.glassfish.bootstrap.cfg.StartupContextUtil;
import com.sun.enterprise.module.bootstrap.StartupContext;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.File;
import java.util.Map;

import javax.naming.InitialContext;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * This is the Server Context object.
 *
 * @author Jerome Dochez
 */
@Service
@Singleton
public class ServerContextImpl implements ServerContext, PostConstruct {

    @Inject
    ServerEnvironmentImpl env;

    @Inject
    StartupContext startupContext;

    @Inject
    ServiceLocator services;

    File instanceRoot;
    String[] args;

    /** Creates a new instance of ServerContextImpl */
    @Override
    public void postConstruct() {
        this.instanceRoot = env.getInstanceRoot();
        this.args = new String[startupContext.getArguments().size()*2];
        int i=0;
        for (Map.Entry<Object, Object> entry : startupContext.getArguments().entrySet()) {
            args[i++] = entry.getKey().toString();
            args[i++] = entry.getValue().toString();
        }
    }

    public File getInstanceRoot() {
        return instanceRoot;
    }

    @Override
    public String[] getCmdLineArgs() {
        return args;
    }

    @Override
    public File getInstallRoot() {
        return StartupContextUtil.getInstallRoot(startupContext);
    }

    @Override
    public String getInstanceName() {
        return env.getInstanceName();
    }

    @Override
    public String getServerConfigURL() {
        File domainXML = new File(instanceRoot, ServerEnvironmentImpl.kConfigDirName);
        domainXML = new File(domainXML, ServerEnvironmentImpl.kConfigXMLFileName);
        return domainXML.toURI().toString();
    }

    @Override
    public com.sun.enterprise.config.serverbeans.Server getConfigBean() {
        return services.getService(com.sun.enterprise.config.serverbeans.Server.class);
    }

    @Override
    public InitialContext getInitialContext() {
        GlassfishNamingManager gfNamingManager =
            services.getService(GlassfishNamingManager.class);
        return (InitialContext)gfNamingManager.getInitialContext();
    }

    @Override
    public ClassLoader getCommonClassLoader() {
        return services.<CommonClassLoaderServiceImpl>getService(CommonClassLoaderServiceImpl.class).getCommonClassLoader();
    }

    @Override
    public ClassLoader getSharedClassLoader() {
        return services.<ClassLoaderHierarchy>getService(ClassLoaderHierarchy.class).getConnectorClassLoader(null);
    }

    @Override
    public ClassLoader getLifecycleParentClassLoader() {
        return services.<ClassLoaderHierarchy>getService(ClassLoaderHierarchy.class).getConnectorClassLoader(null);
    }

    @Override
    public InvocationManager getInvocationManager() {
        return services.getService(InvocationManager.class);
    }

    @Override
    public String getDefaultDomainName() {
        return "glassfish-web";
    }
    /**
     * Returns the default services for this instance
     * @return default services
     */
    @Override
    public ServiceLocator getDefaultServices() {
        return services;

    }
}
