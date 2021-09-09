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

import com.sun.enterprise.config.serverbeans.JavaConfig;

import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * User: Jerome Dochez
 * Date: Apr 7, 2008
 * Time: 11:13:22 AM
 */
public class JavaConfigSubTypesTest extends ConfigPersistence {

    @Override
    @Test
    public void doTest() throws TransactionFailure {
        JavaConfig javaConfig = locator.getService(JavaConfig.class);
        SingleConfigCode<JavaConfig> configCode = (SingleConfigCode<JavaConfig>) jvm -> {
            List<String> jvmOptions = jvm.getJvmOptions();
            jvmOptions.add("-XFooBar=true");
            return jvmOptions;
        };
        ConfigSupport.apply(configCode, javaConfig);
    }


    @Override
    public void assertResult(String xml) {
        assertThat(xml, stringContainsInOrder("-XFooBar"));
    }


    @Test
    public void testSubTypesOfDomain() throws Exception {
        JavaConfig config = locator.getService(JavaConfig.class);
        Class<?>[] subTypes = ConfigSupport.getSubElementsTypes((ConfigBean) Dom.unwrap(config));
        boolean found = false;
        for (Class<?> subType : subTypes) {
            Logger.getAnonymousLogger().fine("Found class " + subType);
            if (subType.getName().equals(List.class.getName())) {
                found = true;
            }
        }
        assertTrue(found);
    }
}
