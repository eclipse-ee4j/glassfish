/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.cli;

import com.sun.enterprise.admin.cli.Environment;
import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherFactory;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.admin.util.CommandModelData.ParamModelData;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.process.ProcessStreamDrainer;
import com.sun.enterprise.universal.xml.MiniXmlParserException;

import jakarta.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.security.common.FileRealmHelper;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.admin.cli.CLIConstants.RESTART_DEBUG_OFF;
import static com.sun.enterprise.admin.cli.CLIConstants.RESTART_DEBUG_ON;
import static com.sun.enterprise.admin.cli.CLIConstants.RESTART_NORMAL;
import static com.sun.enterprise.admin.cli.CLIConstants.WALL_CLOCK_START_PROP;
import static java.util.logging.Level.FINER;
import static org.glassfish.api.admin.RuntimeType.DAS;

/**
 * The start-domain command.
 *
 * @author bnevins
 * @author Bill Shannon
 */
@Service(name = "start-domain")
@PerLookup
public class StartDomainCommand extends LocalDomainCommand implements StartServerCommand {

    private static final LocalStringsImpl strings = new LocalStringsImpl(StartDomainCommand.class);

    @Param(optional = true, shortName = "v", defaultValue = "false")
    private boolean verbose;

    @Param(optional = true, defaultValue = "false")
    private boolean upgrade;

    @Param(optional = true, shortName = "w", defaultValue = "false")
    private boolean watchdog;

    /**
     * Starts the server in debug mode with suspend set in domain.xml
     */
    @Param(optional = true, shortName = "d", defaultValue = "false")
    private boolean debug;

    /**
     * Starts the server in debug mode with suspend on
     */
    @Param(optional = true, shortName = "s", defaultValue = "false")
    private boolean suspend;

    @Param(name = "domain_name", primary = true, optional = true)
    private String domainName0;

    @Param(name = "dry-run", shortName = "n", optional = true, defaultValue = "false")
    private boolean dry_run;

    @Param(name = "drop-interrupted-commands", optional = true, defaultValue = "false")
    private boolean drop_interrupted_commands;

    @Inject
    ServerEnvironment serverEnvironment;

    private GFLauncherInfo launchParameters;
    private GFLauncher glassFishLauncher;
    private StartServerHelper startServerHelper;

    // the name of the master password option
    private final String newpwName = Environment.getPrefix() + "NEWPASSWORD";

    @Override
    public List<String> getLauncherArgs() {
        return glassFishLauncher.getCommandLine();
    }

    @Override
    public RuntimeType getType() {
        return DAS;
    }

    @Override
    protected void validate() throws CommandException, CommandValidationException {
        setDomainName(domainName0);
        super.validate();
    }

    @Override
    protected int executeCommand() throws CommandException {
        try {
            // createLauncher needs to go before the startServerHelper is created!!
            createLauncher();
            String masterPassword = getMasterPassword();

            startServerHelper = new StartServerHelper(programOpts.isTerse(), getServerDirs(), glassFishLauncher, masterPassword);

            if (!startServerHelper.prepareForLaunch()) {
                return ERROR;
            }

            if (!upgrade && glassFishLauncher.needsManualUpgrade()) {
                logger.info(strings.get("manualUpgradeNeeded"));
                return ERROR;
            }

            doAutoUpgrade(masterPassword);

            if (dry_run) {
                logger.fine(Strings.get("dry_run_msg"));
                List<String> cmd = glassFishLauncher.getCommandLine();
                int indexOfReadStdin = cmd.indexOf("-read-stdin");
                String cmdToLog = IntStream.range(0, cmd.size())
                        // Don't print -read-stdin option as it's not needed to run the server
                        // Also skip the next line with "true", which is related to this option
                        .filter(index -> index < indexOfReadStdin || index > indexOfReadStdin + 1)
                        .mapToObj(cmd::get)
                        .collect(Collectors.joining("\n"))
                        + "\n";
                logger.info(cmdToLog);
                return SUCCESS;
            }

            doAdminPasswordCheck();

            // Launch returns very quickly if verbose is not set
            // if verbose is set then it returns after the domain dies
            glassFishLauncher.launch();

            if (verbose || upgrade || watchdog) { // we can potentially loop forever here...
                while (true) {
                    int returnValue = glassFishLauncher.getExitValue();

                    switch (returnValue) {
                    case RESTART_NORMAL:
                        logger.info(strings.get("restart"));
                        break;
                    case RESTART_DEBUG_ON:
                        logger.info(strings.get("restartChangeDebug", "on"));
                        launchParameters.setDebug(true);
                        break;
                    case RESTART_DEBUG_OFF:
                        logger.info(strings.get("restartChangeDebug", "off"));
                        launchParameters.setDebug(false);
                        break;
                    default:
                        return returnValue;
                    }

                    if (env.debug()) {
                        System.setProperty(WALL_CLOCK_START_PROP, "" + System.currentTimeMillis());
                    }

                    glassFishLauncher.relaunch();
                }

            } else {
                startServerHelper.waitForServerStart();
                startServerHelper.report();
                return SUCCESS;
            }
        } catch (GFLauncherException gfle) {
            throw new CommandException(gfle.getMessage());
        } catch (MiniXmlParserException me) {
            throw new CommandException(me);
        }
    }

    /**
     * Create a glassFishLauncher for the domain specified by arguments to this command. The glassFishLauncher is for a
     * server of the specified type. Sets the glassFishLauncher and launchParameters fields. It has to be public because it
     * is part of an interface
     */
    @Override
    public void createLauncher() throws GFLauncherException, MiniXmlParserException {
        glassFishLauncher = GFLauncherFactory.getInstance(getType());
        launchParameters = glassFishLauncher.getInfo();

        launchParameters.setDomainName(getDomainName());
        launchParameters.setDomainParentDir(getDomainsDir().getPath());
        launchParameters.setVerbose(verbose || upgrade);
        launchParameters.setSuspend(suspend);
        launchParameters.setDebug(debug);
        launchParameters.setUpgrade(upgrade);
        launchParameters.setWatchdog(watchdog);
        launchParameters.setDropInterruptedCommands(drop_interrupted_commands);

        launchParameters.setRespawnInfo(programOpts.getClassName(), programOpts.getClassPath(), respawnArgs());

        glassFishLauncher.setup();
    }

    /**
     * @return the asadmin command line arguments necessary to start this domain admin server.
     */
    private String[] respawnArgs() {
        List<String> args = new ArrayList<>(15);
        args.addAll(Arrays.asList(programOpts.getProgramArguments()));

        // now the start-domain specific arguments
        args.add(getName()); // the command name
        args.add("--verbose=" + String.valueOf(verbose));
        args.add("--watchdog=" + String.valueOf(watchdog));
        args.add("--debug=" + String.valueOf(debug));
        args.add("--domaindir");
        args.add(getDomainsDir().toString());
        if (ok(getDomainName())) {
            args.add(getDomainName()); // the operand
        }

        if (logger.isLoggable(FINER)) {
            logger.log(FINER, "Respawn args: {0}", args.toString());
        }
        String[] a = new String[args.size()];
        args.toArray(a);
        return a;
    }

    /**
     * If this domain needs to be upgraded and --upgrade wasn't specified, first start the domain to do the upgrade and then
     * start the domain again for real.
     */
    private void doAutoUpgrade(String mpv) throws GFLauncherException, MiniXmlParserException, CommandException {
        if (upgrade || !glassFishLauncher.needsAutoUpgrade()) {
            return;
        }

        logger.info(strings.get("upgradeNeeded"));
        launchParameters.setUpgrade(true);
        glassFishLauncher.setup();
        glassFishLauncher.launch();
        Process glassFishProcess = glassFishLauncher.getProcess();
        int exitCode = -1;
        try {
            exitCode = glassFishProcess.waitFor();
        } catch (InterruptedException ex) {
            // should never happen
        }

        if (exitCode != SUCCESS) {
            ProcessStreamDrainer psd = glassFishLauncher.getProcessStreamDrainer();
            String output = psd.getOutErrString();
            if (ok(output)) {
                throw new CommandException(strings.get("upgradeFailedOutput", launchParameters.getDomainName(), exitCode, output));
            } else {
                throw new CommandException(strings.get("upgradeFailed", launchParameters.getDomainName(), exitCode));
            }
        }
        logger.info(strings.get("upgradeSuccessful"));

        // need a new glassFishLauncher to start the domain for real
        createLauncher();
        // continue with normal start...
    }

    /**
     * Check to make sure that at least one admin user is able to login. If none is found, then prompt for an admin
     * password.
     *
     * NOTE: this depends on glassFishLauncher.setup having already been called.
     */
    private void doAdminPasswordCheck() throws CommandException {
        String adminRealmKeyFile = glassFishLauncher.getAdminRealmKeyFile();
        if (adminRealmKeyFile != null) {
            try {
                FileRealmHelper fileRealmHelper = new FileRealmHelper(adminRealmKeyFile);
                if (!fileRealmHelper.hasAuthenticatableUser()) {

                    // Prompt for the password for the first user and set it
                    Set<String> adminUsers = fileRealmHelper.getUserNames();
                    if (adminUsers == null || adminUsers.isEmpty()) {
                        throw new CommandException("no admin users");
                    }

                    String firstAdminUser = adminUsers.iterator().next();
                    ParamModelData npwo = new ParamModelData(newpwName, String.class, false, null);
                    npwo.prompt = strings.get("new.adminpw", firstAdminUser);
                    npwo.promptAgain = strings.get("new.adminpw.again", firstAdminUser);
                    npwo.param._password = true;

                    logger.info(strings.get("new.adminpw.prompt"));
                    char[] newPasswordArray = super.getPassword(npwo, null, true);
                    String newPassword = newPasswordArray != null ? new String(newPasswordArray) : null;
                    if (newPassword == null) {
                        throw new CommandException(strings.get("no.console"));
                    }

                    fileRealmHelper.updateUser(firstAdminUser, firstAdminUser, newPassword.toCharArray(), null);
                    fileRealmHelper.persist();
                }
            } catch (IOException ioe) {
                throw new CommandException(ioe);
            }
        }
    }
}
