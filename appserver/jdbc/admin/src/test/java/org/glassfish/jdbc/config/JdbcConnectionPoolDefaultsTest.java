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

package org.glassfish.jdbc.config;

import org.glassfish.jdbc.config.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the JdbcConnectionPool config bean's defaults.
 * @author Kedar Mhaswade (km@dev.java.net)
 */

public class JdbcConnectionPoolDefaultsTest extends ConfigApiTest{

    JdbcConnectionPool onlyOnePool = null;
    
    public JdbcConnectionPoolDefaultsTest() {
    }

    @Override
    public String getFileName() {
        return ("JdbcConnectionPoolDefaults"); //this is the xml to load
    }

    @Before
    public void setUp() {
        onlyOnePool = super.getHabitat().getService(JdbcConnectionPool.class);
    }

    @After
    public void tearDown() {
        onlyOnePool = null;
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void testFewDefaults() {
    
        assertEquals("8", onlyOnePool.getSteadyPoolSize());
        assertEquals("32", onlyOnePool.getMaxPoolSize());
        assertEquals("false", onlyOnePool.getMatchConnections());
    }
}
