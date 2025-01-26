/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.web;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.deployment.Application;

import jakarta.inject.Inject;

import java.io.File;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.container.RequestDispatcher;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.deployment.common.ApplicationConfigInfo;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.internal.deployment.GenericHandler;
import org.glassfish.javaee.core.deployment.JavaEEDeployer;
import org.glassfish.loader.util.ASClassLoaderUtil;
import org.glassfish.web.LogFacade;
import org.glassfish.web.deployment.descriptor.WebBundleDescriptorImpl;
import org.glassfish.web.jsp.JSPCompiler;
import org.jvnet.hk2.annotations.Service;

/**
 * Web module deployer.
 *
 * @author jluehe
 * @author Jerome Dochez
 */
@Service
public class WebDeployer extends JavaEEDeployer<WebContainer, WebApplication>{

    private static final Logger logger = LogFacade.getLogger();

    private static final ResourceBundle rb = logger.getResourceBundle();

    @Inject
    ServerContext sc;

    @Inject
    Domain domain;

    @Inject
    RequestDispatcher dispatcher;

    /**
     * Constructor
     */
    public WebDeployer() {
    }

    /**
     * Returns the meta data assocated with this Deployer
     *
     * @return the meta data for this Deployer
     */
    @Override
    public MetaData getMetaData() {
        return new MetaData(false,
                new Class[] {WebBundleDescriptorImpl.class}, new Class[] {Application.class});
    }

    @Override
    public <V> V loadMetaData(Class<V> type, DeploymentContext dc) {

        WebBundleDescriptorImpl wbd = dc.getModuleMetaData(WebBundleDescriptorImpl.class);

        if (wbd.isStandalone()) {
            // the context root should be set using the following precedence
            // for standalone web module
            // 1. User specified value through DeployCommand
            // 2. Context root value specified through sun-web.xml
            // 3. Context root from last deployment if applicable
            // 4. The default context root which is the archive name
            //    minus extension
            DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);
            String contextRoot = params.contextroot;
            if(contextRoot==null) {
                contextRoot = wbd.getContextRoot();
                if("".equals(contextRoot)) {
                    contextRoot = null;
                }
            }
            if(contextRoot==null) {
                contextRoot = params.previousContextRoot;
            }
            if(contextRoot==null) {
                contextRoot = ((GenericHandler)dc.getArchiveHandler()).getDefaultApplicationNameFromArchiveName(dc.getOriginalSource());
            }

            if (!contextRoot.startsWith("/")) {
                contextRoot = "/" + contextRoot;
            }
            wbd.setContextRoot(contextRoot);
            wbd.setName(params.name());

            // set the context root to deployment context props so this value
            // will be persisted in domain.xml
            dc.getAppProps().setProperty(ServerTags.CONTEXT_ROOT, contextRoot);
        }

        return null;
    }

    private WebModuleConfig loadWebModuleConfig(DeploymentContext dc) {

        WebModuleConfig wmInfo = new WebModuleConfig();

        try {
            DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);
            wmInfo.setDescriptor(dc.getModuleMetaData(WebBundleDescriptorImpl.class));
            wmInfo.setVirtualServers(params.virtualservers);
            wmInfo.setLocation(dc.getSourceDir());
            wmInfo.setObjectType(dc.getAppProps().getProperty(ServerTags.OBJECT_TYPE));
        } catch (Exception ex) {
            String msg = rb.getString(LogFacade.UNABLE_TO_LOAD_CONFIG);
            msg = MessageFormat.format(msg, wmInfo.getName());
            logger.log(Level.WARNING, msg, ex);
        }

        return wmInfo;

    }

    @Override
    protected void generateArtifacts(DeploymentContext dc)
        throws DeploymentException {
        DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);
        if (params.precompilejsp) {
            //call JSPCompiler...
            runJSPC(dc);
        }
    }


    @Override
    public WebApplication load(WebContainer container, DeploymentContext dc) {
        super.load(container, dc);
        WebBundleDescriptorImpl wbd = dc.getModuleMetaData(
            WebBundleDescriptorImpl.class);
        if (wbd != null) {
            wbd.setClassLoader(dc.getClassLoader());
        }

        WebModuleConfig wmInfo = loadWebModuleConfig(dc);
        WebApplication webApp = new WebApplication(container, wmInfo,
                new ApplicationConfigInfo(dc.getAppProps()));
        return webApp;
    }


    @Override
    public void unload(WebApplication webApplication, DeploymentContext dc) {

    }

    /**
     * This method setups the in/outDir and classpath and invoke
     * JSPCompiler.
     * @param dc - DeploymentContext to get command parameters and
     *             source directory and compile jsp directory.
     * @throws DeploymentException if JSPCompiler is unsuccessful.
     */
    void runJSPC(final DeploymentContext dc) throws DeploymentException {
        final WebBundleDescriptorImpl wbd = dc.getModuleMetaData(
            WebBundleDescriptorImpl.class);
        try {
            final File outDir = dc.getScratchDir(ServerEnvironment.kCompileJspDirName);
            final File inDir  = dc.getSourceDir();

            StringBuilder classpath = new StringBuilder(super.getCommonClassPath());
            classpath.append(File.pathSeparatorChar);
            final DeployCommandParameters dcParams = dc.getCommandParameters(DeployCommandParameters.class);
            classpath.append(ASClassLoaderUtil.getModuleClassPath(sc.getDefaultServices(),
                wbd.getApplication().getName(), dcParams.libraries));
            classpath.append(File.pathSeparatorChar);
            classpath.append(super.getModuleClassPath(dc));
            JSPCompiler.compile(inDir, outDir, wbd, classpath.toString(), sc);
        } catch (DeploymentException de) {
            String msg = rb.getString(LogFacade.JSPC_FAILED);
            msg = MessageFormat.format(msg, wbd.getApplication().getName());
            logger.log(Level.SEVERE, msg, de);
            throw de;
        }
    }
}
