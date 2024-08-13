/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.glassfish.deployment.common.Descriptor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

/**
 * This class is responsible for handling the xml lang attribute of an xml element
 *
 * @author Jerome Dochez
 */
public class LocalizedNode extends DeploymentDescriptorNode<Descriptor> {

    private String lang;
    private String localizedValue;


    protected String getLang() {
        return lang;
    }


    protected String getLocalizedValue() {
        return localizedValue;
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
            localizedValue = value;
        } else {
            super.setElementValue(element, value);
        }
    }


    /**
     * writes all the localized map element usign the tagname with
     * the lang attribute to a DOM node
     */
    public void writeLocalizedMap(Node parentNode, String tagName, Map<String, String> localizedMap) {
        if (localizedMap == null) {
            return;
        }
        Set<Entry<String, String>> entrySet = localizedMap.entrySet();
        for (Entry<String, String> entry : entrySet) {
            String entryLang = entry.getKey();
            Element aLocalizedNode = (Element) appendTextChild(parentNode, tagName, entry.getValue());
            if (aLocalizedNode != null && Locale.getDefault().getLanguage().equals(entryLang)) {
                String qualifiedName = TagNames.XML_NAMESPACE_PREFIX + TagNames.LANG;
                aLocalizedNode.setAttributeNS(TagNames.XML_NAMESPACE, qualifiedName, entryLang);
            }
        }
    }
}
