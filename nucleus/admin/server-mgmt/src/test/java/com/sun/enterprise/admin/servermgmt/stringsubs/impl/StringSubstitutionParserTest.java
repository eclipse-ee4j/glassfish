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

import com.sun.enterprise.admin.servermgmt.stringsubs.StringSubstitutionException;
import com.sun.enterprise.admin.servermgmt.test.ServerMgmgtTestFiles;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.StringsubsDefinition;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit test class to test {@link StringSubstitutionParser} functionality.
 */
public class StringSubstitutionParserTest {

    private static final ServerMgmgtTestFiles TEST_UTILS = new ServerMgmgtTestFiles(StringSubstitutionParserTest.class);
    private InputStream _configStream;

    @BeforeEach
    public void init() {
        _configStream = TEST_UTILS.openInputStream("stringsubs.xml");
    }


    @AfterEach
    public void close() throws IOException {
        if (_configStream != null) {
            _configStream.close();
        }
    }


    @Test
    public void testParserWithNullInput() {
        try {
            StringSubstitutionParser.parse(null);
            fail("Failed, Parser is allowing null stream to parse.");
        } catch (StringSubstitutionException e) {
            assertEquals("Invalid Stream.", e.getMessage());
        }
    }

    /**
     * Test string subs XML parsing.
     * @throws Exception
     */
    @Test
    public void testParser() throws Exception {
        StringsubsDefinition def = StringSubstitutionParser.parse(_configStream);
        assertNotNull(def);
        assertNotNull(def.getComponent());
        assertNotNull(def.getVersion());
        assertFalse(def.getChangePair().isEmpty());
    }
}
