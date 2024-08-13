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
package org.glassfish.internal.api.events;

import java.util.Optional;

import javax.security.auth.Subject;

import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.messaging.MessageReceiver;
import org.glassfish.hk2.api.messaging.SubscribeTo;
import org.glassfish.security.common.UserPrincipal;

/**
 * <p>
 * An HK2 event that is triggered when an Admin command is invoked via the REST interface (including Admin Console and CLI).
 * </p>
 * <p>
 * To listen to this event, create an HK2 subscriber with the following:
 * </p>
 *
 * <ul>
 *   <li>{@link Service} annotation on the class or otherwise register the class as an HK2 service
 *   </li>
 *   <li>{@link MessageReceiver} qualifier annotation on the class with the {@link CommandInvokedEvent} message type
 *   </li>
 *   <li>A listener method with void return type and a single argument of the {@link CommandInvokedEvent} type annotated with the {@link SubscribeTo} annotation
 *   </li>
 *   <li>Make sure that this subscriber is started as an HK2 service to receive the events
 *   </li>
 * </ul>
 *
 * <p>
 * An example listener:
 * </p>
 *
 * <pre>
 *   {@code

@Service
@RunLevel(value = StartupRunLevel.VAL, mode = RunLevel.RUNLEVEL_MODE_NON_VALIDATING)
@MessageReceiver({CommandInvokedEvent.class})
public class CommandSubscriber {

    public void commandInvoked(@SubscribeTo CommandInvokedEvent event) {
        log(event);
    }

 }
 * }
 * </pre>
 *
 * @author Ondro Mihalyi
 */
public class CommandInvokedEvent {

    public CommandInvokedEvent(String commandName, ParameterMap parameters, Subject subject) {
        this.commandName = commandName;
        this.parameters = parameters;
        this.subject = subject;
    }

    private final String commandName;
    private final ParameterMap parameters;
    private final Subject subject;

    public String getCommandName() {
        return commandName;
    }

    public ParameterMap getParameters() {
        return parameters;
    }

    public Subject getSubject() {
        return subject;
    }

    public Optional<UserPrincipal> getUserPrincipal() {
        return subject.getPrincipals().stream()
                .filter(principal -> principal instanceof UserPrincipal)
                .map(UserPrincipal.class::cast)
                .findAny();
    }

}
