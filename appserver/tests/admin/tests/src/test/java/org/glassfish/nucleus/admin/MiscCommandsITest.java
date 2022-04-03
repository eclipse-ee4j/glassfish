/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.nucleus.admin;

import org.glassfish.nucleus.test.tool.DomainLifecycleExtension;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.glassfish.nucleus.test.tool.NucleusTestUtils.nadmin;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Tom Mueller
 */
@ExtendWith(DomainLifecycleExtension.class)
@TestMethodOrder(OrderAnnotation.class)
public class MiscCommandsITest {

    @Test
    @Order(1)
    public void uptime() {
        assertTrue(nadmin("uptime"));
    }

    @Test
    @Order(1)
    public void version1() {
        assertTrue(nadmin("version"));
    }

    @Test
    @Order(100)
    public void version2() {
        assertTrue(nadmin("stop-domain"));
        assertTrue(nadmin("version", "--local"));
    }
}
