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

/*
 * LocalizedNode.java
 *
 * Created on August 16, 2002, 4:01 PM
 */

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.xml.TagNames;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * This class is responsible for handling the xml lang attribute of
 * an xml element
 *
 * @author Jerome Dochez
 */
public class LocalizedNode extends DeploymentDescriptorNode {

    protected String lang = null;
    protected String localizedValue = null;

    /**
     * @return the descriptor for this node
     */
    @Override
    public Object getDescriptor() {
        return getParentNode().getDescriptor();
    }

    /**
     * notification of element start with attributes.
     */
    @Override
    public void startElement(XMLElement element, Attributes attributes) {
        if (attributes.getLength() > 0) {
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.getLocalName(i).equals(TagNames.LANG)) {
                    lang = attributes.getValue(i);
                }
            }
        }
    }

    /**
     * receives notification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    @Override
    public void setElementValue(XMLElement element, String value) {
        if (element.equals(getXMLRootTag())) {
            localizedValue=value;
        } else {
            super.setElementValue(element, value);
        }
    }

    /**
     * writes all the localized map element usign the tagname with
     * the lang attribute to a DOM node
     */
    public void writeLocalizedMap(Node parentNode, String tagName, Map localizedMap) {
        if (localizedMap != null) {
            Set<Map.Entry> entrySet = localizedMap.entrySet();
            Iterator<Map.Entry> entryIt = entrySet.iterator();
            while (entryIt.hasNext()) {
                Map.Entry entry = entryIt.next();
                String lang = (String) entry.getKey();
                Element aLocalizedNode = (Element) appendTextChild(parentNode, tagName, (String) entry.getValue());
                if (aLocalizedNode != null && Locale.getDefault().getLanguage().equals(lang)) {
                    aLocalizedNode.setAttributeNS(
                        TagNames.XML_NAMESPACE,
                        TagNames.XML_NAMESPACE_PREFIX + TagNames.LANG,
                        lang);
                }
            }
        }
    }

}
