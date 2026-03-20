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

package org.glassfish.main.test.app.persistence.inject.factory;

import org.glassfish.main.itest.tools.ITestBase;
import org.glassfish.main.itest.tools.asadmin.DomainPropertiesBackup;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.switchDerbyPoolToUniqueEmbededded;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Jakarta Persistence in Jakarta EE environment (InjectEMF and UserTransaction)
 *
 * <ul>
 * <li>Step 1. Persisting Entities with Mx1 Relationship
 * <li>Step 2. Checking Lazy Loading by Find
 * <li>Step 3. Checking Lazy Loading by Query
 * </ul>
 */
@TestMethodOrder(OrderAnnotation.class)
public class InjectFactoryTest extends ITestBase {

    private static final DomainPropertiesBackup DERBYPOOL_BACKUP = DomainPropertiesBackup.backupDerbyPool();

    @BeforeAll
    public void deploy() throws Exception {
        switchDerbyPoolToUniqueEmbededded();

        doDeploy(
            ShrinkWrap.create(WebArchive.class, InjectFactoryTest.class.getSimpleName() + "WebApp")
                      .addPackage(JakartaPersistenceServlet.class.getPackage())
                      .deleteClass(InjectFactoryTest.class)
                      .addAsResource(
                          JakartaPersistenceServlet.class.getPackage(), "persistence.xml", "META-INF/persistence.xml")
                      .addAsWebInfResource(
                          JakartaPersistenceServlet.class.getPackage(), "web.xml", "web.xml"));
    }


    @Test
    @Order(1)
    public void persistWithInjectEMF() throws Exception {
        try {
            boolean result = test("llinit");
            assertEquals(true, result, "Unexpected Results");

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);

        }
    }

    @Test
    @Order(2)
    public void lazyLoadingByQuery() throws Exception {
        try {
            boolean result = test("llquery");
            assertEquals(true, result, "Unexpected Results");

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }

    }

    private boolean test(String c) throws Exception {
        return doTest(c, c + ":pass");
    }

    @AfterAll
    public static void cleanup() throws Exception {
        DERBYPOOL_BACKUP.restore();
    }

}
