/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.beans.Introspector;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.glassfish.api.Param;

/**
 * Model for an administrative command
 *
 * @author Jerome Dochez
 */
public abstract class CommandModel {

    /**
     * Returns the command name as it is typed by the user.
     *
     * @return the command name
     */
    public abstract String getCommandName();

    /**
     * Returns a localized description for this command
     *
     * @return a localized displayable description
     */
    public abstract String getLocalizedDescription();

    /**
     * Returns a localized usage text for this command or null if the usage text should be generated from this model.
     *
     * @return the usage text
     */
    public abstract String getUsageText();

    /**
     * Returns the parameter model for a particular parameter
     *
     * @param paramName the requested parameter model name
     * @return the parameter model if the command supports a parameter of the passed name or null if not.
     */
    public abstract ParamModel getModelFor(String paramName);

    /**
     * Returns a collection of parameter names supported by this admininstrative command
     *
     * @return all the command's paramter names.
     */
    public abstract Collection<String> getParametersNames();

    /**
     * Return the class that defines the command. Normally this will be the class that provides the implementation of the
     * command, but for generic CRUD commands it might be the config class that defines the command. The command class is
     * used to locate resources related to the command, e.g., the command's man page. If the command model isn't associated
     * with a command class, null is returned.
     *
     * @return the command class, or null if none
     */
    public abstract Class<?> getCommandClass();

    /**
     * This command is managed job. It is preferred to listen using SSE in case of remote execution.
     *
     * @return {@code true} only if command is @ManagedJob
     */
    public abstract boolean isManagedJob();

    /**
     * Return the cluster parameters for this command or null if none are specified and defaults should be used.
     *
     * @return a {@link ExecuteOn} annotation instance or null
     */
    public abstract ExecuteOn getClusteringAttributes();

    /**
     * Add a ParamModel for this command
     *
     * @param model the new param model to be added
     */
    public abstract void add(ParamModel model);

    /**
     * Returns a collection of parameter model for all the parameters supported by this command.
     *
     * @return the command's parameters models.
     */
    public Collection<ParamModel> getParameters() {
        ArrayList<ParamModel> copy = new ArrayList<>();
        for (String name : getParametersNames()) {
            copy.add(getModelFor(name));
        }
        return copy;
    }

    /**
     * Get the Param name. First it checks if the annotated Param includes a name, if not then it gets the name from the
     * field. If the parameter is a password, add the prefix and change the name to upper case.
     *
     * @param param class annotation
     * @param annotated annotated field or method
     * @return the name of the param
     */
    public static String getParamName(Param param, AnnotatedElement annotated) {
        String name = param.name();
        if (name.equals("")) {
            if (annotated instanceof Field) {
                name = ((Field) annotated).getName();
            }
            if (annotated instanceof Method) {
                if (((Method) annotated).getName().startsWith("is")) {
                    name = ((Method) annotated).getName().substring(2);
                } else {
                    name = ((Method) annotated).getName().substring(3);
                }
                name = Introspector.decapitalize(name);
            }
        }
        return name;
    }

    /**
     * Model for a command parameter.
     *
     */
    public static abstract class ParamModel {

        /**
         * Returns the command parameter name.
         *
         * @return the command parameter name
         */
        public abstract String getName();

        /**
         * Returns the command @Param annotation values.
         *
         * @return the @Param instance for this parameter
         */
        public abstract Param getParam();

        /**
         * Returns a localized description for this parameter
         *
         * @return a localized String
         */
        public abstract String getLocalizedDescription();

        /**
         * Returns a localized prompt for this parameter
         *
         * @return a localized String
         */
        public abstract String getLocalizedPrompt();

        /**
         * Returns a localized confirmation prompt for this parameter. This is only used for passwords.
         *
         * @return a localized String
         */
        public abstract String getLocalizedPromptAgain();

        /**
         * Returns the parameter type.
         *
         * @return the parameter type.
         */
        public abstract Class getType();

        public boolean isParamId(String key) {
            if (getParam().primary()) {
                return "DEFAULT".equals(key) || getName().equalsIgnoreCase(key);
            }

            return getName().equalsIgnoreCase(key) || getParam().shortName().equals(key) || getParam().alias().equalsIgnoreCase(key);
        }

    }

    /**
     * Should an unknown option be considered an operand by asadmin?
     *
     * @return true if unknown options are operands.
     */
    public boolean unknownOptionsAreOperands() {
        return false; // default implementation
    }

}
