/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.admin.monitor.cli;

import com.sun.enterprise.admin.monitor.jndi.JndiNameLookupHelper;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.util.List;

import javax.naming.NamingException;

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
import org.jvnet.hk2.annotations.Service;

@Service(name = "list-jndi-entries")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.jndi.entries")
@ExecuteOn(value = { RuntimeType.INSTANCE })
@TargetType(value = { CommandTarget.DOMAIN, CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER,
        CommandTarget.CLUSTERED_INSTANCE })
@RestEndpoints({
        @RestEndpoint(configBean = Resources.class, opType = RestEndpoint.OpType.GET, path = "list-jndi-entries", description = "list-jndi-entries") })
public class ListJndiEntries implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListJndiEntries.class);

    @Param(name = "context", optional = true)
    String contextName;

    @Param(primary = true, optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    String target;

    public void execute(AdminCommandContext context) {
        List<String> names = null;
        final ActionReport report = context.getActionReport();

        try {
            names = getNames(contextName);
        } catch (NamingException e) {
            report.setMessage(localStrings.getLocalString("list.jndi.entries.namingexception", "Naming Exception caught.") + " "
                    + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }

        try {
            if (names.isEmpty()) {
                final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setMessage(localStrings.getLocalString("list.jndi.entries.empty", "Nothing to list."));
            } else {
                for (String jndiName : names) {
                    final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                    part.setMessage(jndiName);
                }
            }
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("" + "list.jndi.entries.fail", "Unable to list jndi entries.") + " "
                    + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

    private List<String> getNames(String context) throws NamingException {
        List<String> names = null;
        JndiNameLookupHelper helper = new JndiNameLookupHelper();
        names = helper.getJndiEntriesByContextPath(context);
        return names;
    }
}
