/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.inject.Inject;

import org.glassfish.apf.AnnotationHandler;
import org.glassfish.apf.impl.AnnotationUtils;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hk2.testing.junit.HK2Runner;

/**
 * Tests for the AnnotationUtils
 *
 * @author jwells
 *
 */
public class AnnotationUtilsTest extends HK2Runner {
    @Inject
    private ServiceLocator locator;

    @Test
    public void testGetAnnotationHandlerForDescriptor() {
        {
            ActiveDescriptor<AnnotationHandler> forA =
                AnnotationUtils.getAnnotationHandlerForDescriptor(locator, ClassA.class);
            Assert.assertNotNull(forA);
            Assert.assertFalse(forA.isReified());  // not classloaded by HK2
            Assert.assertEquals(HandlerForClassA.class.getName(), forA.getImplementation());
        }

        {
            ActiveDescriptor<AnnotationHandler> forB =
                AnnotationUtils.getAnnotationHandlerForDescriptor(locator, ClassB.class);
            Assert.assertNotNull(forB);
            Assert.assertFalse(forB.isReified());  // not classloaded by HK2
            Assert.assertEquals(HandlerForClassB.class.getName(), forB.getImplementation());
        }

        {
            ActiveDescriptor<AnnotationHandler> forC =
                AnnotationUtils.getAnnotationHandlerForDescriptor(locator, ClassC.class);
            Assert.assertNull(forC);
        }

    }
}
