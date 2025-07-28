/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.admin.remote.RemoteRestAdminCommand;
import com.sun.enterprise.admin.remote.ServerRemoteRestAdminCommand;
import com.sun.enterprise.admin.util.RemoteInstanceCommandHelper;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.util.ObjectAnalyzer;
import com.sun.enterprise.util.StringUtils;

import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.v3.admin.cluster.StartInstanceCommand.getTimeout;

/**
 *
 * @author bnevins
 */
@Service(name = "restart-instance")
@PerLookup
@CommandLock(CommandLock.LockType.NONE) // don't prevent _synchronize-files
@I18n("restart.instance.command")
@ExecuteOn(RuntimeType.DAS)
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="_restart-instance",
        description="_restart-instance"),
    @RestEndpoint(configBean=Server.class,
        opType=RestEndpoint.OpType.POST,
        path="restart-instance",
        description="restart-instance",
        params={
            @RestParam(name="id", value="$parent"),
        })
})
public class RestartInstanceCommand implements AdminCommand {

    @Inject
    private ServiceLocator habitat;
    @Inject
    private ServerEnvironment env;
    @Param(optional = false, primary = true)
    private String instanceName;
    // no default value!  We use the Boolean as a tri-state.
    @Param(name = "debug", optional = true)
    private String debug;
    @Param(optional = true)
    private Integer timeout;

    private Logger logger;
    private RemoteInstanceCommandHelper helper;
    private ActionReport report;
    private Server instance;
    private String host;
    private int port;
    private String oldPid;
    private AdminCommandContext ctx;


    @Override
    public void execute(AdminCommandContext context) {
        try {
            ctx = context;
            helper = new RemoteInstanceCommandHelper(habitat);
            report = context.getActionReport();
            logger = context.getLogger();
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

            // Each of the methods below immediately returns if there has been an error
            // This is just to avoid a ton of error-checking in this top-level method
            // i.e. it's for readability.

            if (!env.isDas()) {
                setError(Strings.get("restart.instance.notDas", env.getRuntimeType().toString()));
            }

            prepare();
            setOldPid();
            logger.log(Level.FINE, () -> "Restart-instance old-pid = " + oldPid);
            callInstance();
            if (!isError() && !waitForRestart()) {
                setError(Strings.get("restart.instance.timeout", instanceName));
            }
            if (!isError()) {
                String msg = Strings.get("restart.instance.success", instanceName);
                logger.info(msg);
                report.setMessage(msg);
            }
        } catch (InstanceNotRunningException inre) {
            start();
        } catch (CommandException ce) {
            setError(Strings.get("restart.instance.racError", instanceName, ce.getLocalizedMessage()));
        }
    }

    private void prepare() throws InstanceNotRunningException {
        if (isError()) {
            return;
        }

        if (!StringUtils.ok(instanceName)) {
            setError(Strings.get("stop.instance.noInstanceName"));
            return;
        }

        instance = helper.getServer(instanceName);

        if (instance == null) {
            setError(Strings.get("stop.instance.noSuchInstance", instanceName));
            return;
        }

        host = instance.getAdminHost();

        if (host == null) {
            setError(Strings.get("stop.instance.noHost", instanceName));
            return;
        }
        port = helper.getAdminPort(instance);

        if (port < 0) {
            setError(Strings.get("stop.instance.noPort", instanceName));
            return;
        }

        if (!isInstanceRestartable()) {
            setError(Strings.get("restart.notRestartable", instanceName));
        }

        if (logger.isLoggable(Level.FINER)) {
            logger.finer(ObjectAnalyzer.toString(this));
        }
    }

    /**
     * return null if all went OK...
     *
     */
    private void callInstance() throws CommandException {
        if (isError()) {
            return;
        }

        RemoteRestAdminCommand rac = createRac("_restart-instance");
        // notice how we do NOT send in the instance's name as an operand!!
        ParameterMap map = new ParameterMap();

        if (debug != null) {
            map.add("debug", debug);
        }

        rac.executeCommand(map);
    }

    private boolean isInstanceRestartable() throws InstanceNotRunningException {
        if (isError()) {
            return false;
        }

        String cmdName = "_get-runtime-info";

        RemoteRestAdminCommand rac;
        try {
            rac = createRac(cmdName);
            rac.executeCommand(new ParameterMap());
        }
        catch (CommandException ex) {
            // there is only one reason that _get-runtime-info would have a problem
            // namely if the instance isn't running.
            throw new InstanceNotRunningException();
        }

        String val = rac.findPropertyInReport("restartable");
        if (val != null && val.equals("false")) {
            return false;
        }
        return true;
    }

    private boolean waitForRestart() {
        return ProcessUtils.waitFor(() -> {
            try {
                String newpid = getPid();
                if (newpid != null && !newpid.equals(oldPid)) {
                    logger.log(Level.INFO, "Restarted instance pid = {0}", newpid);
                    return true;
                }
            } catch (CommandException e) {
                // ignore
            }
            return false;
        }, getTimeout(timeout), false);
    }

    private RemoteRestAdminCommand createRac(String cmdName) throws CommandException {
        // I wonder why the signature is so unwieldy?
        // hiding it here...
        return new ServerRemoteRestAdminCommand(habitat, cmdName, host,
                port, false, "admin", null, logger);
    }

    private void setError(String s) {
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        report.setMessage(s);
    }

    private void setSuccess(String s) {
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        report.setMessage(s);
    }

    private boolean isError() {
        return report.getActionExitCode() == ActionReport.ExitCode.FAILURE;
    }

    private void setOldPid() throws CommandException {
        if (isError()) {
            return;
        }

        oldPid = getPid();

        if (!StringUtils.ok(oldPid)) {
            setError(Strings.get("restart.instance.nopid", instanceName));
        }
    }

    private String getPid() throws CommandException {
        String cmdName = "_get-runtime-info";
        RemoteRestAdminCommand rac = createRac(cmdName);
        rac.executeCommand(new ParameterMap());
        return rac.findPropertyInReport("pid");
    }

    /*
     * The instance is not running -- so let's try to start it.
     * There is no good way to call a Command on ourself.  So use the
     * command directly.
     * See issue 16322 for more details
     */
    private void start() {
        try {
            StartInstanceCommand sic = new StartInstanceCommand(habitat, instanceName, Boolean.parseBoolean(debug),
                timeout, env);
            sic.execute(ctx);
        } catch (Exception e) {
            // Perhaps a NPE or something **after** the report was set to success???
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }

        // save start-instance's message
        String messageFromStartCommand = report.getMessage();
        if (isError()) {
            setError(Strings.get("restart.instance.startFailed", messageFromStartCommand));
        } else {
            setSuccess(Strings.get("restart.instance.startSucceeded", messageFromStartCommand));
        }
    }

    private static class InstanceNotRunningException extends Exception {
        private static final long serialVersionUID = 1957218902901618942L;
    }
}
