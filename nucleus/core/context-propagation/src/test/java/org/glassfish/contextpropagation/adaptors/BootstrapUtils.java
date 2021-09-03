/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.contextpropagation.adaptors;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;

import org.glassfish.contextpropagation.ContextMap;
import org.glassfish.contextpropagation.ContextViewFactory;
import org.glassfish.contextpropagation.InsufficientCredentialException;
import org.glassfish.contextpropagation.PropagationMode;
import org.glassfish.contextpropagation.View;
import org.glassfish.contextpropagation.ViewCapable;
import org.glassfish.contextpropagation.bootstrap.ContextBootstrap;
import org.glassfish.contextpropagation.internal.Utils.ContextMapAdditionalAccessors;
import org.glassfish.contextpropagation.spi.ContextMapHelper;
import org.glassfish.contextpropagation.wireadapters.WireAdapter;

import mockit.internal.reflection.FieldReflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class BootstrapUtils {

    private static class MyViewCapable implements ViewCapable {

        View view;

        public MyViewCapable(View aView) {
            view = aView;
            view.put(".value", "a value", PropagationMode.defaultSet());
        }


        public String getValue() {
            return view.get(".value");
        }
    }

    public static void populateMap() throws InsufficientCredentialException {
        ContextMap wcMap = ContextMapHelper.getScopeAwareContextMap();
        wcMap.put("true", true, PropagationMode.defaultSet());
        wcMap.put("string", "string", PropagationMode.defaultSet());
        wcMap.put("one", 1L, PropagationMode.defaultSet());
        ((ContextMapAdditionalAccessors) wcMap).putAscii("ascii", "ascii", PropagationMode.defaultSet());
        ((ContextMapAdditionalAccessors) wcMap).putSerializable("serializable", new HashSet<>(Arrays.asList("foo")),
            PropagationMode.defaultSet());
        Byte byteValue = (byte) 'b';
        wcMap.put("byte", byteValue, PropagationMode.defaultSet());

        // View Capable Stuff
        // 1 - Create the factory (assumes that you have already created a ViewCapable class
        ContextViewFactory viewCapableFactory = new ContextViewFactory() {

            @Override
            public ViewCapable createInstance(View view) {
                return new MyViewCapable(view);
            }


            @Override
            public EnumSet<PropagationMode> getPropagationModes() {
                return PropagationMode.defaultSet();
            }
        };
        // 2 - Register the factory
        ContextMapHelper.registerContextFactoryForPrefixNamed("view capable", viewCapableFactory);
        // 3 - Create the ViewCapable instance
        wcMap.createViewCapable("view capable");
        assertEquals("a value", ((MyViewCapable) wcMap.get("view capable")).getValue());

        wcMap.get("ascii");
    }


    public static void bootstrap(WireAdapter wireAdapter) {
        reset();
        /*
         * ThreadLocalAccessor tla = Deencapsulation.getField(ContextBootstrap.class,
         * "threadLocalAccessor");
         * tla.set(null);
         */
        ContextBootstrap.configure(new MockLoggerAdapter(), wireAdapter, new MockThreadLocalAccessor(),
            new MockContextAccessController(), "guid");
    }


    public static void reset() {
        try {
            setStaticField(ContextBootstrap.class, "isConfigured", false);
            ContextMapHelper.getScopeAwareContextMap().get("true");
            fail("Should get IllegalStateException");
        } catch (IllegalStateException e) {
            // ignored
        } catch (Exception e) {
            fail(e.toString());
        }
    }


    // Reason for following methods: API of JMockit changes with minor versions
    public static void setStaticField(Class<?> clazz, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set static field " + fieldName + " of " + clazz + " to " + value, e);
        }
    }

    public static void getStaticField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.get(null);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get static field " + fieldName + " of " + clazz, e);
        }
    }

    public static void setField(Object instance, String fieldName, Object value) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            FieldReflection.setFieldValue(field, instance, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set field " + fieldName + " of " + instance + " to " + value, e);
        }
    }



    public static <T> T getField(Object instance, String fieldName) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            return FieldReflection.getFieldValue(field, instance);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get a value of field " + fieldName + " of " + instance , e);
        }
    }
}
