/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.config.UnprocessedConfigListener;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.UnprocessedChangeEvent;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 * Return the "restart required" flag.
 *
 * @author Bill Shannon
 */
@Service(name = "_get-restart-required")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("get.restart.required.command")
@RestEndpoints({
        @RestEndpoint(configBean = Domain.class, opType = RestEndpoint.OpType.GET, path = "_get-restart-required", description = "Restart Reasons") })
@AccessRequired(resource = "domain", action = "dump")
public class GetRestartRequiredCommand implements AdminCommand {
    @Param(optional = true)
    private boolean why;

    @Inject
    private UnprocessedConfigListener ucl;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        ActionReport.MessagePart mp = report.getTopMessagePart();

        Properties extraProperties = new Properties();
        Map<String, Object> entity = new HashMap<>();
        mp.setMessage(Boolean.toString(ucl.serverRequiresRestart()));
        entity.put("restartRequired", Boolean.toString(ucl.serverRequiresRestart()));
        List<String> unprocessedChanges = new ArrayList<>();

        for (UnprocessedChangeEvents es : ucl.getUnprocessedChangeEvents()) {
            for (UnprocessedChangeEvent e : es.getUnprocessed()) {
                if (why) {
                    mp.addChild().setMessage(e.getReason());
                }
                unprocessedChanges.add(e.getReason());
            }
        }

        if (!unprocessedChanges.isEmpty()) {
            entity.put("unprocessedChanges", unprocessedChanges);
        }
        extraProperties.put("entity", entity);
        report.setExtraProperties(extraProperties);
    }
}
