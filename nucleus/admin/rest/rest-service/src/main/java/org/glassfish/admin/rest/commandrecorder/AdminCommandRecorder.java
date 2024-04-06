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
package org.glassfish.admin.rest.commandrecorder;

import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.security.auth.Subject;
import org.glassfish.admin.rest.RestLogging;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.security.common.UserPrincipal;

/**
 *
 * @author Ondro Mihalyi
 */
public class AdminCommandRecorder {

    public void logCommand(String commandName, ParameterMap parameters, Subject subject) {
        if (shouldLogCommand(commandName)) {
            final Logger logger = RestLogging.restLogger;
            String commandLine = constructCommandLine(commandName, parameters);
            Optional<UserPrincipal> userPrincipalMaybe = getUserPrincipal(subject);
            logger.info(() -> {
                return userPrincipalMaybe.map(user -> "User " + user.getName())
                        .orElse("Unknown user")
                        + " executed command in the Admin Console: " + commandLine;
            });
        }

    }

    private String constructCommandLine(String commandName, ParameterMap parameters) {
        final String DEFAULT_PARAM_KEY = "DEFAULT";
        final StringBuilder commandLineBuilder = new StringBuilder();
        final Stream<String> namedParamsStream = parameters.entrySet().stream()
                .filter(param -> !"userpassword".equals(param.getKey()))
                .filter(param -> !DEFAULT_PARAM_KEY.equals(param.getKey()))
                .map(param -> "--" + param.getKey() + "=" + param.getValue().get(0));
        final List<String> unnamedParams = parameters.get(DEFAULT_PARAM_KEY);
        return Stream.concat(Stream.concat(Stream.of(commandName), namedParamsStream), unnamedParams != null ? unnamedParams.stream() : Stream.empty())
                .collect(joining(" "));
    }

    private boolean shouldLogCommand(String commandName) {
        return Stream.of("version", "_(.*)", "list(.*)", "get(.*)", "(.*)-list-services", "uptime",
                "enable-asadmin-recorder", "disable-asadmin-recorder", "set-asadmin-recorder-configuration",
                "asadmin-recorder-enabled")
                .filter(commandName::matches)
                .findAny().isEmpty();
    }

    private Optional<UserPrincipal> getUserPrincipal(Subject subject) {
        return subject.getPrincipals().stream()
                .filter(principal -> principal instanceof UserPrincipal)
                .map(UserPrincipal.class::cast)
                .findAny();
    }

}
