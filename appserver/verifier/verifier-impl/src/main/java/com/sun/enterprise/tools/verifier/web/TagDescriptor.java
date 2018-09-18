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

package com.sun.enterprise.tools.verifier.web;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * This is the descriptor class for the tag element of the taglib root element
 * for a .tld file used in a jsp.
 */
public class TagDescriptor {
    Node node;
    public static final String TAG_CLASS_NAME = "tag-class"; // NOI18N
    public static final String TAG_CLASS_1_1_NAME = "tagclass"; // NOI18N
    public static final String DYNAMIC_ATTRIB = "dynamic-attributes"; // NOI18N
    public static final String _FALSE = "false"; // NOI18N
    public static final String TAG_NAME = "name"; // NOI18N

    public TagDescriptor (Node n) {
        this.node=n;
    }

    /**
     * <tag-class>className<tag-class>
     * @return class name as String
     */
    public String getTagClass() {
        NodeList n1 = node.getChildNodes();
        String className = null;
        for (int k = 0; k < n1.getLength(); k++) {
            String name = n1.item(k).getNodeName();
            if (name == TAG_CLASS_NAME || name == TAG_CLASS_1_1_NAME) {
                className = n1.item(k).getFirstChild().getNodeValue();
            }
        }
        return className;
    }

    /**
     *
     * @return dynamic-attributes value defined for a tag
     */
    public String getDynamicAttributes() {
        NodeList n1 = node.getChildNodes();
        String dynAttr = _FALSE;
        for (int k = 0; k < n1.getLength(); k++) {
            String name = n1.item(k).getNodeName();
            if (name == DYNAMIC_ATTRIB) {
                dynAttr = n1.item(k).getFirstChild().getNodeValue();
            }
        }
        return dynAttr;
    }

    /**
     *
     * @return name of the tag
     */
    public String getTagName() {
        NodeList n1 = node.getChildNodes();
        String tagName = null;
        for (int k = 0; k < n1.getLength(); k++) {
            String name = n1.item(k).getNodeName();
            if (name == TAG_NAME) {
                 tagName = n1.item(k).getFirstChild().getNodeValue();
            }
        }
        return tagName;
    }
}
