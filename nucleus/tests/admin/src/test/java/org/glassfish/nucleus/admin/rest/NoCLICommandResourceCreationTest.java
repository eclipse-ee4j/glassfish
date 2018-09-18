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
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.admin.rest.client.utils.MarshallingUtils;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

/**
 * @author Mitesh Meswani
 */
public class NoCLICommandResourceCreationTest extends RestTestBase {
    private static final String URL_SERVER_PROPERTY = "/domain/servers/server/server/property";

    @Test
    public void testPropertyCreation() {
        final String propertyKey  = "propertyName" + generateRandomString();
        String propertyValue = generateRandomString();

        //Create a property
        Map<String, String> param = new HashMap<String, String>();
        param.put("name", propertyKey);
        param.put("value",propertyValue);
        Response response = getClient().target(getAddress(URL_SERVER_PROPERTY))
                .request(RESPONSE_TYPE)
                .post(Entity.entity(MarshallingUtils.getXmlForProperties(param), MediaType.APPLICATION_XML), Response.class);
        assertTrue(isSuccess(response));

        //Verify the property got created
        String propertyURL = URL_SERVER_PROPERTY + "/" + propertyKey;
        response = get (propertyURL);
        assertTrue(isSuccess(response));
        Map<String, String> entity = getEntityValues(response);
        assertTrue(entity.get("name").equals(propertyKey));
        assertTrue(entity.get("value").equals(propertyValue));

        // Verify property update
        propertyValue = generateRandomString();
        param.put("value", propertyValue);
        response = getClient().target(getAddress(URL_SERVER_PROPERTY))
                .request(RESPONSE_TYPE)
                .put(Entity.entity(MarshallingUtils.getXmlForProperties(param), MediaType.APPLICATION_XML), Response.class);
        assertTrue(isSuccess(response));
        response = get (propertyURL);
        assertTrue(isSuccess(response));
        entity = getEntityValues(response);
        assertTrue(entity.get("name").equals(propertyKey));
        assertTrue(entity.get("value").equals(propertyValue));

        //Clean up to leave domain.xml good for next run
        response = delete(propertyURL, new HashMap<String, String>());
        assertTrue(isSuccess(response));
    }

}
