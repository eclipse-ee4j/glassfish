/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.test;

import org.glassfish.admin.amx.impl.util.SingletonEnforcer;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public final class SingletonEnforcerTest extends TestBase
{
    public SingletonEnforcerTest() {
    }

    private static final class Dummy {}

    @Test
    public void testForNull() {
        assertTrue( SingletonEnforcer.get( Dummy.class ) == null );
    }

    @Test
    public void testVariety() {
        SingletonEnforcer.register( String.class, "hello" );
        assertNotNull( SingletonEnforcer.get( String.class ) );

        SingletonEnforcer.register( Boolean.class, Boolean.TRUE );
        assertNotNull( SingletonEnforcer.get( Boolean.class ) );

        SingletonEnforcer.register( Integer.class, new Integer(0) );
        assertNotNull( SingletonEnforcer.get( Integer.class ) );
    }

    /*
    @Test(expected=IllegalArgumentException.class)
    public void testForBrokenJUnit() {
        throw new IllegalArgumentException( "expected" );
    }
    */


    private static final class Dummy2 {}
    @Test
    public void testForDuplicates() {
        final String s = "";
        SingletonEnforcer.register( Dummy2.class, this );
        try {
            SingletonEnforcer.register( Dummy2.class, this );
        }
        catch( IllegalArgumentException e) { /*OK*/ }
    }
}






