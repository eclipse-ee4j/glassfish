/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;

public class DefaultProxy implements InvocationHandler {
    private final Map<String, Method> methods;
    private final ConfigBeanProxy parent;

    DefaultProxy(ConfigBeanProxy parent, final Class<? extends ConfigBeanProxy> sslClass) {
        this.parent = parent;
        methods = new HashMap<String, Method>();
        final Method[] list = sslClass.getDeclaredMethods();
        for (Method method : list) {
            if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
                methods.put(method.getName(), method);
            }
        }
    }

    public ConfigBeanProxy getParent() {
        return parent;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object value = null;
        if (methods.get(method.getName()) != null) {
            final Attribute annotation = method.getAnnotation(Attribute.class);
            if (annotation != null) {
                String defValue = annotation.defaultValue().trim();
                if (defValue.length() > 0) {
                    value = defValue;
                }
            }
        } else if ("getParent".equals(method.getName())) {
            value = getParent();
        } else {
            throw new GrizzlyConfigException(String.format("Method not implemented for a %s: %s", getClass().getName(),
                    method.getName()));
        }
        return value;
    }

    public static ConfigBeanProxy createDummyProxy(ConfigBeanProxy parent,
            final Class<? extends ConfigBeanProxy> type) {
        return (ConfigBeanProxy) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type},
                new DefaultProxy(parent, type));
    }
}
