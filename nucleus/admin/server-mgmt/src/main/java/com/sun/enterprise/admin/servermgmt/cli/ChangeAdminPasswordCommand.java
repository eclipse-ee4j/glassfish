/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.admin.cli.ProgramOptions;
import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherFactory;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.admin.remote.RemoteRestAdminCommand;
import com.sun.enterprise.config.serverbeans.SecureAdmin;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.net.NetUtils;

import java.io.Console;
import java.io.IOException;
import java.net.ConnectException;

import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.glassfish.security.common.FileRealmHelper;
import org.jvnet.hk2.annotations.Service;

/**
 * The change-admin-password command. The remote command implementation presents a different interface (set of options)
 * than the local command. This special local implementation adapts the local interface to the requirements of the
 * remote command.
 *
 * The remote command is different in that it accepts the user name as an operand. This command accepts it via the
 * --user parameter. If the --user option isn't specified, this command prompts for the user name.
 *
 * Another difference is that the local command will prompt for the old password only once. The default behavior
 * for @Param for passwords is to prompt for the password twice. *
 *
 * @author Bill Shannon
 */
@Service(name = "change-admin-password")
@PerLookup
@I18n("change.admin.password")
public class ChangeAdminPasswordCommand extends LocalDomainCommand {
    private ParameterMap params;

    private static final LocalStringsImpl strings = new LocalStringsImpl(ChangeAdminPasswordCommand.class);

    @Param(name = "domain_name", optional = true)
    private String userArgDomainName;

    @Param(password = true, optional = true)
    private String password;

    @Param(password = true, optional = true)
    private String newpassword;

    private SecureAdmin secureAdmin = null;

    /**
     * Require the user to actually type the passwords unless they are in the file specified by the --passwordfile option.
     */
    @Override
    protected void validate() throws CommandException, CommandValidationException {
        setDomainName(userArgDomainName);
        super.validate();
        /*
         * If --user wasn't specified as a program option,
         * we treat it as a required option and prompt for it
         * if possible.
         */
        if (programOpts.getUser() == null) {
            // prompt for it (if interactive)
            Console cons = System.console();
            if (cons != null && programOpts.isInteractive()) {
                cons.printf("%s", strings.get("AdminUserDefaultPrompt", SystemPropertyConstants.DEFAULT_ADMIN_USER));
                String val = cons.readLine();
                if (ok(val)) {
                    programOpts.setUser(val);
                } else {
                    programOpts.setUser(SystemPropertyConstants.DEFAULT_ADMIN_USER);
                }
            } else {
                //logger.info(strings.get("AdminUserRequired"));
                throw new CommandValidationException(strings.get("AdminUserRequired"));
            }
        }

        if (password == null) {
            // prompt for it (if interactive)
            char[] pwdChar = getPassword("password", strings.get("AdminPassword"), null, false);
            password = pwdChar != null ? new String(pwdChar) : null;
            if (password == null) {
                throw new CommandValidationException(strings.get("AdminPwRequired"));
            }
            programOpts.setPassword(password.toCharArray(), ProgramOptions.PasswordLocation.USER);
        }

        if (newpassword == null) {
            // prompt for it (if interactive)
            char[] pwdChar = getPassword("newpassword", strings.get("change.admin.password.newpassword"),
                    strings.get("change.admin.password.newpassword.again"), true);
            newpassword = pwdChar != null ? new String(pwdChar) : null;
            if (newpassword == null) {
                throw new CommandValidationException(strings.get("AdminNewPwRequired"));
            }
        }

        /*
         * Now that the user-supplied parameters have been validated,
         * we set the parameter values for the remote command.
         */
        params = new ParameterMap();
        params.set("DEFAULT", programOpts.getUser());
        params.set("password", password);
        params.set("newpassword", newpassword);
    }

    /**
     * Execute the remote command using the parameters we've collected.
     */
    @Override
    protected int executeCommand() throws CommandException {

        if (ok(domainDirParam) || ok(userArgDomainName)) {
            //If domaindir or domain arguments are provided,
            // do not attempt remote connection. Change password locally
            String domainDir = (ok(domainDirParam)) ? domainDirParam : getDomainsDir().getPath();
            String domainName = (ok(userArgDomainName)) ? userArgDomainName : getDomainName();
            return changeAdminPasswordLocally(domainDir, domainName);

        }
        try {
            RemoteRestAdminCommand rac = new RemoteRestAdminCommand(name, programOpts.getHost(), programOpts.getPort(),
                    programOpts.isSecure(), programOpts.getUser(), programOpts.getPassword(), logger, false, false);
            rac.executeCommand(params);
            return SUCCESS;
        } catch (CommandException ce) {
            if (ce.getCause() instanceof ConnectException) {
                //Remote change failure - change password with default values of
                // domaindir and domain name,if the --host option is not provided.
                if (!isLocalHost(programOpts.getHost())) {
                    throw ce;
                }
                return changeAdminPasswordLocally(getDomainsDir().getPath(), getDomainName());
            }
            throw ce;
        }
    }

    private int changeAdminPasswordLocally(String domainDir, String domainName) throws CommandException {

        if (!isLocalHost(programOpts.getHost())) {
            throw new CommandException(strings.get("CannotExecuteLocally"));
        }

        GFLauncher launcher = null;
        try {
            launcher = GFLauncherFactory.getInstance(RuntimeType.DAS);
            GFLauncherInfo info = launcher.getInfo();
            info.setDomainName(domainName);
            info.setDomainParentDir(domainDir);
            launcher.setup();

            //If secure admin is enabled and if new password is null
            //throw new exception
            if (launcher.isSecureAdminEnabled()) {
                if ((newpassword == null) || (newpassword.isEmpty())) {
                    throw new CommandException(strings.get("NullNewPassword"));
                }
            }

            String adminKeyFile = launcher.getAdminRealmKeyFile();

            if (adminKeyFile != null) {
                //This is a FileRealm, instantiate it.
                FileRealmHelper helper = new FileRealmHelper(adminKeyFile);

                //Authenticate the old password
                String[] groups = helper.authenticate(programOpts.getUser(), password.toCharArray());
                if (groups == null) {
                    throw new CommandException(strings.get("InvalidCredentials", programOpts.getUser()));
                }
                helper.updateUser(programOpts.getUser(), programOpts.getUser(), newpassword.toCharArray(), null);
                helper.persist();
                return SUCCESS;

            } else {
                //Cannot change password locally for non file realms
                throw new CommandException(strings.get("NotFileRealmCannotChangeLocally"));

            }

        } catch (MiniXmlParserException ex) {
            throw new CommandException(ex);
        } catch (GFLauncherException ex) {
            throw new CommandException(ex);
        } catch (IOException ex) {
            throw new CommandException(ex);
        }
    }

    private static boolean isLocalHost(String host) {
        if (host != null && (NetUtils.isThisHostLocal(host) || NetUtils.isLocal(host))) {
            return true;
        }
        return false;
    }
}
