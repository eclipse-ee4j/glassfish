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
import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.TagNames;

import org.glassfish.web.deployment.runtime.LocaleCharsetMap;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
/**
* node for web property tag
*
* @author Jerome Dochez
*/
public class LocaleCharsetMapNode extends RuntimeDescriptorNode<LocaleCharsetMap> {

    protected LocaleCharsetMap descriptor = null;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public LocaleCharsetMap getDescriptor() {
        if (descriptor == null) {
            descriptor = new LocaleCharsetMap();
        }
        return descriptor;
    }


    /**
     * receives notification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    @Override
    public void setElementValue(XMLElement element, String value) {
        RuntimeDescriptor descriptor = getDescriptor();
        if (element.getQName().equals(RuntimeTagNames.LOCALE)) {
            descriptor.setAttributeValue(LocaleCharsetMap.LOCALE, value);
        } else if (element.getQName().equals(RuntimeTagNames.AGENT)) {
            descriptor.setAttributeValue(LocaleCharsetMap.AGENT, value);
        }
        if (element.getQName().equals(RuntimeTagNames.CHARSET)) {
            descriptor.setAttributeValue(LocaleCharsetMap.CHARSET, value);
        }
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName node name for the descriptor
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, LocaleCharsetMap descriptor) {
        Element locale = (Element) super.writeDescriptor(parent, nodeName, descriptor);

        // description?
        appendTextChild(locale, TagNames.DESCRIPTION, descriptor.getDescription());

        // locale, agent, charset attributes
        setAttribute(locale, RuntimeTagNames.LOCALE, descriptor.getAttributeValue(LocaleCharsetMap.LOCALE));
        setAttribute(locale, RuntimeTagNames.AGENT, descriptor.getAttributeValue(LocaleCharsetMap.AGENT));
        setAttribute(locale, RuntimeTagNames.CHARSET, descriptor.getAttributeValue(LocaleCharsetMap.CHARSET));

        return locale;
    }
}
