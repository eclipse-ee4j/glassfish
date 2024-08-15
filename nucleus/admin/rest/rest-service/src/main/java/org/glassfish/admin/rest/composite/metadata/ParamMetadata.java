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

package org.glassfish.admin.rest.composite.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Level;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.OptionsCapable;
import org.glassfish.admin.rest.RestLogging;
import org.glassfish.admin.rest.composite.CompositeUtil;
import org.jvnet.hk2.config.Attribute;

/**
 *
 * @author jdlee
 */
public class ParamMetadata {
    private String name;
    private Type type;
    private String help;
    private Object defaultValue;
    private boolean readOnly = false;
    private boolean confidential = false;
    private boolean immutable = false;
    private boolean createOnly = false;
    private OptionsCapable context;

    public ParamMetadata() {

    }

    public ParamMetadata(OptionsCapable context, Type paramType, String name, Annotation[] annotations) {
        this.name = name;
        this.context = context;
        this.type = paramType;
        final CompositeUtil instance = CompositeUtil.instance();
        help = instance.getHelpText(annotations);
        defaultValue = getDefaultValue(annotations);

        for (Annotation a : annotations) {
            if (a.annotationType().equals(ReadOnly.class)) {
                readOnly = true;
            }
            if (a.annotationType().equals(Confidential.class)) {
                confidential = true;
            }
            if (a.annotationType().equals(Immutable.class)) {
                immutable = true;
            }
            if (a.annotationType().equals(CreateOnly.class)) {
                createOnly = true;
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return "ParamMetadata{" + "name=" + name + ", type=" + getTypeString() + ", help=" + help + '}';
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        //        o.put("name", name);
        o.put("type", getTypeString());
        o.put("help", help);
        Object defVal = (defaultValue != null) ? defaultValue : JSONObject.NULL;
        o.put("default", defVal);
        o.put("readOnly", readOnly);
        o.put("confidential", confidential);
        o.put("immutable", immutable);
        o.put("createOnly", createOnly);
        return o;
    }

    protected String getTypeString() {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            StringBuilder sb = new StringBuilder(((Class<?>) pt.getRawType()).getSimpleName());
            sb.append("<");
            String sep = "";
            for (Type t : pt.getActualTypeArguments()) {
                sb.append(sep).append(((Class<?>) t).getSimpleName());
                sep = ";";
            }
            return sb.append(">").toString();
        } else {
            return ((Class<?>) type).getSimpleName();
        }
    }

    /**
     * This method will process the annotations for a field to try to determine the default value, if one has been
     * specified.
     *
     * @param annos
     * @return
     */
    private Object getDefaultValue(Annotation[] annos) {
        Object defval = null;
        if (annos != null) {
            for (Annotation annotation : annos) {
                if (Default.class.isAssignableFrom(annotation.getClass())) {
                    try {
                        Default def = (Default) annotation;
                        Class clazz = def.generator();
                        if (def.useContext()) {
                            defval = ((DefaultsGenerator) context).getDefaultValue(name);
                        } else if (clazz != null && clazz != Void.class) {
                            if (DefaultsGenerator.class.isAssignableFrom(clazz)) {
                                defval = ((DefaultsGenerator) clazz.newInstance()).getDefaultValue(name);
                            } else {
                                RestLogging.restLogger.log(Level.SEVERE, RestLogging.DOESNT_IMPLEMENT_DEFAULTS_GENERATOR);
                            }
                        } else {
                            defval = parseValue(def.value());
                        }
                        break;
                    } catch (Exception ex) {
                        RestLogging.restLogger.log(Level.SEVERE, null, ex);
                    }
                } else if (Attribute.class.isAssignableFrom(annotation.getClass())) {
                    Attribute attr = (Attribute) annotation;
                    defval = attr.defaultValue();
                    break;
                }
            }
        }
        return defval;
    }

    private Object parseValue(String value) {
        Class<?> clazz = (Class<?>) type;
        try {
            if (clazz.equals(String.class)) {
                return value;
            }
            if (clazz.equals(Boolean.TYPE) || clazz.equals(Boolean.class)) {
                return Boolean.valueOf(value);
            }
            if (clazz.equals(Integer.TYPE) || clazz.equals(Integer.class)) {
                return Integer.valueOf(value);
            }
            if (clazz.equals(Long.TYPE) || clazz.equals(Long.class)) {
                return Long.valueOf(value);
            }
            if (clazz.equals(Double.TYPE) || clazz.equals(Double.class)) {
                return Double.valueOf(value);
            }
            if (clazz.equals(Float.TYPE) || clazz.equals(Float.class)) {
                return Float.valueOf(value);
            }
            // TBD - arrays/lists of values
            RestLogging.restLogger.log(Level.SEVERE, RestLogging.UNSUPPORTED_FIXED_VALUE);
        } catch (NumberFormatException e) {
            RestLogging.restLogger.log(Level.SEVERE, RestLogging.VALUE_DOES_NOT_MATCH_TYPE);
        }
        return null;
    }
}
