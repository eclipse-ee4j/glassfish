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

package org.glassfish.nucleus.admin.rest;

import jakarta.ws.rs.core.Response;

import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * This class will test that the zero-config effort is handled correctly by the
 * REST layer. Specifically, with zero-config, certain elements, if their values
 * are all the defaults, will not be persisted to domain.xml.  This poses a problem
 * for the REST layer, which walks the Dom tree, as the element requested by the
 * user is now not represented in the tree.  The REST layer, then, must make sure
 * that the request does not die (with an NPE), but, rather, returns the expected
 * data.
 * @author jdlee
 */
public class ZeroConfigTest extends RestTestBase {
    public static final String BASE_SERVER_CONFIG_URL = "domain/configs/config/server-config";
    /**
     * Currently (6/29/2012), the transaction-service element is missing from
     * server-config out of the box.  This should continue to be the case once
     * zero-config is fully implemented and integrated.
     */
    @Test
    @Disabled
    public void testTransactionService() {
        final Response response = get(BASE_SERVER_CONFIG_URL + "/transaction-service");
        assertEquals(200, response.getStatus());
        Map<String, String> entity = getEntityValues(response);
        assertNotNull(entity);
    }
}
