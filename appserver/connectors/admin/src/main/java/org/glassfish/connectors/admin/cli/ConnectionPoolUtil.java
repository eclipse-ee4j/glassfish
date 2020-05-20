/*
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
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ServerEnvironment;
import com.sun.enterprise.config.serverbeans.Module;

import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Singleton;

import jakarta.inject.Inject;

@Service
@Singleton
public class ConnectionPoolUtil {

    @Inject
    private Applications applications;

    @Inject
    private Domain domain;

    @Inject
    private ServerEnvironment env;

    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(ConnectionPoolUtil.class);

    public boolean isValidApplication(String applicationName, String poolName, ActionReport report) {

        boolean isValid = false;

        if(applicationName == null){
            setAppNameNeededErrorMessage(report);
            return isValid;
        }

        Application application = applications.getApplication(applicationName);
        if (application != null) {
            if (application.getEnabled().equalsIgnoreCase("true")) {
                Server server = domain.getServerNamed(env.getInstanceName());
                ApplicationRef appRef = server.getApplicationRef(applicationName);
                if (appRef != null) {
                        if (appRef.getRef().equals(applicationName)) {
                            if (appRef.getEnabled().equalsIgnoreCase("false")) {
                                setAppDisabledErrorMessage(report, applicationName, poolName);
                            } else {
                                isValid = true;
                            }
                        }
                } else {
                    setAppDisabledErrorMessage(report, applicationName, poolName);
                }
            } else {
                setAppDisabledErrorMessage(report, applicationName, poolName);
            }
        } else {
            setApplNotFoundErrorMessage(report, applicationName);
        }
        return isValid;
    }

    public boolean isValidModule(String applicationName, String moduleName, String poolName, ActionReport report) {
        boolean isValid = false;

        Application application = applications.getApplication(applicationName);

        if(!isValidApplication(applicationName, poolName, report)){
            return false;
        }

        Module module = application.getModule(moduleName);
        if(module != null){
            isValid = true;
        }else{
            setModuleNotFoundErrorMessage(report, moduleName, applicationName);
        }
        return isValid;
    }

    public boolean isValidPool(Resources resources, String poolName, String prefix, ActionReport report) {
        boolean isValid = false;
        if (resources != null) {
            if (ConnectorsUtil.getResourceByName(resources, ResourcePool.class, poolName) != null) {
                isValid = true;
            } else {
                setResourceNotFoundErrorMessage(report, poolName);
            }
        } else {
            setResourceNotFoundErrorMessage(report, poolName);
        }
        return isValid;
    }

    private void setAppNameNeededErrorMessage(ActionReport report) {
        report.setMessage(localStrings.getLocalString(
                "pool.util.app.name.needed",
                "--appname is needed when --modulename is specified"));
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);

    }

    private void setAppDisabledErrorMessage(ActionReport report, String applicationName, String poolName) {
        report.setMessage(localStrings.getLocalString(
                "pool.util.app.is.not.enabled",
                "Application [ {0} ] in which the pool " +
                "[ {1} ] is defined, is not enabled", applicationName, poolName));
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);

    }

    private void setApplNotFoundErrorMessage(ActionReport report, String applicationName){
        report.setMessage(localStrings.getLocalString(
                "pool.util.app.does.not.exist",
                "Application {0} does not exist.", applicationName));
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
    }

    private void setModuleNotFoundErrorMessage(ActionReport report, String moduleName, String applicationName){
        report.setMessage(localStrings.getLocalString(
                "pool.util.module.does.not.exist",
                "Module {0} does not exist in application {1}.", moduleName, applicationName));
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
    }

    private void setResourceNotFoundErrorMessage(ActionReport report, String poolName){
        report.setMessage(localStrings.getLocalString(
                "pool.util.pool.does.not-exist",
                "Pool {0} does not exist.", poolName));
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
    }
}
