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

package org.glassfish.security.services.impl.common;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;


import org.glassfish.security.services.api.common.Attributes;



public class AttributesTest {

    @Test
    public void testAttributes() {

        String attName = "test";
        Attributes att = new AttributesImpl();
        att.addAttribute(attName, "value1", false);
        att.addAttribute(attName, "value2", false);

        Set<String> vs = att.getAttributeValues(attName);
        Assert.assertEquals(2, vs.size());
        Assert.assertTrue(vs.contains("value1"));
        Assert.assertTrue(vs.contains("value2"));
    }


    @Test
    public void testAttributesReplace() {

        String attName = "test";
        Attributes att = new AttributesImpl();
        att.addAttribute(attName, "value1", false);
        att.addAttribute(attName, "value2", true);

        Set<String> vs = att.getAttributeValues(attName);
        Assert.assertEquals(1, vs.size());
        Assert.assertFalse(vs.contains("value1"));
        Assert.assertTrue(vs.contains("value2"));
    }

    @Test
    public void testAttributesNull() {

        String attName = "test";
        Attributes att = new AttributesImpl();
        att.addAttribute(attName, (String)null, false);

        Set<String> vs = att.getAttributeValues(attName);
        Assert.assertEquals(0, vs.size());
    }

    @Test
    public void testAttributesEmpty() {

        String attName = "test";
        Attributes att = new AttributesImpl();
        att.addAttribute(attName, "", false);

        Set<String> vs = att.getAttributeValues(attName);
        Assert.assertEquals(0, vs.size());
    }

}
