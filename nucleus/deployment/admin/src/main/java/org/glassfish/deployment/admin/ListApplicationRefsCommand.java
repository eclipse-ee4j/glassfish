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

import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.ColumnFormatter;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * List application ref command
 */
@Service(name="list-application-refs")
@I18n("list.application.refs")
@ExecuteOn(value={RuntimeType.DAS})
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@TargetType(value={CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
@RestEndpoints({
    @RestEndpoint(configBean=Applications.class,
        opType=RestEndpoint.OpType.GET,
        path="list-application-refs",
        description="list-applications-refs")
})
public class ListApplicationRefsCommand implements AdminCommand, AdminCommandSecurity.AccessCheckProvider {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListApplicationRefsCommand.class);

    @Param(primary=true, optional=true)
    String target = "server";

    @Param(optional=true, defaultValue="false", name="long", shortName="l")
    public Boolean long_opt = false;

    @Param(optional=true, defaultValue="false", shortName="t")
    public Boolean terse = false;

    @Inject
    Domain domain;

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



    /**
     * Entry point from the framework into the command execution
     * @param context context for the command.
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final ActionReport subReport = report.addSubActionsReport();
        ColumnFormatter cf = new ColumnFormatter();

        ActionReport.MessagePart part = report.getTopMessagePart();
        int numOfApplications = 0;
        if ( !terse && long_opt ) {
            String[] headings= new String[] { "NAME", "STATUS" };
            cf = new ColumnFormatter(headings);
        }
        for (ApplicationRef ref : appRefs) {
            Object[] row = new Object[] { ref.getRef() };
            if( !terse && long_opt ){
                row = new Object[]{ ref.getRef(), getLongStatus(ref) };
            }
            cf.addRow(row);
            numOfApplications++;
        }
        if (numOfApplications != 0) {
            report.setMessage(cf.toString());
        } else if ( !terse) {
            subReport.setMessage(localStrings.getLocalString(
                    DeployCommand.class,
                    "NoSuchAppDeployed",
                    "No applications are deployed to this target {0}.",
                    new Object[] {this.target}));
            part.setMessage(localStrings.getLocalString("list.components.no.elements.to.list", "Nothing to List."));
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private String getLongStatus(ApplicationRef ref) {
       String message = "";
       if (DeploymentUtils.isDomainTarget(target)) {
           // ignore --verbose for target domain
           return message;
       }
       boolean isVersionEnabled = domain.isAppRefEnabledInTarget(ref.getRef(), target);
       if ( isVersionEnabled ) {
           message = localStrings.getLocalString("list.applications.verbose.enabled", "enabled");
       } else {
           message = localStrings.getLocalString("list.applications.verbose.disabled", "disabled");
       }
       return message;
   }
}
