/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.impl.common;

import java.util.Set;

import org.glassfish.security.services.api.common.Attribute;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class AttributeTest {


    @Test
    public void testAttribute() {
        String attName = "test";
        Attribute att = new AttributeImpl(attName);
        att.addValue("value1");
        att.addValue("value2");

        Set<String> vs = att.getValues();
        assertEquals(2, att.getValueCount());
        assertThat(vs, containsInAnyOrder("value1", "value2"));
    }

    @Test
    public void testAttributeNullValue() {
        String attName = "testnull";
        Attribute att = new AttributeImpl(attName);
        att.addValue(null);

        Set<String> vs = att.getValues();
        assertTrue(vs.isEmpty());
    }


    @Test
    public void testAttributeNullValue1() {
        String attName = "testnull1";
        Attribute att = new AttributeImpl(attName);
        att.addValue(null);
        att.addValue("value1");

        Set<String> vs = att.getValues();
        assertEquals(1, att.getValueCount());
        assertThat(vs, contains("value1"));
    }

    @Test
    public void testAttributeEmptyValue() {
        String attName = "testEmpty";
        Attribute att = new AttributeImpl(attName);
        att.addValue("");

        Set<String> vs = att.getValues();
        assertTrue(vs.isEmpty());
    }


}
