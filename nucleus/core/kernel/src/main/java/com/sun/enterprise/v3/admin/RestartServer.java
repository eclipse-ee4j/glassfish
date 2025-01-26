/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.StringUtils;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.Properties;
import java.util.logging.Logger;

import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.internal.api.Globals;

/**
 * For non-verbose mode: Stop this server, spawn a new JVM that will wait for this JVM to die. The new JVM then starts
 * the server again.
 *
 * For verbose mode: We want the asadmin console itself to do the respawning -- so just return a special int from
 * System.exit(). This tells asadmin to restart.
 *
 * @author Byron Nevins
 */
public class RestartServer {
    @Inject
    private Provider<GlassFish> glassfishProvider;

    private ModulesRegistry registry;
    private Boolean debug;
    private Properties props;
    private Logger logger;
    private boolean verbose;
    private String classpath;
    private String classname;
    private String argsString;
    private String[] args;
    private String serverName = "";
    private static final LocalStringsImpl strings = new LocalStringsImpl(RestartServer.class);
    private static final String AS_RESTART_PID = "-DAS_RESTART=" + ProcessHandle.current().pid();
    private static final String[] normalProps = { AS_RESTART_PID };
    private static final int RESTART_NORMAL = 10;
    private static final int RESTART_DEBUG_ON = 11;
    private static final int RESTART_DEBUG_OFF = 12;

    protected final void setDebug(Boolean b) {
        debug = b;
    }

    protected final void setRegistry(final ModulesRegistry registryIn) {
        registry = registryIn;
    }

    protected final void setServerName(String serverNameIn) {
        serverName = serverNameIn;
    }

    /**
     * Restart of the application server :
     *
     * All running services are stopped. LookupManager is flushed.
     *
     * Client code that started us should notice the special return value and restart us.
     */
    protected final void doExecute(AdminCommandContext context) {
        try {
            // unfortunately we can't rely on constructors with HK2...
            if (registry == null) {
                throw new NullPointerException("registry was not set");
            }

            init(context);

            // get the GlassFish object - we have to wait in case startup is still in progress
            // This is a temporary work-around until HK2 supports waiting for the service to
            // show up in the ServiceLocator.
            GlassFish gfKernel = glassfishProvider.get();
            while (gfKernel == null) {
                Thread.onSpinWait();
                gfKernel = glassfishProvider.get();
            }
            if (!verbose) {
                if (!setupReincarnationWithAsadmin() && !setupReincarnationWithOther()) {
                    throw new IllegalStateException(strings.get("restart.server.noStartupInfo", props));
                }
                scheduleReincarnation();
            }
            gfKernel.stop();
        } catch (Exception e) {
            throw new Error(strings.get("restart.server.failure"), e);
        }

        final int restartType;
        if (debug == null) {
            restartType = RESTART_NORMAL;
        } else {
            restartType = debug ? RESTART_DEBUG_ON : RESTART_DEBUG_OFF;
        }
        // return a special int from System.exit()
        System.exit(restartType);
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ///////// ALL PRIVATE BELOW ////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    private void init(AdminCommandContext context) {
        logger = context.getLogger();
        props = Globals.get(StartupContext.class).getArguments();
        verbose = Boolean.parseBoolean(props.getProperty("-verbose", "false"));
        logger.info(strings.get("restart.server.init"));
    }

    private void scheduleReincarnation() throws RDCException {
        try {
            Runtime.getRuntime().addShutdownHook(new StartServerShutdownHook(classpath, normalProps, classname, args));
        } catch (Exception e) {
            throw new RDCException(e);
        }
    }

    private boolean setupReincarnationWithAsadmin() throws RDCException {
        classpath = props.getProperty("-asadmin-classpath");
        classname = props.getProperty("-asadmin-classname");
        argsString = props.getProperty("-asadmin-args");

        return verify("restart.server.asadminError");
    }

    private boolean setupReincarnationWithOther() throws RDCException {

        classpath = props.getProperty("-startup-classpath");
        classname = props.getProperty("-startup-classname");
        argsString = props.getProperty("-startup-args");

        return verify("restart.server.nonAsadminError");
    }

    private boolean verify(String errorStringKey) throws RDCException {
        // Either asadmin or non-asadmin startup params have been set -- check them!
        // THREE possible returns:
        // 1) true
        // 2) false
        // 3) RDCException
        if (classpath == null && classname == null && argsString == null) {
            return false;
        }

        // now that at least one is set -- demand that ALL OF THEM be set...
        if (!ok(classpath) || !ok(classname) || argsString == null) {
            throw new RDCException(strings.get(errorStringKey));
        }

        args = argsString.split(",,,");
        handleDebug();
        return true;
    }

    private void handleDebug() {
        if (debug == null) { // nothing to do!
            return;
        }

        stripDebugFromArgs();
        stripOperandFromArgs();
        int oldlen = args.length;
        int newlen = oldlen + 2;
        String debugArg = "--debug=" + debug.toString();
        String[] newArgs = new String[newlen];

        // copy all but the last arg (domain-name)
        System.arraycopy(args, 0, newArgs, 0, args.length);
        newArgs[newlen - 2] = debugArg;
        newArgs[newlen - 1] = serverName;
        args = newArgs;
    }

    private void stripDebugFromArgs() {
        // this is surprisingly complex!
        // "--debug domain1" is one
        // "--debug=true" is one
        // "--debug false" is two
        boolean twoArgs = false;
        int indexOfDebug = -1;

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--debug=")) {
                indexOfDebug = i;
                break;
            }
            if (args[i].startsWith("--debug")) {
                indexOfDebug = i;

                // who knows what happens in CLI when the domain's name is "true" ?!?
                // we could potentially be fooled by that one very unlikely scenario
                if (args.length > i + 1) {// broken into two if's for readability...
                    if (args[i + 1].equals("true") || args[i + 1].equals("false")) {
                        twoArgs = true;
                    }
                }
                break;
            }
        }

        if (indexOfDebug < 0) {
            return;
        }

        int oldlen = args.length;
        int newlen = oldlen - 1;

        if (twoArgs) {
            --newlen;
        }

        String[] newArgs = new String[newlen];
        int ctr = 0;

        for (int i = 0; i < oldlen; i++) {
            if ((i == indexOfDebug) || (twoArgs && i == (indexOfDebug + 1))) {
                continue;
            }

            newArgs[ctr++] = args[i];
        }

        args = newArgs;
    }

    private void stripOperandFromArgs() {
        // remove the domain-name operand
        // it may not be here!
        if (args.length < 2 || !StringUtils.ok(serverName)) {
            return;
        }

        int newlen = args.length - 1;

        if (serverName.equals(args[newlen])) {
            String[] newargs = new String[newlen];
            System.arraycopy(args, 0, newargs, 0, newlen);
            args = newargs;
        }
    }

    private boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    // We use this simply to tell the difference between fatal errors and other
    // non-fatal conditions.
    private static class RDCException extends Exception {

        private static final long serialVersionUID = 3003852706975708610L;

        private RDCException(Exception e) {
            super(e);
        }

        private RDCException(String message) {
            super(message);
        }
    }
}
