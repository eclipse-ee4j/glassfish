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

package org.glassfish.connectors.admin.cli.internal;


import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.enterprise.config.serverbeans.Resources;

import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Jagadish Ramu
 */
@Service(name = "_get-admin-object-config-properties")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@RestEndpoints({
    @RestEndpoint(configBean=Resources.class,
        opType=RestEndpoint.OpType.GET,
        path="get-admin-object-config-properties",
        description="Get Admin Object Config Properties")
})
public class GetAdminObjectConfigProps implements AdminCommand {
    @Inject
    private ConnectorRuntime connectorRuntime;

    @Param
    private String rarName;

    @Param
    private String adminObjectInterface;

    @Param(optional = true)
    private String adminObjectClass;

    /**
     * @inheritDoc
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        Map<String, String> adminObjectConfigProps ;
        Properties extraProperties = new Properties();
        try {
            if (adminObjectClass == null) {
                adminObjectConfigProps = connectorRuntime.getAdminObjectConfigProps(rarName, adminObjectInterface);
            } else {
                adminObjectConfigProps =
                        connectorRuntime.getAdminObjectConfigProps(rarName, adminObjectInterface, adminObjectClass);
            }
            extraProperties.put("adminObjectConfigProps", adminObjectConfigProps);
            List<String> confidentialProperties = connectorRuntime.getConfidentialProperties(
                    rarName, ConnectorConstants.AO,
                    adminObjectInterface, adminObjectClass);
            extraProperties.put("confidentialConfigProps", confidentialProperties);
            report.setExtraProperties(extraProperties);

        } catch (Exception e) {
            report.setMessage("_get-admin-object-config-properties failed : " + e.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }

        ActionReport.ExitCode ec = ActionReport.ExitCode.SUCCESS;
        report.setActionExitCode(ec);
    }
}
