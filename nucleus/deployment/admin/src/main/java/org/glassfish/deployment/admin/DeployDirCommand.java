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

import com.sun.enterprise.util.LocalStringManagerImpl;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;



/**
 *
 * deploydir command
 *
 */
@Service(name="deploydir")
@PerLookup
@I18n("deploydir.command")
@ExecuteOn(value={RuntimeType.DAS})
@TargetType(value={CommandTarget.DOMAIN, CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
public class DeployDirCommand extends DeployCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeployDirCommand.class);

    /**
     * Executes the command.
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        ActionReport.MessagePart msgPart = report.getTopMessagePart();
        msgPart.setChildrenType("WARNING");
        ActionReport.MessagePart childPart = msgPart.addChild();
        String commandName = getClass().getAnnotation(Service.class).name();
        childPart.setMessage(localStrings.getLocalString("deploydir.command.deprecated", "{0} command deprecated.  Please use {1} command instead.", commandName, "deploy"));
        super.execute(context);
    }
}
