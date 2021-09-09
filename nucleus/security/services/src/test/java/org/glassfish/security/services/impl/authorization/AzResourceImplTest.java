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

package org.glassfish.security.services.impl.authorization;

import java.net.URI;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Set;

import org.glassfish.security.services.api.common.Attribute;
import org.glassfish.security.services.api.common.Attributes;
import org.glassfish.security.services.impl.common.AttributesImpl;
import org.junit.jupiter.api.Test;

import static org.glassfish.security.services.impl.authorization.AzResourceImpl.addAttributesFromUriQuery;
import static org.glassfish.security.services.impl.authorization.AzResourceImpl.decodeURI;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @see AzResourceImpl
 */
public class AzResourceImplTest {

    @Test
    public void testGetters() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> new AzResourceImpl(null));

        URI uri;
        AzResourceImpl impl;


        // Doesn't care about scheme
        uri = new URI("http://foo");
        impl = new AzResourceImpl(uri);
        assertSame(uri, impl.getUri(), "non-admin OK");

        // Empty domain (i.e. default)
        uri = new URI("admin:///tenants/tenant/zirka?locked=true%3D");
        impl = new AzResourceImpl(uri);

        // Test getters
        assertEquals(uri, impl.getUri(), "URI");
        assertEquals("admin:///tenants/tenant/zirka?locked=true%3D", impl.toString(), "toString");

        // Non-empty domain, empty path
        uri = new URI("admin://myDomain?locked=true%3D");
        impl = new AzResourceImpl(uri);

        // Test getters
        assertEquals(uri, impl.getUri(), "URI");
        assertEquals("admin://myDomain?locked=true%3D", impl.toString(), "toString");
    }


    @Test
    public void testAddAttributesFromUriQuery() throws Exception {
        final boolean REPLACE = true;

        assertThrows(IllegalArgumentException.class,
            () -> addAttributesFromUriQuery(null, new AttributesImpl(), REPLACE));
        assertThrows(IllegalArgumentException.class,
            () -> addAttributesFromUriQuery(new URI("admin:///tenants/tenant/zirka?locked=true"), null, REPLACE));

        Attributes attributes = new AttributesImpl();
        Attribute attribute;
        Set<String> values;
        Iterator<String> iter;
        BitSet bitset;
        assertEquals(0, attributes.getAttributeCount(), "Empty attributes");

        // No params
        URI uri = new URI("admin:///tenants/tenant/zirka");
        addAttributesFromUriQuery(uri, attributes, !REPLACE);
        assertEquals(0, attributes.getAttributeCount(), "Empty attributes");

        // 1 param
        uri = new URI("admin:///tenants/tenant/zirka?name1=value1");
        addAttributesFromUriQuery(uri, attributes, !REPLACE);
        assertEquals(1, attributes.getAttributeCount(), "Attributes count");

        attribute = attributes.getAttribute("name1");
        assertNotNull(attributes.getAttribute("name1"), "attribute");

        values = attribute.getValues();
        assertThat("Values count", values, hasSize(1));
        iter = values.iterator();
        assertTrue(iter.hasNext());
        assertEquals("value1", iter.next(), "Values value");
        assertFalse(iter.hasNext());

        // Repeat, no dup value
        addAttributesFromUriQuery(uri, attributes, !REPLACE);
        assertEquals(1, attributes.getAttributeCount(), "Attributes count");
        attribute = attributes.getAttribute("name1");
        assertNotNull(attribute, "attribute");
        values = attribute.getValues();
        assertEquals(1, values.size(), "Values count");
        iter = values.iterator();
        assertTrue(iter.hasNext(), "iterator");
        assertEquals("value1", iter.next(), "Values value");
        assertFalse(iter.hasNext(), "iterator");

        // New value
        uri = new URI("admin:///tenants/tenant/boris?name1=value2");
        addAttributesFromUriQuery(uri, attributes, !REPLACE);
        assertEquals(1, attributes.getAttributeCount(), "Attributes count");
        attribute = attributes.getAttribute("name1");
        assertNotNull(attribute, "attribute");
        values = attribute.getValues();
        assertThat("Values count", values, hasSize(2));
        bitset = new BitSet(2);
        for (String v : values) {
            if ("value1".equals(v) && !bitset.get(0)) {
                bitset.set(0);
            } else if ("value2".equals(v) && !bitset.get(1)) {
                bitset.set(1);
            } else {
                fail("Unexpected attribute value " + v);
            }
        }

        // Replace attribute
        uri = new URI("admin:///tenants/tenant/lucky?name1=value3");
        addAttributesFromUriQuery(uri, attributes, REPLACE);
        assertEquals(1, attributes.getAttributeCount(), "Attributes count");
        assertNotNull(attribute = attributes.getAttribute("name1"), "attribute");
        values = attribute.getValues();
        assertEquals(1, values.size(), "Values count");
        iter = values.iterator();
        assertTrue(iter.hasNext(), "iterator");
        assertEquals("value3", iter.next(), "Values value");
        assertFalse(iter.hasNext(), "iterator");

        // New attribute
        uri = new URI("admin:///tenants/tenant/lucky?name2=value21&name2=value22");
        addAttributesFromUriQuery(uri, attributes, !REPLACE);
        assertEquals(2, attributes.getAttributeCount(), "Attributes count");
        assertNotNull(attributes.getAttribute("name1"), "attribute");
        assertNotNull(attribute = attributes.getAttribute("name2"), "attribute");
        values = attribute.getValues();
        assertEquals(2, values.size(), "Values count");
        bitset = new BitSet(2);
        for (String v : values) {
            if ("value21".equals(v) && !bitset.get(0)) {
                bitset.set(0);
            } else if ("value22".equals(v) && !bitset.get(1)) {
                bitset.set(1);
            } else {
                fail("Unexpected attribute value " + v);
            }
        }

        // Encoded attribute
        attributes = new AttributesImpl();
        uri = new URI("admin:///tenants/tenant/lucky?na%3Dme2=val%26ue1&na%3Dme2=val%3Due2");
        addAttributesFromUriQuery(uri, attributes, !REPLACE);
        assertEquals(1, attributes.getAttributeCount(), "Attributes count");
        assertNotNull(attribute = attributes.getAttribute("na=me2"), "attribute");
        values = attribute.getValues();
        assertEquals(2, values.size(), "Values count");
        bitset = new BitSet(2);
        for (String v : values) {
            if ("val&ue1".equals(v) && !bitset.get(0)) {
                bitset.set(0);
            } else if ("val=ue2".equals(v) && !bitset.get(1)) {
                bitset.set(1);
            } else {
                fail("Unexpected attribute value " + v);
            }
        }
    }


    @Test
    public void testdecodeURI() throws Exception {
        assertNull(decodeURI(null), "Expected null");
        assertEquals(
            "#($^&#&(*$C@#$*&^@#*&(|}|}{|}dfaj;",
            decodeURI("%23(%24%5E%26%23%26(*%24C%40%23%24*%26%5E%40%23*%26(%7C%7D%7C%7D%7B%7C%7Ddfaj%3B"),
            "decoded");
    }
}
