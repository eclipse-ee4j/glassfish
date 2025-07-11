/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.universal.glassfish.TokenResolver;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.net.NetUtils;

import jakarta.inject.Inject;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Remote AdminCommand to create a config node.  This command is run only on DAS.
 *  Register the config node on DAS
 *
 * @author Carla Mott
 */
@Service(name = "create-node-config")
@I18n("create.node.config")
@PerLookup
@ExecuteOn({RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Nodes.class,
        opType=RestEndpoint.OpType.POST,
        path="create-node-config",
        description="Create Node Config")
})
public class CreateNodeConfigCommand implements AdminCommand {


    @Inject
    private CommandRunner cr;

    @Param(name="name", primary = true)
    String name;

    @Param(name="nodedir", optional= true)
    String nodedir;

    @Param(name="nodehost", optional= true)
    String nodehost;

    @Param(name = "installdir", optional= true)
    String installdir;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();

        //validate installdir if passed and running on localhost
        if (StringUtils.ok(nodehost)){
            if (NetUtils.isThisHostLocal(nodehost) && StringUtils.ok(installdir)){
                TokenResolver resolver = null;

                // Create a resolver that can replace system properties in strings
                Map<String, String> systemPropsMap = new HashMap<String, String>((Map) (System.getProperties()));
                resolver = new TokenResolver(systemPropsMap);
                Path resolvedInstallDir = new File(resolver.resolve(installdir)).toPath();
                Path actualInstallDir = resolvedInstallDir.resolve(NodeUtils.LANDMARK_FILE);
                if (!actualInstallDir.toFile().exists()) {
                    report.setMessage(Strings.get("invalid.installdir", installdir));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
        }
        CommandInvocation ci = cr.getCommandInvocation("_create-node", report, context.getSubject());
        ParameterMap map = new ParameterMap();
        map.add("DEFAULT", name);
        if (StringUtils.ok(nodedir)) {
            map.add(NodeUtils.PARAM_NODEDIR, nodedir);
        }
        if (StringUtils.ok(installdir)) {
            map.add(NodeUtils.PARAM_INSTALLDIR, installdir);
        }
        if (StringUtils.ok(nodehost)) {
            map.add(NodeUtils.PARAM_NODEHOST, nodehost);
        }
        map.add(NodeUtils.PARAM_TYPE,"CONFIG");
        ci.parameters(map);
        ci.execute();

        NodeUtils.sanitizeReport(report);
    }
}
