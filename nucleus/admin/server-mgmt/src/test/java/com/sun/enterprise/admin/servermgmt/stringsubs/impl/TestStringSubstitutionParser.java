/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.stringsubs.impl;

import java.io.File;
import java.io.InputStream;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sun.enterprise.admin.servermgmt.stringsubs.StringSubstitutionException;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.StringsubsDefinition;

/**
 * Unit test class to test {@link StringSubstitutionParser} functionality.
 */
public class TestStringSubstitutionParser {

    // Test string-subs.xml file.
    private final String _stringSubsPath = TestStringSubstitutionParser.class.getPackage().
            getName().replace(".", File.separator) + File.separator + "stringsubs.xml";
    private InputStream _configStream = null;

    @BeforeClass
    public void init() {
        _configStream = TestStringSubstitutionParser.class.getClassLoader().getResourceAsStream(_stringSubsPath);  
    }

    @Test
    public void testParserWithNullInput() {
        try {
            StringSubstitutionParser.parse(null);
        } catch (StringSubstitutionException e) {
            return;
        }
        Assert.fail("Failed, Parser is allowing null stream to parse.");
    }

    /**
     * Test string subs XML parsing.
     */
    @Test
    public void testParser() {
        StringsubsDefinition def = null;
        try {
            def = StringSubstitutionParser.parse(_configStream);
        } catch (StringSubstitutionException e) {
            Assert.fail("Failed to parse xml : " + _stringSubsPath, e);
        }
        Assert.assertNotNull(def);
        Assert.assertNotNull(def.getComponent());
        Assert.assertNotNull(def.getVersion());
        Assert.assertFalse(def.getChangePair().isEmpty());
    }
}
