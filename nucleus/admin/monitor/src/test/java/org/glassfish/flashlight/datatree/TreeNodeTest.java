/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

/**
 * @author Harpreet Singh
 */
package org.glassfish.flashlight.datatree;

import java.lang.reflect.Method;
import java.util.List;

import org.glassfish.flashlight.datatree.factory.TreeNodeFactory;
import org.glassfish.flashlight.statistics.Average;
import org.glassfish.flashlight.statistics.Counter;
import org.glassfish.flashlight.statistics.TimeStats;
import org.glassfish.flashlight.statistics.factory.AverageFactory;
import org.glassfish.flashlight.statistics.factory.CounterFactory;
import org.glassfish.flashlight.statistics.factory.TimeStatsFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * @author hsingh
 */
public class TreeNodeTest {

    /**
     * This method is used by the test
     */
    public String helloWorld() {
        return "Hello World";
    }


    @Test
    public void testSimpleTree() {
        TreeNode server = setupSimpleTree();
        TreeNode grandson = server.getNode("wto.wtoson.wtograndson");
        assertEquals("wtograndson", grandson.getName());
    }


    @Test
    public void testSimpleTreeWrongElement() {
        TreeNode server = setupSimpleTree();
        TreeNode grandson = server.getNode("wto.foobar.wtograndson");
        assertNull(grandson);
    }


    @Test
    public void testSimpleTreeWithMethodInvoker() throws Exception {
        TreeNode server = setupSimpleTree();
        Method m = this.getClass().getMethod("helloWorld", (Class[]) null);
        TreeNode methodInv = TreeNodeFactory.createMethodInvoker("helloWorld", this, "categoryName", m);
        TreeNode wto = server.getNode("wto");
        wto.addChild(methodInv);

        TreeNode child = server.getNode("wto.helloWorld");
        assertEquals(child.getValue(), "Hello World");
    }


    @Test
    @Disabled("FIXME: Found NPE in AbstractTreeNode.decodeName!")
    public void testCounterInTree() {
        TreeNode server = setupSimpleTree();
        Counter counter = CounterFactory.createCount(10);
        for (int i = 0; i < 3; i++) {
            counter.increment();
        }

        TreeNode grandson = server.getNode("wto.wtoson.wtograndson");
        grandson.addChild(counter);

        TreeNode counterNode = server.getNode("wto.wtoson.wtograndson.counter");
        assertEquals(13L, counterNode.getValue());
    }


    @Test
    public void testAverageInTree() {
        TreeNode server = setupSimpleTree();
        Average average = AverageFactory.createAverage();
        for (int i = 0; i < 3; i++) {
            average.addDataPoint((i + 1) * 3);
        }

        TreeNode grandson = server.getNode("wto.wtoson.wtograndson");
        grandson.addChild((TreeNode) average);
        TreeNode averageNode = server.getNode("wto.wtoson.wtograndson.average");

        assertAll(
            () -> assertEquals(6.0, average.getAverage(), "average.average"),
            () -> assertEquals(3, average.getMin(), "average.min"),
            () -> assertEquals(9, average.getMax(), "average.max"),
            () -> assertEquals(6.0, averageNode.getValue())
        );
    }


    @Test
    public void testIncorrectGetCompletePathName() {
        TreeNode server = setupSimpleTree();
        TreeNode grandson = server.getNode("wto:wtoson.wtograndson");
        assertNull(grandson);
    }


    @Test
    public void testGetCompletePathName() {
        TreeNode server = setupSimpleTree();
        TreeNode grandson = server.getNode("wto.wtoson.wtograndson");
        assertEquals("server.wto.wtoson.wtograndson", grandson.getCompletePathName());
    }


    @Test
    public void testTraverse() {
        TreeNode server = setupComplexTree();
        List<TreeNode> list = server.traverse(false);
        String[] expected = new String[7];
        expected[0] = new String("server");
        expected[1] = new String("server.wto");
        expected[2] = new String("server.wto.wtoson");
        expected[3] = new String("server.wto.wtoson.wtosonsdaughter");
        expected[4] = new String("server.wto.wtoson.wtosonsson");
        expected[5] = new String("server.wto.wtodaughter");
        expected[6] = new String("server.wto.wtodaughter.wtodaughtersdaughter");
        assertEquals(expected.length, list.size());
    }


    @Test
    public void testTraverseIgnoreDisabled() {
        TreeNode server = setupComplexTree();
        TreeNode wtoson = server.getNode("wto.wtoson");
        wtoson.setEnabled(false);
        List<TreeNode> list = server.traverse(true);
        String[] expected = new String[4];
        expected[0] = new String("server");
        expected[1] = new String("server.wto");
        expected[2] = new String("server.wto.wtodaughter");
        expected[3] = new String("server.wto.wtodaughter.wtodaughtersdaughter");
        assertEquals(expected.length, list.size());
    }


    @Test
    public void testV2Compatible() {
        TreeNode server = setupComplexTree();
        List<TreeNode> list = server.getNodes("*wtodaughter*", false, true);
        int expectedLength = 2;
        assertEquals(expectedLength, list.size());
    }


    @Test
    public void testGetAll() {
        TreeNode server = setupComplexTree();
        List<TreeNode> list = server.getNodes("*", false, true);
        String[] expected = new String[7];
        expected[0] = new String("server");
        expected[1] = new String("server.wto");
        expected[2] = new String("server.wto.wtoson");
        expected[3] = new String("server.wto.wtoson.wtosonsdaughter");
        expected[4] = new String("server.wto.wtoson.wtosonsson");
        expected[5] = new String("server.wto.wtodaughter");
        expected[6] = new String("server.wto.wtodaughter.wtodaughtersdaughter");
        assertEquals(expected.length, list.size());
    }


    @Test
    public void testGetSonsAndGrandSons() {
        TreeNode server = setupComplexTree();
        List<TreeNode> list = server.getNodes(".*son.*", false, false);
        int expectedCount = 3;
        int actualCount = 0;
        for (TreeNode node : list) {
            if (node.getCompletePathName().contains("son")) {
                actualCount++;
            }
        }
        assertEquals(expectedCount, actualCount);
    }


    @Test
    public void testGetDaughter() {
        TreeNode server = setupComplexTree();
        List<TreeNode> list = server.getNodes(".*wtodaughter", false, false);
        int expectedCount = 1;
        int actualCount = 0;
        for (TreeNode node : list) {
            if (node.getCompletePathName().contains("wtodaughter")) {
                actualCount++;
            }
        }
        assertEquals(expectedCount, actualCount);
    }


    @Test
    public void testTimeStatsMillis() {
        TimeStats timeStat = TimeStatsFactory.createTimeStatsMilli();
        long min = 1000;
        long mid = 2000;
        long max = 4000;
        long count = 3;
        double average = (min + mid + max) / 3.0;
        timeStat.setTime(min);
        timeStat.setTime(mid);
        timeStat.setTime(max);
        assertEquals(min, timeStat.getMinimumTime());
        assertEquals(average, timeStat.getTime());
        assertEquals(max, timeStat.getMaximumTime());
        assertEquals(count, timeStat.getCount());
    }


    private TreeNode setupSimpleTree (){
        TreeNode server = TreeNodeFactory.createTreeNode ("server", this, "server");
        TreeNode wto = TreeNodeFactory.createTreeNode("wto", this, "web");
        TreeNode wtoson = TreeNodeFactory.createTreeNode ("wtoson", this, "web");
        TreeNode wtograndson = TreeNodeFactory.createTreeNode ("wtograndson",
                this, "web");
        wtoson.addChild(wtograndson);
        wto.addChild(wtoson);
        server.addChild(wto);
        return server;
    }


    private TreeNode setupComplexTree() {
        TreeNode server = TreeNodeFactory.createTreeNode("server", this, "server");

        TreeNode wto = TreeNodeFactory.createTreeNode("wto", this, "web");
        server.addChild(wto);

        TreeNode wtoson = TreeNodeFactory.createTreeNode("wtoson", this, "web");
        wto.addChild(wtoson);
        TreeNode wtodaughter = TreeNodeFactory.createTreeNode("wtodaughter", this, "web");
        wto.addChild(wtodaughter);

        TreeNode wtosonsson = TreeNodeFactory.createTreeNode("wtosonsson", this, "web");
        wtoson.addChild(wtosonsson);

        TreeNode wtodaughtersdaughter = TreeNodeFactory.createTreeNode("wtodaughtersdaughter", this, "web");
        wtodaughter.addChild(wtodaughtersdaughter);

        TreeNode wtosonsdaughter = TreeNodeFactory.createTreeNode("wtosonsdaughter", this, "web");
        wtoson.addChild(wtosonsdaughter);

        return server;
    }
}
