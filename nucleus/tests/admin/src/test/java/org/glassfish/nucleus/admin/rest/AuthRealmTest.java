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
import java.util.List;
import java.util.Map;
import jakarta.ws.rs.core.Response;
import org.glassfish.admin.rest.client.utils.MarshallingUtils;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

public class AuthRealmTest extends RestTestBase {
    public static final String URL_LIST_GROUP_NAMES = "/domain/configs/config/server-config/security-service/auth-realm/admin-realm/list-group-names";
    public static final String URL_SUPPORTS_USER_MANAGEMENT = "/domain/configs/config/server-config/security-service/auth-realm/admin-realm/supports-user-management";
    public static final String URL_LIST_ADMIN_REALM_USERS = "/domain/configs/config/server-config/security-service/auth-realm/admin-realm/list-users";
    public static final String URL_LIST_FILE_USERS = "/domain/configs/config/server-config/security-service/auth-realm/file/list-users";
    public static final String URL_CREATE_USER = "/domain/configs/config/server-config/security-service/auth-realm/file/create-user";
    public static final String URL_DELETE_USER = "/domain/configs/config/server-config/security-service/auth-realm/file/delete-user";
    public static final String URL_AUTH_REALM_CLASS_NAMES = "/domain/list-predefined-authrealm-classnames";

    // Disable this test for now...
//    @Test
    public void testListGroupNames() {
        Response response = get(URL_LIST_GROUP_NAMES, new HashMap<String, String>() {{
            put("userName", "admin");
            put("realmName", "admin-realm");
        }});
        checkStatusForSuccess(response);
        final String entity = response.readEntity(String.class);
        Map responseMap = MarshallingUtils.buildMapFromDocument(entity);
        Map extraProperties = (Map)responseMap.get("extraProperties");
        List<String> groups = (List<String>)extraProperties.get("groups");

        assertTrue(groups.size() > 0);
    }

    @Test
    public void testSupportsUserManagement() {
        List<String> groups = getCommandResults(get(URL_SUPPORTS_USER_MANAGEMENT));
        assertEquals("true", groups.get(0));
    }

//    @Test
    public void testUserManagement() {
        final String userName = "user" + generateRandomString();
        Map<String, String> newUser = new HashMap<String, String>() {{
           put ("id", userName);
           put ("AS_ADMIN_USERPASSWORD", "password");
        }};

        Response response = post(URL_CREATE_USER, newUser);
        assertTrue(isSuccess(response));

        List<String> values = getCommandResults(get(URL_LIST_FILE_USERS));
        assertTrue(values.contains(userName));

        response = delete(URL_DELETE_USER, newUser);
        assertTrue(isSuccess(response));

        values = getCommandResults(get(URL_LIST_FILE_USERS));
        assertFalse(values.contains(userName));
    }

    @Test
    public void testListAuthRealmClassNames() {
        List<String> classNameList = getCommandResults(get(URL_AUTH_REALM_CLASS_NAMES));
        assertTrue(!classNameList.isEmpty());
    }
}
