/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.appclient.client.jws.boot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.glassfish.appclient.client.acc.AppClientContainer;
import org.glassfish.appclient.client.acc.Util;

/**
 *
 * @author tjquinn
 */
public class LaunchSecurityHelper {

    private static final String PERMISSIONS_TEMPLATE_NAME = "jwsclient.policy";
    private static final String SYSTEM_CODEBASE_PROPERTY = "appclient.system.codebase";
    private static final int BUFFER_SIZE = 1024;

    public static void setPermissions() {
        try {
            /*
             * Get the permissions template and write it to a temporary file.
             */
            final String permissionsTemplate = loadResource(LaunchSecurityHelper.class, PERMISSIONS_TEMPLATE_NAME);

            /*
             * The Java security logic will process property references in
             * the policy file template automatically.
             */
            boolean retainTempFiles = Boolean.getBoolean(AppClientContainer.APPCLIENT_RETAIN_TEMP_FILES_PROPERTYNAME);
            File policyFile = Util.writeTextToTempFile(permissionsTemplate, "jwsacc", ".policy", retainTempFiles);

        } catch (IOException ioe) {
            throw new RuntimeException("Error loading permissions template", ioe);
        }
    }

    /**
     * Retrieves a resource as a String.
     * <p>
     * This method does not save the template in a cache.  Use the instance method
     * getTemplate for that purpose.
     *
     * @param a class, the class loader of which should be used for searching for the template
     * @param the path of the resource to load, relative to the contextClass
     * @return the resource's contents
     * @throws IOException if the resource is not found or in case of error while loading it
     */
    private static String loadResource(Class contextClass, String resourcePath) throws IOException {
        String result = null;
        InputStream is = null;
        BufferedReader reader = null;
        try {
            is = contextClass.getResourceAsStream(resourcePath);
            if (is == null) {
                throw new IOException("Could not locate the requested resource relative to class " + contextClass.getName());
            }

            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(is));
            int charsRead;
            char [] buffer = new char [BUFFER_SIZE];
            while ((charsRead = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, charsRead);
            }

            result= sb.toString();
            return result;
        } catch (IOException ioe) {
            throw new IOException("Error loading resource " + resourcePath, ioe);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
    /**
     * Locates the first free policy.url.x setting.
     * @return the int value for the first unused policy setting
     */
    private static int firstFreePolicyIndex() {
        int i = 0;
        String propValue;
        do {
            propValue = java.security.Security.getProperty("policy.url." + String.valueOf(++i));
        } while ((propValue != null) && ( ! propValue.equals("")));

        return i;
    }

    /**
     * Refreshes the current policy object using the contents of the specified file
     * as additional policy.
     * @param policyFile the file containing additional policy
     */

}
