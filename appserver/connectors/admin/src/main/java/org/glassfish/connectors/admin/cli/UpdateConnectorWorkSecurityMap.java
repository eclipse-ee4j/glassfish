/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.connectors.config.GroupMap;
import org.glassfish.connectors.config.PrincipalMap;
import org.glassfish.connectors.config.WorkSecurityMap;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Update Connector Work Security Map command
 */
@Service(name = "update-connector-work-security-map")
@PerLookup
@I18n("update.connector.work.security.map")
@RestEndpoints({
    @RestEndpoint(configBean=WorkSecurityMap.class,
        opType=RestEndpoint.OpType.POST,
        path="update-connector-work-security-map",
        description="Update",
        params={
            @RestParam(name="id", value="$parent")
        })
})
public class UpdateConnectorWorkSecurityMap implements AdminCommand {
    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(UpdateConnectorSecurityMap.class);

    @Param(name = "raname")
    String raName;

    @Param(name = "addprincipals", optional = true)
    Properties addPrincipals;

    @Param(name = "addgroups", optional = true)
    Properties addGroups;

    @Param(name = "removeprincipals", optional = true)
    List<String> removePrincipals;

    @Param(name = "removegroups", optional = true)
    List<String> removeGroups;

    @Param(name = "mapname", primary = true)
    String securityMapName;

    @Inject
    Domain domain;


    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        if (addPrincipals == null && addGroups == null && removeGroups == null && removePrincipals == null) {
            report.setMessage(localStrings.getLocalString("update.connector.work.security.map.noargs",
                    "update-connector-work-security-map should be executed with atleast one optional argument of " +
                            "either add(principals/usergroups) or remove(principals/usergroups)"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (!WorkSecurityMapHelper.doesResourceAdapterNameExist(raName, domain.getResources())) {
            report.setMessage(localStrings.getLocalString("update.connector.work.security.map.noSuchRAFound",
                    "Resource Adapter {0} does not exist. Please specify a resource adapter name.", raName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (!WorkSecurityMapHelper.doesMapNameExist(raName, securityMapName, domain.getResources())) {
            report.setMessage(localStrings.getLocalString("update.connector.work.security.map.mapNotExist",
                    "WorkSecurity map {0} does not exist for resource adapter {1}. Please give a valid map name.",
                    securityMapName, raName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }


        //check if addPrincipals and removePrincipals have the same value
        if (addPrincipals != null && removePrincipals != null) {
            Iterator it_1 = addPrincipals.entrySet().iterator();
            while (it_1.hasNext()) {
                String ap = ((Map.Entry) it_1.next()).getKey().toString();
                for (String rp : removePrincipals) {
                    if (rp.equals(ap)) {
                        report.setMessage(localStrings.getLocalString(
                                "update.connector.work.security.map.samePrincipalValues",
                                "This value {0} is given in both --addprincipals and --removeprincipals. " +
                                        "The same value cannot given for these options.",
                                ap));
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        return;
                    }

                }
            }
        }

        //check if addUserGroups and removeUserGroups have the same value
        if (addGroups != null && removeGroups != null) {
            Iterator it_1 = addGroups.entrySet().iterator();
            while (it_1.hasNext()) {
                String ag = ((Map.Entry) it_1.next()).getKey().toString();
                for (String rg : removeGroups) {
                    if (rg.equals(ag)) {
                        report.setMessage(localStrings.getLocalString(
                                "update.connector.work.security.map.sameUsergroupValues",
                                "This value {0} is given in both --addusergroups and --removeusergroups. " +
                                        "The same value cannot given for these options.",
                                ag));
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        return;
                    }

                }
            }
        }

        WorkSecurityMap map = WorkSecurityMapHelper.getSecurityMap(securityMapName, raName, domain.getResources());
        final List<PrincipalMap> existingPrincipals = new ArrayList(map.getPrincipalMap());
        final List<GroupMap> existingUserGroups = new ArrayList(map.getGroupMap());

        if (existingPrincipals.isEmpty() && addPrincipals != null) {
            report.setMessage(localStrings.getLocalString("update.connector.work.security.map." +
                    "addPrincipalToExistingUserGroupsWorkSecurityMap",
                    "Failed to add principals to a security map with user groups."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (existingUserGroups.isEmpty() && addGroups != null) {
            report.setMessage(localStrings.getLocalString("update.connector.work.security.map." +
                    "addUserGroupsToExistingPrincipalsWorkSecurityMap",
                    "Failed to add user groups to a security map with principals."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (addPrincipals == null && addGroups == null) {
            boolean principalsEmpty = false;
            boolean userGroupsEmpty = false;

            if ((removePrincipals != null) &&
                    (removePrincipals.size() == existingPrincipals.size())) {
                principalsEmpty = true;
            }
            if ((removeGroups != null) &&
                    (removeGroups.size() == existingUserGroups.size())) {
                userGroupsEmpty = true;
            }
            if (userGroupsEmpty || principalsEmpty) {
                report.setMessage(localStrings.getLocalString("" +
                        "update.connector.work.security.map.principals_usergroups_will_be_null",
                        "The values in your command will delete all principals and usergroups. You cannot " +
                                "delete all principals and usergroups. Atleast one of them must exist."));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        if (removePrincipals != null) {
            for (String rp : removePrincipals) {
                boolean principalExist = false;
                for (PrincipalMap pm : existingPrincipals) {
                    if (pm.getEisPrincipal().equals(rp)) {
                        principalExist = true;
                        break;
                    }
                }
                if (!principalExist) {
                    report.setMessage(localStrings.getLocalString("" +
                            "update.connector.work.security.map.principalNotExists",
                            "The principal {0} that you want to delete does not exist in security map {1}." +
                                    " Please give a valid principal name.",
                            rp, securityMapName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }

        }

        if (removeGroups != null) {
            for (String rg : removeGroups) {
                boolean usergroupExist = false;
                for (GroupMap gm : existingUserGroups) {
                    if (gm.getEisGroup().equals(rg)) {
                        usergroupExist = true;
                        break;
                    }
                }
                if (!usergroupExist) {
                    report.setMessage(localStrings.getLocalString("" +
                            "update.connector.work.security.map.usergroupNotExists",
                            "The usergroup {0} that you want to delete does not exist in security map {1}. " +
                                    "Please give a valid user-group name.",
                            rg, securityMapName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
        }

        if (addPrincipals != null) {
            for (Map.Entry e : addPrincipals.entrySet()) {
                for (PrincipalMap pm : existingPrincipals) {
                    if (pm.getEisPrincipal().equals(e.getKey())) {
                        report.setMessage(localStrings.getLocalString("" +
                                "update.connector.work.security.map.principalExists",
                                "The principal {0} already exists in security map {1}. " +
                                        "Please give a different principal name.",
                                e.getKey(), securityMapName));
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        return;
                    }
                }
            }
        }

        if (addGroups != null) {
            for (Map.Entry e : addGroups.entrySet()) {
                for (GroupMap gm : existingUserGroups) {
                    if (gm.getEisGroup().equals(e.getKey())) {
                        report.setMessage(localStrings.getLocalString("" +
                                "update.connector.work.security.map.groupExists",
                                "The Group {0} already exists in security map {1}. " +
                                        "Please give a different group name.",
                                e.getKey(), securityMapName));
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        return;
                    }
                }
            }
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<WorkSecurityMap>() {
                public Object run(WorkSecurityMap wsm) throws PropertyVetoException, TransactionFailure {
                    if (addGroups != null) {
                        for (Map.Entry e : addGroups.entrySet()) {
                            GroupMap gm = wsm.createChild(GroupMap.class);
                            gm.setEisGroup((String) e.getKey());
                            gm.setMappedGroup((String) e.getValue());
                            wsm.getGroupMap().add(gm);
                        }
                    } else if (addPrincipals != null) {
                        for (Map.Entry e : addPrincipals.entrySet()) {
                            PrincipalMap pm = wsm.createChild(PrincipalMap.class);
                            pm.setEisPrincipal((String) e.getKey());
                            pm.setMappedPrincipal((String) e.getValue());
                            wsm.getPrincipalMap().add(pm);
                        }
                    }
                    if (removeGroups != null) {
                        for (String rg : removeGroups) {
                            for (GroupMap gm : existingUserGroups) {
                                if (gm.getEisGroup().equals(rg)) {
                                    wsm.getGroupMap().remove(gm);
                                }
                            }
                        }
                    } else if (removePrincipals != null) {
                        for (String rp : removePrincipals) {
                            for (PrincipalMap pm : existingPrincipals) {
                                if (pm.getEisPrincipal().equals(rp)) {
                                    wsm.getPrincipalMap().remove(pm);
                                }
                            }
                        }
                    }
                    return wsm;
                }
            }, map);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (TransactionFailure tfe) {
            Object params[] = {securityMapName, raName};
            report.setMessage(localStrings.getLocalString("update.connector.work.security.map.fail",
                    "Unable to update security map {0} for resource adapter {1}.", params) +
                    " " + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }
    }
}
