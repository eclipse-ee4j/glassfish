/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.server.logging.commands;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.server.logging.ServerLogFileManager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * @author cmott
 */
@ExecuteOn(value = {RuntimeType.INSTANCE}, ifOffline = FailurePolicy.Error, ifNeverStarted = FailurePolicy.Error)
@Service(name = "rotate-log")
@TargetType({CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CLUSTERED_INSTANCE})
@PerLookup
@I18n("rotate.log")
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="rotate-log",
        description="Rotate Log")
})
public class RotateLog implements AdminCommand {

    @Inject
    private ServerLogFileManager serverLogFileManager;

    @Inject
    Domain domain;

    @Inject
    ServerEnvironment env;

    @Param(optional = true)
    String target = SystemPropertyConstants.DAS_SERVER_NAME;

    final private static LocalStringManagerImpl LOCAL_STRINGS = new LocalStringManagerImpl(RotateLog.class);

    @Override
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        serverLogFileManager.roll();

        String msg = LOCAL_STRINGS.getLocalString("rotated.log.message", "Rotated log for server instance {0}.", env.getInstanceName());
        report.setMessage(msg);
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
