/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.cli;

import com.sun.appserv.management.client.prefs.LoginInfo;
import com.sun.appserv.management.client.prefs.LoginInfoStore;
import com.sun.appserv.management.client.prefs.LoginInfoStoreFactory;
import com.sun.enterprise.admin.cli.remote.DASUtils;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.io.Console;
import java.util.logging.Level;

import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * The asadmin login command. Pretend to be a remote command so that program options are allowed.
 *
 * @author Nandini Ektare
 * @author Bill Shannon
 */
@Service(name = "login")
@PerLookup
public class LoginCommand extends CLICommand {

    private String adminUser;
    private char[] adminPassword;

    private static final LocalStringsImpl strings = new LocalStringsImpl(LoginCommand.class);

    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {

        // Step 1: Get admin username and password
        programOpts.setInteractive(true); // force it
        adminUser = getAdminUser();
        programOpts.setUser(adminUser);
        adminPassword = SystemPropertyConstants.DEFAULT_ADMIN_PASSWORD.toCharArray();
        programOpts.setPassword(adminPassword, ProgramOptions.PasswordLocation.DEFAULT);
        boolean interactive = programOpts.isInteractive(); // save value
        programOpts.setInteractive(false); // no more prompting allowed

        // Step 2: Invoke version command to validate the authentication info
        boolean tryAgain = false;
        do {
            switch (DASUtils.pingDASWithAuth(programOpts, env)) {
                case NONE:
                    tryAgain = false;
                    break;
                case AUTHENTICATION:
                    if (tryAgain) { // already tried once
                        throw new CommandException(strings.get("InvalidCredentials", programOpts.getUser()));
                    }
                    tryAgain = true;

                    // maybe we need a password?
                    programOpts.setInteractive(interactive);
                    adminPassword = getAdminPassword();
                    programOpts.setPassword(adminPassword, ProgramOptions.PasswordLocation.USER);
                    programOpts.setInteractive(false);
                    break;
                case CONNECTION:
                    throw new CommandException(
                        strings.get("ConnectException", programOpts.getHost(), "" + programOpts.getPort()));
                case IO:
                    throw new CommandException(
                        strings.get("IOException", programOpts.getHost(), "" + programOpts.getPort()));
                case UNKNOWN:
                    throw new CommandException(
                        strings.get("UnknownException", programOpts.getHost(), "" + programOpts.getPort()));
            }
        } while (tryAgain);

        // Step 3: Save in <userhomedir>/.asadminpass the string
        // asadmin://<adminuser>@<adminhost>:<adminport><encrypted adminpassword>
        saveLogin(programOpts.getHost(), programOpts.getPort(), adminUser, adminPassword);
        return 0;
    }

    /**
     * Prompt for the admin user name.
     */
    private String getAdminUser() {
        Console cons = System.console();
        String user = null;
        String defuser = programOpts.getUser();
        if (defuser == null) {
            defuser = SystemPropertyConstants.DEFAULT_ADMIN_USER;
        }
        if (cons != null) {
            cons.printf("%s", strings.get("AdminUserPrompt", defuser));
            String val = cons.readLine();
            if (val != null && val.length() > 0) {
                user = val;
            } else {
                user = defuser;
            }
        }
        return user;
    }

    /**
     * This methods prompts for the admin password.
     *
     * @return admin password
     * @throws CommandValidationException if adminpassword can't be fetched
     */
    private char[] getAdminPassword() {
        final String prompt = strings.get("AdminPasswordPrompt");

        return readPassword(prompt);
    }

    /*
     * Saves the login information to the login store. Usually this is the file
     * ".asadminpass" in user's home directory.
     */
    private void saveLogin(String host, final int port, final String user, final char[] passwd) {
        if (!ok(host)) {
            host = "localhost";
        }
        // to avoid putting commas in the port number (e.g., "4,848")...
        String sport = Integer.toString(port);
        try {
            // By definition, the host name will default to "localhost" and
            // entry is overwritten
            final LoginInfoStore store = LoginInfoStoreFactory.getStore(null);
            final LoginInfo login = new LoginInfo(host, port, user, passwd);
            if (store.exists(login.getHost(), login.getPort())) {
                // Let the user know that the user has chosen to overwrite the
                // login information. This is non-interactive, on purpose
                logger.info(strings.get("OverwriteLoginMsgCreateDomain", login.getHost(), "" + login.getPort()));
            }
            store.store(login, true);
            logger.info(strings.get("LoginInfoStored", user, login.getHost(), sport, store.getName()));
        } catch (final Exception e) {
            logger.warning(strings.get("LoginInfoNotStored", host, sport));
            logger.log(Level.FINER, "Could not save login!", e);
        }
    }
}
