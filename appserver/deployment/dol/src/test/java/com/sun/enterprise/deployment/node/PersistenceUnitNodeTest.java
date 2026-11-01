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

import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.xml.PersistenceTagNames;

import jakarta.persistence.FetchType;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PersistenceUnitNodeTest {

    @Test
    void readsPersistence40Elements() {
        PersistenceUnitDescriptor descriptor = readElements(
            PersistenceTagNames.PACKAGE_DESCRIPTOR, "com.example.model",
            PersistenceTagNames.MODULE_DESCRIPTOR, "com.example",
            PersistenceTagNames.DEFAULT_TO_ONE_FETCH_TYPE, "LAZY");

        Assertions.assertEquals(List.of("com.example.model"), descriptor.getPackageDescriptors());
        Assertions.assertEquals(List.of("com.example"), descriptor.getModuleDescriptors());
        Assertions.assertEquals(FetchType.LAZY, descriptor.getDefaultToOneFetchType());
    }

    @Test
    void defaultsToEagerToOneFetchType() {
        Assertions.assertEquals(FetchType.EAGER, readElements().getDefaultToOneFetchType());
    }

    @Test
    void ignoresDefaultAsDefaultToOneFetchType() {
        PersistenceUnitDescriptor descriptor = readElements(PersistenceTagNames.DEFAULT_TO_ONE_FETCH_TYPE, "DEFAULT");

        Assertions.assertEquals(FetchType.EAGER, descriptor.getDefaultToOneFetchType());
    }

    /**
     * Passes element values to the node the way SaxParserHandler does.
     */
    private static PersistenceUnitDescriptor readElements(String... namesAndValues) {
        PersistenceUnitNode node = new PersistenceUnitNode();
        node.abstractDescriptor = new PersistenceUnitDescriptor();
        for (int i = 0; i < namesAndValues.length; i += 2) {
            node.setElementValue(new XMLElement(namesAndValues[i]), namesAndValues[i + 1]);
        }
        return (PersistenceUnitDescriptor) node.getDescriptor();
    }
}
