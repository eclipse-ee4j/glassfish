/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.tests;

import org.junit.Test;
import junit.framework.Assert;
import org.glassfish.api.event.EventTypes;

public class ImmutableTest {


    @Test
    public void test1() {

        EventTypes evt1 = EventTypes.create("foo");
        EventTypes evt2 = EventTypes.create("foo");
        EventTypes evt3 = EventTypes.create("foo34");

        Assert.assertNotSame(evt1, evt3);
        Assert.assertEquals(evt1, evt2);

        Assert.assertTrue(evt1==evt2);
        Assert.assertFalse(evt1==evt3);
    }

}
