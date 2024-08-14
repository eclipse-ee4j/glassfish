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

package com.sun.enterprise.admin.util;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.glassfish.api.Param;
import org.glassfish.api.ParamDefaultCalculator;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.ExecuteOn;

/**
 * A command and parameter model that allows the data to be supplied directly.
 *
 * @author Jerome Dochez
 * @author Bill Shannon
 */
public class CommandModelData extends CommandModel {

    // use a LinkedHashMap so params appears in the order they are added
    private final Map<String, CommandModel.ParamModel> params = new LinkedHashMap<String, ParamModel>();
    private final String commandName;
    public boolean managedJob = false;
    public boolean dashOk = false;

    public CommandModelData(String name) {
        commandName = name;
    }

    CommandModelData() {
        commandName = null;
    }

    @Override
    public String getLocalizedDescription() {
        return null;
    }

    @Override
    public String getUsageText() {
        return null;
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public CommandModel.ParamModel getModelFor(String paramName) {
        return params.get(paramName);
    }

    @Override
    public Collection<String> getParametersNames() {
        return params.keySet();
    }

    @Override
    public Class getCommandClass() {
        return null;
    }

    @Override
    public ExecuteOn getClusteringAttributes() {
        return null;
    }

    @Override
    public boolean isManagedJob() {
        return this.managedJob;
    }

    /**
     * Should an unknown option be considered an operand by asadmin?
     */
    @Override
    public boolean unknownOptionsAreOperands() {
        return dashOk;
    }

    /**
     * Add the ParamModel to this CommandModel.
     */
    public void add(ParamModel model) {
        if (!params.containsKey(model.getName())) {
            params.put(model.getName(), model);
        }
    }

    /**
     * A parameter model that's just data.
     */
    public static class ParamModelData extends ParamModel {

        public String name;
        public ParamData param;
        public Class type;
        // from the server, for password fields
        public String prompt;
        public String promptAgain;

        public ParamModelData(String name, Class type, boolean optional, String def) {
            this(name, type, optional, def, null);
        }

        public ParamModelData(String name, Class type, boolean optional, String def, String shortName) {
            this(name, type, optional, def, shortName, false);
        }

        public ParamModelData(String name, Class type, boolean optional, String def, String shortName, boolean obsolete) {
            this(name, type, optional, def, shortName, obsolete, "");
        }

        public ParamModelData(String name, Class type, boolean optional, String def, String shortName, boolean obsolete, String alias) {
            ParamData param = new ParamData();
            param._name = name;
            param._optional = optional;
            param._defaultValue = def;
            if (shortName == null)
                shortName = "";
            param._shortName = shortName;
            param._obsolete = obsolete;
            if (alias == null)
                alias = "";
            param._alias = alias;
            ParamModelData.this.name = name;
            ParamModelData.this.type = type;
            ParamModelData.this.param = param;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getLocalizedDescription() {
            return "";
        }

        @Override
        public String getLocalizedPrompt() {
            return getPrompt();
        }

        @Override
        public String getLocalizedPromptAgain() {
            return getPromptAgain();
        }

        @Override
        public Param getParam() {
            return param;
        }

        @Override
        public Class getType() {
            return type;
        }

        // unique to ParamModelData
        public String getPrompt() {
            return prompt;
        }

        public String getPromptAgain() {
            return promptAgain;
        }

        @Override
        public String toString() {
            return "ParamModelData: name=" + name + ", type=" + type + ", i18n=" + getLocalizedDescription() + ", param="
                    + param.toString();
        }
    }

    /**
     * A Param annotation simulated with data.
     */
    public static class ParamData implements Param {
        public String _name = "";
        public String _acceptableValues = "";
        public boolean _optional = false;
        public String _shortName = "";
        public boolean _primary = false;
        public String _defaultValue = "";
        public boolean _password = false;
        public char _separator = ',';
        public boolean _multiple = false;
        public boolean _obsolete = false;
        public String _alias = "";

        @Override
        public Class<? extends Annotation> annotationType() {
            return Param.class;
        }

        @Override
        public String name() {
            return _name;
        }

        @Override
        public String acceptableValues() {
            return _acceptableValues;
        }

        @Override
        public boolean optional() {
            return _optional;
        }

        @Override
        public String shortName() {
            return _shortName;
        }

        @Override
        public boolean primary() {
            return _primary;
        }

        @Override
        public String defaultValue() {
            return _defaultValue;
        }

        @Override
        public Class<? extends ParamDefaultCalculator> defaultCalculator() {
            return ParamDefaultCalculator.class;
        }

        @Override
        public boolean password() {
            return _password;
        }

        @Override
        public char separator() {
            return _separator;
        }

        @Override
        public boolean multiple() {
            return _multiple;
        }

        @Override
        public boolean obsolete() {
            return _obsolete;
        }

        @Override
        public String alias() {
            return _alias;
        }

        @Override
        public String toString() {
            return "ParamData: name=" + _name + ", acceptableValues=" + _acceptableValues + ", optional=" + _optional + ", shortName="
                    + _shortName + ", primary=" + _primary + ", defaultValue=" + _defaultValue + ", password=" + _password + ", separator="
                    + _separator + ", multiple=" + _multiple + ", obsolete=" + _obsolete + ", alias=" + _alias;
        }
    }
}
