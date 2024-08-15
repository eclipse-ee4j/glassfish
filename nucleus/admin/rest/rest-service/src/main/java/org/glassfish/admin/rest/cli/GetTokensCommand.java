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

package org.glassfish.admin.rest.cli;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.v3.common.ActionReporter;

import jakarta.inject.Inject;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.PropertyResolver;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author jasonlee
 */
@Service(name = "__resolve-tokens")
@PerLookup
@TargetType(value = { CommandTarget.DAS, CommandTarget.DOMAIN, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE,
        CommandTarget.CLUSTERED_INSTANCE })
@ExecuteOn(RuntimeType.DAS)
@RestEndpoints({
        @RestEndpoint(configBean = Cluster.class, opType = RestEndpoint.OpType.GET, path = "resolve-tokens", description = "Resolve Tokens", params = {
                @RestParam(name = "target", value = "$parent") }),
        @RestEndpoint(configBean = Domain.class, opType = RestEndpoint.OpType.GET, path = "resolve-tokens", description = "Resolve Tokens", params = {
                @RestParam(name = "target", value = "$parent") }),
        @RestEndpoint(configBean = Server.class, opType = RestEndpoint.OpType.GET, path = "resolve-tokens", description = "Resolve Tokens", params = {
                @RestParam(name = "target", value = "$parent") }),
        @RestEndpoint(configBean = Config.class, opType = RestEndpoint.OpType.GET, path = "resolve-tokens", description = "Resolve Tokens", params = {
                @RestParam(name = "target", value = "$parent") }) })
public class GetTokensCommand implements AdminCommand {
    @Inject
    private Domain domain;

    @Inject
    private ServiceLocator habitat;

    @Param(separator = ',', primary = true)
    String[] tokens;

    @Param(name = "check-system-properties", defaultValue = "false", optional = true)
    boolean checkSystemProperties;

    @Param(optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    String target = SystemPropertyConstants.DAS_SERVER_NAME;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReporter report = (ActionReporter) context.getActionReport();
        PropertyResolver resolver = new PropertyResolver(domain, target);

        String sep = "";
        String eol = System.getProperty("line.separator");
        StringBuilder output = new StringBuilder();
        Map<String, String> values = new TreeMap<String, String>();
        Properties properties = new Properties();
        properties.put("tokens", values);

        for (String token : tokens) {
            String value = resolver.getPropertyValue(token);
            if ((value == null) && (checkSystemProperties)) {
                value = System.getProperty(token);
            }
            output.append(sep).append(token).append(" = ").append(value);
            sep = eol;
            values.put(token, value);
        }

        report.setMessage(output.toString());
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        report.setExtraProperties(properties);

        return;
    }
}
