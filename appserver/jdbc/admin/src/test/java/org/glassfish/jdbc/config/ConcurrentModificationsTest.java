/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.jdbc.config;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;

import jakarta.inject.Inject;

import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jdbc.admin.cli.test.JdbcAdminJunit5Extension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(JdbcAdminJunit5Extension.class)
public class ConcurrentModificationsTest {

    @Inject
    private ServiceLocator locator;

    @Test
    public void collectionTest() throws TransactionFailure {

        final Resources resources = locator.<Domain>getService(Domain.class).getResources();
        assertNotNull(resources);

        SingleConfigCode<Resources> configCode = writeableResources -> {

            assertNotNull(writeableResources);
            JdbcResource newResource = writeableResources.createChild(JdbcResource.class);
            newResource.setJndiName("foo");
            newResource.setDescription("Random ");
            newResource.setPoolName("bar");
            newResource.setEnabled("true");
            writeableResources.getResources().add(newResource);

            // now let's check I have my copy...
            assertTrue(containsFoo(writeableResources.getResources()), "writeableResources should NOT contain foo");

            // now let's check that my readonly copy does not see it...
            assertFalse(containsFoo(resources.getResources()), "resources should contain foo");

            // now I am throwing a transaction failure since I don't care about saving it
            throw new TransactionFailure("Test passed", null);
        };
        assertThrows(TransactionFailure.class, () -> ConfigSupport.apply(configCode, resources));
    }


    private boolean containsFoo(List<Resource> resources) {
        for (Resource resource : resources) {
            if (resource instanceof JdbcResource) {
                JdbcResource jdbc2 = (JdbcResource) resource;
                if (jdbc2.getJndiName().equals("foo")) {
                    return true;
                }
            }
        }
        return false;
    }
}
