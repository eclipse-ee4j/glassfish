/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.admin.cli;

import com.sun.ejb.containers.EJBTimerService;
import com.sun.ejb.containers.EjbContainerUtil;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.util.List;

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
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.Target;
import org.jvnet.hk2.annotations.Service;


@Service(name="list-timers")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.timers")
@ExecuteOn(value={RuntimeType.DAS})
@TargetType(value={CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.GET,
        path="list-timers",
        description="List Timers")
})
public class ListTimers implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ListTimers.class);

    @Param(primary=true, optional=true,
    defaultValue=SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    String target;

    @Inject
    private EjbContainerUtil ejbContainerUtil;

    @Inject
    Target targetUtil;

    /**
     * Executes the command
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        String[] serverIds = null;
        if(targetUtil.isCluster(target)) {
            List<Server> serversInCluster = targetUtil.getInstances(target);
            serverIds = new String[serversInCluster.size()];
            for(int i = 0; i < serverIds.length; i++) {
                serverIds[i] = serversInCluster.get(i).getName();
            }
        } else {
            serverIds = new String[] {target};
        }
        try {
            String[] timerCounts = listTimers(serverIds);
            for (int i = 0; i < serverIds.length; i++) {
                final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setMessage(serverIds[i] + ": " + timerCounts[i]);
            }
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("list.timers.failed",
                    "List Timers command failed"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

    private String[] listTimers( String[] serverIds ) {
        String[] result = new String[serverIds.length];
        if (EJBTimerService.isEJBTimerServiceLoaded()) {
            EJBTimerService ejbTimerService = EJBTimerService.getEJBTimerService();
            if (ejbTimerService != null) {
                result = ejbTimerService.listTimers( serverIds );
            }
        } else {
            //FIXME: Should throw IllegalStateException
            for (int i=0; i<serverIds.length; i++) {
                result[i] = "0";
            }
            //throw new com.sun.enterprise.admin.common.exception.AFException("EJB Timer service is null. "
                    //+ "Cannot list timers.");
        }

        return result;
    }

}
