/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul;

import java.util.logging.LogManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.glassfish.main.jul.cfg.GlassFishLoggingConstants.CLASS_LOG_MANAGER_GLASSFISH;
import static org.glassfish.main.jul.cfg.GlassFishLoggingConstants.CLASS_LOG_MANAGER_JUL;
import static org.glassfish.main.jul.cfg.GlassFishLoggingConstants.JVM_OPT_LOGGING_MANAGER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


/**
 * This test is extremely trivial. But it is not so simple ...
 * <p>
 * The problem is that the LogManager is set forever when anything in JVM tries to use the logging
 * system for the first time. And that is ServiceLoaderTestEngineRegistry of JUnit5 at this moment.
 * <p>
 * So we have three possible test methods, one of them should be executed.
 *
 * @author David Matejcek
 */
public class GlassFishLogManagerInitializerTest {

    @Test
    @DisabledIfSystemProperty(named = JVM_OPT_LOGGING_MANAGER, matches = ".+")
    void implicitJUL() {
        assertFalse(GlassFishLogManagerInitializer.tryToSetAsDefault());
        assertEquals(CLASS_LOG_MANAGER_JUL, LogManager.getLogManager().getClass().getCanonicalName());
    }


    @Test
    @EnabledIfSystemProperty(named = JVM_OPT_LOGGING_MANAGER, matches = CLASS_LOG_MANAGER_JUL)
    void explicitJUL() {
        assertFalse(GlassFishLogManagerInitializer.tryToSetAsDefault());
        assertEquals(CLASS_LOG_MANAGER_JUL, LogManager.getLogManager().getClass().getCanonicalName());
    }


    @Test
    @EnabledIfSystemProperty(named = JVM_OPT_LOGGING_MANAGER, matches = CLASS_LOG_MANAGER_GLASSFISH)
    void gjule() {
        assertFalse(GlassFishLogManagerInitializer.tryToSetAsDefault());
        assertEquals(CLASS_LOG_MANAGER_GLASSFISH, LogManager.getLogManager().getClass().getCanonicalName());
    }
}
