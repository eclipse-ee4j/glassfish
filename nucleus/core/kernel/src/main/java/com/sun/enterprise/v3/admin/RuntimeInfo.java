/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.util.StringUtils.ok;
import static org.glassfish.api.ActionReport.ExitCode.SUCCESS;

/**
 * https://glassfish.dev.java.net/issues/show_bug.cgi?id=12483
 *
 * @author Byron Nevins
 * @author Ludovic Champenois
 */
@Service(name = "_get-runtime-info")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn({ RuntimeType.INSTANCE })
@TargetType({ CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTERED_INSTANCE })
@RestEndpoints({
    @RestEndpoint(
        configBean = Domain.class,
        opType = RestEndpoint.OpType.GET,
        path = "get-runtime-info",
        description = "Get Runtime Info") })
@AccessRequired(resource = "domain", action = "read")
public class RuntimeInfo implements AdminCommand {

    @Inject
    ServerEnvironment env;

    @Inject
    private StartupContext startupContext;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config config;

    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.SERVER_NAME)
    String target;

    private boolean jpdaEnabled;
    private boolean javaEnabledOnCmd;
    private JavaConfig javaConfig;
    private ActionReport report;
    private ActionReport.MessagePart top;
    private Logger logger;
    private final StringBuilder reportMessage = new StringBuilder();

    private boolean restartable;

    public RuntimeInfo() {
    }

    @Override
    public void execute(AdminCommandContext context) {
        report = context.getActionReport();
        report.setActionExitCode(SUCCESS);
        top = report.getTopMessagePart();
        logger = context.getLogger();
        javaEnabledOnCmd = Boolean.parseBoolean(startupContext.getArguments().getProperty("-debug"));
        javaConfig = config.getJavaConfig();
        jpdaEnabled = javaEnabledOnCmd || Boolean.parseBoolean(javaConfig.getDebugEnabled());
        int debugPort = parsePort(javaConfig.getDebugOptions());
        top.addProperty("debug", Boolean.toString(jpdaEnabled));
        top.addProperty("debugPort", Integer.toString(debugPort));
        final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        top.addProperty("os.arch", osBean.getArch());
        top.addProperty("os.name", osBean.getName());
        top.addProperty("os.version", osBean.getVersion());
        top.addProperty("availableProcessorsCount", "" + osBean.getAvailableProcessors());

        // getTotalPhysicalMemorySize is from com.sun.management.OperatingSystemMXBean and cannot easily access it via OSGi
        // also if we are not on a sun jdk, we will not return this attribute.
        if (!OS.isAix()) {
            try {
                final Method jm = osBean.getClass().getMethod("getTotalPhysicalMemorySize");
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    @Override
                    public Object run() throws Exception {
                        if (!jm.trySetAccessible()) {
                            throw new InaccessibleObjectException("Unable to make accessible: " + jm);
                        }
                        return null;
                    }
                });

                top.addProperty("totalPhysicalMemorySize", "" + jm.invoke(osBean));

            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }

        }
        RuntimeMXBean rmxb = ManagementFactory.getRuntimeMXBean();
        top.addProperty("startTimeMillis", "" + rmxb.getStartTime());
        top.addProperty("pid", "" + rmxb.getName());
        setDasName();
        top.addProperty("java.vm.name", System.getProperty("java.vm.name"));
        setRestartable();
        reportMessage.append(Strings.get("runtime.info.debug", jpdaEnabled ? "enabled" : "not enabled"));
        report.setMessage(reportMessage.toString());
    }

    private void setDasName() {
        try {
            String name = env.getInstanceRoot().getName();
            top.addProperty("domain_name", name);
        } catch (Exception ex) {
            // ignore
        }
    }

    /**
     * March 11 2011 -- See JIRA 16197 Say the user started the server with a passwordfile arg. After they started it they
     * deleted the password file. If we don't do anything special restart-server will take down the server -- but it will
     * not startup again. The user will have no clue why. We can NOT tell the user directly because the restart server
     * command is asynchronous (@Async annotation). So -- this method was added as a pre-flight check. The client restart
     * commands should run this command and check the restartable flag to make sure the restart doesn't fail because of a
     * missing password file.
     */
    private void setRestartable() {
        // false positive is MUCH better than false negative. Err on the side of
        // trying to restart if in doubt. No harm can result from that.
        restartable = true;
        String passwordFile = null;

        try {
            Properties props = Globals.get(StartupContext.class).getArguments();
            String argsString = props.getProperty("-asadmin-args");

            if (ok(argsString) && argsString.indexOf("--passwordfile") >= 0) {
                String[] args = argsString.split(",,,");

                for (int i = 0; i < args.length; i++) {
                    if (args[i].equals("--passwordfile")) {
                        if ((i + 1) < args.length && ok(args[i + 1])) {
                            passwordFile = args[i + 1];
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // nothing to do, but I'll do this anyway because I'm paranoid
            restartable = true;
        }

        if (ok(passwordFile)) {
            // the --passwordfile is here -- so it had best point to a file that
            // exists and can be read! In all other cases -- restartable is true
            File pwf = new File(passwordFile);
            restartable = pwf.canRead();
        }
        top.addProperty("restartable", Boolean.toString(restartable));
    }

    private int parsePort(String s) {
        // "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9009"
        int port = -1;
        String[] ss = s.split(",");

        for (String sub : ss) {
            if (sub.startsWith("address=")) {
                try {
                    port = Integer.parseInt(sub.substring(8));
                } catch (Exception e) {
                    port = -1;
                }
                break;
            }
        }
        return port;
    }


}
