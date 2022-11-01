/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.admin.test.rest;

import jakarta.ws.rs.core.Response;

import java.util.Map;

import org.glassfish.main.admin.test.tool.RandomGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jasonlee
 */
public class PartialUpdateITest extends RestTestBase {

    @Test
    public void testPartialUpdate() {
        final String endpoint = URL_JDBC_CONNECTION_POOL + "/DerbyPool";
        final String newDesc = RandomGenerator.generateRandomString();
        Map<String, String> origAttrs = getEntityValues(managementClient.get(endpoint));
        Map<String, String> newAttrs = Map.of("description", newDesc);
        Response response = managementClient.post(endpoint, newAttrs);
        assertEquals(200, response.getStatus());
        Map<String, String> updatedAttrs = getEntityValues(managementClient.get(endpoint));
        assertEquals(newDesc, updatedAttrs.get("description"));
        assertEquals(origAttrs.get("driverClassname"), updatedAttrs.get("driverClassname"));
        assertEquals(origAttrs.get("resType"), updatedAttrs.get("resType"));
        assertEquals(origAttrs.get("validationClassname"), updatedAttrs.get("validationClassname"));
        assertEquals(origAttrs.get("datasourceClassname"), updatedAttrs.get("datasourceClassname"));
        assertEquals(origAttrs.get("name"), updatedAttrs.get("name"));
        assertEquals(origAttrs.get("transactionIsolationLevel"), updatedAttrs.get("transactionIsolationLevel"));
        assertEquals(origAttrs.get("initSql"), updatedAttrs.get("initSql"));
        assertEquals(origAttrs.get("sqlTraceListeners"), updatedAttrs.get("sqlTraceListeners"));
        assertEquals(origAttrs.get("validationTableName"), updatedAttrs.get("validationTableName"));
        assertEquals(origAttrs.get("resType"), updatedAttrs.get("resType"));
    }
}
