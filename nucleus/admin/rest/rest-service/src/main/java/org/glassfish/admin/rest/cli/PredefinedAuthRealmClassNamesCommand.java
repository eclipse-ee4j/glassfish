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

package org.glassfish.admin.rest.cli;

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * returns the list of targets
 *
 * @author ludovic Champenois
 */
@Service(name = "__list-predefined-authrealm-classnames")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@RestEndpoints({
        @RestEndpoint(configBean = Domain.class, path = "list-predefined-authrealm-classnames", description = "List Auth Realm Class Names") })
public class PredefinedAuthRealmClassNamesCommand implements AdminCommand {

    @Inject
    Domain domain;

    @Override
    public void execute(AdminCommandContext context) {
        SecurityUtil su = new SecurityUtil(domain);
        String[] list = su.getPredefinedAuthRealmClassNames();
        ActionReport report = context.getActionReport();
        report.setActionExitCode(ExitCode.SUCCESS);
        ActionReport.MessagePart part = report.getTopMessagePart();

        for (String s : list) {

            ActionReport.MessagePart childPart = part.addChild();
            childPart.setMessage(s);
        }
    }
}
