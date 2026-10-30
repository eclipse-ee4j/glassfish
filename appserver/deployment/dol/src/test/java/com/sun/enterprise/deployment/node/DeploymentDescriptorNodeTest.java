/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

import javax.xml.parsers.DocumentBuilderFactory;

import org.glassfish.api.naming.SimpleJndiName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

class DeploymentDescriptorNodeTest {

    @Test
    void skipsEmptySimpleJndiName() throws Exception {
        Element parent = createParentElement();
        var child = DeploymentDescriptorNode.appendTextChild(parent, TagNames.MAPPED_NAME, SimpleJndiName.of(""));

        Assertions.assertNull(child);
        Assertions.assertEquals(0, parent.getElementsByTagName(TagNames.MAPPED_NAME).getLength());
    }

    @Test
    void writesNonEmptySimpleJndiName() throws Exception {
        Element parent = createParentElement();
        var child = DeploymentDescriptorNode.appendTextChild(parent, TagNames.MAPPED_NAME, SimpleJndiName.of("java:global/probe"));

        Assertions.assertEquals("java:global/probe", child.getTextContent());
    }

    private static Element createParentElement() throws Exception {
        var document = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().newDocument();
        Element parent = document.createElement("parent");
        document.appendChild(parent);
        return parent;
    }
}
