/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.admin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * Utility class for command framework. Currently it just provides hooks for command runner, to extend command
 * functionality using aspects. It might be extended in future with more listeners for command life cycle phases, and
 * additional utility methods. This class is in development and is subject to change.
 *
 * @author andriy.zhdanov
 *
 */
public final class CommandSupport {

    /**
     * Get parameter value for a command.
     *
     * @param command
     * @param name parameter name
     *
     * @return parameter value or null in case of any problem.
     */
    public static String getParamValue(AdminCommand command, String name) {
        return getParamValue(command, name, String.class);
    }

    /**
     * Get parameter value for a command.
     *
     * @param command
     * @param name parameter name
     * @param paramType expected return type
     *
     * @return parameter value or null in case of any problem.
     */
    public static <T> T getParamValue(AdminCommand command, String name, Class<T> paramType) {
        AdminCommand unwrappedCommand = getUnwrappedCommand(command);
        Class<?> commandClass = unwrappedCommand.getClass();
        for (final Field field : commandClass.getDeclaredFields()) {
            Param param = field.getAnnotation(Param.class);
            if (param != null && name.equals(CommandModel.getParamName(param, field))) {
                if (!paramType.isAssignableFrom(field.getType())) {
                    break; // return null
                }
                try {
                    AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                        field.setAccessible(true);
                        return null;
                    });
                    Object value = field.get(unwrappedCommand);
                    return paramType.cast(value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unexpected error", e);
                }
            }
        }
        return null;
    }

    /**
     * Execute aspects when command is just completely initialized, i..e injected with parameters.
     */
    public static void init(final ServiceLocator serviceLocator, final AdminCommand command, final AdminCommandContext context, final Job instance) {

        processAspects(serviceLocator, command, (a, aspect, command1) -> {
            aspect.init(a, command1, context, instance);
            return command1;
        });
    }

    /**
     * Execute aspects when command is finished successfully or not.
     */
    public static void done(final ServiceLocator serviceLocator, final AdminCommand command, final Job instance, boolean isNotify) {

        processAspects(serviceLocator, command, (a, aspect, command1) -> {
            aspect.done(a, command1, instance);
            return command1;
        });
        if (isNotify) {
            CommandAspectFacade commandAspectFacade = serviceLocator.getService(CommandAspectFacade.class);
            if (commandAspectFacade != null) {
                commandAspectFacade.done(command, instance);
            }
        }
    }

    public static void done(final ServiceLocator serviceLocator, final AdminCommand command, final Job instance) {
        done(serviceLocator, command, instance, false);
    }

    /**
     * Execute wrapping aspects, see {@link org.glassfish.api.AsyncImpl} for example.
     */
    public static AdminCommand createWrappers(final ServiceLocator serviceLocator, final CommandModel model, final AdminCommand command,
            final ActionReport report) {

        return processAspects(serviceLocator, command, (a, cai, command1) -> cai.createWrapper(a, model, command1, report));
    }

    private static AdminCommand processAspects(ServiceLocator serviceLocator, AdminCommand command, Function function) {

        Annotation annotations[] = getUnwrappedCommand(command).getClass().getAnnotations();
        // TODO: annotations from wrapper class
        for (Annotation a : annotations) {
            CommandAspect ca = a.annotationType().getAnnotation(CommandAspect.class);
            if (ca != null) {
                CommandAspectImpl<Annotation> cai = serviceLocator.<CommandAspectImpl<Annotation>>getService(ca.value());
                command = function.apply(a, cai, command);
            }
        }

        return command;
    }

    // Get root of wrapped command.
    private static AdminCommand getUnwrappedCommand(AdminCommand wrappedCommand) {
        if (wrappedCommand instanceof WrappedAdminCommand) {
            return ((WrappedAdminCommand) wrappedCommand).getWrappedCommand();
        }
        return wrappedCommand;
    }

    private interface Function {
        AdminCommand apply(Annotation ca, CommandAspectImpl<Annotation> cai, AdminCommand object);
    }

}
