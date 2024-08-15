/*
 * Copyright (c) 2013, 2018-2021 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.admin.servermgmt.xml.stringsubs.ModeType;

import java.io.File;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link ModeProcessor} functionality.
 */
public class ModeProcessorTest {

    /**
     * Test for <code>null</code> input string.
     */
    @Test
    public void testNullInput() {
        String outputStr = ModeProcessor.processModeType(ModeType.DOUBLE, null);
        assertNull(outputStr);
    }

    /**
     * Test for <code>null</code> mode.
     */
    @Test
    public void testInvalidMode() {
        String inputStr = "TEST";
        assertEquals(inputStr, ModeProcessor.processModeType(null, inputStr));
    }

    /**
     * Test for the empty input string.
     */
    @Test
    public void testEmptyInput() {
        String outputStr = ModeProcessor.processModeType(ModeType.DOUBLE, "");
        assertTrue(outputStr.isEmpty());
    }

    /**
     * Test the processing for FORWARD ModeType.
     */
    @Test
    public void testForwardMode() {
        String inputStr = "First Slash \\ Second Double Slash \\\\";
        String expectedOutput = "First Slash / Second Double Slash //";
        String outputStr = ModeProcessor.processModeType(ModeType.FORWARD, inputStr);
        assertEquals(outputStr, expectedOutput);
    }

    /**
     * Test the processing for DOUBLE ModeType.
     */
    @Test
    public void testDoubleMode() {
        String inputStr = "First Slash \\ First Colon : Second Double Slash \\\\ Second Double Colon ::";
        String expectedOutput = "First Slash \\\\ First Colon \\: Second Double Slash \\\\\\\\ Second Double Colon \\:\\:";
        String outputStr = ModeProcessor.processModeType(ModeType.DOUBLE, inputStr);
        assertEquals(outputStr, expectedOutput);
    }

    /**
     * Test the processing for POLICY ModeType.
     */
    @Test
    public void testPolicyMode() {
        StringBuilder builder = new StringBuilder();
        builder.append("First Separator ");
        builder.append(File.separator);
        builder.append(" Second Double Separator ");
        builder.append(File.separator);
        builder.append(File.separator);
        String expectedOutput = "First Separator ${/} Second Double Separator ${/}${/}";
        String outputStr = ModeProcessor.processModeType(ModeType.POLICY, builder.toString());
        assertEquals(outputStr, expectedOutput);
    }
}
