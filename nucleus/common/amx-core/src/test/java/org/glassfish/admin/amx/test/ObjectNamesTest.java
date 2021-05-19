/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.test;

import org.glassfish.admin.amx.impl.util.ObjectNameBuilder;

import org.junit.Test;
import org.junit.Before;

import java.lang.management.ManagementFactory;


public final class ObjectNamesTest extends TestBase
{

    public ObjectNamesTest() {
    }

    private ObjectNameBuilder get() {
        return new ObjectNameBuilder( ManagementFactory.getPlatformMBeanServer(), amxDomain());
    }

    @Before
    public void setUp() {
        initBootUtil();
    }

    @Test
    public void testCreate() {
        final ObjectNameBuilder objectNames = get();
    }

    private String amxDomain()
    {
        return "test";
    }

    @Test
    public void testMisc() {
        get().getJMXDomain();
    }
}






