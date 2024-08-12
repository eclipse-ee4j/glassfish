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

package com.sun.enterprise.admin.servermgmt.stringsubs.impl.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Unit test class for {@link RadixTree}.
 */
@TestMethodOrder(OrderAnnotation.class)
public class RadixTreeTest {

    private RadixTree tree;

    @BeforeEach
    public void init() {
        tree = new RadixTree();
        populateTree();
    }

    /**
     * Test insertion of null key in tree.
     */
    @Test
    public void testInsertionForNullKey() {
        assertThrows(IllegalArgumentException.class, () -> tree.insert(null, "value"));
    }

    /**
     * Test insertion of null key in tree.
     */
    @Test
    public void testInsertionForEmptyKey() {
        assertThrows(IllegalArgumentException.class, () -> tree.insert(null, "value"));
    }

    /**
     * Test the tree structure.
     */
    @Test
    @Order(1)
    public void testTreeStructure() {
        RadixTreeNode rootNode = tree.getRootNode();

        // Validate root node
        assertEquals(rootNode.getKey(), "");
        assertEquals(rootNode.getValue(), null);
        assertEquals(rootNode.getParentNode(), null);

        // Validate first child node of rootNode.
        assertEquals(rootNode.getChildNodes().size(), 2);
        RadixTreeNode firstNode = rootNode.getChildNode('a');
        RadixTreeNode secondNode = rootNode.getChildNode('s');

        assertNotNull(firstNode);
        assertEquals(firstNode.getParentNode(), rootNode);
        assertNull(firstNode.getValue());
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
        assertTrue(node.getChildNodes().isEmpty());
        RadixTreeNode firstNodeSecondChild = firstNode.getChildNode('c');
        assertEquals(firstNodeSecondChild.getValue(), "acidVal");
        assertEquals(firstNodeSecondChild.getParentNode(), firstNode);
        assertEquals(firstNodeSecondChild.getKey(), "cid");
        assertEquals(firstNodeSecondChild.getChildNodes().size(), 1);
        node = firstNodeSecondChild.getChildNode('i');
        assertEquals(node.getValue(), "acidicVal");
        assertEquals(node.getParentNode(), firstNodeSecondChild);
        assertTrue(node.getChildNodes().isEmpty());

        assertEquals(secondNode.getParentNode(), rootNode);
        assertNull(secondNode.getValue());
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
    @Test
    @Order(2)
    public void testInsertExistingKey() {
        tree.insert("sick", "newValue");
        assertEquals(tree.getRootNode().getChildNode('s').getChildNode('i').getValue(), "newValue");
    }

    /**
     * Populate tree.
     */
    private void populateTree() {
        tree.insert("acid", "acidVal");
        tree.insert("son", "sonVal");
        tree.insert("abet", "abetVal");
        tree.insert("ab", "abVal");
        tree.insert("sick", "sickVal");
        tree.insert("abait", "abaitVal");
        tree.insert("soft", "softVal");
        tree.insert("acidic", "acidicVal");
    }
}
