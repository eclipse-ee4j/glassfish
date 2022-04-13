/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.osgi.cli.remote;

import com.sun.enterprise.admin.remote.ServerRemoteAdminCommand;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;

import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.osgi.cli.remote.impl.OsgiShellService;
import org.glassfish.osgi.cli.remote.impl.SessionOperation;
import org.jvnet.hk2.annotations.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;

import static org.glassfish.osgi.cli.remote.impl.OsgiShellService.ASADMIN_OSGI_SHELL;
import static org.glassfish.osgi.cli.remote.impl.OsgiShellServiceProvider.detectService;

/**
 * A simple AdminCommand that bridges to the Felix Shell Service.
 * Since the Felix Shell Service is compatible with all OSGi platforms,
 * this command is named as osgi instead of felix.
 *
 * @author ancoron
 */
@Service(name = "osgi")
@CommandLock(CommandLock.LockType.SHARED)
@I18n("osgi")
@PerLookup
@TargetType({CommandTarget.CLUSTERED_INSTANCE, CommandTarget.STANDALONE_INSTANCE})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="osgi",
        description="Remote OSGi Shell Access")
})
@AccessRequired(resource="domain/osgi/shell", action="execute")
public class OSGiShellCommand implements AdminCommand, PostConstruct {

    private static final Logger LOG = Logger.getLogger(OSGiShellCommand.class.getPackage().getName());

    @Param(name = "command-line", primary = true, optional = true, multiple = true, defaultValue = "help")
    private Object commandLine;

    @Param(name = "session", optional = true, acceptableValues = "new,list,execute,stop")
    private String sessionOp;

    @Param(name = "session-id", optional = true)
    private String sessionId;

    @Param(name = "instance", optional = true)
    private String instance;

    protected BundleContext ctx;

    @Inject
    ServiceLocator locator;

    @Inject
    Domain domain;


    @Override
    public void postConstruct() {
        if (ctx == null) {
            final Bundle me = BundleReference.class.cast(getClass().getClassLoader()).getBundle();
            ctx = me.getBundleContext();
        }
    }


    @Override
    public void execute(final AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        if (instance != null) {
            final Server server = domain.getServerNamed(instance);
            if (server == null) {
                report.setMessage("No server target found for " + instance);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
            final String host = server.getAdminHost();
            final int port = server.getAdminPort();
            try {
                final ServerRemoteAdminCommand remote
                    = new ServerRemoteAdminCommand(locator, "osgi", host, port, false, "admin", "", LOG);

                final ParameterMap params = new ParameterMap();
                if (commandLine == null) {
                    params.set("default", ASADMIN_OSGI_SHELL);
                } else if (commandLine instanceof String) {
                    params.set("default", (String) commandLine);
                } else if (commandLine instanceof List) {
                    params.set("default", (List<String>) commandLine);
                }
                if (sessionOp != null) {
                    params.set("session", sessionOp);
                }
                if (sessionId != null) {
                    params.set("session-id", sessionId);
                }
                report.setMessage(remote.executeCommand(params));
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
                return;
            } catch(final CommandException e) {
                report.setMessage("Remote execution failed: " + e.getMessage());
                report.setFailureCause(e);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        // must not be null, called services forbid it.
        final String cmdName;
        final String cmd;
        if (commandLine == null) {
            cmdName = ASADMIN_OSGI_SHELL;
            cmd = ASADMIN_OSGI_SHELL;
        } else if (commandLine instanceof String) {
            cmdName = (String) commandLine;
            cmd = cmdName;
        } else if (commandLine instanceof List) {
            @SuppressWarnings("unchecked")
            final List<String> list = (List<String>) commandLine;
            cmdName = list.isEmpty() ? "" : list.get(0);
            cmd = list.isEmpty() ? "" : list.stream().collect(Collectors.joining(" "));
        } else if (commandLine instanceof String[]) {
            final String[] list = (String[]) commandLine;
            cmdName = list.length == 0 ? "" : list[0];
            cmd = list.length == 0 ? "" : Arrays.stream(list).collect(Collectors.joining(" "));
        } else {
            report.setMessage("Unable to deal with argument list of type " + commandLine.getClass().getName());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        try {
            final SessionOperation sessionOperation = SessionOperation.parse(sessionOp);
            final OsgiShellService service = detectService(ctx, sessionOperation, sessionId, report);
            if (service == null) {
                report.setMessage("No Shell Service available");
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
            service.exec(cmdName, cmd);
        } catch (final Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage());
            report.setMessage(ex.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
    }
}
