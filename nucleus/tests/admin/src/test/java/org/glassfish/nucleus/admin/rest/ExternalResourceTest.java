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

package org.glassfish.nucleus.admin.rest;

import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.resources.custom.factory.PrimitivesAndStringFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author jasonlee
 */
@Disabled
public class ExternalResourceTest extends RestTestBase {

    protected static final String URL_EXTERNAL_RESOURCE = "domain/resources/external-jndi-resource";

    @Test
    public void createAndDeleteExternalResource() {
        final String resourceName = "resource_" + generateRandomString();
        final String jndiName = "jndi/" + resourceName;
        Map<String, String> newResource = new HashMap<>() {
            {
                put("id", resourceName);
                put("jndilookupname", jndiName);
                put("factoryClass", PrimitivesAndStringFactory.class.getName());
                put("restype", Double.class.getName());
            }
        };
        Response response = post(URL_EXTERNAL_RESOURCE, newResource);
        checkStatus(response);

        response = get(URL_EXTERNAL_RESOURCE + "/" + resourceName);
        checkStatus(response);

        response = delete(URL_EXTERNAL_RESOURCE + "/" + resourceName);
        checkStatus(response);
    }
}
