/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.universal.xml;

import com.sun.enterprise.universal.xml.SysPropsHandler.Type;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Byron Nevins
 */
public class SysPropsHandlerTest {

    /**
     * Test of getCombinedSysProps method, of class SysPropsHandler.
     */
    @Test
    public void exercise() {
        SysPropsHandler instance = new SysPropsHandler();
        instance.add(Type.SERVER, "test", "from-server");
        instance.add(Type.CLUSTER, "test", "from-cluster");
        instance.add(Type.CONFIG, "test", "from-config");
        instance.add(Type.DOMAIN, "test", "from-domain");
        final Map<String, String> map = instance.getCombinedSysProps();
        assertAll(
            () -> assertThat(map.entrySet(), hasSize(1)),
            () -> assertEquals("from-server", map.get("test"))
        );

        instance.add(Type.CLUSTER, "test2", "from-cluster");
        instance.add(Type.CONFIG, "test2", "from-config");
        instance.add(Type.DOMAIN, "test2", "from-domain");

        instance.add(Type.CONFIG, "test3", "from-config");
        instance.add(Type.DOMAIN, "test3", "from-domain");

        instance.add(Type.DOMAIN, "test4", "from-domain");

        final Map<String, String> map2 = instance.getCombinedSysProps();
        assertAll(
            () -> assertThat(map2.entrySet(), hasSize(4)),
            () -> assertThat(map2, hasEntry("test", "from-server")),
            () -> assertThat(map2, hasEntry("test2", "from-cluster")),
            () -> assertThat(map2, hasEntry("test3", "from-config")),
            () -> assertThat(map2, hasEntry("test4", "from-domain"))
        );
    }
}
