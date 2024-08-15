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
import com.sun.enterprise.deployment.runtime.web.IdempotentUrlPattern;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import org.w3c.dom.Element;
import org.w3c.dom.Node;



/**
* node to handle idempotent-url-pattern node
*/
public class IdempotentUrlPatternNode extends RuntimeDescriptorNode<IdempotentUrlPattern> {

    protected IdempotentUrlPattern descriptor = null;

    /**
    * @return the descriptor instance to associate with this XMLNode
    */
    @Override
    public IdempotentUrlPattern getDescriptor() {
        if (descriptor == null) {
            descriptor = new IdempotentUrlPattern();
        }
        return descriptor;
    }

    /**
     * parsed an attribute of an element
     *
     * @param elementName the element name
     * @param attributeName the attribute name
     * @param value the attribute value
     * @return true if the attribute was processed
     */
    @Override
    protected boolean setAttributeValue(XMLElement elementName, XMLElement attributeName, String value) {
        if (attributeName.getQName().equals(RuntimeTagNames.URL_PATTERN)) {
            descriptor.setAttributeValue(IdempotentUrlPattern.URL_PATTERN,
                value);
            return true;
        } else if (attributeName.getQName().equals(
            RuntimeTagNames.NUM_OF_RETRIES)) {
            descriptor.setAttributeValue(IdempotentUrlPattern.NUM_OF_RETRIES,
                value);
            return true;
        }
        return false;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName node name for the descriptor
     * @param pattern the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName,
       IdempotentUrlPattern pattern) {
       Element patternNode =
            (Element)super.writeDescriptor(parent, nodeName, pattern);

        // url-pattern
        if (pattern.getAttributeValue(pattern.URL_PATTERN) != null) {
            setAttribute(patternNode, RuntimeTagNames.URL_PATTERN, pattern.getAttributeValue(pattern.URL_PATTERN));
        }

        // num-of-retries
        if (pattern.getAttributeValue(pattern.NUM_OF_RETRIES) != null) {
            setAttribute(patternNode, RuntimeTagNames.NUM_OF_RETRIES, pattern.getAttributeValue(pattern.NUM_OF_RETRIES));
        }

        return patternNode;
    }
}

