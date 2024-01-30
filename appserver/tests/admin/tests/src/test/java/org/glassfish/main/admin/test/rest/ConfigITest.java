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

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;

import org.glassfish.main.itest.tools.RandomGenerator;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author jasonlee
 */
public class ConfigITest extends RestTestBase {

    @Test
    public void testConfigCopy() {
        String configName = "config-" + RandomGenerator.generateRandomString();
        MultivaluedHashMap<String, String> formData = new MultivaluedHashMap<>();
        formData.add("id", "default-config");
        formData.add("id", configName);
        createAndVerifyConfig(configName, formData);
        deleteAndVerifyConfig(configName);
    }

    @Test
    public void duplicitCopyShouldFail() {
        MultivaluedHashMap<String, String> formData = new MultivaluedHashMap<>();
        formData.add("id", "default-config");
        formData.add("id", "server-config");
        Response response = managementClient.post(URL_CONFIGS + "/copy-config", formData);
        // FIXME: causes HTTP 500 without any log, should be 422.
        assertThat(response.getStatus(), equalTo(500));
    }
}
