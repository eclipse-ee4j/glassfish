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

package org.glassfish.contextpropagation.internal;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.contextpropagation.ContextViewFactory;
import org.glassfish.contextpropagation.PropagationMode;
import org.glassfish.contextpropagation.View;
import org.glassfish.contextpropagation.ViewCapable;
import org.glassfish.contextpropagation.adaptors.BootstrapUtils;
import org.glassfish.contextpropagation.adaptors.RecordingLoggerAdapter;
import org.glassfish.contextpropagation.bootstrap.ContextBootstrap;
import org.glassfish.contextpropagation.bootstrap.LoggerAdapter.Level;
import org.glassfish.contextpropagation.bootstrap.LoggerAdapter.MessageID;
import org.glassfish.contextpropagation.wireadapters.glassfish.DefaultWireAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.glassfish.tests.utils.Utils.setStaticField;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UtilsTest {

    @BeforeAll
    public static void setupClass() {
        BootstrapUtils.bootstrap(new DefaultWireAdapter());
    }


    @Test
    public void testGetScopeAwarePropagator() {
        assertNotNull(Utils.getScopeAwarePropagator());
    }


    @Test
    public void testGetScopeAwareContextMap() {
        assertNotNull(Utils.getScopeAwarePropagator());
    }


    @Test
    public void testRegisterContextFactoryForPrefixNamed() {
        Utils.registerContextFactoryForPrefixNamed("prefix", new ContextViewFactory() {

            @Override
            public EnumSet<PropagationMode> getPropagationModes() {
                return PropagationMode.defaultSet();
            }


            @Override
            public ViewCapable createInstance(View view) {
                return new ViewCapable() {
                };
            }
        });
        assertNotNull(Utils.getFactory("prefix"));
    }

    private static final Object CONTEXT_VIEW_FACTORY = new ContextViewFactory() {

        @Override
        public ViewCapable createInstance(View view) {
            return null;
        }


        @Override
        public EnumSet<PropagationMode> getPropagationModes() {
            return null;
        }
    };

    private static MessageID msgID = MessageID.WRITING_KEY; // We need a dummy MessageID

    @Test
    public void testValidateFactoryRegistrationArgsNullKey() {
        assertThrows(IllegalArgumentException.class,
            () -> Utils.validateFactoryRegistrationArgs(null, msgID, "context class name", CONTEXT_VIEW_FACTORY, null));
    }


    @Test
    public void testValidateFactoryRegistrationArgsNullContextClassName() {
        assertThrows(IllegalArgumentException.class,
            () -> Utils.validateFactoryRegistrationArgs("key", msgID, null, CONTEXT_VIEW_FACTORY, null));
    }


    @Test
    public void testValidateFactoryRegistrationArgsNullFactory() {
        assertThrows(IllegalArgumentException.class,
            () -> Utils.validateFactoryRegistrationArgs("key", msgID, "context class name", null, null));
    }


    @Test
    public void testValidateFactoryRegistration() {
        Map<String, ?> map = Collections.emptyMap();
        Utils.validateFactoryRegistrationArgs("key", msgID, "context class name", CONTEXT_VIEW_FACTORY, map);
    }


    @Test
    public void testValidateFactoryRegistrationNullKey() {
        Map<String, ?> map = Collections.emptyMap();
        assertThrows(IllegalArgumentException.class,
            () -> Utils.validateFactoryRegistrationArgs(null, msgID, "context class name", CONTEXT_VIEW_FACTORY, map));
    }


    @Test
    public void testValidateFactoryRegistrationNullClassName() {
        Map<String, ?> map = Collections.emptyMap();
        assertThrows(IllegalArgumentException.class,
            () -> Utils.validateFactoryRegistrationArgs("key", msgID, null, CONTEXT_VIEW_FACTORY, map));
    }


    @Test
    public void testValidateFactoryRegistrationNullFactory() {
        Map<String, ?> map = Collections.emptyMap();
        assertThrows(IllegalArgumentException.class,
            () -> Utils.validateFactoryRegistrationArgs("key", msgID, "context class name", null, map));
    }


    @Test
    public void testValidateFactoryRegistrationNullMessageID() {
        Map<String, ?> map = Collections.emptyMap();
        assertThrows(IllegalArgumentException.class,
            () -> Utils.validateFactoryRegistrationArgs("key", null, "context class name", CONTEXT_VIEW_FACTORY, map));
    }


    @Test
    public void testValidateFactoryRegistrationAlreadyRegistered() {
        RecordingLoggerAdapter logger = new RecordingLoggerAdapter();
        setStaticField(ContextBootstrap.class, "loggerAdapter", logger);
        Map<String, Object> map = new HashMap<>();
        Utils.validateFactoryRegistrationArgs("key", msgID, "context class name", CONTEXT_VIEW_FACTORY, map);
        logger.verify(null, null, null, (Object[]) null);
        map.put("context class name", "something");
        Utils.validateFactoryRegistrationArgs("key", msgID, "context class name", CONTEXT_VIEW_FACTORY, map);
        logger.verify(Level.WARN, null, msgID, "context class name", "something", CONTEXT_VIEW_FACTORY);
    }

}
