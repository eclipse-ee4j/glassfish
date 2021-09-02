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

package org.glassfish.admin.amx.impl.util;

import org.glassfish.admin.amx.test.AmxTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(AmxTestExtension.class)
public final class SingletonEnforcerTest {

    @Test
    public void testForNull() {
        assertNull(SingletonEnforcer.get(Dummy.class));
    }


    @Test
    public void testVariety() {
        SingletonEnforcer.register(String.class, "hello");
        assertNotNull(SingletonEnforcer.get(String.class));

        SingletonEnforcer.register(Boolean.class, Boolean.TRUE);
        assertNotNull(SingletonEnforcer.get(Boolean.class));

        SingletonEnforcer.register(Integer.class, Integer.valueOf(0));
        assertNotNull(SingletonEnforcer.get(Integer.class));
    }


    @Test
    public void testForDuplicates() {
        assertDoesNotThrow(() -> SingletonEnforcer.register(Dummy2.class, this));
        assertThrows(IllegalArgumentException.class, () -> SingletonEnforcer.register(Dummy2.class, this));
    }

    private static final class Dummy {
    }

    private static final class Dummy2 {
    }
}
