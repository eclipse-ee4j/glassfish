/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config.provider.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

/**
 * Utilities for transaction config reflection.
 *
 * @author Jeff Trent
 */
class ReflectionHelper {

    static <T extends Annotation> T annotation(Object obj, Class<T> annotation) {
        if (null == obj) {
            return null;
        }

        Class<?> clazz = obj.getClass();
        if (Proxy.isProxyClass(clazz) || clazz.isAnonymousClass()) {
            for (Class<?> iface : clazz.getInterfaces()) {
                T t = iface.getAnnotation(annotation);
                if (null != t) {
                    return t;
                }
            }

            if (clazz.isAnonymousClass()) {
                clazz = clazz.getSuperclass();
            }
        }

        return clazz.getAnnotation(annotation);
    }


    @SuppressWarnings("unchecked")
    static void annotatedWith(Set<Class<?>> contracts, Object obj, Class annotation) {
        if (null != obj) {
            Class<?> clazz = obj.getClass();

            while (Object.class != clazz) {
                if (!clazz.isAnonymousClass()) {
                    Object t = clazz.getAnnotation(annotation);
                    if (null != t) {
                        contracts.add(clazz);
                    } else {
                        annotatedWith(contracts, annotation, clazz);
                    }

                    for (Class<?> iface : clazz.getInterfaces()) {
                        t = iface.getAnnotation(annotation);
                        if (null != t) {
                            contracts.add(iface);
                        } else {
                            annotatedWith(contracts, annotation, iface);
                        }
                    }
                }

                clazz = clazz.getSuperclass();
            }
        }
    }


    @SuppressWarnings("unchecked")
    private static void annotatedWith(Set<Class<?>> contracts, Class annotation, Class<?> clazz) {
        if (!Proxy.isProxyClass(clazz)) {
            Annotation[] annArr = clazz.getAnnotations();
            for (Annotation ann : annArr) {
                Class<?> x = ann.annotationType();
                Object t = x.getAnnotation(annotation);
                if (null != t) {
                    contracts.add(clazz);
                    return;
                }
            }
        }
    }


    static String nameOf(Object configBean) {
        String name = null;

        if (null != configBean) {
            try {
                Method m = configBean.getClass().getMethod("getName", (Class<?>[]) null);
                name = String.class.cast(m.invoke(configBean, (Object[]) null));
            } catch (Exception e) {
                // swallow
            }
        }

        return name;
    }

}
