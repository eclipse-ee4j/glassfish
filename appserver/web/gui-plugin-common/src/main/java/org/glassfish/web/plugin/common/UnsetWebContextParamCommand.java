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

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.web.config.serverbeans.WebModuleConfig;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author tjquinn
 */
@Service(name="unset-web-context-param")
@I18n("unsetWebContextParam.command")
@PerLookup
@RestEndpoints({
    @RestEndpoint(configBean=Application.class,
        opType=RestEndpoint.OpType.POST, // TODO: Should be DELETE?
        path="unset-web-context-param",
        description="unset-web-context-param",
        params={
            @RestParam(name="id", value="$parent")
        })
})
public class UnsetWebContextParamCommand extends WebModuleConfigCommand {

    @Param(name="name")
    private String name;

    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();

        try {
            WebModuleConfig config = webModuleConfig(report);
            if (config == null) {
                return;
            }
            config.deleteContextParam(name);
        } catch (Exception e) {
            fail(report, e, "errUnsetContextParam", "Error unsetting context-param");
        }

    }

}
