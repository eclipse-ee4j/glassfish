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

import org.glassfish.security.services.api.common.Attributes;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

public class AttributesTest {

    @Test
    public void testAttributes() {
        String attName = "test";
        Attributes att = new AttributesImpl();
        att.addAttribute(attName, "value1", false);
        att.addAttribute(attName, "value2", false);

        Set<String> vs = att.getAttributeValues(attName);
        assertThat(vs, containsInAnyOrder("value1", "value2"));
    }


    @Test
    public void testAttributesReplace() {
        String attName = "test";
        Attributes att = new AttributesImpl();
        att.addAttribute(attName, "value1", false);
        att.addAttribute(attName, "value2", true);

        Set<String> vs = att.getAttributeValues(attName);
        assertThat(vs, contains("value2"));
    }


    @Test
    public void testAttributesNull() {
        String attName = "test";
        Attributes att = new AttributesImpl();
        att.addAttribute(attName, (String)null, false);

        Set<String> vs = att.getAttributeValues(attName);
        assertThat(vs, hasSize(0));
    }


    @Test
    public void testAttributesEmpty() {
        String attName = "test";
        Attributes att = new AttributesImpl();
        att.addAttribute(attName, "", false);

        Set<String> vs = att.getAttributeValues(attName);
        assertThat(vs, hasSize(0));
    }

}
