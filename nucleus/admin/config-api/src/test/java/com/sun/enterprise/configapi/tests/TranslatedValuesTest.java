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

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple test for translated values access
 *
 * @author Jerome Dochez
 */
public class TranslatedValuesTest extends ConfigApiTest {

    @Override
    public String getFileName() {
        return "DomainTest";
    }

    @BeforeEach
    public void setup() {
        System.setProperty("com.sun.aas.instanceRoot", "cafebabe");
        System.setProperty("com.sun.aas.javaRoot", System.getProperty("user.home"));
    }


    @Test
    public void testAppRoot() {
        Domain domain = getHabitat().getService(Domain.class);
        String appRoot = domain.getApplicationRoot();
        assertTrue(appRoot.startsWith("cafebabe"));
    }

    @Test
    public void testJavaRoot() {
        if (System.getProperty("user.home").contains(File.separator)) {
            JavaConfig config = getHabitat().getService(JavaConfig.class);
            String javaRoot = config.getJavaHome();
            assertThat(javaRoot, stringContainsInOrder(File.separator));
        }
    }

}
