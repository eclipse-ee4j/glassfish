/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.v3.server;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.v3.admin.CommandRunnerImpl;
import com.sun.enterprise.v3.common.XMLActionReporter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.config.ApplicationName;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.InternalSystemAdministrator;
import org.glassfish.kernel.KernelLoggerInfo;

/**
 * Triggers reloads of deployed applications depending on the presence of and
 * timestamp on a .reload file in the application's top-level directory.
 *
 * An instance of this class can be reused, its run method invoked repeatedly
 * to check all known apps for their .reload files.
 *
 * @author tjquinn
 */
public class DynamicReloader implements Runnable {

    private static final String RELOAD_FILE_NAME = ".reload";

    private static class SyncBoolean {
        private boolean b;

        private SyncBoolean(final boolean initialValue) {
            b = initialValue;
        }

        private synchronized void set(final boolean value) {
            b = value;
        }

        private synchronized boolean get() {
            return b;
        }
    }
    private final SyncBoolean inProgress;

    /** Records info about apps being monitored */
    private Map<String,AppReloadInfo> appReloadInfo;

    private final AtomicBoolean cancelRequested = new AtomicBoolean(false);

    private final Applications applications;

    private final Logger logger = KernelLoggerInfo.getLogger();

    private final ServiceLocator locator;

    private final Subject kernelSubject;

    DynamicReloader(Applications applications, ServiceLocator locator) throws URISyntaxException {
        this.applications = applications;
        this.locator = locator;
        initAppReloadInfo(applications);
        inProgress = new SyncBoolean(false);
        final InternalSystemAdministrator kernelIdentity = locator.getService(InternalSystemAdministrator.class);
        kernelSubject = kernelIdentity.getSubject();
    }

    /**
     * Records reload information about the currently-known applications.
     *
     * @param applications
     */
    private synchronized void initAppReloadInfo(Applications applications) throws URISyntaxException {
        appReloadInfo = new HashMap<>();
        logger.fine("[Reloader] Preparing list of apps to monitor:");
        for (ApplicationName m : applications.getModules()) {
            if (m instanceof Application) {
                Application app = (Application) m;
                if (Boolean.parseBoolean(app.getDeployProperties().getProperty(ServerTags.IS_LIFECYCLE))) {
                    // skip lifecycle modules
                    continue;
                }
                AppReloadInfo info = new AppReloadInfo(app);
                appReloadInfo.put(app.getName(), info);
                logger.fine("[Reloader] Monitoring " + app.getName() + " at " + app.getLocation());
            }
        }
    }

    @Override
    public void run() {
        markInProgress();
        try {
            reloadApps();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clearInProgress();
        }
    }

    void cancel() {
        cancelRequested.set(true);
    }

    void init() {
        cancelRequested.set(false);
    }

    private void reloadApps() throws URISyntaxException, IOException {
        List<AppReloadInfo> appsToReload = chooseAppsToReload();
        for (AppReloadInfo appInfo : appsToReload) {
            if (cancelRequested.get()) {
                break;
            }
            reloadApp(appInfo);
        }
    }

    private synchronized List<AppReloadInfo> chooseAppsToReload() throws URISyntaxException {
        List<AppReloadInfo> result = new ArrayList<>();

        /*
         * The collectionof AppReloadInfo might not contain entries for all
         * current apps (for example, if an app has been deployed since the
         * previous run of the reloader).  Use the current list of all known
         * apps, and for each of those try to find an AppReloadInfo entry for
         * it.
         */
        Set<AppReloadInfo> possiblyUndeployedApps = new HashSet<>(appReloadInfo.values());

        for (ApplicationName m : applications.getModules()) {
            if (m instanceof Application) {
                Application app = (Application) m;
                if (Boolean.parseBoolean(app.getDeployProperties().getProperty
                    (ServerTags.IS_LIFECYCLE))) {
                    // skip lifecycle modules
                    continue;
                }
                AppReloadInfo reloadInfo = findOrCreateAppReloadInfo(app);
                if (reloadInfo.needsReload()) {
                    logger.fine("[Reloader] Selecting app " + reloadInfo.getApplication().getName() + " to reload");
                    result.add(reloadInfo);
                }
                possiblyUndeployedApps.remove(reloadInfo);
            }
        }

        /*
         * Remove any apps from the reload info that are no longer present.
         */
        for (AppReloadInfo info : possiblyUndeployedApps) {
            logger.fine("[Reloader] Removing undeployed app " + info.getApplication().getName() + " from reload info");
            appReloadInfo.remove(info.getApplication().getName());
        }


        return result;
    }

    private synchronized AppReloadInfo findOrCreateAppReloadInfo(Application app) throws URISyntaxException {
        AppReloadInfo result = appReloadInfo.get(app.getName());
        if (result == null) {
            logger.fine("[Reloader] Recording info for new app " + app.getName() + " at " + app.getLocation());
            result = new AppReloadInfo(app);
            appReloadInfo.put(app.getName(), result);
        }
        return result;
    }

    private void reloadApp(AppReloadInfo appInfo) throws IOException {
        logger.fine("[Reloader] Reloading " + appInfo.getApplication().getName());

        /*
         * Prepare a deploy command and invoke it, taking advantage of the
         * DeployCommand's logic to deal with redeploying an existing app.
         *
         * Note that the redeployinplace internal option tells the undeploy
         * command (which is invoked by the deploy command) to preserve the
         * existing directory, even if the configuration does not indicate that
         * the app is directory-deployed.
         *
         */
        CommandRunnerImpl commandRunner = locator.getService(CommandRunnerImpl.class);

        ParameterMap deployParam = new ParameterMap();
        deployParam.set(DeploymentProperties.FORCE, Boolean.TRUE.toString());
        deployParam.set(DeploymentProperties.PATH, appInfo.getApplicationDirectory().getCanonicalPath());
        deployParam.set(DeploymentProperties.NAME, appInfo.getApplication().getName());
        deployParam.set(DeploymentProperties.KEEP_REPOSITORY_DIRECTORY, "true");
        CommandInvocation invocation = commandRunner
            .getCommandInvocation("deploy", new XMLActionReporter(), kernelSubject).parameters(deployParam);
        invocation.execute();
        appInfo.recordLoad();
    }

    private void markInProgress() {
        inProgress.set(true);
    }

    private void clearInProgress() {
        synchronized(inProgress) {
            inProgress.set(false);
            inProgress.notifyAll();
        }
    }

    public void waitUntilIdle() throws InterruptedException {
        synchronized(inProgress) {
            while (inProgress.get()) {
                inProgress.wait();
            }
        }
    }

    /**
     * Records information about every application, regardless of whether the
     * app has a .reload file or not.
     *
     * The latestRecordedLoad time records either the object creation time (which should
     * be about the same as the initial load time of the app during a server
     * restart or after a deployment) or the time at which an app was reloaded.
     *
     * Note that this class uses the fact that lastModified of a non-existing
     * file is 0.
     */
    private final static class AppReloadInfo {
        /** points to the .reload file, whether one exists for this app or not */
        private final File reloadFile;

        private long latestRecordedLoad;

        /** application info */
        private final Application app;

        private final File appDir;

        private AppReloadInfo(Application app) throws URISyntaxException {
            this.app = app;
            this.appDir = app.getLocation() == null ? null : new File(new URI(app.getLocation()));
            reloadFile = new File(appDir, RELOAD_FILE_NAME);
            recordLoad();
        }

        private Application getApplication() {
            return app;
        }

        private boolean needsReload() {
            boolean answer = reloadFile.lastModified() > latestRecordedLoad;
            return answer;
        }

        private void recordLoad() {
            latestRecordedLoad = System.currentTimeMillis();
        }

        private File getApplicationDirectory() {
            return appDir;
        }
    }

}
