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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test class for {@link RadixTreeNode}.
 */
public class TestRadixTreeNode {

    private static final String UNIT_TEST = "unitTest";

    /**
     * Test the node creation.
     */
    @Test
    public void testNodeCreation() {
        RadixTreeNode rootNode = new RadixTreeNode(UNIT_TEST, UNIT_TEST);
        Assert.assertEquals(rootNode.getKey(), UNIT_TEST);
        Assert.assertEquals(rootNode.getValue(), UNIT_TEST);
        rootNode.setKey("newTest");
        Assert.assertEquals(rootNode.getKey(), "newTest");
        rootNode.setValue("newValue");
        Assert.assertEquals(rootNode.getValue(), "newValue");
        Assert.assertNull(rootNode.getParentNode());
        Assert.assertTrue(rootNode.getChildNodes().isEmpty());
    }

    /**
     * Test the child node addition for <code>null</code> child.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAdditionForNullNode() {
        RadixTreeNode rootNode = new RadixTreeNode(UNIT_TEST, UNIT_TEST);
        rootNode.addChildNode(null);
    }

    /**
     * Test addition of child node having empty key.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAdditionForEmptyChildKey() {
        RadixTreeNode rootNode = new RadixTreeNode(UNIT_TEST, UNIT_TEST);
        RadixTreeNode firstChildNode = new RadixTreeNode("", "");
        rootNode.addChildNode(firstChildNode);
    }

    /**
     * Test addition of child node having <code>null</code> key.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAdditionForNullChildKey() {
        RadixTreeNode rootNode = new RadixTreeNode(UNIT_TEST, UNIT_TEST);
        RadixTreeNode firstChildNode = new RadixTreeNode(null, "");
        rootNode.addChildNode(firstChildNode);
    }

    /**
     * Test child node addition.
     */
    @Test
    public void testChildNodeAddition() {
        RadixTreeNode rootNode = new RadixTreeNode(UNIT_TEST, UNIT_TEST);
        RadixTreeNode firstChildNode = new RadixTreeNode("firstChildKey", "firstChildValue");
        rootNode.addChildNode(firstChildNode);
        Assert.assertTrue(rootNode.getChildNodes().size() == 1);
        Assert.assertEquals(rootNode.getChildNode('f'), firstChildNode);
    }

    /**
     * Test addition of duplicate child node.
     */
    @Test
    public void testDuplicateChildNodeAddition() {
        RadixTreeNode rootNode = new RadixTreeNode(UNIT_TEST, UNIT_TEST);
        RadixTreeNode firstChildNode = new RadixTreeNode("test", "oldtest");
        rootNode.addChildNode(firstChildNode);
        Assert.assertEquals(firstChildNode.getParentNode(), rootNode);
        Assert.assertEquals(rootNode.getChildNode('t'), firstChildNode);
        RadixTreeNode duplicateNode = new RadixTreeNode("test", "newtest");
        rootNode.addChildNode(duplicateNode);
        Assert.assertNull(firstChildNode.getParentNode());
        Assert.assertEquals(rootNode.getChildNode('t'), duplicateNode);
        Assert.assertTrue(rootNode.getChildNodes().size() == 1);
    }

    /**
     *  Test the child node removal for <code>null</code> child.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemovalForNullNode() {
        RadixTreeNode rootNode = new RadixTreeNode(UNIT_TEST, UNIT_TEST);
        rootNode.removeChildNode(null);
    }

    /**
     * Test removal of child node having empty key.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemovalForEmptyChildKey() {
        RadixTreeNode rootNode = new RadixTreeNode(UNIT_TEST, UNIT_TEST);
        RadixTreeNode firstChildNode = new RadixTreeNode("", "");
        rootNode.removeChildNode(firstChildNode);
    }

    /**
     * Test removal of child node having <code>null</code> key.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemovalForNullChildKey() {
        RadixTreeNode rootNode = new RadixTreeNode(UNIT_TEST, UNIT_TEST);
        RadixTreeNode firstChildNode = new RadixTreeNode(null, "");
        rootNode.removeChildNode(firstChildNode);
    }

    /**
     * Test child node removal for invalid child node.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidChildNodeRemoval() {
        RadixTreeNode rootNode = new RadixTreeNode(UNIT_TEST, UNIT_TEST);
        RadixTreeNode firstChildNode = new RadixTreeNode("firstChildKey", "firstChildValue");
        rootNode.addChildNode(firstChildNode);
        RadixTreeNode invalidNode = new RadixTreeNode("InvalidChildKey", "InvalidChildValue");
        rootNode.removeChildNode(invalidNode);
    }

    /**
     * Test child node removal.
     */
    @Test
    public void testChildNodeRemoval() {
        RadixTreeNode rootNode = new RadixTreeNode(UNIT_TEST, UNIT_TEST);
        RadixTreeNode firstChildNode = new RadixTreeNode("firstChildKey", "firstChildValue");
        rootNode.addChildNode(firstChildNode);
        Assert.assertTrue(rootNode.getChildNodes().size() == 1);
        Assert.assertEquals(rootNode.getChildNode('f'), firstChildNode);
        rootNode.removeChildNode(firstChildNode);
        Assert.assertTrue(rootNode.getChildNodes().isEmpty());
    }
}
