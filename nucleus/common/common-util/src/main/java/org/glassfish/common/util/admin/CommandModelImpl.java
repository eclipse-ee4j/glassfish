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

package org.glassfish.common.util.admin;

import com.sun.enterprise.util.AnnotationUtil;
import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ParamDefaultCalculator;
import org.glassfish.api.UnknownOptionsAreOperands;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ManagedJob;
import org.glassfish.api.admin.config.ModelBinding;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Attribute;

/**
 * Model for an administrative command
 *
 * @author Jerome Dochez
 */
public class CommandModelImpl extends CommandModel {

    // use a LinkedHashMap so params appears in the order they are declared in the class.
    final private Map<String, CommandModel.ParamModel> params;
    final private String commandName;
    final private Class<?> commandClass;
    final private ExecuteOn execOn;
    final private I18n i18n;
    final private boolean dashOk;
    final private LocalStringManager localStrings;
    private boolean managedJob;

    public CommandModelImpl(Class<?> commandType) {

        Service service = commandType.getAnnotation(Service.class);
        commandName = service != null ? service.name() : null;
        commandClass = commandType;
        i18n = commandType.getAnnotation(I18n.class);
        execOn = commandType.getAnnotation(ExecuteOn.class);
        localStrings = new LocalStringManagerImpl(commandType);
        managedJob = AnnotationUtil.presentTransitive(ManagedJob.class, commandType);

        params = init(commandType, i18n, localStrings);
        Class currentClazz = commandType;
        boolean found = false;
        while (currentClazz != null) {
            if (currentClazz.isAnnotationPresent(UnknownOptionsAreOperands.class)) {
                found = true;
            }
            currentClazz = currentClazz.getSuperclass();
        }
        dashOk = found;
    }


    public static Map<String, ParamModel> init(Class commandType, I18n i18n, LocalStringManager localStrings) {

        Class currentClazz = commandType;
        Map<String, ParamModel> results = new LinkedHashMap<String, ParamModel>();
        while (currentClazz != null) {


            for (Field f : currentClazz.getDeclaredFields()) {
                I18n fieldI18n = f.getAnnotation(I18n.class);
                if (fieldI18n!=null) {
                    localStrings = new LocalStringManagerImpl(commandType);
                }
                add(results, f, i18n, localStrings);
            }

            for (Method m : currentClazz.getDeclaredMethods()) {
                I18n fieldI18n = m.getAnnotation(I18n.class);
                if (fieldI18n!=null) {
                    localStrings = new LocalStringManagerImpl(commandType);
                }
                add(results, m, i18n, localStrings);
            }

            currentClazz = currentClazz.getSuperclass();
        }
        return results;
    }

    @Override
    public String getLocalizedDescription() {
        if (i18n!=null) {
            return localStrings.getLocalString(i18n.value(), "");
        } else {
            return null;
        }
    }

    @Override
    public String getUsageText() {
        if (i18n!=null) {
            return localStrings.getLocalString(i18n.value()+".usagetext", null);
        } else {
            return null;
        }
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
    public Class<?> getCommandClass() {
        return commandClass;
    }

    @Override
    public ExecuteOn getClusteringAttributes() {
        return execOn;
    }

    @Override
    public boolean isManagedJob() {
        return managedJob;
    }

    public void setManagedJob(boolean value) {
        this.managedJob = value;
    }


    /**
     * Should an unknown option be considered an operand by asadmin?
     */
    @Override
    public boolean unknownOptionsAreOperands() {
        return dashOk;
    }

    public void addParam(String name, CommandModel.ParamModel param) {
        params.put(name, param);
    }

    private static void add(Map<String, ParamModel> results, AnnotatedElement e, I18n parentI18n, LocalStringManager localStrings) {
        Param param = e.getAnnotation(Param.class);
        if (param!=null) {
            String defaultValue = param.defaultValue();
            ModelBinding mb = e.getAnnotation(ModelBinding.class);
            if (mb!=null && defaultValue.isEmpty()) {
                Method m = null;
                try {
                    m = mb.type().getMethod(mb.getterMethodName());
                } catch (NoSuchMethodException e1) {
                    // ignore.
                }
                if (m!=null) {
                    Attribute attr = m.getAnnotation(Attribute.class);
                    if (attr!=null) {
                        defaultValue = attr.defaultValue();
                    }
                }
            }
            ParamModel model = new ParamModelImpl(e, defaultValue, parentI18n, localStrings);
            if (!results.containsKey(model.getName())) {
                results.put(model.getName(), model);
            }
        }
    }

    @Override
    public void add(ParamModel model) {
        if (!params.containsKey(model.getName())) {
            params.put(model.getName(), model);
        }
    }

    private static class ParamModelImpl extends ParamModel {

        final private String name;
        final private Param param;
        final private I18n i18n;
        final private I18n parentI18n;
        final private LocalStringManager localStrings;
        final private Class type;
        final private String defaultValue;


        ParamModelImpl(AnnotatedElement e, String defaultValue, I18n parentI18n, LocalStringManager localStrings) {
            Param p = e.getAnnotation(Param.class);
            this.parentI18n = parentI18n;
            this.localStrings = localStrings;
            name = getParamName(p, e);
            this.defaultValue = defaultValue;
            param = p;
            i18n = e.getAnnotation(I18n.class);

            if (e instanceof Method) {
                type = ((Method) e).getReturnType();
            } else if (e instanceof Field) {
                type = ((Field) e).getType();
            } else {
                type = String.class;
            }
        }


        @Override
        public String getName() {
            return name;
        }

        @Override
        public Param getParam() {
            return new Param() {
                @Override
                public String name() {
                    return param.name();
                }

                @Override
                public String acceptableValues() {
                    return param.acceptableValues();
                }

                @Override
                public boolean optional() {
                    return param.optional();
                }

                @Override
                public String shortName() {
                    return param.shortName();
                }

                @Override
                public boolean primary() {
                    return param.primary();
                }

                @Override
                public String defaultValue() {
                    return defaultValue;
                }

                @Override
                public Class<? extends ParamDefaultCalculator> defaultCalculator() {
                    return param.defaultCalculator();
                }

                @Override
                public boolean password() {
                    return param.password();
                }

                @Override
                public char separator() {
                    return param.separator();
                }

                @Override
                public boolean multiple() {
                    return param.multiple();
                }

                @Override
                public boolean obsolete() {
                    return param.obsolete();
                }

                @Override
                public String alias() {
                    return param.alias();
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return param.annotationType();
                }
            };
        }

        private String getLocalizedString(String type) {
            String paramDesc=null;
            if (i18n!=null) {
                paramDesc = localStrings.getLocalString(i18n.value() + type, "");
            } else {
                if (parentI18n!=null) {
                     paramDesc = localStrings.getLocalString(parentI18n.value() + "." + name + type, "");
                }
            }
            if (paramDesc==null) {
                paramDesc="";
            }
            return paramDesc;
        }
        @Override
        public String getLocalizedDescription() {
            return getLocalizedString("");
        }

        @Override
        public String getLocalizedPrompt() {
            return getLocalizedString(".prompt");
        }

        @Override
        public String getLocalizedPromptAgain() {
            return getLocalizedString(".promptAgain");
        }

        public I18n getI18n() {
            return i18n;
        }

        @Override
        public Class getType() {
            return type;
        }
    }
}
