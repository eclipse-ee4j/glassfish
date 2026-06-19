/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.layout.facelets;

import com.sun.jsftemplating.ContextMocker;
import com.sun.jsftemplating.layout.descriptors.LayoutDefinition;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * <p>
 * Tests for the {@link FaceletsLayoutDefinitionReader}.
 * </p>
 *
 */

public class FaceletsLayoutDefinitionReaderTest {

  private final ClassLoader cl = FaceletsLayoutDefinitionReaderTest.class.getClassLoader();

  @Before
  public void init(){
    ContextMocker.init();
  }

    /**
     *
     * <p>
     * Simple test to ensure we can read a facelets file.
     * </p>
     *
     */
    @Test
    public void testRead1() {
        try {
            FaceletsLayoutDefinitionReader reader =
                new FaceletsLayoutDefinitionReader("foo", cl.getResource("./simple.xhtml"));
            LayoutDefinition ld = reader.read();
            Assert.assertEquals("LayoutDefinition.unevaluatedId", "foo", ld
                    .getUnevaluatedId());
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    public void timeTest(String fileName, int iterations) {
        try {
        java.util.Date start = new java.util.Date();
        for (int x=0; x<iterations; x++) {
        FaceletsLayoutDefinitionReader reader =
            new FaceletsLayoutDefinitionReader("foo", cl.getResource(fileName));
        LayoutDefinition ld = reader.read();
        }
        java.util.Date end = new java.util.Date();
System.out.println("Faclets performance " + fileName + " (" + iterations + "), lower is better: " + (end.getTime() - start.getTime()));
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testSpeed() {
    timeTest("./speed-s.xhtml", 1);
//    timeTest(new URL("file:"),".speed-m.xhtml", 500);
//    timeTest(new URL("file:"),".speed-l.xhtml", 500);
//    timeTest(new URL("file:"),".speed-xl.xhtml", 500);
    }

    /**
     *
     * <p>
     * This tests the accuracy of what was read.
     * </p>
     *
     * public void testReadAccuracy() {
     *
     * try {
     *
     * FaceletsLayoutDefinitionReader reader =
     *
     * new FaceletsLayoutDefinitionReader("bar", new
     * URL(new URL("file:"),".readTest1.jsf"));
     *
     * LayoutDefinition ld = reader.read();
     *
     * List<LayoutElement> children = ld.getChildLayoutElements();
     *
     * if (children.size() < 5) {
     *
     * throw new RuntimeException("Not enough children!");
     *  }
     *
     * assertEquals("testReadAccuracy.id.y",
     *
     * "y", children.get(2).getUnevaluatedId());
     *
     * assertEquals("testReadAccuracy.value.abcd",
     *
     * "abcd", ((LayoutComponent) children.get(2)).getOption("value"));
     *
     *
     *
     * assertEquals("testReadAccuracy.id.hhh",
     *
     * "hhh", children.get(3).getUnevaluatedId());
     *
     * assertEquals("testReadAccuracy.text.some tree",
     *
     * "some tree",
     *
     * ((LayoutComponent) children.get(3)).getOption("text"));
     *
     * assertEquals("testReadAccuracy.hhh:treeNode1.id",
     *
     * "treeNode1",
     *
     * children.get(3).getChildLayoutElements().get(0).getUnevaluatedId());
     *
     * assertEquals("testReadAccuracy.hhh:treeNode1.text",
     *
     * "abc",
     *
     * ((LayoutComponent)
     * children.get(3).getChildLayoutElements().get(0)).getOption("text"));
     *
     *
     *
     * assertEquals("testReadAccuracy.id.theform",
     *
     * "theform", children.get(4).getUnevaluatedId());
     *
     * assertEquals("testReadAccuracy.style1",
     *
     * "border: 1px solid red;",
     *
     * ((LayoutComponent) children.get(4)).getOption("style"));
     *
     *
     *
     * for (LayoutElement elt : children.get(4).getChildLayoutElements()) {
     *
     * System.out.println(elt.getUnevaluatedId());
     *  }
     *  } catch (Exception ex) {
     *
     * ex.printStackTrace();
     *
     * fail(ex.getMessage());
     *  }
     *  }
     *
     */
}
