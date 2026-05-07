/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.ejb.deployment.node;

import com.sun.enterprise.deployment.RunAsIdentityDescriptor;

import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.DummyEjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.internal.api.Globals;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Element;


public class EjbNodeTest {

    private static final String TEST_LOCATOR_NAME = EjbNodeTest.class.getName();
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

    /** Tests all meaningful branches of {@link EjbNode#writeSecurityIdentityDescriptor}. */
    @ParameterizedTest
    @MethodSource("securityIdentityWriteScenarios")
    void writeSecurityIdentityDescriptor(Boolean usesCallerIdentity, boolean withRunAs, int expectedSecurityIdentityCount) throws Exception {
        DummyEjbDescriptor descriptor = new DummyEjbDescriptor();
        if (withRunAs) {
            // setRunAsIdentity requires false first (EjbBeanDescriptor constraint).
            descriptor.setUsesCallerIdentity(false);
            descriptor.setRunAsIdentity(new RunAsIdentityDescriptor("test-role"));
        }
        if (usesCallerIdentity != null) {
            descriptor.setUsesCallerIdentity(usesCallerIdentity);
        }

        Element parent = createParentElement();
        TestEjbNode ejbNode = new TestEjbNode(descriptor);
        ejbNode.writeSecurityIdentityDescriptor(parent, descriptor);

        Assertions.assertEquals(expectedSecurityIdentityCount, parent.getElementsByTagName(EjbTagNames.SECURITY_IDENTITY).getLength());
    }

    private static Stream<Arguments> securityIdentityWriteScenarios() {
        return Stream.of(
                Arguments.of(null, false, 0),
                Arguments.of(null, true, 1),
                Arguments.of(false, false, 0),
                Arguments.of(false, true, 1),
                Arguments.of(true, false, 1),
                Arguments.of(true, true, 1)
        );
    }

    private static Element createParentElement() throws Exception {
        var document = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().newDocument();
        Element parent = document.createElement("ejb");
        document.appendChild(parent);
        return parent;
    }

    /** Minimal concrete subclass of {@link EjbNode} for testing protected methods. */
    private static class TestEjbNode extends EjbNode<EjbDescriptor> {

        private final EjbDescriptor descriptor;

        TestEjbNode(EjbDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public EjbDescriptor getEjbDescriptor() {
            return descriptor;
        }
    }

}
