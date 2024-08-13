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

package org.glassfish.orb.admin.cli;


import com.sun.enterprise.config.serverbeans.Config;
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
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.glassfish.orb.admin.config.IiopListener;
import org.glassfish.orb.admin.config.IiopService;
import org.jvnet.hk2.annotations.Service;


/**
 * List IIOP Listener command
 *
 */

@Service(name="list-iiop-listeners")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.iiop.listeners")
@ExecuteOn(value={RuntimeType.DAS})
@TargetType(value={CommandTarget.CLUSTER,CommandTarget.CONFIG,
    CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,
    CommandTarget.CLUSTERED_INSTANCE, CommandTarget.DOMAIN }
)
@RestEndpoints({
    @RestEndpoint(configBean=IiopService.class,
        opType=RestEndpoint.OpType.GET,
        path="list-iiop-listeners",
        description="list-iiop-listeners")
})
public class ListIiopListeners implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ListIiopListeners.class);

    @Param( primary=true, name="target", optional=true,
        defaultValue=SystemPropertyConstants.DAS_SERVER_NAME)
    String target ;

    @Inject
    ServiceLocator services ;


    /**
     * Executes the command
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final Target targetUtil = services.getService(Target.class ) ;
        final Config config = targetUtil.getConfig(target) ;
        final IiopService iiopService = config.getExtensionByType(IiopService.class);

        try {
            List<IiopListener> listenerList = iiopService.getIiopListener();
            for (IiopListener listener : listenerList) {
                final ActionReport.MessagePart part = report.getTopMessagePart()
                        .addChild();
                part.setMessage(listener.getId());
            }
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("list.iiop.listener" +
                    ".fail", "List IIOP listeners failed."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }
}
