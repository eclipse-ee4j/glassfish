/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation.
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
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author mmares
 */
public class AdminCacheUtilsTest {

    private final AdminCacheUtils acu = AdminCacheUtils.getInstance();

    @Test
    public void testSimpleGetProvider_String() throws IOException {
        final String string = "The Man Who Sold The World";
        final DataProvider provider = acu.getProvider(string.getClass());
        assertNotNull(provider);
        final byte[] data = toByteArray(string, provider);
        assertNotNull(data);
        assertEquals(string, toInstance(data, String.class, provider));
    }

    @Test
    public void testSimpleGetProvider_byteArray() throws IOException {
        final byte[] byteArray = "The Man Who Sold The World".getBytes(UTF_8);
        final DataProvider provider = acu.getProvider(byteArray.getClass());
        assertNotNull(provider);
        final byte[] data = toByteArray(byteArray, provider);
        assertNotNull(data);
        assertArrayEquals(byteArray, data);
        assertArrayEquals(byteArray, toInstance(data, byte[].class, provider));
    }

    @Test
    public void testGetProvider4CommandModel() throws Exception {
        final DataProvider provider = acu.getProvider(CommandModel.class);
        assertNotNull(provider);
        final CachedCommandModel beatles1 = CachedCommandModelTest.createBeateles();
        final byte[] data = toByteArray(beatles1, provider);
        assertNotNull(data);
        final CachedCommandModel beatles2 = toInstance(data, CachedCommandModel.class, provider);
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


    private byte[] toByteArray(final Object object, final DataProvider provider) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        provider.writeToStream(object, baos);
        return baos.toByteArray();
    }


    // yeah, we know that
    @SuppressWarnings("unchecked")
    private <T> T toInstance(final byte[] data, final Class<T> targetClass, final DataProvider provider)
        throws IOException {
        return (T) provider.toInstance(new ByteArrayInputStream(data), targetClass);
    }
}
