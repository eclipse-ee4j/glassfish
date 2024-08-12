/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.connectors.config.ResourceAdapterConfig;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;

/**
 * List Resource Adapter Configs command
 *
 */
@TargetType(value={CommandTarget.DAS,CommandTarget.DOMAIN, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTERED_INSTANCE })
@Service(name="list-resource-adapter-configs")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn(value={RuntimeType.DAS})
@I18n("list.resource.adapter.configs")
@RestEndpoints({
    @RestEndpoint(configBean=Resources.class,
        opType=RestEndpoint.OpType.GET,
        path="list-resource-adapter-configs",
        description="list-resource-adapter-configs")
})
public class ListResourceAdapterConfigs implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListResourceAdapterConfigs.class);

    @Param(name="raname", optional=true)
    private String raName;

    @Param(name="long", optional=true, defaultValue="false", shortName="l", alias="verbose")
    private Boolean long_opt;

    @Param(primary = true, optional = true, alias = "targetName", obsolete = true)
    private String target;

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

        try {
            HashMap<String, List<Property>> raMap = new HashMap<String, List<Property>>();
            boolean raExists = false;
            Collection<ResourceAdapterConfig> resourceAdapterConfigs =
                    domain.getResources().getResources(ResourceAdapterConfig.class);
            for (ResourceAdapterConfig r : resourceAdapterConfigs) {
                if (raName != null && !raName.isEmpty()) {
                    if (r.getResourceAdapterName().equals(raName)) {
                        raMap.put(raName, r.getProperty());
                        raExists = true;
                        break;
                    }
                } else {
                    raMap.put(r.getResourceAdapterName(), r.getProperty());
                }
            }
            if (raName != null && !raName.isEmpty() && !raExists) {
                report.setMessage(localStrings.getLocalString("delete.resource.adapter.config.notfound",
                        "Resource adapter {0} not found.", raName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
            /**
              get the properties if long_opt=true. Otherwise return the name.
             */
            if (long_opt) {
                for (Entry<String, List<Property>> raEntry : raMap.entrySet()) {
                    final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                    part.setMessage(raEntry.getKey());
                    for (Property prop : raEntry.getValue()) {
                        final ActionReport.MessagePart propPart = part.addChild();
                        propPart.setMessage("\t" + prop.getName() + "=" + prop.getValue());
                    }
                }
            } else {
                for (String ra : raMap.keySet()) {
                    final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                    part.setMessage(ra);
                }
            }
        } catch (Exception e) {
            String failMsg = localStrings.getLocalString("list.resource.adapter.configs.fail",
                    "Unable to list resource adapter configs.");
            Logger.getLogger(ListResourceAdapterConfigs.class.getName()).log(Level.SEVERE,
                    failMsg, e);
            report.setMessage(failMsg + " " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
