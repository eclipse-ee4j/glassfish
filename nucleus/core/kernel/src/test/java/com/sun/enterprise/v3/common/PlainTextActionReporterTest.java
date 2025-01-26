/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.glassfish.api.ActionReport;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author bnevins
 */
public class PlainTextActionReporterTest {

    @Test
    public void failureTest() throws Exception {
        ActionReport report = new PlainTextActionReporter();
        report.setActionDescription("My Action Description");
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        ActionReport.MessagePart top = report.getTopMessagePart();
        top.setMessage("FailureTest Message Here!!");
        report.setFailureCause(new IndexOutOfBoundsException("Hi I am a phony Exception!!"));
        String text = printReport(report);
        assertThat(text, equalTo("PlainTextActionReporterFAILURE"
            + "java.lang.IndexOutOfBoundsException: Hi I am a phony Exception!!FailureTest Message Here!!"));
    }

    @Test
    public void babyTest() throws Exception {
        ActionReport report = new PlainTextActionReporter();
        report.setActionDescription("My Action Description");
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        ActionReport.MessagePart top = report.getTopMessagePart();
        top.setMessage("BabyTest Message Here!!");
        String text = printReport(report);
        assertThat(text, stringContainsInOrder("PlainTextActionReporter", "SUCCESS", "BabyTest Message Here!!"));
    }

    @Test
    public void mamaTest() throws Exception {
        ActionReport report = new PlainTextActionReporter();
        report.setActionDescription("My Action Description");
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        ActionReport.MessagePart top = report.getTopMessagePart();
        top.setMessage("Mama Test Top Message");
        top.setChildrenType("Module");

        for(int i = 0; i < 8; i++) {
            ActionReport.MessagePart childPart = top.addChild();
            childPart.setMessage("child" + i + " Message here");
            childPart.addProperty("ChildKey" + i, "ChildValue" + i);
            childPart.addProperty("AnotherChildKey" + i, "AnotherChildValue" + i);

            ActionReport.MessagePart grandkids = childPart.addChild();
            grandkids.setMessage("Grand Kids #" + i + " Top Message");
        }
        String text = printReport(report);
        assertThat(text,
            stringContainsInOrder("PlainTextActionReporter", "SUCCESS", "Description", "My Action Description",
                "Mama Test Top Message", "child7", "[ChildKey7=ChildValue7", "Grand Kids #7 Top Message"));
    }

    @Test
    public void papaTest() throws Exception {
        ActionReport report = new PlainTextActionReporter();
        report.setActionDescription("My Action Description");
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        ActionReport.MessagePart top = report.getTopMessagePart();
        top.setMessage("Papa Test Top Message");
        top.setChildrenType("Module");

        for(int i = 0; i < 8; i++) {
            ActionReport.MessagePart childPart = top.addChild();
            childPart.setMessage("child" + i + " Message here");
            childPart.addProperty("ChildKey" + i, "ChildValue" + i);
            childPart.addProperty("AnotherChildKey" + i, "AnotherChildValue" + i);

            for(int j = 0; j < 3; j++) {
                ActionReport.MessagePart grandkids = childPart.addChild();
                grandkids.setMessage("Grand Kid#" + j + " from child#" + i + " Top Message");
                grandkids.addProperty("Grand Kid#" + j + " from child#" + i + "key", "value");
            }
        }
        String text = printReport(report);
        assertThat(text,
            stringContainsInOrder("PlainTextActionReporter", "SUCCESS", "Description", "My Action Description",
                "Papa Test Top Message", "child7", "[ChildKey7=ChildValue7", "[Grand Kid#2 from child#7key=value"));
    }

    @Test
    public void aggregateTest() {
        ActionReporter successfulRoot = new PlainTextActionReporter();
        assertTrue(successfulRoot.hasSuccesses());
        assertFalse(successfulRoot.hasFailures());
        assertFalse(successfulRoot.hasWarnings());
        ActionReport failedChild = successfulRoot.addSubActionsReport();
        failedChild.setActionExitCode(ActionReport.ExitCode.FAILURE);
        assertTrue(successfulRoot.hasSuccesses());
        assertTrue(successfulRoot.hasFailures());
        assertFalse(successfulRoot.hasWarnings());
        assertFalse(failedChild.hasSuccesses());
        assertFalse(failedChild.hasWarnings());
        assertTrue(failedChild.hasFailures());

        ActionReport warningChild = failedChild.addSubActionsReport();
        warningChild.setActionExitCode(ActionReport.ExitCode.WARNING);
        assertTrue(successfulRoot.hasSuccesses());
        assertTrue(successfulRoot.hasFailures());
        assertTrue(successfulRoot.hasWarnings());
        assertFalse(failedChild.hasSuccesses());
        assertTrue(failedChild.hasWarnings());
        assertTrue(failedChild.hasFailures());
        assertTrue(warningChild.hasWarnings());
        assertFalse(warningChild.hasSuccesses());

        ActionReport successfulChild = warningChild.addSubActionsReport();
        assertTrue(successfulChild.hasSuccesses());
        assertTrue(failedChild.hasSuccesses());
        assertTrue(warningChild.hasSuccesses());
        assertFalse(warningChild.hasFailures());

        StringBuilder sb = new StringBuilder();
        successfulRoot.setMessage("sr");
        successfulRoot.getCombinedMessages(successfulRoot, sb);
        assertEquals("sr", sb.toString());
        warningChild.setMessage("wc");
        sb = new StringBuilder();
        successfulRoot.getCombinedMessages(successfulRoot, sb);
        assertEquals("sr\nwc", sb.toString());
        failedChild.setMessage("fc");
        sb = new StringBuilder();
        successfulRoot.getCombinedMessages(successfulRoot, sb);
        assertEquals("sr\nfc\nwc", sb.toString());
    }


    private String printReport(ActionReport report) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PrintStream stream = new PrintStream(outputStream)) {
            report.writeReport(stream);
        }
        return outputStream.toString(UTF_8);
    }
}
