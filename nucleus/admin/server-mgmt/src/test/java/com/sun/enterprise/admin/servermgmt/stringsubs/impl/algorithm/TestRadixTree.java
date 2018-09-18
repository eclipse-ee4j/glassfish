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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit test class for {@link RadixTree}.
 */
public class TestRadixTree {

    private RadixTree _tree;

    @BeforeClass
    public void init() {
        _tree = new RadixTree();
        populateTree();
    }

    /**
     * Test insertion of null key in tree.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInsertionForNullKey() {
        _tree.insert(null, "value");
    }

    /**
     * Test insertion of null key in tree.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInsertionForEmptyKey() {
        _tree.insert(null, "value");
    }

    /**
     * Test the tree structure.
     */
    @Test
    public void testTreeStructure() {
        RadixTreeNode rootNode = _tree.getRootNode();

        // Validate root node
        assertEquals(rootNode.getKey(), "");
        assertEquals(rootNode.getValue(), null);
        assertEquals(rootNode.getParentNode(), null);

        // Validate first child node of rootNode.
        assertEquals(rootNode.getChildNodes().size(), 2);
        RadixTreeNode firstNode = rootNode.getChildNode('a');
        RadixTreeNode secondNode = rootNode.getChildNode('s');;

        Assert.assertNotNull(firstNode);
        assertEquals(firstNode.getParentNode(), rootNode);
        Assert.assertNull(firstNode.getValue());
        RadixTreeNode firstNodeFirstChild = firstNode.getChildNode('b');
        assertEquals(firstNodeFirstChild.getValue(), "abVal");
        assertEquals(firstNodeFirstChild.getParentNode(), firstNode);
        RadixTreeNode node = firstNodeFirstChild.getChildNode('e');
        assertEquals(node.getValue(), "abetVal");
        assertEquals(node.getKey(), "et");
        assertEquals(node.getParentNode(), firstNodeFirstChild);
        node = firstNodeFirstChild.getChildNode('a');
        assertEquals(node.getValue(), "abaitVal");
        assertEquals(node.getKey(), "ait");
        assertEquals(node.getParentNode(), firstNodeFirstChild);
        Assert.assertTrue(node.getChildNodes().isEmpty());
        RadixTreeNode firstNodeSecondChild = firstNode.getChildNode('c');
        assertEquals(firstNodeSecondChild.getValue(), "acidVal");
        assertEquals(firstNodeSecondChild.getParentNode(), firstNode);
        assertEquals(firstNodeSecondChild.getKey(), "cid");
        assertEquals(firstNodeSecondChild.getChildNodes().size(), 1);
        node = firstNodeSecondChild.getChildNode('i');
        assertEquals(node.getValue(), "acidicVal");
        assertEquals(node.getParentNode(), firstNodeSecondChild);
        Assert.assertTrue(node.getChildNodes().isEmpty());

        assertEquals(secondNode.getParentNode(), rootNode);
        Assert.assertNull(secondNode.getValue());
        RadixTreeNode secondNodeFirstChild = secondNode.getChildNode('i');
        assertEquals(secondNodeFirstChild.getValue(), "sickVal");
        assertEquals(secondNodeFirstChild.getParentNode(), secondNode);
        RadixTreeNode secondNodeSecondChild = secondNode.getChildNode('o');
        assertEquals(secondNodeSecondChild.getValue(), null);
        assertEquals(secondNodeSecondChild.getParentNode(), secondNode);
        assertEquals(rootNode.getChildNodes().size(), 2);
        node = secondNodeSecondChild.getChildNode('n');
        assertEquals(node.getValue(), "sonVal");
        assertEquals(node.getParentNode(), secondNodeSecondChild);
        node = secondNodeSecondChild.getChildNode('f');
        assertEquals(node.getValue(), "softVal");
        assertEquals(node.getKey(), "ft");
        assertEquals(node.getParentNode(), secondNodeSecondChild);
    }


    /**
     * Test insert and duplicate insert in RadixTree.
     * Test-case depends on another method as this method changes the tree
     * structure which may cause the failure for other tests.
     */
    @Test(dependsOnMethods = {"testTreeStructure"})
    public void testInsertExistingKey() {
        _tree.insert("sick", "newValue");
        assertEquals(_tree.getRootNode().getChildNode('s').getChildNode('i').getValue(), "newValue");
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
