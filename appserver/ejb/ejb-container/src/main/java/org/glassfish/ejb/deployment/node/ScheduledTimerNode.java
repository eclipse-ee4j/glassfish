/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.deployment.node;

import java.util.GregorianCalendar;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.MethodNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.util.DOLUtils;
import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.ScheduledTimerDescriptor;
import org.w3c.dom.Node;

public class ScheduledTimerNode extends DeploymentDescriptorNode<ScheduledTimerDescriptor> {

    private ScheduledTimerDescriptor descriptor;

    public ScheduledTimerNode() {
        super();
        registerElementHandler(new XMLElement(EjbTagNames.TIMEOUT_METHOD), MethodNode.class,
                "setTimeoutMethod");
    }

    @Override
    public ScheduledTimerDescriptor getDescriptor() {
        if (descriptor == null) descriptor = new ScheduledTimerDescriptor();
        return descriptor;
    }

    @Override
    protected Map getDispatchTable() {
        // no need to be synchronized for now
        Map table = super.getDispatchTable();

        table.put(EjbTagNames.TIMER_SECOND, "setSecond");
        table.put(EjbTagNames.TIMER_MINUTE, "setMinute");
        table.put(EjbTagNames.TIMER_HOUR, "setHour");
        table.put(EjbTagNames.TIMER_DAY_OF_MONTH, "setDayOfMonth");
        table.put(EjbTagNames.TIMER_MONTH, "setMonth");
        table.put(EjbTagNames.TIMER_DAY_OF_WEEK, "setDayOfWeek");
        table.put(EjbTagNames.TIMER_YEAR, "setYear");

        table.put(EjbTagNames.TIMER_PERSISTENT, "setPersistent");
        table.put(EjbTagNames.TIMER_INFO,  "setInfo");
        table.put(EjbTagNames.TIMER_TIMEZONE, "setTimezone");


        return table;
    }

    @Override
    public void setElementValue(XMLElement element, String value) {

        if (EjbTagNames.TIMER_START.equals(element.getQName())) {
            try {
                DatatypeFactory dFactory = DatatypeFactory.newInstance();

                XMLGregorianCalendar xmlGreg = dFactory.newXMLGregorianCalendar(value);
                GregorianCalendar cal = xmlGreg.toGregorianCalendar();
                descriptor.setStart(cal.getTime());
            } catch (Exception e) {
                DOLUtils.getDefaultLogger().warning(e.getMessage());
            }

        } else if(EjbTagNames.TIMER_END.equals(element.getQName())) {
            try {
                DatatypeFactory dFactory = DatatypeFactory.newInstance();

                XMLGregorianCalendar xmlGreg = dFactory.newXMLGregorianCalendar(value);
                GregorianCalendar cal = xmlGreg.toGregorianCalendar();
                descriptor.setEnd(cal.getTime());
            } catch (Exception e) {
                DOLUtils.getDefaultLogger().warning(e.getMessage());
            }

        } else {
            super.setElementValue(element, value);
        }

    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, ScheduledTimerDescriptor desc) {
        Node timerNode = super.writeDescriptor(parent, nodeName, descriptor);

        Node scheduleNode = appendChild(timerNode, EjbTagNames.TIMER_SCHEDULE);

        appendTextChild(scheduleNode, EjbTagNames.TIMER_SECOND, desc.getSecond());
        appendTextChild(scheduleNode, EjbTagNames.TIMER_MINUTE, desc.getMinute());
        appendTextChild(scheduleNode, EjbTagNames.TIMER_HOUR, desc.getHour());
        appendTextChild(scheduleNode, EjbTagNames.TIMER_DAY_OF_MONTH, desc.getDayOfMonth());
        appendTextChild(scheduleNode, EjbTagNames.TIMER_MONTH, desc.getMonth());
        appendTextChild(scheduleNode, EjbTagNames.TIMER_DAY_OF_WEEK, desc.getDayOfWeek());
        appendTextChild(scheduleNode, EjbTagNames.TIMER_YEAR, desc.getYear());

        try {
            DatatypeFactory dFactory = DatatypeFactory.newInstance();
            GregorianCalendar cal = new GregorianCalendar();

            if (desc.getStart() != null) {
                cal.setTime(desc.getStart());
                XMLGregorianCalendar xmlGreg = dFactory.newXMLGregorianCalendar(cal);
                appendTextChild(timerNode, EjbTagNames.TIMER_START, xmlGreg.toXMLFormat());
            }

            if (desc.getEnd() != null) {
                cal.setTime(desc.getEnd());
                XMLGregorianCalendar xmlGreg = dFactory.newXMLGregorianCalendar(cal);
                appendTextChild(timerNode, EjbTagNames.TIMER_END, xmlGreg.toXMLFormat());
            }
        } catch (Exception e) {
            DOLUtils.getDefaultLogger().log(Level.WARNING, e.getMessage(), e);
        }

        MethodNode methodNode = new MethodNode();

        methodNode.writeJavaMethodDescriptor(timerNode, EjbTagNames.TIMEOUT_METHOD,
                 desc.getTimeoutMethod());

        appendTextChild(timerNode, EjbTagNames.TIMER_PERSISTENT,
            Boolean.toString(desc.getPersistent()));


        String tz = desc.getTimezone();
        if( tz != null ) {
            appendTextChild(timerNode, EjbTagNames.TIMER_TIMEZONE, tz);
        }

        String info = desc.getInfo();
        if( info != null ) {
            appendTextChild(timerNode, EjbTagNames.TIMER_INFO, info);
        }

        return timerNode;
     }

}
