/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import org.glassfish.admin.amx.impl.config.AMXConfigImpl;

public final class AMXConfigTest {
    public AMXConfigTest()
    {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
    private void _testConvertName( final String str, final String out)
    {
        assert AMXConfigImpl.convertAttributeName(str).equals(out) :
            "Expected " + out + " for " + str;

        assert AMXConfigImpl.convertAttributeName(out).equals(out) :
            "Expected " + out + " for " + out + " (no change), but got " + AMXConfigImpl.convertAttributeName(out);
    }
    
    @Test
    public void testDomConvertName() {
        _testConvertName( "Is", "is" );
        
        _testConvertName( "IsFooBar", "is-foo-bar" );
        
        _testConvertName( "IsConnectionValidationRequired", "is-connection-validation-required" );
    }
}






