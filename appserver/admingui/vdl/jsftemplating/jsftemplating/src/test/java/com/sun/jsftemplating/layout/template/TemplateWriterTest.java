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

package com.sun.jsftemplating.layout.template;

import com.sun.jsftemplating.ContextMocker;
import com.sun.jsftemplating.layout.descriptors.LayoutDefinition;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *  <p>    Tests for the {@link TemplateWriter}.</p>
 */
public class TemplateWriterTest {
    /**
     *    <p> </p>
     */
  private final ClassLoader cl = TemplateWriterTest.class.getClassLoader();

  @Before
  public void init(){
    ContextMocker.init();
  }

    @Test
    public void testWrite1() {
    try {
        // First read some data
        TemplateReader reader =
        new TemplateReader("foo", cl.getResource("./TemplateFormat.jsf"));
        LayoutDefinition ld = reader.read();
//        assertEquals("LayoutDefinition.unevaluatedId", "id2", ld.getUnevaluatedId());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        TemplateWriter writer =
        new TemplateWriter(stream);
        writer.write(ld);
// FIXME: Add some sort of check here
//        System.err.println(stream.toString());
    } catch (IOException ex) {
        ex.printStackTrace();
        Assert.fail(ex.getMessage());
    }
    }
}
