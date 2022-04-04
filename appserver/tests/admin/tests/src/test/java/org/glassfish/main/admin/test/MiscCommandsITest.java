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

package org.glassfish.main.admin.test;

import org.glassfish.main.admin.test.tool.asadmin.Asadmin;
import org.glassfish.main.admin.test.tool.asadmin.GlassFishTestEnvironment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import static org.glassfish.main.admin.test.tool.AsadminResultMatcher.asadminOK;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Tom Mueller
 */
@TestMethodOrder(OrderAnnotation.class)
public class MiscCommandsITest {

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @AfterAll
    public static void startDomainAgain() {
        assertThat(ASADMIN.exec("start-domain"), asadminOK());
    }

    @Test
    @Order(1)
    public void uptime() {
        assertThat(ASADMIN.exec("uptime"), asadminOK());
    }

    @Test
    @Order(1)
    public void version1() {
        assertThat(ASADMIN.exec("version"), asadminOK());
    }

    @Test
    @Order(100)
    public void version2() {
        assertThat(ASADMIN.exec("stop-domain"), asadminOK());
        assertThat(ASADMIN.exec("version", "--local"), asadminOK());
    }
}
