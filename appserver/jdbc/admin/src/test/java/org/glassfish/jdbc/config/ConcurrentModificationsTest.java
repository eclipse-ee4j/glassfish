/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jdbc.config.JdbcResource;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;

import java.beans.PropertyVetoException;

public class ConcurrentModificationsTest extends ConfigApiTest{

    /**
     * Returns the file name without the .xml extension to load the test configuration
     * from. By default, it's the name of the TestClass.
     *
     * @return the configuration file name
     */
    public String getFileName() {
        return "DomainTest";
    }

    @Test(expected= TransactionFailure.class)
    public void collectionTest() throws TransactionFailure {

        ServiceLocator habitat = super.getHabitat();
        final Resources resources = habitat.<Domain>getService(Domain.class).getResources();
        assertTrue(resources!=null);

        ConfigSupport.apply(new SingleConfigCode<Resources>() {

            public Object run(Resources writeableResources) throws PropertyVetoException, TransactionFailure {

                assertTrue(writeableResources!=null);
                JdbcResource newResource = writeableResources.createChild(JdbcResource.class);
                newResource.setJndiName("foo");
                newResource.setDescription("Random ");
                newResource.setPoolName("bar");
                newResource.setEnabled("true");
                writeableResources.getResources().add(newResource);

                // now let's check I have my copy...
                boolean found=false;
                for (Resource resource : writeableResources.getResources()) {
                    if (resource instanceof JdbcResource) {
                        JdbcResource jdbc = (JdbcResource) resource;
                        if (jdbc.getJndiName().equals("foo")) {
                            found = true;
                            break;
                        }
                    }
                }
                assertTrue(found);

                // now let's check that my readonly copy does not see it...
                boolean shouldNot = false;
                for (Resource resource : resources.getResources()) {
                    if (resource instanceof JdbcResource) {
                        JdbcResource jdbc = (JdbcResource) resource;
                        if (jdbc.getJndiName().equals("foo")) {
                            shouldNot = true;
                            break;
                        }
                    }
                }
                assertFalse(shouldNot);

                // now I am throwing a transaction failure since I don't care about saving it
                throw new TransactionFailure("Test passed", null);
            }        
        },resources);
    }
}
