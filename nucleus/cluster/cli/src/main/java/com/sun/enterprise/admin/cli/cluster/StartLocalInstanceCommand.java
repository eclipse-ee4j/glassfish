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

import com.sun.enterprise.admin.cli.CLIConstants;
import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherFactory;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.admin.servermgmt.cli.StartServerCommand;
import com.sun.enterprise.admin.servermgmt.cli.StartServerHelper;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import com.sun.enterprise.util.ObjectAnalyzer;

import java.io.File;
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

import static com.sun.enterprise.admin.cli.CLIConstants.RESTART_DEBUG_OFF;
import static com.sun.enterprise.admin.cli.CLIConstants.RESTART_DEBUG_ON;
import static com.sun.enterprise.admin.cli.CLIConstants.RESTART_NORMAL;

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
    private boolean dry_run;

    private GFLauncherInfo info;
    private GFLauncher launcher;

    // handled by superclass
    //@Param(name = "instance_name", primary = true, optional = false)
    //private String instanceName0;

    private StartServerHelper helper;

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
    protected void validate() throws CommandException {
        super.validate();

        File dir = getServerDirs().getServerDir();

        if(!dir.isDirectory()) {
            throw new CommandException(Strings.get("Instance.noSuchInstance"));
        }
    }

    /**
     */
    @Override
    protected int executeCommand() throws CommandException {
        logger.finer(() -> toString());

        if (sync.equals("none")) {
            logger.info(Strings.get("Instance.nosync"));
        } else {
            if (!synchronizeInstance()) {
                File domainXml =
                    new File(new File(instanceDir, "config"), "domain.xml");
                if (!domainXml.exists()) {
                    logger.info(Strings.get("Instance.nodomainxml"));
                    return ERROR;
                }
                logger.info(Strings.get("Instance.syncFailed"));
            }
        }

        try {
                 // createLauncher needs to go before the helper is created!!
            createLauncher();
            final String mpv = getMasterPassword();

            helper = new StartServerHelper(programOpts.isTerse(), getServerDirs(), launcher, mpv, debug);

            if (!helper.prepareForLaunch()) {
                return ERROR;
            }

            if (dry_run) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(Strings.get("dry_run_msg"));
                }
                logger.info(getLauncher().getCommandLine().toString("\n"));
                return SUCCESS;
            }

            getLauncher().launch();

            if (!verbose && !watchdog) {
                helper.waitForServerStart();
                helper.report();
                return SUCCESS;
            }

            // we can potentially loop forever here...
            while (true) {
                int returnValue = getLauncher().getExitValue();
                switch (returnValue) {
                    case RESTART_NORMAL:
                        logger.info(Strings.get("restart"));
                        break;
                    case RESTART_DEBUG_ON:
                        logger.info(Strings.get("restartChangeDebug", "on"));
                        getInfo().setDebug(true);
                        break;
                    case RESTART_DEBUG_OFF:
                        logger.info(Strings.get("restartChangeDebug", "off"));
                        getInfo().setDebug(false);
                        break;
                    default:
                        return returnValue;
                }

                if (env.debug()) {
                    System.setProperty(CLIConstants.WALL_CLOCK_START_PROP,
                                        "" + System.currentTimeMillis());
                }
                getLauncher().relaunch();
            }
        } catch (GFLauncherException gfle) {
            throw new CommandException(gfle.getMessage());
        } catch (MiniXmlParserException me) {
            throw new CommandException(me);
        }
    }

    /**
     * Create a launcher for the instance specified by arguments to
     * this command.  The launcher is for a server of the specified type.
     * Sets the launcher and info fields.
     */
    @Override
    public void createLauncher()
                        throws GFLauncherException, MiniXmlParserException {
            setLauncher(GFLauncherFactory.getInstance(getType()));
            setInfo(getLauncher().getInfo());
            getInfo().setInstanceName(instanceName);
            getInfo().setInstanceRootDir(instanceDir);
            getInfo().setVerbose(verbose);
            getInfo().setWatchdog(watchdog);
            getInfo().setDebug(debug);
            getInfo().setRespawnInfo(programOpts.getClassName(),
                            programOpts.getClassPath(),
                            respawnArgs());

            getLauncher().setup();
    }

    /**
     * Return the asadmin command line arguments necessary to
     * start this server instance.
     */
    private String[] respawnArgs() {
        List<String> args = new ArrayList<>(15);
        args.addAll(Arrays.asList(programOpts.getProgramArguments()));

        // now the start-local-instance specific arguments
        args.add(getName());    // the command name
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

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Respawn args: " + args.toString());
        }
        String[] a = new String[args.size()];
        args.toArray(a);
        return a;
    }

    private GFLauncher getLauncher() {
        if(launcher == null) {
            throw new RuntimeException(Strings.get("internal.error", "GFLauncher was not initialized"));
        }

        return launcher;
    }
    private void setLauncher(GFLauncher gfl) {
        launcher = gfl;
    }

    private GFLauncherInfo getInfo() {
        if(info == null) {
            throw new RuntimeException(Strings.get("internal.error", "GFLauncherInfo was not initialized"));
        }

            return info;
    }

    private void setInfo(GFLauncherInfo inf) {
            info = inf;
    }

    @Override
    public String toString() {
        return ObjectAnalyzer.toStringWithSuper(this);
    }
}
