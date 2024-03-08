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

package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.config.serverbeans.Nodes;
import org.glassfish.api.ActionReport;
import com.sun.enterprise.universal.glassfish.TokenResolver;
import com.sun.enterprise.util.StringUtils;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.hk2.api.PerLookup;

import org.jvnet.hk2.annotations.Service;
import java.util.Map;
import java.util.HashMap;
import com.sun.enterprise.util.net.NetUtils;
import java.io.File;
import jakarta.inject.Inject;

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
                Map<String, String> systemPropsMap =
                        new HashMap<String, String>((Map)(System.getProperties()));
                resolver = new TokenResolver(systemPropsMap);
                String resolvedInstallDir = resolver.resolve(installdir);
                File actualInstallDir = new File( resolvedInstallDir+"/" + NodeUtils.LANDMARK_FILE);


                if (!actualInstallDir.exists()){
                    report.setMessage(Strings.get("invalid.installdir",installdir));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
        }
        CommandInvocation ci = cr.getCommandInvocation("_create-node", report, context.getSubject());
        ParameterMap map = new ParameterMap();
        map.add("DEFAULT", name);
        if (StringUtils.ok(nodedir))
            map.add(NodeUtils.PARAM_NODEDIR, nodedir);
        if (StringUtils.ok(installdir))
            map.add(NodeUtils.PARAM_INSTALLDIR, installdir);
        if (StringUtils.ok(nodehost))
            map.add(NodeUtils.PARAM_NODEHOST, nodehost);
        map.add(NodeUtils.PARAM_TYPE,"CONFIG");
        ci.parameters(map);
        ci.execute();

        NodeUtils.sanitizeReport(report);
    }
}
