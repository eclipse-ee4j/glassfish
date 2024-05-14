/*
 * Copyright (c) 2022, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul.handler;

import java.util.logging.Level;

import org.glassfish.main.jul.GlassFishLogManager;
import org.glassfish.main.jul.cfg.GlassFishLogManagerConfiguration;
import org.glassfish.main.jul.cfg.LoggingProperties;
import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.glassfish.main.jul.cfg.GlassFishLogManagerProperty.KEY_ROOT_HANDLERS;

/**
 * @author David Matejcek
 */
public class SyslogHandlerTest {

    private static GlassFishLogManager manager;
    private static GlassFishLogManagerConfiguration configuration;

    @BeforeAll
    public static void backupConfig() {
        manager = GlassFishLogManager.getLogManager();
        configuration = manager.getConfiguration();
    }

    @AfterAll
    public static void resetConfig() {
        manager.reconfigure(configuration);
    }

    @Test
    public void test() throws Exception {
        LoggingProperties properties = new LoggingProperties();
        properties.setProperty(SyslogHandlerProperty.LEVEL.getPropertyFullName(), "ALL");
        properties.setProperty(SyslogHandlerProperty.HOST.getPropertyFullName(), "localhost");
        properties.setProperty(SyslogHandlerProperty.PORT.getPropertyFullName(), "514");
        properties.setProperty(KEY_ROOT_HANDLERS.getPropertyName(), SyslogHandler.class.getName());
        manager.reconfigure(new GlassFishLogManagerConfiguration(properties));

        SyslogHandler handler = new SyslogHandler();
        handler.publish(new GlassFishLogRecord(Level.SEVERE, "This is a test.", false));
        Thread.sleep(100L);
    }

}
