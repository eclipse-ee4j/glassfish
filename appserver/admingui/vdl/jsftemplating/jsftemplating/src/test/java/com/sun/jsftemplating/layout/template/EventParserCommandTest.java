/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.jsftemplating.layout.template;

import com.sun.jsftemplating.ContextMocker;
import com.sun.jsftemplating.layout.descriptors.LayoutDefinition;
import com.sun.jsftemplating.layout.descriptors.handler.Handler;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 *  <p>    Tests for the {@link EventParserCommand}.</p>
 */
public class EventParserCommandTest {

    @Before
    public void init() {
        ContextMocker.init();
    }
    /**
     *    <p> Simple test to ensure we can read a template.</p>
     * @throws Exception
     */
    @Test
    public void testHandlerParsing() throws Exception {
        // Create element to contain handlers...
        LayoutDefinition elt = new LayoutDefinition("top");

        // Setup the parser...
        TemplateParser parser = new TemplateParser(new ByteArrayInputStream(HANDLERS1));
        parser.open();  // Needed to initialize things.

        // Setup the reader...
        TemplateReader reader = new TemplateReader("foo", parser);
        reader.pushTag("event"); // The tag will be popped at the end

        // Read the handlers...
        command.process(bpc, new ProcessingContextEnvironment(reader, elt, true), "beforeEncode");

        // Clean up
        parser.close();

        // Test to see if things worked...
        Assert.assertEquals("component.id", "top", elt.getUnevaluatedId());
        List<Handler> handlers = elt.getHandlers("beforeEncode", null);
        Assert.assertEquals("handler.size", 3, handlers.size());
        Assert.assertEquals("handler.name1", "println", handlers.get(0).getHandlerDefinition().getId());
        Assert.assertEquals("handler.name2", "setAttribute", handlers.get(1).getHandlerDefinition().getId());
        Assert.assertEquals("handler.name3", "printStackTrace", handlers.get(2).getHandlerDefinition().getId());
        Assert.assertEquals("handler.valueInput1", "This is a test!", handlers.get(0).getInputValue("value"));
        Assert.assertEquals("handler.keyInput2", "foo", handlers.get(1).getInputValue("key"));
        Assert.assertEquals("handler.valueInput2", "bar", handlers.get(1).getInputValue("value"));
        Assert.assertEquals("handler.msgInput3", "test", handlers.get(2).getInputValue("msg"));
        Assert.assertEquals("handler.stOutput3.name", "stackTrace", handlers.get(2).getOutputValue("stackTrace").getOutputName());
        Assert.assertEquals("handler.stOutput3.type",
        "com.sun.jsftemplating.layout.descriptors.handler.RequestAttributeOutputType",
        handlers.get(2).getOutputValue("stackTrace").getOutputType().getClass().getName());
        Assert.assertEquals("handler.stOutput3.mappedName", "st", handlers.get(2).getOutputValue("stackTrace").getOutputKey());
    }

    @Test
    public void testHandlerParsing2() throws Exception {
        // Create element to contain handlers...
        LayoutDefinition elt = new LayoutDefinition("newTop");

        // Setup the parser...
        TemplateParser parser = new TemplateParser(new ByteArrayInputStream(HANDLERS2));
        parser.open();  // Needed to initialize things.

        // Setup the reader...
        TemplateReader reader = new TemplateReader("foo", parser);
        reader.pushTag("event"); // The tag will be popped at the end

        // Read the handlers...
        command.process(bpc, new ProcessingContextEnvironment(reader, elt, true), "beforeEncode");

        // Clean up
        parser.close();

        // Test to see if things worked...
        Assert.assertEquals("component.id", "newTop", elt.getUnevaluatedId());

        List<Handler> handlers = elt.getHandlers("beforeEncode", null);
        Assert.assertEquals("handler.size", 1, handlers.size());
        Assert.assertEquals("handler.valueInput1", "test", handlers.get(0).getInputValue("value"));
    }

    private static final byte[] HANDLERS1   = (
    "\nprintln(\"This is a test!\");\n"
    + "setAttribute(key='foo' value='bar');"
    + "printStackTrace('test', stackTrace=>$attribute{st});/>").getBytes();

    private static final byte[] HANDLERS2   = "println('test');/>".getBytes();

    /**
     *    <p> This class should be re-usable and thread-safe.</p>
     */
    EventParserCommand command = new EventParserCommand();
    BaseProcessingContext bpc = new BaseProcessingContext(); // Not used
}
