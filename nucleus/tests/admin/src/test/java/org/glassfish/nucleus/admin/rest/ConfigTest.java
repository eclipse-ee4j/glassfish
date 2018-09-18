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

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

/**
 *
 * @author jasonlee
 */
public class ConfigTest extends RestTestBase {

    public static final String BASE_CONFIGS_URL = "/domain/configs";

    @Test
    public void testConfigCopy() {
        String configName = "config-" + generateRandomString();
        MultivaluedMap formData = new MultivaluedHashMap();
        formData.add("id", "default-config");
        formData.add("id", configName);
        createAndVerifyConfig(configName, formData);
        deleteAndVerifyConfig(configName);
    }

    @Test
    public void duplicateCopyShouldFail() {
        MultivaluedMap formData = new MultivaluedHashMap();
        formData.add("id", "default-config");
        formData.add("id", "server-config");

        Response response = post(BASE_CONFIGS_URL + "/copy-config", formData);
        assertFalse(isSuccess(response));
    }

    public void createAndVerifyConfig(String configName, MultivaluedMap configData) {
        Response response = post(BASE_CONFIGS_URL + "/copy-config", configData);
        checkStatusForSuccess(response);

        response = get(BASE_CONFIGS_URL + "/config/" + configName);
        checkStatusForSuccess(response);
    }

    public void deleteAndVerifyConfig(String configName) {
        Response response = post(BASE_CONFIGS_URL + "/config/" + configName + "/delete-config");
        checkStatusForSuccess(response);

        response = get(BASE_CONFIGS_URL + "/config/" + configName);
        assertFalse(isSuccess(response));
    }
}
