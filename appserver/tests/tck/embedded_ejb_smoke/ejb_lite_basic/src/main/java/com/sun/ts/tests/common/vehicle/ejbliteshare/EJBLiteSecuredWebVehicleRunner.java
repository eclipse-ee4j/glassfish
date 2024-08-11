/*
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ts.tests.common.vehicle.ejbliteshare;

import com.sun.ts.lib.porting.TSURL;
import com.sun.ts.lib.util.BASE64Encoder;
import com.sun.ts.lib.util.TestUtil;
import com.sun.ts.tests.common.vehicle.VehicleRunnable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import static com.sun.ts.tests.common.vehicle.ejbliteshare.EJBLiteClientIF.TEST_PASSED;

public class EJBLiteSecuredWebVehicleRunner implements VehicleRunnable {
    private final static Logger logger = Logger.getLogger(EJBLiteSecuredWebVehicleRunner.class.getName());

    protected String getServletPath(String vehicle) {
        return "/" + vehicle + "_vehicle.jsp";
    }

    @Override
    public Status run(String[] argv, Properties p) {
        String testName = p.getProperty("testName");
        String vehicle = p.getProperty("vehicle");
        String contextRoot = p.getProperty("vehicle_archive_name");
        String queryString = "?testName=" + testName;
        String requestUrl = "/" + contextRoot + getServletPath(vehicle) + queryString;

        String username = p.getProperty("user");
        String password = p.getProperty("password");

        TSURL ctsURL = new TSURL();
        URL url = null;
        HttpURLConnection connection = null;
        int statusCode = Status.NOT_RUN;
        String response = null;
        try {
            url = ctsURL.getURL("http", p.getProperty("webServerHost"), Integer.parseInt(p.getProperty("webServerPort")), requestUrl);

            // Encode authData
            String authData = username + ":" + password;

            BASE64Encoder encoder = new BASE64Encoder();

            String encodedAuthData = encoder.encode(authData.getBytes());

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);

            // set request property
            connection.setRequestProperty("Authorization", "Basic " + encodedAuthData.trim());

            logger.info("Connecting " + url.toExternalForm());
            connection.connect();

            response = TestUtil.getResponse(connection).trim();
            if (response.indexOf(TEST_PASSED) >= 0) {
                statusCode = Status.PASSED;
            } else {
                statusCode = Status.FAILED;
            }
        } catch (IOException e) {
            statusCode = Status.FAILED;
            response = "Failed to connect to the test webapp." + TestUtil.printStackTraceToString(e);
        }
        return new ReasonableStatus(statusCode, response);
    }

}
