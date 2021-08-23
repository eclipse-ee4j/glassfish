/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.tiger_types;

import java.net.Proxy.Type;
import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Kohsuke Kawaguchi
 */
public class ListerTest {

    // is used in the test method
    public EnumSet<Type> set;

    @Test
    public void testEnumSet() throws Exception {
        final Lister<?> lister = Lister.create(getClass().getDeclaredField("set").getGenericType());
        lister.add(Type.HTTP);
        lister.add(Type.SOCKS);
        final Set<?> col = (Set<?>) lister.toCollection();
        assertAll(
            () -> assertThat(col, instanceOf(EnumSet.class)),
            () -> assertThat(col, containsInAnyOrder(Type.HTTP, Type.SOCKS))
        );
    }
}
