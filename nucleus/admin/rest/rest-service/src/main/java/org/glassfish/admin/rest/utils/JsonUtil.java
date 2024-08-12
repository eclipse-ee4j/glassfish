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

package org.glassfish.admin.rest.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.composite.RestModel;
import org.glassfish.admin.rest.composite.metadata.Confidential;
import org.glassfish.admin.rest.model.ResponseBody;

/**
 *
 * @author jdlee
 */
public class JsonUtil {
    public static final String CONFIDENTIAL_PROPERTY_SET = "@_Oracle_Confidential_Property_Set_V1.1_#";
    public static final String CONFIDENTIAL_PROPERTY_UNSET = null;

    public static Object getJsonObject(Object object) throws JSONException {
        return getJsonObject(object, true);
    }

    public static Object getJsonObject(Object object, boolean hideConfidentialProperties) throws JSONException {
        Object result;
        if (object instanceof Collection) {
            result = processCollection((Collection) object);
        } else if (object instanceof Map) {
            result = processMap((Map) object);
        } else if (object == null) {
            result = JSONObject.NULL;
        } else if (RestModel.class.isAssignableFrom(object.getClass())) {
            result = getJsonForRestModel((RestModel) object, hideConfidentialProperties);
        } else if (object instanceof ResponseBody) {
            result = ((ResponseBody) object).toJson();
        } else {
            Class<?> clazz = object.getClass();
            if (clazz.isArray()) {
                JSONArray array = new JSONArray();
                final int lenth = Array.getLength(object);
                for (int i = 0; i < lenth; i++) {
                    array.put(getJsonObject(Array.get(object, i)));
                }
                result = array;
            } else {
                result = object;
            }
        }

        return result;
    }

    public static JSONObject getJsonForRestModel(RestModel model, boolean hideConfidentialProperties) {
        JSONObject result = new JSONObject();
        for (Method m : model.getClass().getDeclaredMethods()) {
            if (m.getName().startsWith("get")) { // && !m.getName().equals("getClass")) {
                String propName = m.getName().substring(3);
                propName = propName.substring(0, 1).toLowerCase(Locale.getDefault()) + propName.substring(1);
                if (!model.isTrimmed() || model.isSet(propName)) { // TBD - remove once the conversion to the new REST style guide is completed
                    //              if (model.isSet(propName)) {
                    // Only include properties whose value has been set in the model
                    try {
                        result.put(propName, getJsonObject(getRestModelProperty(model, m, hideConfidentialProperties)));
                    } catch (Exception e) {
                    }
                }
            }
        }

        return result;
    }

    private static Object getRestModelProperty(RestModel model, Method method, boolean hideConfidentialProperties) throws Exception {
        Object object = method.invoke(model);
        if (hideConfidentialProperties && isConfidentialString(model, method)) {
            String str = (String) object;
            return (StringUtil.notEmpty(str)) ? CONFIDENTIAL_PROPERTY_SET : CONFIDENTIAL_PROPERTY_UNSET;
        } else {
            return object;
        }
    }

    private static boolean isConfidentialString(RestModel model, Method method) {
        if (!String.class.equals(method.getReturnType())) {
            return false;
        }
        // TBD - why aren't the annotations available from 'method'?
        return isConfidentialProperty(model.getClass(), method.getName());
    }

    public static boolean isConfidentialProperty(Class clazz, String getterMethodName) {
        // Try this class
        if (getConfidentialAnnotation(clazz, getterMethodName) != null) {
            return true;
        }
        // Try its interfaces
        for (Class<?> iface : clazz.getInterfaces()) {
            if (getConfidentialAnnotation(iface, getterMethodName) != null) {
                return true;
            }
        }
        return false;
    }

    private static Confidential getConfidentialAnnotation(Class clazz, String getterMethodName) {
        try {
            Method m = clazz.getDeclaredMethod(getterMethodName);
            return m.getAnnotation(Confidential.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static JSONArray processCollection(Collection c) throws JSONException {
        JSONArray result = new JSONArray();
        Iterator i = c.iterator();
        while (i.hasNext()) {
            Object item = getJsonObject(i.next());
            result.put(item);
        }

        return result;
    }

    public static JSONObject processMap(Map map) throws JSONException {
        JSONObject result = new JSONObject();

        for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
            result.put(entry.getKey().toString(), getJsonObject(entry.getValue()));
        }

        return result;
    }

    public static String getString(JSONObject jsonObject, String key, String dflt) {
        try {
            if (jsonObject.isNull(key)) {
                return null;
            }
            return jsonObject.getString(key);
        } catch (JSONException e) {
            return dflt;
        }
    }

    public static int getInt(JSONObject jsonObject, String key, int dflt) {
        try {
            return jsonObject.getInt(key);
        } catch (JSONException e) {
            return dflt;
        }
    }

    public static void put(JSONObject jsonObject, String key, Object value) {
        try {
            synchronized (jsonObject) {
                jsonObject.put(key, value != null ? value : JSONObject.NULL);
            }
        } catch (JSONException e) {
            // ignore. The exception is thrown only if the value is non-finite number
            // or if the key is null.
        }
    }

    public static void put(JSONArray jsonArray, JSONObject item) {
        synchronized (jsonArray) {
            jsonArray.put(item);
        }
    }
}
