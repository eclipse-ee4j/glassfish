/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.perms;

import java.io.FilePermission;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.URIParameter;
import java.security.cert.Certificate;
import java.util.Enumeration;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SMGlobalPolicyUtilTest {

    private static final String plfile = "server.policy";
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        URL serverPF = SMGlobalPolicyUtilTest.class.getResource(plfile);
        System.out.println("policy file url = " + serverPF + ", path = " + serverPF.getPath());
        System.setProperty(SMGlobalPolicyUtil.SYS_PROP_JAVA_SEC_POLICY, serverPF.getPath());
    }

    
    @Test
    public void testSystemPolicyPath() {
        System.out.println("path= " + SMGlobalPolicyUtil.domainCfgFolder);
       
        Assert.assertNotNull(SMGlobalPolicyUtil.domainCfgFolder);
    }
    
    @Test    
    public void testTYpeConvert() {
        SMGlobalPolicyUtil.CommponentType componentType = SMGlobalPolicyUtil.convertComponentType("ejb");
        System.out.println("Converted type = " + componentType);
        Assert.assertEquals("Converted type should be Ejb", SMGlobalPolicyUtil.CommponentType.ejb, componentType);
        
        componentType = SMGlobalPolicyUtil.convertComponentType("ear");
        System.out.println("Converted type = " + componentType);
        Assert.assertEquals("Converted type should be ear", SMGlobalPolicyUtil.CommponentType.ear, componentType);
        
        componentType = SMGlobalPolicyUtil.convertComponentType("war");
        System.out.println("Converted type = " + componentType);
        Assert.assertEquals("Converted type should be web", SMGlobalPolicyUtil.CommponentType.war, componentType);
        
        componentType = SMGlobalPolicyUtil.convertComponentType("rar");
        System.out.println("Converted type = " + componentType);
        Assert.assertEquals("Converted type should be rar", SMGlobalPolicyUtil.CommponentType.rar, componentType);
        
        componentType = SMGlobalPolicyUtil.convertComponentType("car");
        System.out.println("Converted type = " + componentType);
        Assert.assertEquals("Converted type should be car", SMGlobalPolicyUtil.CommponentType.car, componentType);
        
        
        try {
            componentType = SMGlobalPolicyUtil.convertComponentType("");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            
        }
        
        try {
            componentType = SMGlobalPolicyUtil.convertComponentType("bla");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            
        }
        
        try {
            componentType = SMGlobalPolicyUtil.convertComponentType(null);
            Assert.fail();
        } catch (NullPointerException e) {
            
        }
    }
    
    
    @Test
    public void testPolicyLoading() throws NoSuchAlgorithmException, MalformedURLException, URISyntaxException {
        System.out.println("Starting testDefPolicy loading - ee");

        PermissionCollection defaultPC = 
            Policy.getInstance("JavaPolicy", new URIParameter(SMGlobalPolicyUtilTest.class.getResource("nobody.policy").toURI()))
                  .getPermissions(new CodeSource(new URL("file:/module/ALL"), (Certificate[])null ));
        
        int defaultCount = dumpPermissions("Grant", "ALL", defaultPC);

        PermissionCollection defEjbGrantededPC 
            = SMGlobalPolicyUtil.getEECompGrantededPerms(SMGlobalPolicyUtil.CommponentType.ejb);
        int count = dumpPermissions("Grant", "Ejb", defEjbGrantededPC);
        Assert.assertEquals(5, count - defaultCount);
        
        PermissionCollection defWebGrantededPC 
            = SMGlobalPolicyUtil.getEECompGrantededPerms(SMGlobalPolicyUtil.CommponentType.war);
        count = dumpPermissions("Grant", "Web", defWebGrantededPC);
        Assert.assertEquals(6, count - defaultCount);
                
        PermissionCollection defRarGrantededPC 
            = SMGlobalPolicyUtil.getEECompGrantededPerms(SMGlobalPolicyUtil.CommponentType.rar); 
        count = dumpPermissions("Grant", "Rar", defRarGrantededPC);        
        Assert.assertEquals(5, count - defaultCount);
        
        PermissionCollection defClientGrantededPC 
            = SMGlobalPolicyUtil.getEECompGrantededPerms(SMGlobalPolicyUtil.CommponentType.car);
        count = dumpPermissions("Grant", "Client", defClientGrantededPC);
        Assert.assertEquals(10, count - defaultCount);
        
        System.out.println("Starting testDefPolicy loading - ee restrict");
        
        PermissionCollection defEjbRestrictedPC 
            = SMGlobalPolicyUtil.getCompRestrictedPerms(SMGlobalPolicyUtil.CommponentType.ejb);
        count = dumpPermissions("Restricted", "Ejb", defEjbRestrictedPC);
        Assert.assertEquals(2, count - defaultCount);        
        
        PermissionCollection defWebRestrictedPC 
            = SMGlobalPolicyUtil.getCompRestrictedPerms(SMGlobalPolicyUtil.CommponentType.war);
        count = dumpPermissions("Restricted", "Web", defWebRestrictedPC);
        Assert.assertEquals(2, count - defaultCount);

        PermissionCollection defRarRestrictedPC 
            = SMGlobalPolicyUtil.getCompRestrictedPerms(SMGlobalPolicyUtil.CommponentType.rar);
        count = dumpPermissions("Restricted", "Rar", defRarRestrictedPC);
        Assert.assertEquals(1, count - defaultCount);
        
        PermissionCollection defClientRestrictedPC        
            = SMGlobalPolicyUtil.getCompRestrictedPerms(SMGlobalPolicyUtil.CommponentType.car);
        count = dumpPermissions("Restricted", "Client", defClientRestrictedPC);
        Assert.assertEquals(2, count - defaultCount);
        
    }
    
    
    @Test
    public void testFilePermission() {
        System.out.println("Starting testFilePermission");
        
        FilePermission fp1 = new FilePermission("-", "delete");
        FilePermission fp2 = new FilePermission("a/file.txt", "delete");
        
        Assert.assertTrue(fp1.implies(fp2));
        
        FilePermission fp3 = new FilePermission("*", "delete");
        FilePermission fp4 = new FilePermission("file.txt", "delete");
        
        Assert.assertTrue(fp3.implies(fp4));
        
        
        FilePermission fp5 = new FilePermission("/scratch/xyz/*", "delete");
        FilePermission fp6 = new FilePermission("/scratch/xyz/deleteit.txt", "delete");
        
        Assert.assertTrue(fp5.implies(fp6));
        

        FilePermission fp7 = new FilePermission("/scratch/xyz/", "delete");
        FilePermission fp8 = new FilePermission("/scratch/xyz", "delete");
        
        Assert.assertTrue(fp7.implies(fp8));

        
        Permission fp9 = new java.security.UnresolvedPermission("VoidPermission", "", "", null);
        Permission fp10 = new java.security.AllPermission();
        
        Assert.assertTrue(fp10.implies(fp9));
        Assert.assertTrue(!fp9.implies(fp10));
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
