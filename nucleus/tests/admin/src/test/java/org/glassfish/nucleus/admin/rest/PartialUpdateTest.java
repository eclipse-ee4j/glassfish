/*
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

package org.glassfish.nucleus.admin.rest;

import java.util.HashMap;
import java.util.Map;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

/**
 *
 * @author jasonlee
 */
public class PartialUpdateTest extends RestTestBase {
    @Test(enabled=false)
    // TODO: rework this to use something nucleus-friendly
    public void testPartialUpdate() {
        final String endpoint = JdbcTest.BASE_JDBC_CONNECTION_POOL_URL + "/DerbyPool";
        final String newDesc = generateRandomString();
        Map<String, String> origAttrs = getEntityValues(get(endpoint));
        Map<String, String> newAttrs = new HashMap<String, String>() {{
            put ("description", newDesc);
        }};
        post(endpoint, newAttrs);
        Map<String, String> updatedAttrs = getEntityValues(get(endpoint));
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
