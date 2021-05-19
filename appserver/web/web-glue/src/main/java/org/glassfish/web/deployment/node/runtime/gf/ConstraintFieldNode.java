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

package org.glassfish.web.deployment.node.runtime.gf;

import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import java.util.Map;

import org.glassfish.web.deployment.runtime.ConstraintField;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

/**
* node for cache-mapping tag
*
* @author Jerome Dochez
*/
public class ConstraintFieldNode extends RuntimeDescriptorNode<ConstraintField> {

    protected ConstraintField descriptor = null;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public ConstraintField getDescriptor() {
        if (descriptor==null) {
            descriptor = new ConstraintField();
        }
        return descriptor;
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> dispatchTable = super.getDispatchTable();
        // for backward compatibility with S1AS 7 dtd
        dispatchTable.put(RuntimeTagNames.VALUE, "addValue");
        dispatchTable.put(RuntimeTagNames.CONSTRAINT_FIELD_VALUE, "addValue");
        return dispatchTable;
    }

    @Override
    public void startElement(XMLElement element, Attributes attributes) {
        if (element.getQName().equals(RuntimeTagNames.CONSTRAINT_FIELD)) {
            ConstraintField descriptor = getDescriptor();
            for (int i=0; i<attributes.getLength();i++) {
                if (RuntimeTagNames.NAME.equals(attributes.getQName(i))) {
                    descriptor.setAttributeValue(ConstraintField.NAME, attributes.getValue(i));
                } else if (RuntimeTagNames.SCOPE.equals(attributes.getQName(i))) {
                    descriptor.setAttributeValue(ConstraintField.SCOPE, attributes.getValue(i));
                } else if (RuntimeTagNames.CACHE_ON_MATCH.equals(attributes.getQName(i))) {
                    descriptor.setAttributeValue(ConstraintField.CACHE_ON_MATCH, attributes.getValue(i));
                } else if (RuntimeTagNames.CACHE_ON_MATCH_FAILURE.equals(attributes.getQName(i))) {
                    descriptor.setAttributeValue(ConstraintField.CACHE_ON_MATCH_FAILURE, attributes.getValue(i));
                }
            }
            // From sun-web-app_2_3-0.dtd to sun-web-app_2_4-0.dtd,
            // the element name "value" is changed to "constraint-field-value",
            // need to make sure both will work
        } else if (element.getQName().equals(RuntimeTagNames.VALUE)
            || element.getQName().equals(RuntimeTagNames.CONSTRAINT_FIELD_VALUE)) {
            ConstraintField descriptor = getDescriptor();
            int index = descriptor.sizeValue();
            for (int i = 0; i < attributes.getLength(); i++) {
                if (RuntimeTagNames.MATCH_EXPR.equals(attributes.getQName(i))) {
                    descriptor.setAttributeValue(ConstraintField.VALUE, index, ConstraintField.MATCH_EXPR,
                        attributes.getValue(i));
                } else if (RuntimeTagNames.CACHE_ON_MATCH.equals(attributes.getQName(i))) {
                    descriptor.setAttributeValue(ConstraintField.VALUE, index, ConstraintField.CACHE_ON_MATCH,
                        attributes.getValue(i));
                } else if (RuntimeTagNames.CACHE_ON_MATCH_FAILURE.equals(attributes.getQName(i))) {
                    descriptor.setAttributeValue(ConstraintField.VALUE, index, ConstraintField.CACHE_ON_MATCH_FAILURE,
                        attributes.getValue(i));
                }
            }
        } else {
            super.startElement(element, attributes);
        }
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name
     * @param the array of descriptor to write
     * @return the DOM tree top node
     */
    public void writeDescriptor(Node parent, String nodeName, ConstraintField[] descriptors) {
        for (int i=0;i<descriptors.length;i++) {
            writeDescriptor(parent, nodeName, descriptors[i]);
        }
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName node name
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, ConstraintField descriptor) {

        Element constraintField = (Element) super.writeDescriptor(parent, nodeName, descriptor);

        // value*
        String[] values = descriptor.getValue();
        for (int i=0;i<values.length;i++) {
            Element value = (Element) appendTextChild(constraintField, RuntimeTagNames.CONSTRAINT_FIELD_VALUE, values[i]);
            setAttribute(value, RuntimeTagNames.MATCH_EXPR, descriptor.getAttributeValue(ConstraintField.VALUE, i, ConstraintField.MATCH_EXPR));
            setAttribute(value, RuntimeTagNames.CACHE_ON_MATCH, descriptor.getAttributeValue(ConstraintField.VALUE, i, ConstraintField.CACHE_ON_MATCH));
            setAttribute(value, RuntimeTagNames.CACHE_ON_MATCH_FAILURE, descriptor.getAttributeValue(ConstraintField.VALUE, i, ConstraintField.CACHE_ON_MATCH_FAILURE));

        }
        // name, scope, cache-on-match, cache-on-match-failure attributes
        setAttribute(constraintField, RuntimeTagNames.NAME, descriptor.getAttributeValue(ConstraintField.NAME));
        setAttribute(constraintField, RuntimeTagNames.SCOPE, descriptor.getAttributeValue(ConstraintField.SCOPE));
        setAttribute(constraintField, RuntimeTagNames.CACHE_ON_MATCH, descriptor.getAttributeValue(ConstraintField.CACHE_ON_MATCH));
        setAttribute(constraintField, RuntimeTagNames.CACHE_ON_MATCH_FAILURE, descriptor.getAttributeValue(ConstraintField.CACHE_ON_MATCH_FAILURE));

        return constraintField;
    }
}
