/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.kernel.deployment;

import com.sun.enterprise.config.serverbeans.Server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventListener.Event;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.main.core.kernel.test.KernelJUnitExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * User: dochez
 * Date: Mar 12, 2009
 * Time: 9:26:57 AM
 */
@TestMethodOrder(OrderAnnotation.class)
@ExtendWith(KernelJUnitExtension.class)
public class EventsTest {

    private static final List<Event<?>> allEvents = new ArrayList<>();

    @Inject
    private ServiceLocator locator;
    private File application;
    private final EventListener listener = allEvents::add;


    @BeforeEach
    public void setup() throws Exception {
        Server server = locator.getService(Server.class, "server");
        assertNotNull(server, "server");
        ServiceLocatorUtilities.addOneConstant(locator, server, ServerEnvironment.DEFAULT_INSTANCE_NAME, Server.class);

        application = File.createTempFile("kerneltest", "tmp");
        application.delete();
        application.mkdirs();

        Events events = locator.getService(Events.class);
        events.register(listener);
    }

    @AfterEach
    public void deleteApplications() {
       if (application != null) {
           application.delete();
       }
    }


    @Test
    @Order(1)
    public void deployUndeployTest() throws Exception {
        final List<EventTypes<?>> myTestEvents = getSingletonModuleSuccessfullDeploymentEvents();
        Events events = locator.getService(Events.class);
        EventListener listenerRemovingEvents = event -> {
            if (myTestEvents.contains(event.type())) {
                myTestEvents.remove(event.type());
            }
        };
        events.register(listenerRemovingEvents);
        Deployment deployment = locator.getService(Deployment.class);
        DeployCommandParameters params = new DeployCommandParameters(application);
        params.name = "fakeApplication";
        params.target = "server";
        ActionReport report = locator.getService(ActionReport.class, "hk2-agent");
        ExtendedDeploymentContext dc = deployment.getBuilder(Logger.getAnonymousLogger(), params, report).source(application).build();
        deployment.deploy(dc);
        events.unregister(listenerRemovingEvents);
        assertThat(myTestEvents.toString(), myTestEvents, hasSize(6));

        final List<EventTypes<?>> myTestEvents2 = getSingletonModuleSuccessfullUndeploymentEvents();
        EventListener listener2 = event -> {
            if (myTestEvents2.contains(event.type())) {
                myTestEvents2.remove(event.type());
            }
        };
        events.register(listener2);
        UndeployCommandParameters params2 = new UndeployCommandParameters("fakeApplication");
        params2.target = "server";
        ActionReport report2 = locator.getService(ActionReport.class, "hk2-agent");
        ExtendedDeploymentContext dc2 = deployment.getBuilder(Logger.getAnonymousLogger(), params2, report2).source(application).build();
        deployment.undeploy("fakeApplication", dc2);
        events.unregister(listener2);
        assertThat(myTestEvents.toString(), myTestEvents2, hasSize(6));
    }

    @Test
    @Order(2)
    public void badUndeployTest() throws Exception {
        Deployment deployment = locator.getService(Deployment.class);
        UndeployCommandParameters params = new UndeployCommandParameters("notavalidname");
        params.target = "server";
        ActionReport report = locator.getService(ActionReport.class, "hk2-agent");
        ExtendedDeploymentContext dc = deployment.getBuilder(Logger.getAnonymousLogger(), params, report).source(application).build();
        deployment.undeploy("notavalidname", dc);
        assertEquals(ActionReport.ExitCode.FAILURE, report.getActionExitCode());
    }

    @Test
    @Order(3)
    public void asynchronousEvents() {
        List<EventTypes<?>> remaining = asynchonousEvents().stream()
            .filter(et -> allEvents.stream().anyMatch(event -> event.is(et))).collect(Collectors.toList());
        assertThat(remaining.toString(), remaining, hasSize(2));
    }


    private static List<EventTypes<?>> getSingletonModuleSuccessfullDeploymentEvents() {
        ArrayList<EventTypes<?>> events = new ArrayList<>();
        events.add(Deployment.MODULE_PREPARED);
        events.add(Deployment.MODULE_LOADED);
        events.add(Deployment.MODULE_STARTED);
        events.add(Deployment.APPLICATION_PREPARED);
        events.add(Deployment.APPLICATION_LOADED);
        events.add(Deployment.APPLICATION_STARTED);
        return events;
    }

    private static List<EventTypes<?>> getSingletonModuleSuccessfullUndeploymentEvents() {
        ArrayList<EventTypes<?>> events = new ArrayList<>();
        events.add(Deployment.MODULE_STOPPED);
        events.add(Deployment.MODULE_UNLOADED);
        events.add(Deployment.MODULE_CLEANED);
        events.add(Deployment.APPLICATION_STOPPED);
        events.add(Deployment.APPLICATION_UNLOADED);
        events.add(Deployment.APPLICATION_CLEANED);
        return events;
    }

    private static List<EventTypes<?>> asynchonousEvents() {
        ArrayList<EventTypes<?>> events = new ArrayList<>();
        events.add(Deployment.DEPLOYMENT_START);
        events.add(Deployment.DEPLOYMENT_SUCCESS);
        events.add(Deployment.UNDEPLOYMENT_START);
        events.add(Deployment.UNDEPLOYMENT_SUCCESS);
        events.add(Deployment.UNDEPLOYMENT_FAILURE);
        return events;
    }
}
