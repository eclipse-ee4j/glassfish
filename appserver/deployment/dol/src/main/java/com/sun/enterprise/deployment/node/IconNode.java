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

/**
 *
 * @author  dochez
 */
public class IconNode extends LocalizedNode {

    private String smallIcon;
    private String largeIcon;

    /**
     * @return the descriptor for this node
     */
    @Override
    public Descriptor getDescriptor() {
        return null;
    }


    /**
     * receives notification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    @Override
    public void setElementValue(XMLElement element, String value) {
        if (element.getQName().equals(TagNames.SMALL_ICON)) {
            smallIcon = value;
        } else if (element.getQName().equals(TagNames.LARGE_ICON)) {
            largeIcon = value;
        }
    }


    /**
     * notification of the end of XML parsing for this node
     */
    @Override
    public void postParsing() {
        Object o = getParentNode().getDescriptor();
        if (o != null && o instanceof Descriptor) {
            Descriptor descriptor = (Descriptor) o;
            if (largeIcon != null) {
                descriptor.setLocalizedLargeIconUri(getLang(), largeIcon);
            }
            if (smallIcon != null) {
                descriptor.setLocalizedSmallIconUri(getLang(), smallIcon);
            }
        }
    }


    /**
     * writes all localized icon information
     *
     * @param parentNode for all icon xml fragments
     * @param descriptor containing the icon information
     */
    public void writeLocalizedInfo(Node parentNode, Descriptor descriptor) {
        Map<String, String> largeIcons = descriptor.getLocalizedLargeIconUris();
        Map<String, String> smallIcons = descriptor.getLocalizedSmallIconUris();
        if (largeIcons == null && smallIcons == null) {
            return;
        }
        if (smallIcons != null) {
            Set<Entry<String, String>> entrySet = smallIcons.entrySet();
            for (Entry<String, String> entry : entrySet) {
                String lang = entry.getKey();
                String smallIconUri = entry.getValue();
                String largeIconUri = null;
                if (largeIcons != null) {
                    largeIconUri = largeIcons.get(lang);
                }
                addIconInfo(parentNode, lang, smallIconUri, largeIconUri);
            }
        }
        if (largeIcons != null) {
            Set<Entry<String, String>> entrySet = largeIcons.entrySet();
            for (Entry<String, String> entry : entrySet) {
                String lang = entry.getKey();
                String largeIconUri = entry.getValue();
                if (smallIcons != null && smallIcons.get(lang) != null) {
                    // we already wrote this icon info in the previous loop
                    continue;
                }
                addIconInfo(parentNode, lang, null, largeIconUri);
            }
        }

    }


    /**
     * writes xml tag and fragment for a particular icon information
     */
    private void addIconInfo(Node node, String lang, String smallIconUri, String largeIconUri) {
        Element iconNode = appendChild(node, TagNames.ICON);
        if (Locale.ENGLISH.getLanguage().equals(lang)) {
            iconNode.setAttributeNS(TagNames.XML_NAMESPACE, TagNames.XML_NAMESPACE_PREFIX + TagNames.LANG, lang);
        }
        appendTextChild(iconNode, TagNames.SMALL_ICON, smallIconUri);
        appendTextChild(iconNode, TagNames.LARGE_ICON, largeIconUri);
    }
}
