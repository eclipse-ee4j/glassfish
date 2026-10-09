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

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.node.appclient.AppClientNode;
import com.sun.enterprise.deployment.xml.TagNames;

import javax.xml.parsers.DocumentBuilderFactory;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.internal.api.Globals;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

class AbstractBundleNodeTest {

    private static final String TEST_LOCATOR_NAME = AbstractBundleNodeTest.class.getName();
    private static ServiceLocator previousHabitat;

    @BeforeAll
    static void setupServiceLocator() {
        previousHabitat = Globals.getDefaultHabitat();
        Globals.setDefaultHabitat(ServiceLocatorFactory.getInstance().create(TEST_LOCATOR_NAME));
    }

    @AfterAll
    static void teardownServiceLocator() {
        Globals.setDefaultHabitat(previousHabitat);
        ServiceLocatorFactory.getInstance().destroy(TEST_LOCATOR_NAME);
    }

    @Test
    void writesJakartaNamespaceInSchemaLocation() throws Exception {
        Element applicationClient = createParentElement();
        TestAppClientNode node = new TestAppClientNode();

        node.addBundleAttributes(applicationClient, new ApplicationClientDescriptor());

        Assertions.assertEquals(TagNames.JAKARTAEE_NAMESPACE + " " + node.schemaUrl(),
            applicationClient.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"));
    }

    private static Element createParentElement() throws Exception {
        var document = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().newDocument();
        Element parent = document.createElement("application-client");
        document.appendChild(parent);
        return parent;
    }

    private static class TestAppClientNode extends AppClientNode {

        void addBundleAttributes(Element element, ApplicationClientDescriptor descriptor) {
            addBundleNodeAttributes(element, descriptor);
        }

        String schemaUrl() {
            return getSchemaURL();
        }
    }
}
