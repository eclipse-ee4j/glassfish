/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.appclient.common;

import static com.sun.enterprise.security.ee.perms.SMGlobalPolicyUtil.CLIENT_TYPE_CODESOURCE;
import static com.sun.enterprise.security.ee.perms.SMGlobalPolicyUtil.CommponentType.car;

import com.sun.enterprise.security.ee.perms.XMLPermissionsHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.NoSuchAlgorithmException;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.URIParameter;
import java.security.cert.Certificate;

import javax.xml.stream.XMLStreamException;

public class PermissionsUtil {

    protected static final String PERMISSIONS_XML = "META-INF/permissions.xml";

    protected static final String CLIENT_EE_PERMS_FILE = "javaee.client.policy";
    protected static final String CLIENT_EE_PERMS_PKG = "META-INF/" + CLIENT_EE_PERMS_FILE;

    protected static final String CLIENT_RESTRICT_PERMS_FILE = "restrict.client.policy";
    protected static final String CLIENT_RESTRICT_PERMS_PKG = "META-INF/" + CLIENT_RESTRICT_PERMS_FILE;

    // get client declared permissions which is packaged on the client's generated jar,
    // or in the client's module jar if standalone
    // result could be null
    public static PermissionCollection getClientDeclaredPermissions(ClassLoader classLoader) throws IOException {
        URL permUrl = classLoader.getResource(PERMISSIONS_XML);
        if (permUrl == null) {
            return null;
        }

        try {
            return new
                XMLPermissionsHandler(null, permUrl.openStream(), car)
                    .getAppDeclaredPermissions();
        } catch (XMLStreamException | FileNotFoundException e) {
            throw new IOException(e);
        }
    }

    // get the permissions configured inside the javaee.client.policy,
    // which might be packaged inside the client jar,
    // or from the installed folder lib/appclient
    // result could be null if either of the above is found
    public static PermissionCollection getClientEEPolicy(ClassLoader classLoader) throws IOException {
        return getClientPolicy(classLoader, CLIENT_EE_PERMS_PKG, CLIENT_EE_PERMS_FILE);
    }

    // get the permissions configured inside the javaee.client.policy,
    // which might be packaged inside the client jar,
    // or from the installed folder lib/appclient
    // result could be null if either of the above is found
    public static PermissionCollection getClientRestrictPolicy(ClassLoader classLoader) throws IOException {
        return getClientPolicy(classLoader, CLIENT_RESTRICT_PERMS_PKG, CLIENT_RESTRICT_PERMS_FILE);
    }

    private static PermissionCollection getClientPolicy(ClassLoader classLoader, String pkgedFile, String policyFileName) throws IOException {

        // 1st try to find from the packaged client jar
        URL eeClientUrl = classLoader.getResource(pkgedFile);
        if (eeClientUrl != null)
            return getEEPolicyPermissions(eeClientUrl);

        // 2nd try to find from client's installation at lib/appclient folder
        String clientPolicyClocation = getClientInstalledPath();
        if (clientPolicyClocation != null) {
            return getPolicyPermissions(clientPolicyClocation + policyFileName);
        }

        return null;

    }

    private static PermissionCollection getPolicyPermissions(String policyFilename) throws IOException {
        if (!new File(policyFilename).exists()) {
            return null;
        }

        return getEEPolicyPermissions(new URL("file:" + policyFilename));
    }

    private static PermissionCollection getEEPolicyPermissions(URL fileUrl) throws IOException {
        try {
            return
                Policy.getInstance("JavaPolicy", new URIParameter(fileUrl.toURI()))
                      .getPermissions(new CodeSource(new URL(CLIENT_TYPE_CODESOURCE), (Certificate[]) null));
        } catch (NoSuchAlgorithmException | MalformedURLException | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String getClientInstalledPath() {
        String policyPath = System.getProperty("java.security.policy");
        if (policyPath == null) {
            return null;
        }

        return new File(policyPath).getParent() + File.separator;
    }

}
