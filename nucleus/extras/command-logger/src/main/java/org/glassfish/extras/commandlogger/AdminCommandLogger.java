/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.glassfish.extras.commandlogger;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;
import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.security.auth.Subject;
import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.config.support.TranslatedConfigView;
import org.glassfish.hk2.api.messaging.MessageReceiver;
import org.glassfish.hk2.api.messaging.SubscribeTo;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.events.CommandInvokedEvent;
import org.glassfish.security.common.UserPrincipal;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author Ondro Mihalyi
 */
@Service
@RunLevel(value = StartupRunLevel.VAL, mode = RunLevel.RUNLEVEL_MODE_NON_VALIDATING)
@MessageReceiver({CommandInvokedEvent.class})
public class AdminCommandLogger {

    private static final System.Logger logger = System.getLogger(AdminCommandLogger.class.getName());

    public void receiveCommandInvokedEvent(@SubscribeTo CommandInvokedEvent event) {
        logCommand(event.getCommandName(), event.getParameters(), event.getSubject());
    }

    public void logCommand(String commandName, ParameterMap parameters, Subject subject) {
        if (shouldLogCommand(commandName)) {
            String commandLine = constructCommandLine(commandName, parameters);
            Optional<UserPrincipal> userPrincipalMaybe = getUserPrincipal(subject);
            logger.log(INFO, () -> {
                return userPrincipalMaybe.map(user -> "User " + user.getName())
                        .orElse("Unknown user")
                        + " executed admin command: " + commandLine;
            });
        }

    }

    private String constructCommandLine(String commandName, ParameterMap parameters) {
        final String DEFAULT_PARAM_KEY = "DEFAULT";
        final Stream<String> namedParamsStream = parameters.entrySet().stream()
                .filter(param -> !"userpassword".equals(param.getKey()))
                .filter(param -> !DEFAULT_PARAM_KEY.equals(param.getKey()))
                .map(param -> "--" + param.getKey() + "=" + param.getValue().get(0));
        final List<String> unnamedParams = parameters.get(DEFAULT_PARAM_KEY);
        final Stream<? extends String> unnamedParamsStream = unnamedParams != null ? unnamedParams.stream() : Stream.empty();
        return Stream.concat(Stream.concat(
                Stream.of(commandName),
                namedParamsStream),
                unnamedParamsStream)
                .collect(joining(" "));
    }

    private static enum LogMode {
        ALL_COMMANDS, INTERNAL_COMMANDS, WRITE_COMMANDS, READ_WRITE_COMMANDS, NO_COMMAND;

        public static final LogMode DEFAULT = LogMode.NO_COMMAND;
        public static final String PROPERTY_NAME = "glassfish.commandlogger.logmode";

        public static LogMode get() {
            final String logModeValue = TranslatedConfigView.expandValue("${" + LogMode.PROPERTY_NAME + "}");
            if (logModeValue != null && !logModeValue.startsWith("$")) {
                try {
                    return LogMode.valueOf(logModeValue);
                } catch (IllegalArgumentException e) {
                    logger.log(WARNING,
                            () -> "The value of the property " + LogMode.PROPERTY_NAME + " is invalid: " + logModeValue
                            + ". It should be one of " + Arrays.toString(LogMode.values()));
                    return LogMode.DEFAULT;
                }
            } else {
                return LogMode.DEFAULT;
            }
        }

    }

    private boolean shouldLogCommand(String commandName) {
        final LogMode logMode = LogMode.get();
        switch (logMode) {
            case ALL_COMMANDS:
                return true;
            case NO_COMMAND:
                return false;
            case INTERNAL_COMMANDS:
                return !isReadCommand(commandName);
            case READ_WRITE_COMMANDS:
                return !isInternalCommand(commandName);
            case WRITE_COMMANDS:
                return !isReadCommand(commandName) && !isInternalCommand(commandName);
        }
        throw new IllegalStateException("Log mode " + logMode + " not supported yet.");
    }

    private boolean isReadCommand(String commandName) {
        return Stream.of(
                "attach", "backup-domain", "collect-log-files", "export(.*)", "generate-jvm-report", "get(.*)",
                "jms-ping", "list(.*)", "login", "monitor", "ping(.*)", "show(.*)",
                "uptime", "validate(.*)", "verify(.*)", "version", "(.*)-list-services")
                .filter(commandName::matches)
                .findAny().isPresent();
    }

    private boolean isInternalCommand(String commandName) {
        return commandName.matches("_(.*)");
    }

    private Optional<UserPrincipal> getUserPrincipal(Subject subject) {
        return subject.getPrincipals().stream()
                .filter(principal -> principal instanceof UserPrincipal)
                .map(UserPrincipal.class::cast)
                .findAny();
    }

}
