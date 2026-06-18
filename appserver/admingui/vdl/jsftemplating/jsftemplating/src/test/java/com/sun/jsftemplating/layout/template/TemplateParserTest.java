/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.layout.template;

import org.junit.Assert;
import org.junit.Test;


/**
 *  <p>    Tests for the "Template" parser.</p>
 */
public class TemplateParserTest {

  private final ClassLoader cl = TemplateParserTest.class.getClassLoader();

    @Test
  public void testURL() {
    try {
        TemplateParser parser = new TemplateParser(cl.getResource("./TemplateFormat.jsf"));
        Assert.assertEquals(cl.getResource("./TemplateFormat.jsf").toString(), parser.getURL().toString());
    } catch (Exception ex) {
        ex.printStackTrace();
        Assert.fail(ex.getMessage());
    }
    }

    @Test
    public void testOpenClose() {
    try {
        TemplateParser parser = new TemplateParser(cl.getResource("./TemplateFormat.jsf"));
        parser.open();
        parser.close();
    } catch (Exception ex) {
        ex.printStackTrace();
        Assert.fail(ex.getMessage());
    }
    }

    @Test
    public void testNextChar1() {
    try {
        TemplateParser parser = new TemplateParser(cl.getResource("./TemplateFormat.jsf"));
        parser.open();
        Assert.assertEquals("testNextChar1-1", '#', parser.nextChar());
        Assert.assertEquals("testNextChar1-2", ' ', parser.nextChar());
        Assert.assertEquals("testNextChar1-3", 'R', parser.nextChar());
        Assert.assertEquals("testNextChar1-4", 'e', parser.nextChar());
        Assert.assertEquals("testNextChar1-5", 'a', parser.nextChar());
        Assert.assertEquals("testNextChar1-6", 'd', parser.nextChar());
        Assert.assertEquals("testNextChar1-7", 'e', parser.nextChar());
        Assert.assertEquals("testNextChar1-8", 'r', parser.nextChar());
        Assert.assertEquals("testNextChar1-9", ' ', parser.nextChar());
        parser.close();
    } catch (Exception ex) {
        ex.printStackTrace();
        Assert.fail(ex.getMessage());
    }
    }

    @Test
    public void testUnread() {
    try {
        TemplateParser parser = new TemplateParser(cl.getResource("./TemplateFormat.jsf"));
        parser.open();
        Assert.assertEquals("testNextChar1-1", '#', parser.nextChar());
        Assert.assertEquals("testNextChar1-2", ' ', parser.nextChar());
        parser.unread(' ');
        Assert.assertEquals("testNextChar1-3", ' ', parser.nextChar());
        Assert.assertEquals("testNextChar1-4", 'R', parser.nextChar());
        parser.unread('R');
        Assert.assertEquals("testNextChar1-5", 'R', parser.nextChar());
        parser.unread('X');
        Assert.assertEquals("testNextChar1-6", 'X', parser.nextChar());
        Assert.assertEquals("testNextChar1-7", 'e', parser.nextChar());
        parser.unread('1');
        parser.unread('2');
        parser.unread('3');
        Assert.assertEquals("testNextChar1-8", '3', (char) parser.nextChar());
        Assert.assertEquals("testNextChar1-9", '2', (char) parser.nextChar());
        Assert.assertEquals("testNextChar1-10", '1', (char) parser.nextChar());
        parser.close();
    } catch (Exception ex) {
        ex.printStackTrace();
        Assert.fail(ex.getMessage());
    }
    }

    @Test
    public void testNVP() {
    try {
        TemplateParser parser = new TemplateParser(cl.getResource("./TemplateFormat.jsf"));
        parser.open();
        // Read some lines
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();

        // Move in to the NVP (actually 1 past just to see that that works)
        parser.nextChar(); parser.nextChar(); parser.nextChar(); parser.nextChar(); parser.nextChar();
        parser.nextChar(); parser.nextChar(); parser.nextChar(); parser.nextChar(); parser.nextChar();
        parser.nextChar(); parser.nextChar(); parser.nextChar(); parser.nextChar();
        parser.nextChar(); parser.nextChar(); parser.nextChar(); parser.nextChar();
        parser.nextChar(); parser.nextChar(); parser.nextChar();

        NameValuePair nvp = parser.getNVP(null);

        Assert.assertEquals("testNVP1", "ile", nvp.getName());
        Assert.assertEquals("testNVP2", "jsftemplating/js/jsftemplating.js", nvp.getValue());
        Assert.assertEquals("testNVP3", null, nvp.getTarget());

        parser.close();
    } catch (Exception ex) {
        ex.printStackTrace();
        Assert.fail(ex.getMessage());
    }
    }

    @Test
    public void testNVP2() {
    try {
        TemplateParser parser = new TemplateParser(cl.getResource("TemplateFormat.jsf"));
        parser.open();

        // Read 49 lines
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();

        // Move to the output mapping on line 50
        parser.readUntil('v', false);
        parser.unread('v');

        NameValuePair nvp = parser.getNVP(null);

        Assert.assertEquals("testNVP1", "value", nvp.getName());
        Assert.assertEquals("testNVP2", "val", nvp.getValue());
        Assert.assertEquals("testNVP3", "attribute", nvp.getTarget());

        parser.close();
    } catch (Exception ex) {
        ex.printStackTrace();
        Assert.fail(ex.getMessage());
    }
    }

    @Test
    public void testReadLine() {
    try {
        TemplateParser parser = new TemplateParser(cl.getResource("TemplateFormat.jsf"));
        parser.open();
        // Read some lines
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();

        // Read some characters
        parser.nextChar(); parser.nextChar(); parser.nextChar(); parser.nextChar(); parser.nextChar();
        parser.nextChar(); parser.nextChar(); parser.nextChar(); parser.nextChar(); parser.nextChar();

        // Make sure we're at the right spot
        Assert.assertEquals("testReadLine1", ' ', parser.nextChar());
        Assert.assertEquals("testReadLine2", 'm', parser.nextChar());

        // Make sure we can read the rest of the line
        Assert.assertEquals("testReadLine3", "ulti line comment is hit, skip until end is found", parser.readLine());

        // Read more lines
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();

        // Make sure we're at the right spot
        Assert.assertEquals("testReadLine4", "        <sun:script file=\"jsftemplating/js/jsftemplating.js\" />", parser.readLine());
        parser.close();
    } catch (Exception ex) {
        ex.printStackTrace();
        Assert.fail(ex.getMessage());
    }
    }

    @Test
    public void testReadToken() {
    try {
        TemplateParser parser = new TemplateParser(cl.getResource("TemplateFormat.jsf"));
        parser.open();
        // Read some lines
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();

        // Move to the Script Tag
        parser.readUntil('<', false);

        // Test readToken()
        Assert.assertEquals("testReadToken", "sun:script", parser.readToken());

        // Test skipWhiteSpace();
        parser.skipWhiteSpace(TemplateParser.SIMPLE_WHITE_SPACE);

        // Test NVP
        NameValuePair nvp = parser.getNVP(null);

        // NVP should be setup correctly
        Assert.assertEquals("testNVP1", "file", nvp.getName());
        Assert.assertEquals("testNVP2", "jsftemplating/js/jsftemplating.js", nvp.getValue());
        Assert.assertEquals("testNVP3", null, nvp.getTarget());

        parser.close();
    } catch (Exception ex) {
        ex.printStackTrace();
        Assert.fail(ex.getMessage());
    }
    }

    /**
     *    This test tests the String version of Read Until.
     */
    @Test
    public void testReadUntilStr() {
    try {
        TemplateParser parser = new TemplateParser(cl.getResource("./TemplateFormat.jsf"));
        parser.open();

        // Read some lines
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();
        parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine(); parser.readLine();

        // Test readUntil - normalize line endings for cross-platform compatibility
        String result1 = parser.readUntil("<!--", false).replaceAll("\\r\\n", "\n");
        Assert.assertEquals("testReadUntilStr", "    // This text should be commented out.  <tags> should not be parsed.\n    <!--", result1);

        String result2 = parser.readUntil("-->", false).replaceAll("\\r\\n", "\n");
        Assert.assertEquals("testReadUntilStr2", "\n    This text should be commented out.  <tags> should not be parsed.\n    -->", result2);

        String result3 = parser.readUntil("*/", false).replaceAll("\\r\\n", "\n");
        Assert.assertEquals("testReadUntilStr3", "\n    /*\n     *    This text should be commented out.  <tags> should not be parsed.\n     */", result3);

        parser.close();
    } catch (Exception ex) {
        ex.printStackTrace();
        Assert.fail(ex.getMessage());
    }
    }

/*
    public void testAdd() {
    assertTrue(5 == 6);
    }

    public void testEquals() {
    assertEquals(12, 12);
    assertEquals(12L, 12L);
    assertEquals(new Long(12), new Long(12));

    assertEquals("Size", 12, 13);
    assertEquals("Capacity", 12.0, 11.99, 0.0);
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }
*/
}
