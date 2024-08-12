/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.v3.common.ActionReporter;

import jakarta.ws.rs.core.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.glassfish.admin.rest.provider.ActionReportJson2Provider;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.MessagePart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author mmares
 */
public class ActionReportTest {
    private final ActionReportJson2Provider provider = new ActionReportJson2Provider();

    private String marshall(RestActionReporter ar) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        provider.writeTo(ar, ar.getClass(), ActionReporter.class, null, new MediaType("application", "actionreport"), null, baos);
        return baos.toString("UTF-8");
    }

    private String basicMarshallingTest(RestActionReporter ar) throws IOException {
        String str = marshall(ar);
        assertNotNull(str);
        assertFalse(str.isEmpty());
        //System.out.println(str);
        return str;
    }

    @Test
    public void actionReportMarshallingTest() throws IOException {
        RestActionReporter ar = new RestActionReporter();
        ar.setActionDescription("Some description");
        ar.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        ar.setExtraProperties(null);
        basicMarshallingTest(ar);
        ar.getTopMessagePart().setMessage("First message in First report");
        basicMarshallingTest(ar);
        ar.getTopMessagePart().addProperty("AR1-MSG1-PROP1", "1.1.1.");
        basicMarshallingTest(ar);
        ar.getTopMessagePart().addProperty("AR1-MSG1-PROP2", "1.1.2.");
        basicMarshallingTest(ar);
        MessagePart part1 = ar.getTopMessagePart().addChild();
        basicMarshallingTest(ar);
        part1.setMessage("Second message in First report");
        basicMarshallingTest(ar);
        part1.addProperty("AR1-MSG2-PROP1", "1.2.1.");
        part1.addProperty("AR1-MSG2-PROP2", "1.2.2.");
        basicMarshallingTest(ar);
        MessagePart part2 = part1.addChild();
        part2.setMessage("Third message in First report");
        part2.addProperty("AR1-MSG3-PROP1", "1.3.1.");
        part2.addProperty("AR1-MSG3-PROP2", "1.3.2.");
        basicMarshallingTest(ar);
        MessagePart part3 = ar.getTopMessagePart().addChild();
        part3.setMessage("Fourth message in First report");
        part3.addProperty("AR1-MSG4-PROP1", "1.4.1.");
        part3.addProperty("AR1-MSG4-PROP2", "1.4.2.");
        basicMarshallingTest(ar);
        Properties extra = new Properties();
        extra.setProperty("EP1-PROP1", "1.1");
        extra.setProperty("EP1-PROP2", "1.2");
        ar.setExtraProperties(extra);
        ActionReport ar2 = ar.addSubActionsReport();
        ar2.setActionExitCode(ActionReport.ExitCode.WARNING);
        ar2.setActionDescription("Description 2");
        ar2.getTopMessagePart().setMessage("First Message in Second Report");
        MessagePart subPart2 = ar2.getTopMessagePart().addChild();
        subPart2.addProperty("AR2-MSG2-PROP1", "2.2.1.");
        subPart2.setMessage("Second Message in Second Report");
        basicMarshallingTest(ar);
        ActionReport ar3 = ar.addSubActionsReport();
        ar3.setActionExitCode(ActionReport.ExitCode.FAILURE);
        ar3.setActionDescription("Description 3");
        ar3.setFailureCause(new Exception("Some exception message"));
        basicMarshallingTest(ar);
    }

}
