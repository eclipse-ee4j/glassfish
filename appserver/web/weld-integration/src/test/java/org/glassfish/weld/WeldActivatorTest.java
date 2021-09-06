/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld;

import java.lang.reflect.Field;

import org.jboss.weld.bootstrap.api.SingletonProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:j.j.snyder@oracle.com">JJ Snyder</a>
 */
public class WeldActivatorTest {
    @Test
    public void testAll() throws Exception {
        SingletonProvider aclSingletonProvider = new ACLSingletonProvider();

        Field instanceField = SingletonProvider.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        assertNull(instanceField.get(aclSingletonProvider));

        WeldActivator weldActivator = new WeldActivator();
        weldActivator.start(null);

        assertTrue(instanceField.get(aclSingletonProvider) instanceof ACLSingletonProvider);

        weldActivator.stop(null);
        assertNull(instanceField.get(aclSingletonProvider));

    }
}
