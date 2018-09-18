/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.util.cache;

import com.sun.enterprise.admin.util.CachedCommandModel;
import com.sun.enterprise.admin.util.CachedCommandModelTest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.glassfish.api.admin.CommandModel;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author mmares
 */
public class AdminCacheUtilsTest {
    
    private AdminCacheUtils acu = AdminCacheUtils.getInstance();
    
    public AdminCacheUtilsTest() {
    }

//    @BeforeClass
//    public static void setUpClass() throws Exception {
//    }

//    @AfterClass
//    public static void tearDownClass() throws Exception {
//    }
    
    @Test
    public void testSimpleGetProvider() throws IOException {
        DataProvider provider;
        byte[] data;
        //String
        Object o = "The Man Who Sold The World";
        assertNotNull(provider = acu.getProvider(o.getClass()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        provider.writeToStream(o, baos);
        assertNotNull(data = baos.toByteArray());
        assertTrue(o.equals(provider.toInstance(new ByteArrayInputStream(data), 
                o.getClass())));
        //byte array
        o = "The Man Who Sold The World".getBytes();
        assertNotNull(provider = acu.getProvider(o.getClass()));
        baos = new ByteArrayOutputStream();
        provider.writeToStream(o, baos);
        assertNotNull(data = baos.toByteArray());
        assertArrayEquals((byte[]) o, data);
        assertArrayEquals((byte[]) o, (byte[]) provider.toInstance(
                new ByteArrayInputStream(data), byte[].class));
    }
    
    @Test
    public void testGetProvider4CommandModel() throws Exception {
        DataProvider provider;
        byte[] data;
        assertNotNull(provider = acu.getProvider(CommandModel.class));
        CachedCommandModel beatles1 = CachedCommandModelTest.createBeateles();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        provider.writeToStream(beatles1, baos);
        assertNotNull(data = baos.toByteArray());
        System.out.println("BTW: " + new String(data, "UTF-8"));
        CachedCommandModel beatles2 = (CachedCommandModel) provider.toInstance(
                new ByteArrayInputStream(data), CachedCommandModel.class);
        beatles2.setETag(null);
        assertEquals(beatles1.getETag(), CachedCommandModel.computeETag(beatles2));
    }

    @Test
    public void testValidateKey() {
        assertTrue(acu.validateKey("kurt"));
        assertTrue(acu.validateKey("kurt.cobain"));
        assertTrue(acu.validateKey("kurt-cobain"));
        assertTrue(acu.validateKey("kurt_cobain"));
        assertFalse(acu.validateKey("kurt.cobain@nirvana"));
        assertFalse(acu.validateKey("kurt.cobain#nirvana"));
        assertTrue(acu.validateKey("kurt.cobain/smells.like.teen.spirit"));
        assertFalse(acu.validateKey("/kurt.cobain/smells.like.teen.spirit"));
        assertTrue(acu.validateKey("kurt.cobain/smells.like.teen.spirit/"));
    }

}
