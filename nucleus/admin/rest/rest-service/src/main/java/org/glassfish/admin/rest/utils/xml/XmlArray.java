/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.utils.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author jasonlee
 */
public class XmlArray extends XmlObject {
    private List<XmlObject> elements = new ArrayList<XmlObject>();

    public XmlArray(String name) {
        super(name);
    }

    public XmlArray(String name, List<XmlObject> elements) {
        super(name);
        this.elements = elements;
    }

    public XmlArray put(XmlObject obj) {
        elements.add(obj);
        return this;
    }

    @Override
    Node createNode(Document document) {
        Node listNode = document.createElement("list");

        for (XmlObject element : elements) {
            listNode.appendChild(element.createNode(document));
        }

        return listNode;
    }
}
