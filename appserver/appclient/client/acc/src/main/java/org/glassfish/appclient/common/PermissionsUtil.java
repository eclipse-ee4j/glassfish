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

import com.sun.enterprise.security.ee.perms.XMLPermissionsHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.PermissionCollection;

import javax.xml.stream.XMLStreamException;

import static com.sun.enterprise.security.ee.perms.SMGlobalPolicyUtil.CommponentType.car;

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

}
