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

package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.Applications;

import jakarta.inject.Inject;

import java.util.Collection;
import java.util.logging.Logger;

import org.glassfish.api.admin.config.ApplicationName;
import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Modules related tests
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class ModulesTest {

    @Inject
    private ServiceLocator locator;
    @Inject
    private Logger logger;

    private Collection<? extends ApplicationName> modules;

    @BeforeEach
    public void setup() {
        Applications apps = locator.getService(Applications.class);
        assertNotNull(apps);
        modules = apps.getModules();
        assertNotNull(modules);
    }

    @Test
    public void modulesTest() {
        for (ApplicationName module : modules) {
            logger.fine("Found module " + module.getName());
            assertNotNull(module.getName());
        }
    }
}
