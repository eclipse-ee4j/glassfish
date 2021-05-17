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

package org.glassfish.kernel.embedded;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.v3.common.PlainTextActionReporter;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.inject.Inject;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.api.deployment.archive.*;
import org.glassfish.deployment.common.*;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.data.*;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.internal.deployment.SnifferManager;
import org.glassfish.internal.embedded.*;
import org.glassfish.internal.embedded.Server;
import org.glassfish.kernel.KernelLoggerInfo;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * @author Jerome Dochez
 */
@Service
public class EmbeddedDeployerImpl implements EmbeddedDeployer {

    @Inject
    Deployment deployment;

    @Inject
    Server server;

    @Inject
    CommandRunner commandRunner;

    @Inject
    ServiceLocator habitat;

    @Inject
    ArchiveFactory factory;

    @Inject
    SnifferManager snifferMgr;

    @Inject
    ServerEnvironment env;

    @Inject
    DasConfig config;

    Map<String, EmbeddedDeployedInfo> deployedApps = new HashMap<String, EmbeddedDeployedInfo>();

    final static Logger logger = KernelLoggerInfo.getLogger();

    @Override
    public File getApplicationsDir() {
        return env.getApplicationRepositoryPath();
    }

    @Override
    public File getAutoDeployDir() {
        return new File(env.getDomainRoot(), config.getAutodeployDir());
    }

    @Override
    public void setAutoDeploy(final boolean flag) {

        String value = config.getAutodeployEnabled();
        boolean active = value!=null && Boolean.parseBoolean(
                config.getAutodeployEnabled());
        if (active!=flag) {
            try {
                ConfigSupport.apply(new SingleConfigCode<DasConfig>() {
                    @Override
                    public Object run(DasConfig dasConfig) throws PropertyVetoException, TransactionFailure {
                        dasConfig.setAutodeployEnabled(Boolean.valueOf(flag).toString());
                        return null;
                    }
                }, config);
            } catch(TransactionFailure e) {
                logger.log(Level.SEVERE, KernelLoggerInfo.exceptionAutodeployment, e);
            }
        }
    }

    @Override
    public String deploy(File archive, DeployCommandParameters params) {
        try {
            ReadableArchive r = factory.openArchive(archive);
            return deploy(r, params);
        } catch (IOException e) {
            logger.log(Level.SEVERE, KernelLoggerInfo.deployException, e);
        }

        return null;
    }

    @Override
    public String deploy(ReadableArchive archive, DeployCommandParameters params) {

        // ensure server is started. start it if not started.
        try {
            server.start();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }

        ActionReport report = new PlainTextActionReporter();
        if (params==null) {
            params = new DeployCommandParameters();
        }
        ExtendedDeploymentContext initialContext = new DeploymentContextImpl(report, archive, params, env);
        ArchiveHandler archiveHandler = null;
        try {
            archiveHandler = deployment.getArchiveHandler(archive);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (archiveHandler==null) {
                throw new RuntimeException("Cannot find archive handler for source archive");
        }
        if (params.name==null) {
                params.name = archiveHandler.getDefaultApplicationName(archive, initialContext);
            }
        ExtendedDeploymentContext context = null;
        try {
            context = deployment.getBuilder(logger, params, report).source(archive).archiveHandler(archiveHandler).build(initialContext);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(params.property != null){
            context.getAppProps().putAll(params.property);
        }

        if(params.properties != null){
            context.getAppProps().putAll(params.properties);
        }

        ApplicationInfo appInfo = null;
        try {
            appInfo = deployment.deploy(context);
        } catch(Exception e) {
            logger.log(Level.SEVERE, KernelLoggerInfo.deployException, e);
        }
        if (appInfo!=null) {
            boolean isDirectory = new File(archive.getURI().getPath()).isDirectory();
            EmbeddedDeployedInfo info = new EmbeddedDeployedInfo(appInfo, context.getModulePropsMap(), context.getAppProps(),
                    isDirectory);
            deployedApps.put(appInfo.getName(), info);
            return appInfo.getName();
        }
        return null;
    }

    @Override
    public void undeploy(String name, UndeployCommandParameters params) {

        ActionReport report = habitat.getService(ActionReport.class, "plain");
        EmbeddedDeployedInfo info = deployedApps.get(name);
        ApplicationInfo appInfo  = info!=null?info.appInfo:null;
        if (appInfo==null) {
            appInfo = deployment.get(name);
        }
        if (appInfo == null) {
            report.setMessage(
                "Cannot find deployed application of name " + name);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        ReadableArchive source = appInfo.getSource();
        if (source == null) {
            report.setMessage(
                "Cannot get source archive for undeployment");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (params==null) {
            params = new UndeployCommandParameters(name);
        }
        params.origin = UndeployCommandParameters.Origin.undeploy;

        ExtendedDeploymentContext deploymentContext;
        try {
            deploymentContext = deployment.getBuilder(logger, params, report).source(source).build();

            if (info!=null) {
                for (ModuleInfo module : appInfo.getModuleInfos()) {
                    info.map.put(module.getName(), module.getModuleProps());
                    deploymentContext.getModuleProps().putAll(module.getModuleProps());
                }
                deploymentContext.setModulePropsMap(info.map);
                deploymentContext.getAppProps().putAll(info.appProps);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot create context for undeployment ", e);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }


        deployment.undeploy(name, deploymentContext);


        if (report.getActionExitCode().equals(ActionReport.ExitCode.SUCCESS)) {
            if (params.keepreposdir == null) {
                params.keepreposdir = false;
            }
            if ( !params.keepreposdir && info != null && !info.isDirectory && source.exists()) {
                FileUtils.whack(new File(source.getURI()));
            }
            //remove context from generated
            deploymentContext.clean();

        }

    }

    @Override
    public void undeployAll() {
        for (String appName : deployedApps.keySet()) {
            undeploy(appName, null);
        }

    }

    private final static class EmbeddedDeployedInfo {
        final ApplicationInfo appInfo;
        final Map<String, Properties> map;
        final boolean isDirectory;
        Properties appProps;


        public EmbeddedDeployedInfo(ApplicationInfo appInfo, Map<String, Properties> map, Properties appProps,
                boolean isDirectory) {
            this.appInfo = appInfo;
            this.map = map;
            this.appProps = appProps;
            this.isDirectory = isDirectory;
        }
    }
}
