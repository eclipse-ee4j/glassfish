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

package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.connectors.config.WorkSecurityMap;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Delete Connector Work Security Map
 *
 */
@ExecuteOn(RuntimeType.ALL)
@Service(name="delete-connector-work-security-map")
@PerLookup
@I18n("delete.connector.work.security.map")
public class DeleteConnectorWorkSecurityMap implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(DeleteConnectorWorkSecurityMap.class);

    @Param(name="raname")
    private String raName;

    @Param(name="mapname", primary=true)
    private String mapName;

    @Inject
    private Domain domain;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        // ensure we already have this resource
        if (!isResourceExists()) {
            report.setMessage(localStrings.getLocalString(
                    "delete.connector.work.security.map.notFound",
                    "A connector work security map named {0} for resource adapter {1} does not exist.",
                    mapName, raName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            // delete connector-work-security-map
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                Collection<WorkSecurityMap> workSecurityMaps =
                        domain.getResources().getResources(WorkSecurityMap.class);
                public Object run(Resources param) throws PropertyVetoException,
                        TransactionFailure {
                    for (WorkSecurityMap resource : workSecurityMaps) {
                        if (resource.getName().equals(mapName) &&
                                resource.getResourceAdapterName().equals(raName)) {
                            param.getResources().remove(resource);
                            break;
                        }
                    }
                    return workSecurityMaps;
                }
            }, domain.getResources());

        } catch (TransactionFailure tfe) {
            Logger.getLogger(DeleteConnectorWorkSecurityMap.class.getName()).log(Level.SEVERE,
                    "delete-connector-work-security-map failed", tfe);
            report.setMessage(localStrings.getLocalString("" +
                    "delete.connector.work.security.map.fail",
                    "Unable to delete connector work security map {0} for resource adapter {1}", mapName, raName) + " "
                    + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
            return;
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private boolean isResourceExists() {
        for (Resource resource : domain.getResources().getResources()) {
            if (resource instanceof WorkSecurityMap) {
                if (((WorkSecurityMap) resource).getName().equals(mapName) &&
                        ((WorkSecurityMap) resource).getResourceAdapterName().equals(raName))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
