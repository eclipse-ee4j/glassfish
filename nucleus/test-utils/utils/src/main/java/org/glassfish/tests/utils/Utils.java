/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.tests.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.security.common.PrincipalImpl;
import static org.glassfish.hk2.utilities.BuilderHelper.createConstantDescriptor;

/**
 * Utilities to create a configured Habitat and cache them
 *
 * @author Jerome Dochez
 */
public class Utils {
    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    private static final InvocationHandler MOCK_HANDLER = (proxy, method, args) -> {
        throw new UnsupportedOperationException("Feature-free dummy implementation for injection only");
    };

    final static String habitatName = "default";
    final static String inhabitantPath = "META-INF/inhabitants";

    private static Map<String, ServiceLocator> habitats = new HashMap<>();
    public static final Utils instance = new Utils();

    public static void setStaticField(Class<?> clazz, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set static field " + fieldName + " of " + clazz + " to " + value, e);
        }
    }

    public static <T> T getStaticField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(null);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get static field " + fieldName + " of " + clazz, e);
        }
    }

    public static void setField(Object instance, String fieldName, Object value) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set field " + fieldName + " of " + instance + " to " + value, e);
        }
    }

    public static <T> T getField(Object instance, String fieldName) {
        return getField(instance, fieldName, instance.getClass());
    }

    public static <T> T getField(Object instance, String fieldName, Class<?> parentOfInstance) {
        try {
            Field field = parentOfInstance.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(instance);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get a value of field " + fieldName + " of " + instance , e);
        }
    }

    public static Subject createInternalAsadminSubject() {
        final Subject subject = new Subject();
        subject.getPrincipals().add(new PrincipalImpl("asadmin"));
        subject.getPrincipals().add(new PrincipalImpl("_InternalSystemAdministrator_"));
        return subject;
    }


    public static <T> AbstractActiveDescriptor<T> createMockDescriptor(Class<T> clazz) {
        final AbstractActiveDescriptor<T> descriptor = createConstantDescriptor(createMockProxy(clazz), null, clazz);
        // high ranking to override detected HK2 service
        descriptor.setRanking(Integer.MAX_VALUE);
        descriptor.setReified(true);
        return descriptor;
    }


    @SuppressWarnings("unchecked")
    public static <T> T createMockProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz}, MOCK_HANDLER);
    }
}
