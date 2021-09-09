/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.io.File;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple test for translated values access
 *
 * @author Jerome Dochez
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class TranslatedValuesTest {

    @Inject
    private ServiceLocator locator;

    @BeforeAll
    public static void initSysProps() {
        System.setProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY, "cafebabe");
        System.setProperty(SystemPropertyConstants.JAVA_ROOT_PROPERTY, System.getProperty("user.home"));
    }

    @AfterAll
    public static void reset() {
        System.clearProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);
        System.clearProperty(SystemPropertyConstants.JAVA_ROOT_PROPERTY);
    }


    @Test
    public void testAppRoot() {
        Domain domain = locator.getService(Domain.class);
        String appRoot = domain.getApplicationRoot();
        assertTrue(appRoot.startsWith("cafebabe"));
    }

    @Test
    public void testJavaRoot() {
        JavaConfig config = locator.getService(JavaConfig.class);
        String javaRoot = config.getJavaHome();
        assertThat(javaRoot, stringContainsInOrder(File.separator));
    }

}
