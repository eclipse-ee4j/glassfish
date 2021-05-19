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

package org.glassfish.common.locator;

import junit.framework.Assert;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class BasicLocatorTest {
    private final static String TEST_NAME = "CommonSmokeTest";
    /*
    private final static ServiceLocator locator = ServiceLocatorFactory.getInstance().create(TEST_NAME);
    */

    /**
     * Called by junit before any test is run
     *
    @Before
    public void before() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        config.addActiveDescriptor(ProxiableSingletonContext.class);
        config.addActiveDescriptor(SimpleService.class);
        config.addActiveDescriptor(SimpleInjectee.class);

        config.commit();
    }
    */

    /**
     * Tests that a proxied method can be called when injected into a non-proxied class
     */
    @Test
    public void testProxiedMethodCanBeCalled() {
        /*
        SimpleInjectee si = locator.getService(SimpleInjectee.class);
        Assert.assertNotNull(si);

        si.callIt();  // This will throw if there are any problems
        */
    }
}
