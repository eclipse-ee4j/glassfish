/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.cli.embeddable;

import com.sun.enterprise.admin.cli.CLIUtil;
import com.sun.enterprise.admin.cli.Parser;
import com.sun.enterprise.admin.cli.ProgramOptions;

import jakarta.inject.Inject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.embeddable.CommandResult;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.EmbeddedSystemAdministrator;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author bhavanishankar@dev.java.net
 * @author sanjeeb.sahoo@sun.com
 */
@Service()
@PerLookup // this is a PerLookup service
@ContractsProvided({ org.glassfish.embeddable.CommandRunner.class, CommandExecutorImpl.class })
// bcos CommandRunner interface can't depend on HK2, we need ContractProvided here.

public class CommandExecutorImpl implements org.glassfish.embeddable.CommandRunner {

    @Inject
    private CommandRunner<?> commandRunner;

    @Inject
    private ServiceLocator serviceLocator;

    @Inject
    private EmbeddedSystemAdministrator embeddedSystemAdministrator;

    private boolean terse;

    @Override
    public CommandResult run(String command, String... args) {
        try {
            ActionReport actionReport = executeCommand(command, args);
            return convert(actionReport);
        } catch (Exception e) {
            return convert(e);
        }
    }

    ParameterMap getParameters(String command, String[] args) throws CommandException {
        CommandModel commandModel = commandRunner.getModel(command);
        if (commandModel == null) {
            throw new CommandException("Command lookup failed for command " + command);
        }

        // Filter out the global options.
        // We are interested only in --passwordfile option. No other options are relevant when GlassFish is running in embedded mode.
        Parser parser = new Parser(args, 0, ProgramOptions.getValidOptions(), true);
        ParameterMap globalOptions = parser.getOptions();
        List<String> operands = parser.getOperands();
        String argv[] = operands.toArray(new String[operands.size()]);

        parser = new Parser(argv, 0, commandModel.getParameters(), false);
        ParameterMap options = parser.getOptions();
        operands = parser.getOperands();
        options.set("DEFAULT", operands);
        // if command has a "terse" option, set it in options
        if (commandModel.getModelFor("terse") != null) {
            options.set("terse", Boolean.toString(terse));
        }

        // Read the passwords from the password file and set it in command options.
        if (globalOptions.size() > 0) {
            String pwfile = globalOptions.getOne(ProgramOptions.PASSWORDFILE);
            if (pwfile != null && pwfile.length() > 0) {
                Map<String, String> passwords = CLIUtil.readPasswordFileOptions(pwfile, false);
                for (CommandModel.ParamModel opt : commandModel.getParameters()) {
                    if (opt.getParam().password()) {
                        String pwdname = opt.getName();
                        String pwd = passwords.get(pwdname);
                        if (pwd != null) {
                            options.set(pwdname, pwd);
                        }
                    }
                }
            }
        }

        return options;
    }

    @Override
    public void setTerse(boolean terse) {
        this.terse = terse;
    }

    /**
     * Runs a command from somewhere OTHER THAN an already-running, previously-authorized command.
     * <p>
     * If a command is already running then it should have a valid Subject and that Subject must be used in running a nested
     * command. This method uses the internal system admin identity to authorize the command to be run and this should never
     * be done if a user has authenticated to the system and is running a separate, already-authorized command. This method
     * is, therefore, used from some embedded functionality.
     *
     * @param command
     * @param args
     * @return
     * @throws CommandException
     */
    ActionReport executeCommand(String command, String... args) throws CommandException {
        ParameterMap commandParams = getParameters(command, args);
        final ActionReport actionReport = createActionReport();
        Subject subject = embeddedSystemAdministrator.getSubject();
        CommandInvocation<?> inv = commandRunner.getCommandInvocation(command, actionReport, subject);
        inv.parameters(commandParams).execute();
        return actionReport;
    }

    private CommandResult convert(final ActionReport actionReport) {
        return new CommandResult() {
            @Override
            public ExitStatus getExitStatus() {
                final ActionReport.ExitCode actionExitCode = actionReport.getActionExitCode();
                switch (actionExitCode) {
                case SUCCESS:
                    return ExitStatus.SUCCESS;
                case WARNING:
                    return ExitStatus.WARNING;
                case FAILURE:
                    return ExitStatus.FAILURE;
                default:
                    throw new RuntimeException("Unknown exit code: " + actionExitCode);
                }
            }

            @Override
            public String getOutput() {
                final ByteArrayOutputStream os = new ByteArrayOutputStream();
                try {
                    actionReport.writeReport(os);
                    return os.toString(UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        os.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }

            @Override
            public Throwable getFailureCause() {
                return actionReport.getFailureCause();
            }
        };
    }

    private CommandResult convert(final Exception e) {
        return new CommandResult() {
            @Override
            public ExitStatus getExitStatus() {
                return ExitStatus.FAILURE;
            }

            @Override
            public String getOutput() {
                return "Exception while executing command.";
            }

            @Override
            public Throwable getFailureCause() {
                return e;
            }
        };
    }

    ActionReport createActionReport() {
        return serviceLocator.getService(ActionReport.class, "plain");
    }

    CommandRunner<?> getCommandRunner() {
        return commandRunner;
    }

}
