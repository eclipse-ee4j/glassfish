/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.Param;
import org.jvnet.hk2.annotations.Service;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Engine;
import com.sun.enterprise.config.serverbeans.Module;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.util.Collection;

import jakarta.inject.Inject;
import org.glassfish.hk2.api.PerLookup;
import java.util.List;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;

@Service(name="_list-app-refs")
@ExecuteOn(value={RuntimeType.DAS})
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.GET,
        path="_list-app-refs",
        description="_list-app-refs")
})
public class ListAppRefsCommand implements AdminCommand, AdminCommandSecurity.AccessCheckProvider {

    @Param(optional=true)
    String target = "server";

    @Param(optional=true)
    String type = null;

    @Param(optional=true, defaultValue="all")
    String state;

    @Inject
    Domain domain;

    @Inject
    Applications applications;

    private List<ApplicationRef> appRefs;

    @Override
    public Collection<? extends AccessCheck> getAccessChecks() {
        final List<AccessCheck> accessChecks = new ArrayList<AccessCheck>();
        appRefs = domain.getApplicationRefsInTarget(target);
        for (ApplicationRef appRef : appRefs) {
            accessChecks.add(new AccessCheck(AccessRequired.Util.resourceNameFromConfigBeanProxy(appRef), "read"));
        }
        return accessChecks;
    }


    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        ActionReport.MessagePart part = report.getTopMessagePart();
        part.setMessage(target);
        part.setChildrenType("application");
        for (ApplicationRef appRef : appRefs) {
            if (state.equals("all") ||
               (state.equals("running") &&
                Boolean.valueOf(appRef.getEnabled())) ||
               (state.equals("non-running") &&
                !Boolean.valueOf(appRef.getEnabled())) ) {
                if (isApplicationOfThisType(appRef.getRef(), type)) {
                    ActionReport.MessagePart childPart = part.addChild();
                    childPart.setMessage(appRef.getRef());
                }
            }
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }


    private boolean isApplicationOfThisType(String name, String type) {
        if (type == null)  {
            return true;
        }

        Application app = applications.getApplication(name);
        if (app != null) {
            if (!app.isStandaloneModule()) {
                if (type.equals("ear")) {
                    return true;
                } else {
                    return false;
                }
            }
            for (Module module : app.getModule()) {
                final List<Engine> engineList = module.getEngines();
                for (Engine engine : engineList) {
                    if (engine.getSniffer().equals(type)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
