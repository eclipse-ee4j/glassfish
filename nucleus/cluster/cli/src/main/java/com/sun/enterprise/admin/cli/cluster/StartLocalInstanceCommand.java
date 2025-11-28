/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.cli.cluster;

import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherFactory;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.admin.servermgmt.cli.ServerLifeSignCheck;
import com.sun.enterprise.admin.servermgmt.cli.StartServerCommand;
import com.sun.enterprise.admin.servermgmt.cli.StartServerHelper;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import com.sun.enterprise.util.HostAndPort;
import com.sun.enterprise.util.ObjectAnalyzer;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.admin.cli.CLIConstants.MASTER_PASSWORD;
import static com.sun.enterprise.admin.cli.CLIConstants.WAIT_FOR_DAS_TIME_MS;

/**
 * Start a local server instance.
 */
@Service(name = "start-local-instance")
@ExecuteOn(RuntimeType.DAS)
@PerLookup
public class StartLocalInstanceCommand extends SynchronizeInstanceCommand implements StartServerCommand {

    @Param(optional = true, shortName = "v", defaultValue = "false")
    private boolean verbose;

    @Param(optional = true, shortName = "w", defaultValue = "false")
    private boolean watchdog;

    @Param(optional = true, shortName = "d", defaultValue = "false")
    private boolean debug;

    @Param(name = "dry-run", shortName = "n", optional = true, defaultValue = "false")
    private boolean dryRun;

    @Param(optional = true)
    private Integer timeout;

    @Param(name = "check-pid-file", optional = true, defaultValue = "true")
    private boolean checkPidFile;

    @Param(name = "check-process-alive", optional = true, defaultValue = "true")
    private boolean checkProcessAlive;

    @Param(name = "check-admin-port", optional = true, defaultValue = "true")
    private boolean checkAdminEndpoint;

    @Param(name = "server-output", shortName = "o",  optional = true)
    private Boolean printServerOutput;

    @Param(name = "custom-endpoints", optional = true)
    private String customEndpoints;

    private GFLauncherInfo launchParameters;
    private GFLauncher launcher;
    private StartServerHelper startServerHelper;

    @Override
    public RuntimeType getType() {
         return RuntimeType.INSTANCE;
    }

    @Override
    protected boolean mkdirs(File f) {
        // we definitely do NOT want dirs created for this instance if
        // they don't exist!
        return false;
    }

    @Override
    public Duration getTimeout() {
        return timeout == null ? WAIT_FOR_DAS_TIME_MS : Duration.ofSeconds(timeout);
    }

    @Override
    protected void validate() throws CommandException {
        super.validate();

        File dir = getServerDirs().getServerDir();

        if(!dir.isDirectory()) {
            throw new CommandException(Strings.get("Instance.noSuchInstance"));
        }
    }

    @Override
    protected int executeCommand() throws CommandException {
        logger.finer(() -> toString());

        if (sync.equals("none")) {
            logger.info(Strings.get("Instance.nosync"));
        } else {
            if (!synchronizeInstance()) {
                File domainXml = new File(new File(instanceDir, "config"), "domain.xml");
                if (!domainXml.exists()) {
                    logger.info(Strings.get("Instance.nodomainxml"));
                    return ERROR;
                }
                logger.info(Strings.get("Instance.syncFailed"));
            }
        }

        try {
            // createLauncher needs to go before the startServerHelper is created!!
            launcher = createLauncher();

            final List<HostAndPort> userEndpoints = StartServerHelper.parseCustomEndpoints(customEndpoints);
            final ServerLifeSignCheck signOfLife = new ServerLifeSignCheck("instance " + getInstanceName(),
                printServerOutput, checkPidFile, checkProcessAlive, checkAdminEndpoint, userEndpoints);
            startServerHelper = new StartServerHelper(programOpts.isTerse(), getTimeout(), getServerDirs(), launcher, signOfLife);

            if (dryRun) {
                logger.log(Level.FINE, Strings.get("dry_run_msg"));
                logger.log(Level.INFO, launcher.getCommandLine().toString("\n"));
                return SUCCESS;
            }

            launcher.launch();

            if (verbose || watchdog) {
                return startServerHelper.talkWithUser();
            }
            final String report = startServerHelper.waitForServerStart(getTimeout());
            logger.info(report);
            return SUCCESS;
        } catch (GFLauncherException e) {
            throw new CommandException(e.getMessage(), e);
        } catch (MiniXmlParserException e) {
            throw new CommandException(e.getMessage(), e);
        }
    }

    @Override
    public final GFLauncher createLauncher() throws GFLauncherException, MiniXmlParserException, CommandException {
        final GFLauncher gfLauncher = GFLauncherFactory.getInstance(getType());
        this.launchParameters = gfLauncher.getInfo();
        launchParameters.setInstanceName(instanceName);
        launchParameters.setInstanceRootDir(instanceDir);
        launchParameters.setVerbose(verbose);
        launchParameters.setIgnoreOutput(printServerOutput == Boolean.FALSE);
        launchParameters.setWatchdog(watchdog);
        launchParameters.setDebug(debug);
        launchParameters.setRespawnInfo(programOpts.getClassName(), programOpts.getModulePath(),
            programOpts.getClassPath(), respawnArgs());
        launchParameters.setAsadminAdminAddress(getUserProvidedAdminAddress());
        gfLauncher.setup();
        launchParameters.addSecurityToken(MASTER_PASSWORD, getMasterPassword());
        return gfLauncher;
    }

    @Override
    public String toString() {
        return ObjectAnalyzer.toStringWithSuper(this);
    }

    /**
     * @return the asadmin command line arguments necessary to start this server instance.
     */
    private String[] respawnArgs() {
        List<String> args = new ArrayList<>(15);
        args.addAll(Arrays.asList(programOpts.getProgramArguments()));

        // now the start-local-instance specific arguments
        // the command name
        args.add(getName());
        args.add("--verbose=" + String.valueOf(verbose));
        args.add("--watchdog=" + String.valueOf(watchdog));
        args.add("--debug=" + String.valueOf(debug));

        // IT 14015
        // We now REQUIRE all restarted instance to do a sync.
        // just stick with the default...
        //args.add("--nosync=" + String.valueOf(nosync));

        if (ok(nodeDir)) {
            args.add("--nodedir");
            args.add(nodeDir);
        }
        if (ok(node)) {
            args.add("--node");
            args.add(node);
        }
        if (ok(instanceName)) {
            args.add(instanceName); // the operand
        }

        logger.log(Level.FINER, "Respawn args: {0}", args);
        return args.toArray(String[]::new);
    }
}
