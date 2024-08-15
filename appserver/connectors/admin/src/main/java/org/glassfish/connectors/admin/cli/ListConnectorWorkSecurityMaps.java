/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.connectors.config.GroupMap;
import org.glassfish.connectors.config.PrincipalMap;
import org.glassfish.connectors.config.WorkSecurityMap;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * List Connector Work Security Maps
 *
 */
@Service(name="list-connector-work-security-maps")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.connector.work.security.maps")
@RestEndpoints({
    @RestEndpoint(configBean=SecurityService.class,
        opType=RestEndpoint.OpType.GET,
        path="list-connector-work-security-maps",
        description="List Connector Work Security Maps")
})
public class ListConnectorWorkSecurityMaps implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ListConnectorWorkSecurityMaps.class);

    @Param(name="securitymap", optional=true)
    String securityMap;

    @Param(name="resource-adapter-name", primary=true)
    String raName;

    @Inject
    Domain domain;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final ActionReport.MessagePart mp = report.getTopMessagePart();

        try {
            boolean foundWSM = false;
            Collection<WorkSecurityMap> workSecurityMaps =
                    domain.getResources().getResources(WorkSecurityMap.class);
            for (WorkSecurityMap wsm : workSecurityMaps) {
                if (wsm.getResourceAdapterName().equals(raName)) {
                    if (securityMap == null) {
                        listWorkSecurityMap(wsm, mp);
                        foundWSM = true;
                    } else if (wsm.getName().equals(securityMap)) {
                        listWorkSecurityMap(wsm, mp);
                        foundWSM = true;
                        break;
                    }
                }
            }
            if (!foundWSM) {
                 report.setMessage(localStrings.getLocalString(
                        "list.connector.work.security.maps.workSecurityMapNotFound",
                        "Nothing to list. Either the resource adapter {0} does not exist or the " +
                                "resource adapter {0} is not associated with any work security map.", raName));
            }

        } catch (Exception e) {
            Logger.getLogger(ListConnectorWorkSecurityMaps.class.getName()).log(Level.SEVERE,
                    "list-connector-work-security-maps failed", e);
            report.setMessage(localStrings.getLocalString("" +
                    "list.connector.work.security.maps.fail",
                    "Unable to list connector work security map {0} for resource adapter {1}", securityMap, raName) + " " +
                    e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private void listWorkSecurityMap(WorkSecurityMap wsm, ActionReport.MessagePart mp) {
        List<PrincipalMap> principalList = wsm.getPrincipalMap();
        List<GroupMap> groupList = wsm.getGroupMap();

        for (PrincipalMap map : principalList) {
            final ActionReport.MessagePart part = mp.addChild();
            part.setMessage(localStrings.getLocalString(
                    "list.connector.work.security.maps.eisPrincipalAndMappedPrincipal",
                    "{0}: EIS principal={1}, mapped principal={2}",
                    wsm.getName(), map.getEisPrincipal(), map.getMappedPrincipal()));
        }

        for (GroupMap map : groupList) {
            final ActionReport.MessagePart part = mp.addChild();
            part.setMessage(localStrings.getLocalString(
                    "list.connector.work.security.maps.eisGroupAndMappedGroup",
                    "{0}: EIS group={1}, mapped group={2}",
                    wsm.getName(), map.getEisGroup(), map.getMappedGroup()));
        }
    }
}
