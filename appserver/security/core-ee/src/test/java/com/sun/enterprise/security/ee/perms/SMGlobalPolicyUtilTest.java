/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.security.ee.perms;

import java.io.File;
import java.io.FilePermission;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SMGlobalPolicyUtilTest {

    private static final String plfile = "server.policy";

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        String absolutePath = getFile(plfile).getAbsolutePath();
        System.out.println("policy path = " + absolutePath);
        System.setProperty(SMGlobalPolicyUtil.SYS_PROP_JAVA_SEC_POLICY, absolutePath);
    }

    private static File getFile(final String fileName) throws URISyntaxException {
        final URL url = SMGlobalPolicyUtilTest.class.getResource(fileName);
        assertNotNull(url, "url");
        assertEquals("file", url.getProtocol(), "url.protocol");
        final File file = Paths.get(url.toURI()).toFile();
        assertTrue(file.exists(), "File doesn't exist: " + file);
        return file;
    }

    @Test
    public void testSystemPolicyPath() {
        System.out.println("path= " + SMGlobalPolicyUtil.domainCfgFolder);

        assertNotNull(SMGlobalPolicyUtil.domainCfgFolder);
    }

    @Test
    public void testTYpeConvert() {
        SMGlobalPolicyUtil.CommponentType componentType = SMGlobalPolicyUtil.convertComponentType("ejb");
        System.out.println("Converted type = " + componentType);
        assertEquals(SMGlobalPolicyUtil.CommponentType.ejb, componentType, "Converted type should be Ejb");

        componentType = SMGlobalPolicyUtil.convertComponentType("ear");
        System.out.println("Converted type = " + componentType);
        assertEquals(SMGlobalPolicyUtil.CommponentType.ear, componentType, "Converted type should be ear");

        componentType = SMGlobalPolicyUtil.convertComponentType("war");
        System.out.println("Converted type = " + componentType);
        assertEquals(SMGlobalPolicyUtil.CommponentType.war, componentType, "Converted type should be web");

        componentType = SMGlobalPolicyUtil.convertComponentType("rar");
        System.out.println("Converted type = " + componentType);
        assertEquals(SMGlobalPolicyUtil.CommponentType.rar, componentType, "Converted type should be rar");

        componentType = SMGlobalPolicyUtil.convertComponentType("car");
        System.out.println("Converted type = " + componentType);
        assertEquals(SMGlobalPolicyUtil.CommponentType.car, componentType, "Converted type should be car");

        assertThrows(IllegalArgumentException.class, () -> SMGlobalPolicyUtil.convertComponentType(""));
        assertThrows(IllegalArgumentException.class, () -> SMGlobalPolicyUtil.convertComponentType("bla"));
        assertThrows(NullPointerException.class, () -> SMGlobalPolicyUtil.convertComponentType(null));
    }

    @Test
    public void testFilePermission() {
        System.out.println("Starting testFilePermission");

        FilePermission fp1 = new FilePermission("-", "delete");
        FilePermission fp2 = new FilePermission("a/file.txt", "delete");

        assertTrue(fp1.implies(fp2));

        FilePermission fp3 = new FilePermission("*", "delete");
        FilePermission fp4 = new FilePermission("file.txt", "delete");

        assertTrue(fp3.implies(fp4));


        FilePermission fp5 = new FilePermission("/scratch/xyz/*", "delete");
        FilePermission fp6 = new FilePermission("/scratch/xyz/deleteit.txt", "delete");

        assertTrue(fp5.implies(fp6));


        FilePermission fp7 = new FilePermission("/scratch/xyz/", "delete");
        FilePermission fp8 = new FilePermission("/scratch/xyz", "delete");

        assertTrue(fp7.implies(fp8));


        Permission fp9 = new java.security.UnresolvedPermission("VoidPermission", "", "", null);
        Permission fp10 = new java.security.AllPermission();

        assertTrue(fp10.implies(fp9));
        assertTrue(!fp9.implies(fp10));
    }

    private int dumpPermissions(String type, String component, PermissionCollection permissionCollection) {
        int count = 0;

        if (permissionCollection == null) {
            System.out.println("Type= " + type + ", compnent= " + component  + ", Permission is empty ");
            return count;
        }

        Enumeration<Permission> permissions =  permissionCollection.elements();
        while (permissions.hasMoreElements()) {
            Permission permission = permissions.nextElement();
            System.out.println("Type= " + type + ", compnent= " + component  + ", Permission p= " + permission);
            count += 1;
        }

        return count;
    }

}
