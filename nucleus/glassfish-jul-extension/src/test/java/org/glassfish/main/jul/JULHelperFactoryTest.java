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

import java.util.logging.Logger;

import org.glassfish.main.jul.JULHelperFactory.JULHelper;
import org.glassfish.main.jul.handler.GlassFishLogHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * @author David Matejcek
 */
public class JULHelperFactoryTest {

    private final JULHelper helper = JULHelperFactory.getHelper();

    @Test
    public void testGetHelper() {
        assertNotNull(helper, "helper");
        Logger julLogger = helper.getJULLogger(getClass());
        java.lang.System.Logger systemLogger = helper.getSystemLogger(getClass());
        assertAll(
            () -> assertInstanceOf(GlassFishLogger.class, julLogger, "julLogger"),
            () -> assertNotNull(systemLogger, "systemLogger")
        );
        assertAll(
            () -> assertEquals(GlassFishLoggerFinder.class.getName() + "$GlassFishSystemLogger", systemLogger.getClass().getName()),
            () -> assertEquals(getClass().getName(), systemLogger.getName()),
            () -> assertEquals(julLogger.getName(), systemLogger.getName())
        );
    }


    @Test
    public void testFindHandler_ifNotSet() {
        GlassFishLogHandler handler = helper.findGlassFishLogHandler();
        assertNull(handler, "handler");
    }
}
