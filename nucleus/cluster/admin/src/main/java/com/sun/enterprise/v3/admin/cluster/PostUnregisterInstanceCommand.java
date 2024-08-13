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

import com.sun.enterprise.admin.util.ClusterOperationUtil;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.Supplemental;
import org.glassfish.common.util.admin.ParameterMapExtractor;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.jvnet.hk2.annotations.Service;

/**
 * Causes InstanceRegisterInstanceCommand executions on the correct remote instances.
 *
 * @author Jennifer Chou
 */
@Service(name="_post-unregister-instance")
@Supplemental(value="_unregister-instance", ifFailure=FailurePolicy.Warn)
@PerLookup
@ExecuteOn(value={RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="_post-unregister-instance",
        description="_post-unregister-instance")
})
public class PostUnregisterInstanceCommand implements AdminCommand {
    @Param(name = "node", optional = true)
    public String node;
    @Param(name = "name", primary = true)
    public String instanceName;

    @Inject
    private ServiceLocator habitat;

    @Inject
    private Target target;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();
        final String clusterName = context.getActionReport().getResultType(String.class);
        if (clusterName != null) {
            try {
                ParameterMapExtractor pme = new ParameterMapExtractor(this);
                final ParameterMap paramMap = pme.extract();
                List<String> targets = new ArrayList<String>();
                List<Server> instances = target.getInstances(clusterName);
                for (Server s : instances) {
                    targets.add(s.getName());
                }

                ClusterOperationUtil.replicateCommand(
                        "_unregister-instance",
                        FailurePolicy.Warn,
                        FailurePolicy.Warn,
                        FailurePolicy.Ignore,
                        targets,
                        context,
                        paramMap,
                        habitat);
            } catch (Exception e) {
                report.failure(logger, e.getMessage());
            }
        }
    }
}
