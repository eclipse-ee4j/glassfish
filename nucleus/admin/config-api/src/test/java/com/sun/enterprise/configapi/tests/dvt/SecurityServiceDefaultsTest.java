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

package com.sun.enterprise.configapi.tests.dvt;

import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.configapi.tests.ConfigApiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the JdbcConnectionPool config bean's defaults.
 * @author Kedar Mhaswade (km@dev.java.net)
 */

public class SecurityServiceDefaultsTest extends ConfigApiTest {

    SecurityService ss = null;

    public SecurityServiceDefaultsTest() {
    }

    @Override
    public String getFileName() {
        return ("SecurityServiceDefaults"); //this is the xml to load
    }

    @Before
    public void setUp() {
        ss = super.getHabitat().getService(SecurityService.class);
    }

    @After
    public void tearDown() {
        ss = null;
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void testFewDefaults() {
        assertEquals("file", ss.getDefaultRealm());
        assertEquals("true", ss.getActivateDefaultPrincipalToRoleMapping());
        assertEquals("AttributeDeprecated", ss.getAnonymousRole());
        assertEquals("false", ss.getAuditEnabled());
        assertEquals("default", ss.getAuditModules());
        assertEquals("default", ss.getJacc());
    }
}
