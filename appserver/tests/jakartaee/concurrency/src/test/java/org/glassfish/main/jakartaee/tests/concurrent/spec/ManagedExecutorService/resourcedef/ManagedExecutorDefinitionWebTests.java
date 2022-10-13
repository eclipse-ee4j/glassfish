/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.glassfish.main.jakartaee.tests.concurrent.spec.ManagedExecutorService.resourcedef;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionWebBean;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionInterface;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionServlet;
import ee.jakarta.tck.concurrent.spi.context.IntContextProvider;
import ee.jakarta.tck.concurrent.spi.context.StringContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;

;

@Test
public class ManagedExecutorDefinitionWebTests extends TestClient{

    @ArquillianResource(ManagedExecutorDefinitionServlet.class)
    URL baseURL;

    @Deployment(name="ManagedExecutorDefinitionTests", testable=false)
    public static WebArchive createDeployment() {

        WebArchive war = ShrinkWrap.create(WebArchive.class, "ManagedExecutorDefinitionTests_web.war")
                .addPackages(false,
                        ManagedExecutorDefinitionWebTests.class.getPackage(),
                        getFrameworkPackage(),
                        getContextPackage(),
                        getContextProvidersPackage())
                .addClasses(
                        ContextServiceDefinitionInterface.class,
                        ContextServiceDefinitionWebBean.class,
                        ContextServiceDefinitionServlet.class)
                .addAsServiceProvider(ThreadContextProvider.class.getName(), IntContextProvider.class.getName(), StringContextProvider.class.getName());

        return war;
    }

    @Override
    protected String getServletPath() {
        return "ManagedExecutorDefinitionServlet";
    }

    @Test
    public void testCopyCompletableFutureTwice() {
        runTest(baseURL);
    }

}
