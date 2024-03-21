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

package org.glassfish.deployment.admin;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.Param;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.ActionReport;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.deployment.common.DeploymentContextImpl;
import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Inject;

import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.config.Transaction;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Domain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.logging.Logger;
import java.util.Properties;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;

/**
 * The command to create application ref for lifecycle module on instance
 */
@Service(name="_lifecycle")
@PerLookup
@ExecuteOn(value={RuntimeType.INSTANCE})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="_lifecycle",
        description="_lifecycle")
})
public class InstanceLifecycleModuleCommand implements AdminCommand, AdminCommandSecurity.AccessCheckProvider {

    @Param(primary=true)
    public String name = null;

    @Param(optional=true)
    public String target = "server";

    @Param(optional=true)
    public String virtualservers = null;

    @Param(optional=true, defaultValue="true")
    public Boolean enabled = true;

    @Param(separator=':')
    public Properties appprops = null;

    @Inject
    Deployment deployment;

    @Inject
    Applications applications;

    @Inject
    private Domain domain;

    @Override
    public Collection<? extends AccessCheck> getAccessChecks() {
        final List<AccessCheck> accessChecks = new ArrayList<AccessCheck>();
        accessChecks.add(new AccessCheck(DeploymentCommandUtils.getTargetResourceNameForNewAppRef(domain, target), "write"));
        return accessChecks;
    }



    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();

        try {
            Application application = applications.getApplication(name);
            Transaction t = new Transaction();

            // create a dummy context to hold params and props
            DeployCommandParameters commandParams = new DeployCommandParameters();
            commandParams.name = name;
            commandParams.target = target;
            commandParams.enabled = enabled;
            commandParams.virtualservers = virtualservers;

            ExtendedDeploymentContext lifecycleContext = new DeploymentContextImpl(report, null, commandParams, null);
            lifecycleContext.getAppProps().putAll(appprops);

            if (application != null) {
                // application element already been synchronized over
                // just write application-ref
                deployment.registerAppInDomainXML(null, lifecycleContext, t, true);
            } else {
                // write both
                t = deployment.prepareAppConfigChanges(lifecycleContext);
                deployment.registerAppInDomainXML(null, lifecycleContext, t);
            }
        } catch(Exception e) {
            report.failure(logger, e.getMessage());
        }
    }
}
