/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors.service;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.sun.enterprise.connectors.service.ConnectorAdminServiceUtils.getReservePrefixedJNDINameForPool;
import static com.sun.enterprise.connectors.service.ConnectorAdminServiceUtils.isJMSRA;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Matejcek
 */
public class ConnectorAdminServiceUtilsTest {

    @Test
    @Disabled("dmatej: see other commented out blocks in this commit. The fix broke connectors JNDI, but the direction is good, just needs standalone PR.")
    public void testGetReservePrefixedJNDINameForPool() throws Exception {
        assertAll(
            () -> assertEquals(SimpleJndiName.of("__SYSTEM/pools/"),
                getReservePrefixedJNDINameForPool(new PoolInfo(SimpleJndiName.of("")))),
            () -> assertEquals(SimpleJndiName.of(
                "__SYSTEM/pools/__connection_factory_definition/connection-factory-definition-embedraApp/env/Servlet_ConnectionFactory"),
                getReservePrefixedJNDINameForPool(new PoolInfo(SimpleJndiName.of(
                    "__connection_factory_definition/connection-factory-definition-embedraApp/env/Servlet_ConnectionFactory")))),
            () -> assertEquals(SimpleJndiName.of("java:module/env/__SYSTEM/pools/Servlet_ConnectionFactory"),
                getReservePrefixedJNDINameForPool(
                    new PoolInfo(SimpleJndiName.of("java:module/env/Servlet_ConnectionFactory")))),
            () -> assertEquals(SimpleJndiName.of("java:global/env/__SYSTEM/pools/Servlet_ConnectionFactory"),
                getReservePrefixedJNDINameForPool(
                    new PoolInfo(SimpleJndiName.of("java:global/env/Servlet_ConnectionFactory"))))
        );
    }


    @Test
    public void testIsJMSRA() throws Exception {
        assertAll(
            () -> assertFalse(isJMSRA(null)),
            () -> assertTrue(isJMSRA(ConnectorConstants.DEFAULT_JMS_ADAPTER))
        );
    }
}
