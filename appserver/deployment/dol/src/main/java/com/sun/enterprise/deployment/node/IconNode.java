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
 * IconNode.java
 *
 * Created on August 19, 2002, 9:55 AM
 */

package com.sun.enterprise.deployment.node;

import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.xml.TagNames;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author  dochez
 */
public class IconNode extends LocalizedNode {

    private String smallIcon = null;
    private String largeIcon = null;

    /**
     * @return the descriptor for this node
     */
    public Object getDescriptor() {
        return null;
    }

    /**
     * receives notification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        if (element.getQName().equals(TagNames.SMALL_ICON)) {
            smallIcon = value;
        } else
        if (element.getQName().equals(TagNames.LARGE_ICON)) {
            largeIcon = value;
        }
    }

    /**
     * notification of the end of XML parsing for this node
     */
    public void postParsing() {
        Object o = getParentNode().getDescriptor();
        if (o!=null && o instanceof Descriptor) {
            Descriptor descriptor = (Descriptor) o;
            if (largeIcon!=null) {
                descriptor.setLocalizedLargeIconUri(lang, largeIcon);
            }
            if (smallIcon!=null) {
                descriptor.setLocalizedSmallIconUri(lang, smallIcon);
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
        Map largeIcons = descriptor.getLocalizedLargeIconUris();
        Map smallIcons = descriptor.getLocalizedSmallIconUris();
        if (largeIcons==null && smallIcons==null) {
            return;
        }
        if (smallIcons!=null) {
            Set<Map.Entry> entrySet = smallIcons.entrySet();
            Iterator<Map.Entry> entryIt = entrySet.iterator();
            while (entryIt.hasNext()) {
                Map.Entry entry = entryIt.next();
                String lang = (String)entry.getKey();
                String smallIconUri = (String)entry.getValue();
                String largeIconUri = null;
                if (largeIcons!=null) {
                    largeIconUri = (String) largeIcons.get(lang);
                }
                addIconInfo(parentNode, lang, smallIconUri, largeIconUri);
            }
        }
        if (largeIcons!=null) {
            Set<Map.Entry> entrySet = largeIcons.entrySet();
            Iterator<Map.Entry> entryIt = entrySet.iterator();
            while (entryIt.hasNext()) {
                Map.Entry entry = entryIt.next();
                String lang = (String)entry.getKey();
                String largeIconUri = (String)entry.getValue();
                if (smallIcons!=null && smallIcons.get(lang)!=null) {
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

        Element iconNode =appendChild(node, TagNames.ICON);
        if (Locale.ENGLISH.getLanguage().equals(lang)) {
        iconNode.setAttributeNS(TagNames.XML_NAMESPACE, TagNames.XML_NAMESPACE_PREFIX + TagNames.LANG, lang);
    }
        appendTextChild(iconNode, TagNames.SMALL_ICON, smallIconUri);
        appendTextChild(iconNode, TagNames.LARGE_ICON, largeIconUri);
    }
}
