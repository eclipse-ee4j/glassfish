/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.universal.process.ProcessStreamDrainer;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import com.sun.enterprise.util.HostAndPort;
import com.sun.enterprise.util.ObjectAnalyzer;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.glassfish.security.common.FileRealmHelper;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.admin.cli.CLIConstants.MASTER_PASSWORD;
import static com.sun.enterprise.admin.cli.CLIConstants.WAIT_FOR_DAS_TIME_MS;
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

    private static final LocalStringsImpl I18N = new LocalStringsImpl(StartDomainCommand.class);

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

    @Param(name = "drop-interrupted-commands", optional = true, defaultValue = "false")
    private boolean dropInterruptedCommands;

    @Param(name = "domain_name", primary = true, optional = true)
    private String userArgDomainName;

    private GFLauncherInfo launchParameters;
    private GFLauncher launcher;
    private StartServerHelper startServerHelper;

    // the name of the master password option
    private final String newpwName = Environment.getPrefix() + "NEWPASSWORD";

    @Override
    public RuntimeType getType() {
        return DAS;
    }

    @Override
    public Duration getTimeout() {
        return timeout == null ? WAIT_FOR_DAS_TIME_MS : Duration.ofSeconds(timeout);
    }

    @Override
    protected void validate() throws CommandException, CommandValidationException {
        setDomainName(userArgDomainName);
        super.validate();
    }

    @Override
    protected int executeCommand() throws CommandException {
        try {
            // createLauncher needs to go before the startServerHelper is created!!
            launcher = createLauncher();

            final List<HostAndPort> userEndpoints = StartServerHelper.parseCustomEndpoints(customEndpoints);
            final ServerLifeSignCheck signOfLife = new ServerLifeSignCheck("domain " + getDomainName(),
                printServerOutput, checkPidFile, checkProcessAlive, checkAdminEndpoint, userEndpoints);
            startServerHelper = new StartServerHelper(programOpts.isTerse(), getTimeout(), getServerDirs(), launcher, signOfLife);

            if (!upgrade && launcher.needsManualUpgrade()) {
                logger.info(I18N.get("manualUpgradeNeeded"));
                return ERROR;
            }

            if (!upgrade && launcher.needsAutoUpgrade()) {
                doAutoUpgrade();
            }

            if (dryRun) {
                logger.fine("Dump of JVM Invocation line that would be used to launch:");
                List<String> cmd = launcher.getCommandLine().toList();
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
            launcher.launch();

            if (verbose || upgrade || watchdog) {
                return startServerHelper.talkWithUser();
            }
            final String report = startServerHelper.waitForServerStart(getTimeout());
            logger.info(report);
            return SUCCESS;
        } catch (GFLauncherException e) {
            throw new CommandException(e.getMessage(), e);
        } catch (MiniXmlParserException me) {
            throw new CommandException(me);
        }
    }

    @Override
    public final GFLauncher createLauncher() throws GFLauncherException, MiniXmlParserException, CommandException {
        final GFLauncher gfLauncher = GFLauncherFactory.getInstance(getType());
        launchParameters = gfLauncher.getParameters();
        launchParameters.setDomainName(getDomainName());
        launchParameters.setDomainParentDir(getDomainsDir().getPath());
        launchParameters.setVerbose(verbose || upgrade);
        launchParameters.setIgnoreOutput(printServerOutput == Boolean.FALSE);
        launchParameters.setSuspend(suspend);
        launchParameters.setDebug(debug);
        launchParameters.setUpgrade(upgrade);
        launchParameters.setWatchdog(watchdog);
        launchParameters.setDropInterruptedCommands(dropInterruptedCommands);
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
     * @return the asadmin command line arguments necessary to start this domain admin server.
     */
    private String[] respawnArgs() {
        List<String> args = new ArrayList<>(15);
        args.addAll(Arrays.asList(programOpts.getProgramArguments()));

        // now the start-domain specific arguments
        // the command name
        args.add(getName());
        args.add("--verbose=" + String.valueOf(verbose));
        args.add("--watchdog=" + String.valueOf(watchdog));
        args.add("--debug=" + String.valueOf(debug));
        args.add("--domaindir");
        args.add(getDomainsDir().toString());
        if (ok(getDomainName())) {
            // the operand
            args.add(getDomainName());
        }

        logger.log(FINER, "Respawn args: {0}", args);
        return args.toArray(String[]::new);
    }


    /**
     * If this domain needs to be upgraded and --upgrade wasn't specified, first start the domain
     * to do the upgrade and then start the domain again for real.
     *
     * @return new {@link GFLauncher}
     */
    private GFLauncher doAutoUpgrade() throws GFLauncherException, MiniXmlParserException, CommandException {
        logger.info(I18N.get("upgradeNeeded"));
        launchParameters.setUpgrade(true);
        launcher.setup();
        launcher.launch();
        final int exitCode = waitForAutoUpgradeFinish();
        if (exitCode == SUCCESS) {
            logger.info(I18N.get("upgradeSuccessful"));
            // need a new glassFishLauncher to start the domain for real
            return createLauncher();
        }
        final ProcessStreamDrainer psd = launcher.getProcessStreamDrainer();
        final String output = psd.getOutErrString();
        if (ok(output)) {
            throw new CommandException(I18N.get("upgradeFailedOutput", launchParameters.getDomainName(), exitCode, output));
        }
        throw new CommandException(I18N.get("upgradeFailed", launchParameters.getDomainName(), exitCode));
    }

    private int waitForAutoUpgradeFinish() {
        final Process glassFishProcess = launcher.getProcess();
        try {
            return glassFishProcess.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "Waiting for the upgrade was interrupted!", e);
            System.exit(-1);
            return -1;
        }
    }

    /**
     * Check to make sure that at least one admin user is able to login.
     * If none is found, then prompt for an admin password.
     *
     * NOTE: this depends on glassFishLauncher.setup having already been called.
     */
    private void doAdminPasswordCheck() throws CommandException {
        File adminRealmKeyFile = launcher.getAdminRealmKeyFile();
        if (adminRealmKeyFile == null) {
            return;
        }
        try {
            FileRealmHelper fileRealmHelper = new FileRealmHelper(adminRealmKeyFile);
            if (fileRealmHelper.hasAuthenticatableUser()) {
                return;
            }
            // Prompt for the password for the first user and set it
            Set<String> adminUsers = fileRealmHelper.getUserNames();
            if (adminUsers == null || adminUsers.isEmpty()) {
                throw new CommandException("no admin users");
            }

            String firstAdminUser = adminUsers.iterator().next();
            ParamModelData npwo = new ParamModelData(newpwName, String.class, false, null);
            npwo.prompt = I18N.get("new.adminpw", firstAdminUser);
            npwo.promptAgain = I18N.get("new.adminpw.again", firstAdminUser);
            npwo.param._password = true;

            logger.info(I18N.get("new.adminpw.prompt"));
            char[] newPasswordArray = super.getPassword(npwo, null, true);
            String newPassword = newPasswordArray == null ? null : new String(newPasswordArray);
            if (newPassword == null) {
                throw new CommandException("The Master Password is required to start the domain.\n"
                    + "No console, no prompting possible. You should either create the domain\n"
                    + "with --savemasterpassword=true or provide a password file with the --passwordfile option.");
            }

            fileRealmHelper.updateUser(firstAdminUser, firstAdminUser, newPassword.toCharArray(), null);
            fileRealmHelper.persist();
        } catch (IOException ioe) {
            throw new CommandException(ioe);
        }
    }
}
