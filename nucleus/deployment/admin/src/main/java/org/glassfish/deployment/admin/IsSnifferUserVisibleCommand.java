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

import com.sun.enterprise.config.serverbeans.Application;

import jakarta.inject.Inject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.container.Sniffer;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.deployment.SnifferManager;
import org.jvnet.hk2.annotations.Service;

@Service(name="_is-sniffer-user-visible")
@org.glassfish.api.admin.ExecuteOn(value={RuntimeType.DAS})
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@RestEndpoints({
    @RestEndpoint(configBean=Application.class,
        opType=RestEndpoint.OpType.GET,
        path="is-sniffer-user-visible",
        description="Is Sniffer User Visible")
})
@AccessRequired(resource="domain/sniffers/$sniffername", action="read")
public class IsSnifferUserVisibleCommand implements AdminCommand {

    @Param(primary=true)
    public String sniffername = null;

    @Inject
    SnifferManager snifferManager;

    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        ActionReport.MessagePart part = report.getTopMessagePart();

        Sniffer sniffer = snifferManager.getSniffer(sniffername);

        if (sniffer != null) {
            String isVisible = String.valueOf(sniffer.isUserVisible());

            part.addProperty(DeploymentProperties.IS_SNIFFER_USER_VISIBLE, isVisible);

            part.setMessage(isVisible);
        }
    }
}
