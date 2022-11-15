/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.deployment.descriptor;

import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.util.ComponentValidator;
import org.glassfish.api.naming.SimpleJndiName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Matejcek
 */
public class EjbBundleDescriptorImplTest {

    @Test
    public void defaultConstructor() throws Exception {
        EjbBundleDescriptorImpl descriptor = new EjbBundleDescriptorImpl();
        assertDoesNotThrow(() -> descriptor.visit(new ComponentValidator()));
    }


    @Test
    public void std() throws Exception {
        EjbBundleDescriptorImpl descriptor = new EjbBundleDescriptorImpl();
        descriptor.setDisplayName("ejb-bundle-display-name");
        descriptor.setName("ejb-bundle-name");
        descriptor.setModuleID("ejb-bundle-module-id");
        descriptor.setDisableNonportableJndiNames("true");

        EjbReferenceDescriptor reference = new EjbReferenceDescriptor();
        reference.setName("ejb-name");
        reference.setDisplayName("ejb-display-name");
        reference.setJndiName(null);
        reference.setLinkName(null);
        reference.setLookupName(SimpleJndiName.of("ejb-lookup-name"));
        reference.setValue("ejb-value");
        reference.setType("session");
        reference.setLinkName(null);
        reference.setHomeClassName(null);
        descriptor.addEjbReferenceDescriptor(reference);

        EnvironmentProperty property = new EnvironmentProperty("env-name", "env-value", "env-description");
        descriptor.addEnvironmentProperty(property);

        assertAll(
            () -> assertDoesNotThrow(() -> descriptor.visit(new ComponentValidator())),
            () -> assertTrue(descriptor.isEmpty()),
            () -> assertFalse(descriptor.hasChanged()),
            () -> assertFalse(descriptor.hasAssemblyInformation()),
            () -> assertFalse(descriptor.hasContainerTransactions()),
            () -> assertFalse(descriptor.hasEjbReferences()),
            () -> assertFalse(descriptor.hasInterceptors()),
            () -> assertFalse(descriptor.hasPermissionedRoles()),
            () -> assertFalse(descriptor.hasRelationships()),
            () -> assertFalse(descriptor.hasWebServiceClients()),
            () -> assertFalse(descriptor.hasWebServices()),
            () -> assertSame(descriptor.getName(), descriptor.getModuleID()),
            () -> assertSame(descriptor.getName(), descriptor.getRawModuleID())
        );

        assertAll("bidirectional link between EjbReferenceDescriptor and EjbBundleDescriptorImpl",
            () -> assertThat(descriptor.getEjbReferenceDescriptors(), contains(reference)),
            () -> assertSame(reference, descriptor.getEjbReference("ejb-display-name")),
            () -> assertSame(descriptor, reference.getReferringBundleDescriptor())
        );

        assertAll("environment property",
            () -> assertThat(descriptor.getEnvironmentProperties(), contains(property)),
            () -> assertSame(property, descriptor.getEnvironmentPropertyByName("env-name"))
        );
    }
}
