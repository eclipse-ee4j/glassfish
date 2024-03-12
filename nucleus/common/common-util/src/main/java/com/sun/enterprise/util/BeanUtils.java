/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** Static methods which are handy to manipulate java beans
 *
 * @author martinmares
 */
public class BeanUtils {

    /** Loads all getters to the Map.
     */
    public static Map<String, Object> beanToMap(Object bean) throws InvocationTargetException {
        if (bean == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<String, Object>();
        Collection<Method> getters = getGetters(bean);
        for (Method method : getters) {
            try {
                result.put(toAttributeName(method), method.invoke(bean));
            } catch (IllegalAccessException ex) {
                //Checked - can not happen
            } catch (IllegalArgumentException ex) {
                //Checked - can not happen
            }
        }
        return result;
    }

    /** Sets values from map to provided bean.
     *
     * @param bean Set to its setters
     * @param data key is attribute name and value is value to set
     * @param ignoreNotExistingSetter if {@code false} and data contains key which
     *        does not point to any setter then IllegalArgumentException will be thrown
     */
    public static void mapToBean(Object bean, Map<String, Object> data, boolean ignoreNotExistingSetter)
            throws InvocationTargetException, IllegalArgumentException {
        if (data == null || bean == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            try {
                Method mtd = getSetter(bean, entry.getKey());
                if (mtd == null) {
                    if (!ignoreNotExistingSetter) {
                        throw new IllegalArgumentException();
                    }
                    continue;
                }
                mtd.invoke(bean, entry.getValue());
            } catch (IllegalAccessException ex) {
            }
        }
    }

    public static Collection<Method> getGetters(Object bean) {
        if (bean == null) {
            return null;
        }
        Collection<Method> result = new ArrayList<Method>();
        for (Method method : bean.getClass().getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            if (method.getParameterTypes().length == 0) {
                if ((method.getName().length() > 3 && method.getName().startsWith("get"))
                        || (method.getName().length() > 2 && method.getName().startsWith("is"))) {
                    result.add(method);
                }
            }
        }
        return result;
    }

    public static Collection<Method> getSetters(Object bean) {
        if (bean == null) {
            return null;
        }
        Collection<Method> result = new ArrayList<Method>();
        for (Method method : bean.getClass().getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            if (method.getParameterTypes().length == 1) {
                if (method.getName().length() > 3 && method.getName().startsWith("set")) {
                    result.add(method);
                }
            }
        }
        return result;
    }

    /** Extract attribute name from getter or setter.
     *
     * @return IllegalArgumentException if method is not getter or setter.
     */
    public static String toAttributeName(Method m) throws IllegalArgumentException {
        String name = m.getName();
        String result;
        if (name.startsWith("get") || name.startsWith("set")) {
            result = name.substring(3);
        } else if (name.startsWith("is")) {
            result = name.substring(2);
        } else {
            throw new IllegalArgumentException();
        }
        if (result.length() == 0) {
            throw new IllegalArgumentException();
        }
        result = Character.toLowerCase(result.charAt(0)) + result.substring(1);
        return result;
    }

    public static Method getSetter(Object bean, String attributeName) {
        String methodName = "set" + Character.toUpperCase(attributeName.charAt(0)) + attributeName.substring(1);
        for (Method m : bean.getClass().getMethods()) {
            if (m.getParameterTypes().length == 1 && m.getName().equals(methodName)) {
                return m;
            }
        }
        return null;
    }

}
