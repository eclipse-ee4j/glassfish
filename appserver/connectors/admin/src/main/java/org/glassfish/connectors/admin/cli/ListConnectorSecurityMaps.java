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
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.connectors.config.BackendPrincipal;
import org.glassfish.connectors.config.SecurityMap;
import org.glassfish.connectors.config.ConnectorConnectionPool;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.inject.Inject;
import org.glassfish.internal.api.RelativePathResolver;
import org.jvnet.hk2.config.ConfigBean;

/**
 * List Connector Security Maps
 *
 */
@TargetType(value={CommandTarget.DAS,CommandTarget.DOMAIN, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTERED_INSTANCE })
@Service(name="list-connector-security-maps")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn(value={RuntimeType.DAS})
@I18n("list.connector.security.maps")
@RestEndpoints({
    @RestEndpoint(configBean=SecurityService.class,
        opType=RestEndpoint.OpType.GET,
        path="list-connector-security-maps",
        description="List Connector Security Maps")
})
public class ListConnectorSecurityMaps extends ConnectorSecurityMap implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ListConnectorSecurityMaps.class);

    @Param(name="securitymap", optional=true)
    String securityMap;

    @Param(name="long", optional=true, defaultValue="false", shortName="l", alias="verbose")
    Boolean long_opt;

    @Param(name="pool-name", primary=true)
    String poolName;

    @Param(optional = true, alias = "targetName", obsolete = true)
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
        final ActionReport.MessagePart mp = report.getTopMessagePart();

        /* Issue 5918 Used in ManifestManager to keep output sorted */
        //try {
        //    PropsFileActionReporter reporter = (PropsFileActionReporter) report;
        //    reporter.useMainChildrenAttribute(true);
        //} catch(ClassCastException e) {
            // ignore this is not a manifest output.
        //}
        Collection<ConnectorConnectionPool> ccPools =  domain.getResources().getResources(ConnectorConnectionPool.class);

        if (!doesPoolNameExist(poolName, ccPools)) {
            report.setMessage(localStrings.getLocalString("create.connector.security.map.noSuchPoolFound",
                    "Specified connector connection pool {0} does not exist. Please specify a valid pool name.",
                    poolName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (securityMap != null) {
            if (!doesMapNameExist(poolName, securityMap, ccPools)) {
                report.setMessage(localStrings.getLocalString("list.connector.security.maps.securityMapNotFound",
                        "Security map {0} does not exist for connector connection pool {1}. Please give a valid map name.",
                        securityMap, poolName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        try {
            final List<SecurityMap> securityMaps = getAllSecurityMapsForPool(poolName, ccPools);
            if (securityMaps != null && !securityMaps.isEmpty()) {
                if (securityMap == null && long_opt) {
                    for (SecurityMap sm : securityMaps) {
                        listSecurityMapDetails(sm, mp);
                    }
                } else if (securityMap == null && !long_opt) {
                    //print the map names .....
                    for (SecurityMap sm : securityMaps) {
                        listSecurityMapNames(sm, mp);
                    }
                } else {
                    // map name is not null, long_opt is redundant when security map is specified
                    for (SecurityMap sm : securityMaps) {
                        if (sm.getName().equals(securityMap)) {
                            //if (long_opt) {
                                listSecurityMapDetails(sm, mp);
                                break;
                            //} else {
                            //    listSecurityMapNames(sm, mp);
                            //    break;
                            //}
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ListConnectorSecurityMaps.class.getName()).log(Level.SEVERE,
                    "list-connector-security-maps failed", e);
            report.setMessage(localStrings.getLocalString("" +
                    "list.connector.security.maps.fail",
                    "Unable to list security map {0} for connector connection pool {1}", securityMap, poolName) + " " +
                    e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private void listSecurityMapNames(SecurityMap sm, ActionReport.MessagePart mp) {
        final ActionReport.MessagePart part = mp.addChild();
        part.setMessage(sm.getName());
    }

    private void listSecurityMapDetails(SecurityMap sm, ActionReport.MessagePart mp) {
        List<String> principalList = sm.getPrincipal();
        List<String> groupList = sm.getUserGroup();
        BackendPrincipal bp = sm.getBackendPrincipal();

        final ActionReport.MessagePart partSM = mp.addChild();
        partSM.setMessage(sm.getName());

        final ActionReport.MessagePart partPG = partSM.addChild();
        if (!principalList.isEmpty()) {
            partPG.setMessage(localStrings.getLocalString(
                    "list.connector.security.maps.principals","\tPrincipal(s)"));
        }
        if (!groupList.isEmpty()) {
            partPG.setMessage(localStrings.getLocalString(
                    "list.connector.security.maps.groups","\tUser Group(s)"));
        }

        for (String principal : principalList) {
            final ActionReport.MessagePart partP = partPG.addChild();
            partP.setMessage(localStrings.getLocalString(
                    "list.connector.security.maps.eisPrincipal",
                    "\t\t"+principal, principal));
        }

        for (String group : groupList) {
            final ActionReport.MessagePart partG = partPG.addChild();
            partG.setMessage(localStrings.getLocalString(
                    "list.connector.security.maps.eisGroup",
                    "\t\t"+group, group));
        }

        final ActionReport.MessagePart partBP = partPG.addChild();
            partBP.setMessage(localStrings.getLocalString(
                    "list.connector.security.maps.backendPrincipal",
                    "\t"+"Backend Principal"));
        final ActionReport.MessagePart partBPU = partBP.addChild();
            partBPU.setMessage(localStrings.getLocalString(
                    "list.connector.security.maps.username",
                    "\t\t"+"User Name = "+bp.getUserName(), bp.getUserName()));

        if (bp.getPassword() != null && !bp.getPassword().isEmpty()) {
            final String rawPassword = ConfigBean.unwrap(bp).rawAttribute("password");
            final String passwordOutput = RelativePathResolver.getAlias(rawPassword) == null ? "****" : rawPassword;
            final ActionReport.MessagePart partBPP = partBP.addChild();
                partBPP.setMessage(localStrings.getLocalString(
                        "list.connector.security.maps.password",
                        "\t\t"+"Password = "+passwordOutput, passwordOutput));
        }

    }
}
