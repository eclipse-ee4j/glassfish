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

package com.sun.enterprise.v3.admin;

import static com.sun.enterprise.admin.util.ClusterOperationUtil.replicateCommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Dom;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.v3.common.PropsFileActionReporter;

import jakarta.inject.Inject;

/**
 * User: Jerome Dochez Date: Jul 12, 2008 Time: 1:27:53 AM
 */
@Service(name = "list")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
public class ListCommand extends V2DottedNameSupport implements AdminCommand, AdminCommandSecurity.Preauthorization, AdminCommandSecurity.AccessCheckProvider {

    @Inject
    private MonitoringReporter monitoringReporter;

    @Inject
    Domain domain;

    @Inject
    ServerEnvironment serverEnv;

    @Inject
    Target targetService;

    @Inject
    ServiceLocator serviceLocator;

    // How to define short option name?
    @Param(name = "MoniTor", optional = true, defaultValue = "false", shortName = "m", alias = "Mon")
    Boolean monitor;

    @Param(primary = true)
    String pattern = "";

    @Inject
    @Optional
    private MonitoringRuntimeDataRegistry monitoringRuntimeDataRegistry;

    private Map<Dom, String> matchingNodes;
    private TreeNode[] parentNodes;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        if (monitor) {
            return preAuthorizationForMonitoring(context);
        }

        return preAuthorizationForNonMonitoring(context);
    }

    private boolean preAuthorizationForMonitoring(final AdminCommandContext context) {
        monitoringReporter.prepareList(context, pattern);
        return true;
    }

    private boolean preAuthorizationForNonMonitoring(final AdminCommandContext context) {
        // First let's get the parent for this pattern.
        parentNodes = getAliasedParent(domain, pattern);
        Map<Dom, String> dottedNames = new HashMap<>();
        for (TreeNode parentNode : parentNodes) {
            dottedNames.putAll(getAllDottedNodes(parentNode.node));
        }

        // Reset the pattern.
        pattern = parentNodes[0].relativeName;

        matchingNodes = getMatchingNodes(dottedNames, pattern);
        if (matchingNodes.isEmpty() && pattern.lastIndexOf('.') != -1) {
            // it's possible the user is just looking for an attribute, let's remove the
            // last element from the pattern.
            matchingNodes = getMatchingNodes(dottedNames, pattern.substring(0, pattern.lastIndexOf(".")));
        }

        return true;
    }

    @Override
    public Collection<? extends AccessCheck> getAccessChecks() {
        if (monitor) {
            return getAccessChecksForMonitoring();
        }

        return getAccessChecksForNonMonitoring();
    }

    private Collection<? extends AccessCheck> getAccessChecksForMonitoring() {
        return monitoringReporter.getAccessChecksForList();
    }

    private Collection<? extends AccessCheck> getAccessChecksForNonMonitoring() {
        final Collection<AccessCheck> accessChecks = new ArrayList<>();
        for (Entry<Dom, String> entry : matchingNodes.entrySet()) {
            accessChecks.add(new AccessCheck(AccessRequired.Util.resourceNameFromDom(entry.getKey()), "read"));
        }

        return accessChecks;
    }

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();

        /* Issue 5918 Used in ManifestManager to keep output sorted */
        try {
            PropsFileActionReporter reporter = (PropsFileActionReporter) report;
            reporter.useMainChildrenAttribute(true);
        } catch (ClassCastException e) {
            // ignore, this is not a manifest output
        }

        if (monitor) {
            listMonitorElements(context);
            return;
        }

        List<Entry<Dom, String>> matchingNodesSorted = sortNodesByDottedName(matchingNodes);
        for (Entry<Dom, String> node : matchingNodesSorted) {
            ActionReport.MessagePart part = report.getTopMessagePart().addChild();
            part.setChildrenType("DottedName");
            if (parentNodes[0].name.isEmpty()) {
                part.setMessage(node.getValue());
            } else {
                part.setMessage(parentNodes[0].name + "." + node.getValue());
            }
        }
    }

    private void listMonitorElements(AdminCommandContext ctxt) {
        monitoringReporter.execute();
    }

    public void callInstance(ActionReport report, AdminCommandContext context, String targetName) {
        try {
            ParameterMap paramMap = new ParameterMap();
            paramMap.set("MoniTor", "true");
            paramMap.set("DEFAULT", pattern);
            List<Server> targetList = targetService.getInstances(targetName);

            replicateCommand("list", FailurePolicy.Error, FailurePolicy.Warn, FailurePolicy.Ignore, targetList, context, paramMap,
                    serviceLocator);
        } catch (Exception ex) {
            report.setActionExitCode(ExitCode.FAILURE);
            report.setMessage("Failure while trying get details from instance " + targetName);
        }
    }
}
