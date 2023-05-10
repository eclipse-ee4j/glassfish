/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config.types.test;

import java.util.List;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.jupiter.api.Test;
import org.jvnet.hk2.config.ConfigInjector;
import org.jvnet.hk2.config.types.HK2DomConfigTypesUtilities;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for specialized config types
 *
 * @author jwells
 */
public class ConfigTypesTest {

    /**
     * Tests that the enable verb works
     */
    @Test
    public void testConfigTypesUtilities() {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create(null);

        assertNull(locator.getService(ConfigInjector.class));

        HK2DomConfigTypesUtilities.enableHK2DomConfigurationConfigTypes(locator, null);

        // Twice to test idempotence
        HK2DomConfigTypesUtilities.enableHK2DomConfigurationConfigTypes(locator, null);

        List<ActiveDescriptor<?>> injectors = locator.getDescriptors(BuilderHelper.createContractFilter(ConfigInjector.class.getName()));
        assertEquals(1, injectors.size());

        ActiveDescriptor<?> propInjectDesc = injectors.get(0);

        assertAll(
            () -> assertEquals("org.jvnet.hk2.config.types.PropertyInjector", propInjectDesc.getImplementation()),
            () -> assertDoesNotThrow(() -> Class.forName("org.jvnet.hk2.config.types.PropertyInjector"))
        );
    }
}
