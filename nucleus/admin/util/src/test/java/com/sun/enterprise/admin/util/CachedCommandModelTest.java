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

package com.sun.enterprise.admin.util;

import javax.xml.bind.DatatypeConverter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mmares
 */
public class CachedCommandModelTest {
    
    public CachedCommandModelTest() {
    }
    
    public static CachedCommandModel createBeateles() {
        CachedCommandModel result = new CachedCommandModel("TheBeatles");
        result.dashOk = true;
        CommandModelData.ParamModelData prm = new CommandModelData.ParamModelData("Lennon", String.class, false, "John Winston Ono Lennon", "J", false, null);
        prm.param._primary = true;
        prm.param._multiple = true;
        result.add(prm);
        result.add(new CommandModelData.ParamModelData("McCartney", String.class, false, null, "P", false, "Paul"));
        result.add(new CommandModelData.ParamModelData("Harrison", String.class, false, null, "G", false, null));
        result.add(new CommandModelData.ParamModelData("Starr", String.class, true, null, null, false, "Ringo"));
        return result;
    }
    
    @Test
    public void testETag() {
        CachedCommandModel beatles1 = createBeateles();
        assertNotNull(beatles1.getETag());
        assertFalse(beatles1.getETag().isEmpty());
        CachedCommandModel beatles2 = createBeateles();
        assertEquals(beatles1.getETag(), beatles2.getETag());
        CachedCommandModel beatles3 = createBeateles();
        beatles3.add(new CommandModelData.ParamModelData("Sutcliffe", String.class, true, "R.I.P.", null, true, "Stuart"));
        assertFalse(beatles1.getETag().equals(beatles3.getETag()));
        CachedCommandModel beatles4 = createBeateles();
        beatles4.dashOk = false;
        assertFalse(beatles1.getETag().equals(beatles4.getETag()));
    }
}
