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

package com.sun.enterprise.v3.admin;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandContextImpl;
import org.junit.Test;

import com.sun.enterprise.v3.common.DoNothingActionReporter;
import com.sun.enterprise.v3.common.PlainTextActionReporter;

public class AdminCommandContextTest {

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        ActionReport report = new PlainTextActionReporter();
        AdminCommandContext context = new AdminCommandContextImpl(null /* logger */, report);
        report.setFailureCause(new RuntimeException("Test"));
        oos.writeObject(context);
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(is);
        AdminCommandContext restored = (AdminCommandContextImpl) ois.readObject();
        assertEquals("failureCause", "Test", restored.getActionReport().getFailureCause().getMessage());
        // context.setPayload
    }

}
