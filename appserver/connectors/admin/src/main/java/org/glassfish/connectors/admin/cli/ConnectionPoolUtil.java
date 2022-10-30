/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.connectors.admin.cli;

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.naming.SimpleJndiName;
import org.jvnet.hk2.annotations.Service;

@Service
@Singleton
public class ConnectionPoolUtil {

    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(ConnectionPoolUtil.class);

    @Inject
    private Applications applications;
    @Inject
    private Domain domain;
    @Inject
    private ServerEnvironment env;


    public boolean isValidApplication(String applicationName, String poolName, ActionReport report) {
        if (applicationName == null) {
            setAppNameNeededErrorMessage(report);
            return false;
        }

        Application application = applications.getApplication(applicationName);
        if (application == null) {
            setApplNotFoundErrorMessage(report, applicationName);
            return false;
        }
        if ("true".equalsIgnoreCase(application.getEnabled())) {
            Server server = domain.getServerNamed(env.getInstanceName());
            ApplicationRef appRef = server.getApplicationRef(applicationName);
            if (appRef == null) {
                setAppDisabledErrorMessage(report, applicationName, poolName);
                return false;
            }
            if (appRef.getRef().equals(applicationName)) {
                if ("false".equalsIgnoreCase(appRef.getEnabled())) {
                    setAppDisabledErrorMessage(report, applicationName, poolName);
                    return false;
                }
                return true;
            }
        } else {
            setAppDisabledErrorMessage(report, applicationName, poolName);
        }
        return false;
    }

    public boolean isValidModule(String applicationName, String moduleName, String poolName, ActionReport report) {
        Application application = applications.getApplication(applicationName);
        if (!isValidApplication(applicationName, poolName, report)) {
            return false;
        }
        Module module = application.getModule(moduleName);
        if (module == null) {
            setModuleNotFoundErrorMessage(report, moduleName, applicationName);
            return false;
        }
        return true;
    }

    public boolean isValidPool(Resources resources, SimpleJndiName poolName, String prefix, ActionReport report) {
        if (resources == null) {
            setResourceNotFoundErrorMessage(report, poolName);
            return false;
        }
        if (ConnectorsUtil.getResourceByName(resources, ResourcePool.class, poolName) == null) {
            setResourceNotFoundErrorMessage(report, poolName);
            return false;
        }
        return true;
    }

    private void setAppNameNeededErrorMessage(ActionReport report) {
        report.setMessage(I18N.getLocalString(
                "pool.util.app.name.needed",
                "--appname is needed when --modulename is specified"));
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);

    }

    private void setAppDisabledErrorMessage(ActionReport report, String applicationName, String poolName) {
        report.setMessage(I18N.getLocalString(
                "pool.util.app.is.not.enabled",
                "Application [ {0} ] in which the pool " +
                "[ {1} ] is defined, is not enabled", applicationName, poolName));
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);

    }

    private void setApplNotFoundErrorMessage(ActionReport report, String applicationName){
        report.setMessage(I18N.getLocalString(
                "pool.util.app.does.not.exist",
                "Application {0} does not exist.", applicationName));
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
    }

    private void setModuleNotFoundErrorMessage(ActionReport report, String moduleName, String applicationName){
        report.setMessage(I18N.getLocalString(
                "pool.util.module.does.not.exist",
                "Module {0} does not exist in application {1}.", moduleName, applicationName));
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
    }

    private void setResourceNotFoundErrorMessage(ActionReport report, SimpleJndiName poolName){
        report.setMessage(I18N.getLocalString(
                "pool.util.pool.does.not-exist",
                "Pool {0} does not exist.", poolName));
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
    }
}
