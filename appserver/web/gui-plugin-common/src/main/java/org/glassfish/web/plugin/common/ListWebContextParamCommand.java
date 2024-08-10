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

package org.glassfish.web.plugin.common;

import com.sun.enterprise.config.serverbeans.Application;

import java.text.MessageFormat;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.web.config.serverbeans.ContextParam;
import org.glassfish.web.config.serverbeans.WebModuleConfig;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author tjquinn
 */
@Service(name="list-web-context-param")
@I18n("listWebContextParam.command")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@RestEndpoints({
    @RestEndpoint(configBean=Application.class,
        opType=RestEndpoint.OpType.GET,
        path="list-web-context-param",
        description="list-web-context-param",
        params={
            @RestParam(name="name", value="$parent")
        })
})
public class ListWebContextParamCommand extends WebModuleConfigCommand {
    @Param(name="name",optional=true)
    private String name;

    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();

        WebModuleConfig config = webModuleConfig(report);
        if (config == null) {
            return;
        }

        ActionReport.MessagePart part = report.getTopMessagePart();
        final String format = localStrings.getLocalString(
                "listWebContextParamFormat", "{0} = {1} ignoreDescriptorItem={2} //{3}");
        int reported = 0;
        for (ContextParam param : config.contextParamsMatching(name)) {
            ActionReport.MessagePart childPart = part.addChild();
            childPart.setMessage(MessageFormat.format(format,
                    param.getParamName(),
                    param.getParamValue(),
                    param.getIgnoreDescriptorItem(),
                    descriptionValueOrNotSpecified(param.getDescription())));
            reported++;
        }
        succeed(report, "listSummary",
                "Reported {0,choice,0#no {1} settings|1#one {1} setting|1<{0,number,integer} {1} settings}",
                reported, "context-param");
    }
}
