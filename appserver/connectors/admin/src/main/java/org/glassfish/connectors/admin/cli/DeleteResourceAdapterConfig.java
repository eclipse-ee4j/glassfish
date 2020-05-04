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

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.connectors.config.ResourceAdapterConfig;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import java.beans.PropertyVetoException;

import jakarta.inject.Inject;

/**
 * Delete RA Config command
 *
 */
@org.glassfish.api.admin.ExecuteOn(RuntimeType.ALL)
@Service(name="delete-resource-adapter-config")
@PerLookup
@I18n("delete.resource.adapter.config")
public class DeleteResourceAdapterConfig implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(DeleteResourceAdapterConfig.class);

    @Param(name="raname", primary=true)
    private String raName;

    @Param(optional=true, obsolete = true)
    private String target = SystemPropertyConstants.DAS_SERVER_NAME;

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
        if (raName== null) {
            report.setMessage(localStrings.getLocalString("delete.resource.adapter.config.noRARName",
                            "No RAR name defined for resource adapter config."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        // ensure we already have this resource
        if(ConnectorsUtil.getResourceByName(domain.getResources(), ResourceAdapterConfig.class, raName) == null){
            report.setMessage(localStrings.getLocalString("delete.resource.adapter.config.notfound",
                    "Resource-Adapter-Config for {0} does not exist.", raName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            // delete resource-adapter-config
            if (ConfigSupport.apply(new SingleConfigCode<Resources>() {
                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    ResourceAdapterConfig resource = (ResourceAdapterConfig)
                            ConnectorsUtil.getResourceByName(domain.getResources(), ResourceAdapterConfig.class, raName);
                    if (resource != null && resource.getResourceAdapterName().equals(raName)) {
                        return param.getResources().remove(resource);
                    }
                    // not found
                    return null;
                }
            }, domain.getResources()) == null) {
                report.setMessage(localStrings.getLocalString("delete.resource.adapter.config.fail",
                                "Unable to delete resource adapter config {0}", raName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        } catch(TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString("delete.resource.adapter.config.fail",
                            "Unable to delete resource adapter config {0}", raName)
                            + " " + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }

        //report.setMessage(localStrings.getLocalString("delete.resource.adapter.config.success",
        //        "Resource adapter config {0} deleted", raName));
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
