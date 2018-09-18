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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.glassfish.admin.rest.client.utils.MarshallingUtils;
import org.glassfish.admin.rest.provider.ActionReportResultHtmlProvider;
import org.glassfish.admin.rest.provider.ActionReportResultJsonProvider;
import org.glassfish.admin.rest.provider.ActionReportResultXmlProvider;
import org.glassfish.admin.rest.results.ActionReportResult;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.ActionReport.MessagePart;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

/**
 *
 * @author jasonlee
 */
public class EncodingTest {
    @Test
    public void encodeAsJson() {
        RestActionReporter ar = buildActionReport();
        ActionReportResultJsonProvider provider = new ActionReportResultJsonProvider();
        ActionReportResult result = new ActionReportResult("test", ar);
        String json = provider.getContent(result);
        Map responseMap = MarshallingUtils.buildMapFromDocument(json);
        assertEquals(7, responseMap.size());
        assertEquals(4, ((Map)responseMap.get("extraProperties")).size());
        assertTrue(responseMap.get("children") instanceof List);
        assertTrue(responseMap.get("subReports") instanceof List);
    }

    @Test
    public void encodeAsXml() {
        RestActionReporter ar = buildActionReport();
        ActionReportResultXmlProvider provider = new ActionReportResultXmlProvider();
        ActionReportResult result = new ActionReportResult("test", ar);
        String xml = provider.getContent(result);

        Map responseMap = MarshallingUtils.buildMapFromDocument(xml);
        assertEquals(7, responseMap.size());
        assertEquals(4, ((Map)responseMap.get("extraProperties")).size());
        assertTrue(responseMap.get("children") instanceof List);
        assertTrue(responseMap.get("subReports") instanceof List);
    }

    @Test
    public void encodeAsHtml() {
        RestActionReporter ar = buildActionReport();
        ActionReportResultHtmlProvider provider = new ActionReportResultHtmlProvider();
        ActionReportResult result = new ActionReportResult("test", ar);
        String html = provider.getContent(result);
        // How to test this?
    }

    private RestActionReporter buildActionReport() {
        RestActionReporter ar = new RestActionReporter();
        ar.setActionDescription("test description");
        ar.setActionExitCode(ExitCode.SUCCESS);
        ar.setMessage("test message");

        // top message properties
        ar.getTopMessagePart().getProps().put("property1", "value1");
        ar.getTopMessagePart().getProps().put("property2", "value2");

        // extra properties
        Properties props = new Properties();
        props.put("test1", new ArrayList(){{
            add("value1");
            add("value2");
        }});
        props.put("test2", new ArrayList(){{
            add("value1");
            add(new HashMap() {{
                put("entry1", "value1");
                put("entry2", new Long(1000));
                put("entry3", new HashMap() {{
                    put ("foo", new ArrayList() {{
                        add ("bar");
                        add (new BigDecimal(1000));
                    }});
                }});
            }});
        }});
        props.put("test3", new BigInteger("2100"));
        props.put("test4", "A String property");
        ar.setExtraProperties(props);

        // child parts
        MessagePart child1 = ar.getTopMessagePart().addChild();
        child1.setMessage("child 1 message");
        child1.getProps().put("child1 prop1", "child1 value1");
        child1.getProps().put("child1 prop2", "child1 value2");

        MessagePart child2 = ar.getTopMessagePart().addChild();
        child2.setMessage("child 2 message");
        child2.getProps().put("child2 prop1", "child2 value1");
        child2.getProps().put("child2 prop2", "child2 value2");

        MessagePart grandChild1 = child2.addChild();
        grandChild1.setMessage("grand child 1 message");
        grandChild1.getProps().put("gc1 prop1", "gc1 value1");
        grandChild1.getProps().put("gc1 prop2", "gc1 value2");

        // sub reports
        ActionReport subReport1 = ar.addSubActionsReport();
        subReport1.setActionDescription("sub report 1");
        subReport1.setMessage("sub report 1 message");

        ActionReport subReport2 = ar.addSubActionsReport();
        subReport2.setActionDescription("sub report 2");
        subReport2.setMessage("sub report 2 message");

        return ar;
    }
}
