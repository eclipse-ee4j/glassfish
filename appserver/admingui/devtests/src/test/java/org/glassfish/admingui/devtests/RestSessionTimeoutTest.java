/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admingui.devtests;

/*import org.junit.BeforeClass;
import org.junit.Before;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.HashMap;
import java.util.Map;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import org.glassfish.admingui.devtests.util.SeleniumHelper;
import org.junit.Test;
import static org.junit.Assert.*;*/

/**
 *
 * @author jasonlee
 */
/*public class RestSessionTimeoutTest extends BaseSeleniumTestClass {

//    protected Client client = Client.create();
    protected final String baseUrl = "http://localhost:" + SeleniumHelper.getParameter("admin.port", "4848") + "/management/domain";
    protected static final String RESPONSE_TYPE = MediaType.APPLICATION_JSON;
    protected static final String AUTH_USER_NAME = "admin";
    protected static final String AUTH_PASSWORD = "admin";

    @BeforeClass
    public static void setUp() {

    }

    @Before
    @Override
    public void reset() {
        try {
            authenticate();
            setAdminPassword(AUTH_PASSWORD, "");
        } catch (Exception e) {
            //
        }
        client = Client.create();
        super.reset();
    }

    @Test
    public void testTimeout() throws InterruptedException {
        try {
            // Set the REST session token timeout to one minute
            setRestSessionTokenTimeout("1");
            // Set a password on the admin user
            setAdminPassword("", AUTH_PASSWORD);
            // Wait for just over a minute to give the REST session a chance to timeout
            Thread.sleep(1*60*1000); // min * sec * mills
            // Click the home button. We should see the login screen. We do a manual loop
            // here rather than the base class methods as they assume Ajax navigation.
            open("/");
            waitForLoginPageLoad(30);*/
            /*
            for (int seconds = 0;; seconds++) {
                if (seconds >= (30)) {
                    fail("The operation timed out waiting for the page to load.");
                }

                boolean loginFormIsDisplayed = false;

                try {
                    loginFormIsDisplayed = isElementPresent("j_username");
                } catch (Exception ex) {
                }
                if (loginFormIsDisplayed) {
                    break;
                }

                sleep(TIMEOUT_CALLBACK_LOOP);
            }
             */
/*        } finally {
            // Authenticate so the REST call succeeds
            authenticate();
            // Clear password
            setAdminPassword(AUTH_PASSWORD, "");
            // Clear auth credentials
            client = Client.create();
            // Rest token timeout
            setRestSessionTokenTimeout("30");
        }
    }

    protected void setRestSessionTokenTimeout(final String minutes) {
        ClientResponse response = post(baseUrl + "/_set-rest-admin-config", new HashMap<String, String>() {

            {
                put("sessiontokentimeout", minutes);
            }
        });
        checkStatusForSuccess(response);
    }

    protected void setAdminPassword(final String oldPass, final String newPass) {
        ClientResponse response = post(baseUrl + "/change-admin-password", new HashMap<String, String>() {

            {
                put("AS_ADMIN_PASSWORD", oldPass);
                put("AS_ADMIN_NEWPASSWORD", newPass);
                put("id", "admin");
            }
        });
    }

    //*************************************************************************
    // TODO: These methods were copied from the REST devtests. When we have a
    // proper rest-client lib, these can go away.

    protected ClientResponse delete(String address) {
        return client.resource(address).accept(RESPONSE_TYPE).delete(ClientResponse.class);
    }

    protected ClientResponse post(String address, Map<String, String> payload) {
        return client.resource(address).accept(RESPONSE_TYPE).post(ClientResponse.class, buildMultivaluedMap(payload));
    }

    private MultivaluedMap buildMultivaluedMap(Map<String, String> payload) {
        if (payload instanceof MultivaluedMap) {
            return (MultivaluedMap) payload;
        }
        MultivaluedMap formData = new MultivaluedMapImpl();
        if (payload != null) {
            for (final Map.Entry<String, String> entry : payload.entrySet()) {
                formData.add(entry.getKey(), entry.getValue());
            }
        }
        return formData;
    }

    protected void authenticate() {
        client.addFilter(new HTTPBasicAuthFilter(AUTH_USER_NAME, AUTH_PASSWORD));
    }

    protected void checkStatusForSuccess(ClientResponse cr) {
        int status = cr.getStatus();
        if ((status < 200) || (status > 299)) {
            fail("Expected a status between 200 and 299 (inclusive).  Found " + status);
        }
    }
    //**************************************************************************
}*/
