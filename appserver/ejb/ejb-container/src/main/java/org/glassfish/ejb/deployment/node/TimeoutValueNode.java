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


import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.TimeoutValueDescriptor;
import org.w3c.dom.Node;

public class TimeoutValueNode extends DeploymentDescriptorNode<TimeoutValueDescriptor> {

    private static final Map<String, TimeUnit> elementToTimeUnit;
    private static final Map<TimeUnit, String> timeUnitToElement;

    static {

        elementToTimeUnit = new HashMap<String, TimeUnit>();
        elementToTimeUnit.put("Days", TimeUnit.DAYS);
        elementToTimeUnit.put("Hours", TimeUnit.HOURS);
        elementToTimeUnit.put("Minutes", TimeUnit.MINUTES);
        elementToTimeUnit.put("Seconds", TimeUnit.SECONDS);
        elementToTimeUnit.put("Milliseconds", TimeUnit.MILLISECONDS);
        elementToTimeUnit.put("Microseconds", TimeUnit.MICROSECONDS);
        elementToTimeUnit.put("Nanoseconds", TimeUnit.NANOSECONDS);

        timeUnitToElement = new HashMap<TimeUnit, String>();

        Iterator<Map.Entry<String, TimeUnit>> entryIterator = elementToTimeUnit.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, TimeUnit> entry = entryIterator.next();
            timeUnitToElement.put(entry.getValue(),entry.getKey());
        }
    }

    private TimeoutValueDescriptor descriptor = null;

    @Override
    public TimeoutValueDescriptor getDescriptor() {
        if (descriptor == null) descriptor = new TimeoutValueDescriptor();
        return descriptor;
    }

    @Override
    public void setElementValue(XMLElement element, String value) {
        if (EjbTagNames.TIMEOUT_VALUE.equals(element.getQName())) {
            descriptor.setValue(Long.parseLong(value));
        } else if(EjbTagNames.TIMEOUT_UNIT.equals(element.getQName())) {
            descriptor.setUnit(elementToTimeUnit.get(value));
        } else {
            super.setElementValue(element, value);
        }
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, TimeoutValueDescriptor desc) {
        Node timeoutNode = super.writeDescriptor(parent, nodeName, descriptor);
        appendTextChild(timeoutNode, EjbTagNames.TIMEOUT_VALUE, Long.toString(desc.getValue()));
        appendTextChild(timeoutNode, EjbTagNames.TIMEOUT_UNIT, timeUnitToElement.get(desc.getUnit()));
        return timeoutNode;
     }

}
