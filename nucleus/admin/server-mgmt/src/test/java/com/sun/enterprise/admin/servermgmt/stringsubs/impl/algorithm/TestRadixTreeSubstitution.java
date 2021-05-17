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

package com.sun.enterprise.admin.servermgmt.stringsubs.impl.algorithm;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit test class for {@link RadixTreeSubstitution}.
 */
@Test
public class TestRadixTreeSubstitution {

    private RadixTree _tree;
    private RadixTreeSubstitution _substitution;

    @BeforeClass
    public void init() {
        _tree = new RadixTree();
        _substitution = new RadixTreeSubstitution(_tree);
        populateTree();
    }

    /**
     * Test algorithm instance for null tree.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSubstitutionForNullTree() {
        new RadixTreeSubstitution(null);
    }

    /**
     * Test substitution for an extended match.
     */
    @Test
    public void testInputExtendedMatch() {
        assertEquals(callSubstitution("abe"), "abVale");
        assertEquals(callSubstitution("acidic acidi"), "acidicVal acidVali");
    }

    /**
     * Test if the last word in the input string matched completely.
     */
    @Test
    public void testLastWordExactlyMatched() {
        assertEquals(callSubstitution("  aci so abet ... &&& soft"), "  aci so abetVal ... &&& softVal");
    }

    /**
     * Test substitution for continuous input i.e input without any space.
     */
    @Test
    public void testInputWithoutSpace() {
        assertEquals(callSubstitution("acidysonacidyso"), "acidValysonValacidValyso");
    }

    /**
     * Test substitution if last word partially matched.
     */
    @Test
    public void testLastWordPartiallyMatched() {
        assertEquals(callSubstitution("acidic abet softy"), "acidicVal abetVal softValy");
    }

    /**
     * Test substitution if nothing matched in the given input.
     */
    @Test
    public void testUnmatchedInput() {
        assertEquals(callSubstitution("@##ttt {{}:P"), "@##ttt {{}:P");
    }

    /**
     * Test substitution for multiple scenarios.
     * <li>Maintaining the last matching node value, and using the same if no extended match found</li>
     * <li>Covering multiple scenarios for the last word (partially/fully or looking for extended match)</li>
     * <li>Run time tree modification and checking the substitution output.</li>
     *
     * <p>Test-case depends on another methods as this method changes the tree reference
     *  which will cause the failure for other test cases.</p>
     */
    @Test(dependsOnMethods = {"testLastWordPartiallyMatched", "testLastWordExactlyMatched",
            "testInputWithoutSpace", "testInputExtendedMatch"})
    public void testBackTrack() {
        _tree = new RadixTree();
        _tree.insert("acidicity", "acidicityVal");
        _tree.insert("acidical", "acidicalVal");
        _tree.insert("acid", "acidVal");
        _substitution = new RadixTreeSubstitution(_tree);
        assertEquals(callSubstitution("acidicit acidicit"), "acidValicit acidValicit");
        _tree.insert("c", "cVal");
        assertEquals(callSubstitution("acidicit acidicit"), "acidValicValit acidValicValit");
        _tree.insert("ci", "ciVal");
        assertEquals(callSubstitution("acidicit acidicit"), "acidValiciValt acidValiciValt");
        _tree.insert("t", "tVal");
        assertEquals(callSubstitution("acidicit"), "acidValiciValtVal");
        _tree.insert("icit", "icitVal");
        assertEquals(callSubstitution("acidicit acidicit"), "acidValicitVal acidValicitVal");
    }

    /**
     * Calls the algorithm and return the replacement for the
     * given input string.
     *
     * @param input input string for substitution.
     * @return substituted string.
     */
    private String callSubstitution(String input) {
        StringBuffer outputBuffer = new StringBuffer();
        String substitution = null;
        for (char c : input.toCharArray()) {
            substitution = _substitution.substitute(c);
            if (substitution != null) {
                outputBuffer.append(substitution);
            }
        }
        substitution = _substitution.substitute(null);
        if (substitution != null) {
            outputBuffer.append(substitution);
        }
        return outputBuffer.toString();
    }

    /**
     * Populate tree.
     */
    private void populateTree() {
        _tree.insert("acid", "acidVal");
        _tree.insert("son", "sonVal");
        _tree.insert("abet", "abetVal");
        _tree.insert("ab", "abVal");
        _tree.insert("sick", "sickVal");
        _tree.insert("abait", "abaitVal");
        _tree.insert("soft", "softVal");
        _tree.insert("acidic", "acidicVal");
    }
}
