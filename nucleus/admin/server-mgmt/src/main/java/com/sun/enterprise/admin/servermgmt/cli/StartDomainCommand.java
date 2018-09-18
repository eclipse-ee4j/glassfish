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

package com.sun.enterprise.admin.servermgmt.cli;

import com.sun.enterprise.admin.cli.CLIConstants;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.inject.Inject;

import org.jvnet.hk2.annotations.*;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.hk2.api.PerLookup;

import com.sun.enterprise.admin.cli.Environment;
import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherFactory;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.admin.util.CommandModelData.ParamModelData;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.process.ProcessStreamDrainer;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import org.glassfish.security.common.FileRealmHelper;

/**
 * The start-domain command.
 *
 * @author bnevins
 * @author Bill Shannon
 */
@Service(name = "start-domain")
@PerLookup
public class StartDomainCommand extends LocalDomainCommand implements StartServerCommand {

    private GFLauncherInfo info;
    private GFLauncher launcher;
    @Param(optional = true, shortName = "v", defaultValue = "false")
    private boolean verbose;
    @Param(optional = true, defaultValue = "false")
    private boolean upgrade;
    @Param(optional = true, shortName = "w", defaultValue = "false")
    private boolean watchdog;
    @Param(optional = true, shortName = "d", defaultValue = "false")
    private boolean debug;
    @Param(name = "domain_name", primary = true, optional = true)
    private String domainName0;
    @Param(name = "dry-run", shortName = "n", optional = true,
            defaultValue = "false")
    private boolean dry_run;
    @Param(name = "drop-interrupted-commands", optional = true, defaultValue = "false")
    private boolean drop_interrupted_commands;

    @Inject
    ServerEnvironment senv;
    
    private static final LocalStringsImpl strings =
            new LocalStringsImpl(StartDomainCommand.class);
    // the name of the master password option
    private StartServerHelper helper;
    private String newpwName = Environment.getPrefix() + "NEWPASSWORD";

    @Override
    public List<String> getLauncherArgs() {
        return launcher.getCommandLine();
    }

    @Override
    public RuntimeType getType() {
        return RuntimeType.DAS;
    }

    @Override
    protected void validate()
            throws CommandException, CommandValidationException {
        setDomainName(domainName0);
        super.validate();
    }

    @Override
    protected int executeCommand() throws CommandException {
        try {
            // createLauncher needs to go before the helper is created!!
            createLauncher();
            final String mpv = getMasterPassword();

            helper = new StartServerHelper(
                    logger,
                    programOpts.isTerse(),
                    getServerDirs(),
                    launcher,
                    mpv,
                    debug);

            if (helper.prepareForLaunch() == false)
                return ERROR;

            if (!upgrade && launcher.needsManualUpgrade()) {
                logger.info(strings.get("manualUpgradeNeeded"));
                return ERROR;
            }
            doAutoUpgrade(mpv);

            if (dry_run) {
                logger.fine(Strings.get("dry_run_msg"));
                List<String> cmd = launcher.getCommandLine();
                StringBuilder sb = new StringBuilder();
                for (String s : cmd) {
                    sb.append(s);
                    sb.append('\n');
                }
                logger.info(sb.toString());
                return SUCCESS;
            }

            doAdminPasswordCheck();

            // launch returns very quickly if verbose is not set
            // if verbose is set then it returns after the domain dies
            launcher.launch();

            if (verbose || upgrade || watchdog) { // we can potentially loop forever here...
                while (true) {
                    int returnValue = launcher.getExitValue();

                    switch (returnValue) {
                        case CLIConstants.RESTART_NORMAL:
                            logger.info(strings.get("restart"));
                            break;
                        case CLIConstants.RESTART_DEBUG_ON:
                            logger.info(strings.get("restartChangeDebug", "on"));
                            info.setDebug(true);
                            break;
                        case CLIConstants.RESTART_DEBUG_OFF:
                            logger.info(strings.get("restartChangeDebug", "off"));
                            info.setDebug(false);
                            break;
                        default:
                            return returnValue;
                    }

                    if (env.debug())
                        System.setProperty(CLIConstants.WALL_CLOCK_START_PROP,
                                "" + System.currentTimeMillis());

                    launcher.relaunch();
                }

            }
            else {
                helper.waitForServer();
                helper.report();
                return SUCCESS;
            }
        }
        catch (GFLauncherException gfle) {
            throw new CommandException(gfle.getMessage());
        }
        catch (MiniXmlParserException me) {
            throw new CommandException(me);
        }
    }

    /**
     * Create a launcher for the domain specified by arguments to
     * this command.  The launcher is for a server of the specified type.
     * Sets the launcher and info fields.
     * It has to be public because it is part of an interface
     */
    @Override
    public void createLauncher()
            throws GFLauncherException, MiniXmlParserException {
        launcher = GFLauncherFactory.getInstance(getType());
        info = launcher.getInfo();

        info.setDomainName(getDomainName());
        info.setDomainParentDir(getDomainsDir().getPath());
        info.setVerbose(verbose || upgrade);
        info.setDebug(debug);
        info.setUpgrade(upgrade);
        info.setWatchdog(watchdog);
        info.setDropInterruptedCommands(drop_interrupted_commands);

        info.setRespawnInfo(programOpts.getClassName(),
                programOpts.getClassPath(),
                respawnArgs());

        launcher.setup();
    }

    /**
     * Return the asadmin command line arguments necessary to start
     * this domain admin server.
     */
    private String[] respawnArgs() {
        List<String> args = new ArrayList<String>(15);
        args.addAll(Arrays.asList(programOpts.getProgramArguments()));

        // now the start-domain specific arguments
        args.add(getName());    // the command name
        args.add("--verbose=" + String.valueOf(verbose));
        args.add("--watchdog=" + String.valueOf(watchdog));
        args.add("--debug=" + String.valueOf(debug));
        args.add("--domaindir");
        args.add(getDomainsDir().toString());
        if (ok(getDomainName()))
            args.add(getDomainName());  // the operand

        if (logger.isLoggable(Level.FINER))
            logger.log(Level.FINER, "Respawn args: {0}", args.toString());
        String[] a = new String[args.size()];
        args.toArray(a);
        return a;
    }

    /*
     * This is useful for debugging restart-domain problems.
     * In that case the Server process will run this class and it is fairly
     * involved to attach a debugger (though not bad -- see RestartDomain on
     * the server to see how).  Standard output disappears.  This is a
     * generally useful method.  Feel free to copy & paste!
     */
    private void debug(String s) {
        PrintStream ps = null;
        try {
            ps = new PrintStream(new FileOutputStream("startdomain.txt", true));
            ps.println(new Date().toString() + ":  " + s);
        } catch (FileNotFoundException ex) {
            //
        } finally {
            if (ps != null)
                ps.close();
        }
    }

    /*
     * If this domain needs to be upgraded and --upgrade wasn't
     * specified, first start the domain to do the upgrade and
     * then start the domain again for real.
     */
    private void doAutoUpgrade(String mpv) throws GFLauncherException, MiniXmlParserException, CommandException {
        if (upgrade || !launcher.needsAutoUpgrade())
            return;

        logger.info(strings.get("upgradeNeeded"));
        info.setUpgrade(true);
        launcher.setup();
        launcher.launch();
        Process p = launcher.getProcess();
        int exitCode = -1;
        try {
            exitCode = p.waitFor();
        }
        catch (InterruptedException ex) {
            // should never happen
        }
        if (exitCode != SUCCESS) {
            ProcessStreamDrainer psd =
                    launcher.getProcessStreamDrainer();
            String output = psd.getOutErrString();
            if (ok(output))
                throw new CommandException(
                        strings.get("upgradeFailedOutput",
                        info.getDomainName(), exitCode, output));
            else
                throw new CommandException(strings.get("upgradeFailed",
                        info.getDomainName(), exitCode));
        }
        logger.info(strings.get("upgradeSuccessful"));

        // need a new launcher to start the domain for real
        createLauncher();
        // continue with normal start...
    }

    /*
     * Check to make sure that at least one admin user is able to login.
     * If none is found, then prompt for an admin password.
     *
     * NOTE: this depends on launcher.setup having already been called.
     */
    private void doAdminPasswordCheck() throws CommandException {
        String arfile = launcher.getAdminRealmKeyFile();
        if (arfile != null) {
            try {
                FileRealmHelper ar = new FileRealmHelper(arfile);
                if (!ar.hasAuthenticatableUser()) {
                    // Prompt for the password for the first user and set it
                    Set<String> names = ar.getUserNames();
                    if (names == null || names.isEmpty()) {
                        throw new CommandException("no admin users");
                    }
                    String auser = names.iterator().next();
                    ParamModelData npwo = new ParamModelData(newpwName, String.class, false, null);
                    npwo.prompt = strings.get("new.adminpw", auser);
                    npwo.promptAgain = strings.get("new.adminpw.again", auser);
                    npwo.param._password = true;
                    logger.info(strings.get("new.adminpw.prompt"));
                    char[] npwArr = super.getPassword(npwo, null, true);
                    String npw = npwArr != null ? new String(npwArr) : null;
                    if (npw == null) {
                        throw new CommandException(strings.get("no.console"));
                    }
                    ar.updateUser(auser, auser, npw.toCharArray(), null);
                    ar.persist();
                }
            } catch (IOException ioe) {
                throw new CommandException(ioe);
            }
        }
    }
}
