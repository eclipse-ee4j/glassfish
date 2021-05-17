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

package com.sun.enterprise.deployment;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.DynamicAttributesDescriptor;

import java.util.*;

/**
 * This class contains the deployment extensions element for a particular
 * xml node. It can contains sub elements (other ExtensionElementDescriptor
 * instances) or final leafs like attribute or string elements.
 *
 * @author Jerome Dochez
 */
public class ExtensionElementDescriptor extends Descriptor implements Observer {

    private List elementNames;
    private Map elementValues;
    private DynamicAttributesDescriptor attributes;

    /**
     * @return the value holder for all sub elements of
     * this deployment extension element
     */
    public Iterator getElementNames() {
        if (elementNames!=null) {
            return elementNames.iterator();
        }
        return null;
    }

    public void addElement(String elementName, Object value) {
        if (elementNames==null) {
            elementNames = new LinkedList();
            elementValues = new HashMap();
        }
        elementNames.add(elementName);
        elementValues.put(elementName, value);
    }

    public Object getElement(String elementName) {
        if (elementValues!=null) {
            return elementValues.get(elementName);
        }
        return null;
    }

    /**
     * @return a value holder for all attributes of
     * this deployment extension elements
     */
    public DynamicAttributesDescriptor getAttributes() {
        if (attributes==null) {
            attributes = new DynamicAttributesDescriptor();
            attributes.addObserver(this);
        }
        return attributes;
    }

    /**
     * @return true if the deployment extension contains attributes
     */
    public boolean hasAttributes() {
        return attributes!=null;
    }

    /**
     * notification of changed from our attributes/elements
     * storage
     */
    public void update(Observable o, Object arg) {
        setChanged();
        notifyObservers();
    }

    /**
     * @return a meaningful string describing myself
     */
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("ExtensionElementDescriptor");
        toStringBuffer.append("\n");
        super.print(toStringBuffer);
        for (Iterator itr = getElementNames();itr.hasNext();) {
            toStringBuffer.append("\n  Element=").append(getElement((String) itr.next()));
        }
        if (hasAttributes()) {
            toStringBuffer.append("\n  Attributes = ").append(getAttributes());
        }
    }
}
