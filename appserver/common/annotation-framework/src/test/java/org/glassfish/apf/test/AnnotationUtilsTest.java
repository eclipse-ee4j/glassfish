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

package org.glassfish.apf.test;

import org.glassfish.apf.AnnotationHandler;
import org.glassfish.apf.test.example.ClassA;
import org.glassfish.apf.test.example.ClassB;
import org.glassfish.apf.test.example.ClassC;
import org.glassfish.apf.test.example.HandlerForClassA;
import org.glassfish.apf.test.example.HandlerForClassB;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.HK2JUnit5Extension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.inject.Inject;

import static org.glassfish.apf.impl.AnnotationUtils.getAnnotationHandlerForDescriptor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for the AnnotationUtils
 *
 * @author jwells
 */
@ExtendWith(HK2JUnit5Extension.class)
public class AnnotationUtilsTest {

    @Inject
    private ServiceLocator locator;

    @Test
    public void testGetAnnotationHandlerForDescriptor() {
        {
            ActiveDescriptor<AnnotationHandler> forA = getAnnotationHandlerForDescriptor(locator, ClassA.class);
            assertNotNull(forA);
            // not classloaded by HK2
            assertFalse(forA.isReified());
            assertEquals(HandlerForClassA.class.getName(), forA.getImplementation());
        }

        {
            ActiveDescriptor<AnnotationHandler> forB = getAnnotationHandlerForDescriptor(locator, ClassB.class);
            assertNotNull(forB);
            // not classloaded by HK2
            assertFalse(forB.isReified());
            assertEquals(HandlerForClassB.class.getName(), forB.getImplementation());
        }

        {
            ActiveDescriptor<AnnotationHandler> forC = getAnnotationHandlerForDescriptor(locator, ClassC.class);
            assertNull(forC);
        }

    }
}
