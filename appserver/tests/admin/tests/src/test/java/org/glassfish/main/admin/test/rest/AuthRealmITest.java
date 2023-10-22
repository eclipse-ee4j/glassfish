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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.enterprise.security.auth.realm.certificate.CertificateRealm;
import com.sun.enterprise.security.auth.realm.file.FileRealm;
import com.sun.enterprise.security.auth.realm.ldap.LDAPRealm;
import com.sun.enterprise.security.auth.realm.solaris.SolarisRealm;
import com.sun.enterprise.security.ee.authentication.glassfish.jdbc.JDBCRealm;
import com.sun.enterprise.security.ee.authentication.glassfish.pam.PamRealm;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.glassfish.admin.rest.client.utils.MarshallingUtils;
import org.glassfish.main.itest.tools.RandomGenerator;
import org.junit.jupiter.api.Test;

public class AuthRealmITest extends RestTestBase {
    private static final String URL_LIST_GROUP_NAMES = "/domain/configs/config/server-config/security-service/auth-realm/admin-realm/list-group-names";
    private static final String URL_SUPPORTS_USER_MANAGEMENT = "/domain/configs/config/server-config/security-service/auth-realm/admin-realm/supports-user-management";
    private static final String URL_LIST_ADMIN_REALM_USERS = "/domain/configs/config/server-config/security-service/auth-realm/admin-realm/list-users";
    private static final String URL_LIST_FILE_USERS = "/domain/configs/config/server-config/security-service/auth-realm/file/list-users";
    private static final String URL_CREATE_USER = "/domain/configs/config/server-config/security-service/auth-realm/file/create-user";
    private static final String URL_DELETE_USER = "/domain/configs/config/server-config/security-service/auth-realm/file/delete-user";
    private static final String URL_AUTH_REALM_CLASS_NAMES = "/domain/list-predefined-authrealm-classnames";

    @Test
    public void testListGroupNames() {
        Response response = managementClient.get(URL_LIST_GROUP_NAMES, Map.of("userName", "admin", "realmName", "admin-realm"));
        assertEquals(200, response.getStatus());
        final String entity = response.readEntity(String.class);
        Map responseMap = MarshallingUtils.buildMapFromDocument(entity);
        Map extraProperties = (Map) responseMap.get("extraProperties");
        List<String> groups = (List<String>) extraProperties.get("groups");
        assertThat(groups, containsInAnyOrder("asadmin"));
    }

    @Test
    public void testListAdminUsers() {
        Response response = managementClient.get(URL_LIST_ADMIN_REALM_USERS);
        assertEquals(200, response.getStatus());
        final String entity = response.readEntity(String.class);
        Map<String, ?> responseMap = MarshallingUtils.buildMapFromDocument(entity);
        assertThat(responseMap.toString(), responseMap, aMapWithSize(5));
        Map<String, ?> extraProperties = (Map<String, ?>) responseMap.get("extraProperties");
        assertThat(extraProperties.toString(), extraProperties, aMapWithSize(3));
        List<Map<String, List<String>>> users = (List<Map<String, List<String>>>) extraProperties.get("users");
        assertThat(users, hasSize(1));
        Map<String, ?> userAdmin = users.get(0);
        assertThat(userAdmin, aMapWithSize(2));
        assertThat((String) userAdmin.get("name"), equalTo("admin"));
        assertThat((List<String>) userAdmin.get("groups"), contains("asadmin"));
    }

    @Test
    public void testSupportsUserManagement() {
        List<String> groups = getCommandResults(managementClient.get(URL_SUPPORTS_USER_MANAGEMENT));
        assertEquals("true", groups.get(0));
    }


    @Test
    public void testUserManagement() {
        final String userName = "user" + RandomGenerator.generateRandomString();
        {
            Response response = managementClient.post(URL_CREATE_USER, Map.of("id", userName, "AS_ADMIN_USERPASSWORD", "password"));
            assertEquals(200, response.getStatus());
        }
        {
            List<String> values = getCommandResults(managementClient.get(URL_LIST_FILE_USERS));
            assertThat(values, hasItem(userName));
        }
        {
            Response response = managementClient.delete(URL_DELETE_USER, Map.of("id", userName));
            assertEquals(200, response.getStatus());
        }
        {
            List<String> values = getCommandResults(managementClient.get(URL_LIST_FILE_USERS));
            assertThat(values, not(hasItem(userName)));
        }
    }


    @Test
    public void testListAuthRealmClassNames() {
        List<String> classNameList = getCommandResults(managementClient.get(URL_AUTH_REALM_CLASS_NAMES));
        assertThat(classNameList.toString(), classNameList, hasSize(6));

        String[] realms = Stream.of(
                JDBCRealm.class,
                PamRealm.class,
                CertificateRealm.class,
                FileRealm.class,
                LDAPRealm.class,
                SolarisRealm.class).map(Class::getName)
                                   .toArray(String[]::new);

        assertThat(classNameList, containsInAnyOrder(realms));
    }
}
