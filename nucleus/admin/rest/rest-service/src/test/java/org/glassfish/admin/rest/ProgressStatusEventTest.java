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

package org.glassfish.admin.rest;

import com.sun.enterprise.admin.remote.reader.ProgressStatusEventJsonProprietaryReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.admin.rest.provider.ProgressStatusEventJsonProvider;
import org.glassfish.api.admin.progress.ProgressStatusEvent;
import org.glassfish.api.admin.progress.ProgressStatusEventComplete;
import org.glassfish.api.admin.progress.ProgressStatusEventCreateChild;
import org.glassfish.api.admin.progress.ProgressStatusEventProgress;
import org.glassfish.api.admin.progress.ProgressStatusEventSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author martinmares
 */
public class ProgressStatusEventTest {
    
    private static ProgressStatusEventSet EVENT_SET = new ProgressStatusEventSet("a", 1, null);
    private static ProgressStatusEventProgress EVENT_PROGRESS = new ProgressStatusEventProgress("a", 3, "some message", true);
    private static ProgressStatusEventComplete EVENT_COMPLETE = new ProgressStatusEventComplete("a", "some message");
    private static ProgressStatusEventCreateChild EVENT_CREATE_CHILD = new ProgressStatusEventCreateChild("a", "child", "a.b", 10, 5);
    
    private static ProgressStatusEventJsonProvider writer = new ProgressStatusEventJsonProvider();
    private static ProgressStatusEventJsonProprietaryReader reader = new ProgressStatusEventJsonProprietaryReader();
    
    @Test
    public void testEventSet() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer.writeTo(EVENT_SET, null, null, null, MediaType.APPLICATION_JSON_TYPE, null, baos);
        baos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ProgressStatusEvent event = reader.readFrom(bais, MediaType.APPLICATION_JSON);
        assertEquals(event, EVENT_SET);
    }
    
    @Test
    public void testEventProgress() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer.writeTo(EVENT_PROGRESS, null, null, null, MediaType.APPLICATION_JSON_TYPE, null, baos);
        baos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ProgressStatusEvent event = reader.readFrom(bais, MediaType.APPLICATION_JSON);
        assertEquals(event, EVENT_PROGRESS);
    }
    
    @Test
    public void testEventComplete() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer.writeTo(EVENT_COMPLETE, null, null, null, MediaType.APPLICATION_JSON_TYPE, null, baos);
        baos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ProgressStatusEvent event = reader.readFrom(bais, MediaType.APPLICATION_JSON);
        assertEquals(event, EVENT_COMPLETE);
    }
    
    @Test
    public void testEventCreateChild() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer.writeTo(EVENT_CREATE_CHILD, null, null, null, MediaType.APPLICATION_JSON_TYPE, null, baos);
        baos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ProgressStatusEvent event = reader.readFrom(bais, MediaType.APPLICATION_JSON);
        assertEquals(event, EVENT_CREATE_CHILD);
    }
    
}
