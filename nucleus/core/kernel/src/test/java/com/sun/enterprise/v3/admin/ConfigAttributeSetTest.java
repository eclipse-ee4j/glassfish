/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.v3.common.PlainTextActionReporter;

import java.beans.PropertyChangeEvent;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.main.core.kernel.test.KernelJUnitExtension;
import org.glassfish.tests.utils.mock.MockGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * test the set command
 * @author Jerome Dochez
 */
@ExtendWith(KernelJUnitExtension.class)
public class ConfigAttributeSetTest implements ConfigListener {

    @Inject
    private ServiceLocator locator;
    @Inject
    private MockGenerator mockGenerator;
    private PropertyChangeEvent event;
    private Subject adminSubject;

    @BeforeEach
    public void addMissingServices() {
        adminSubject = mockGenerator.createAsadminSubject();
    }

    @Test
    public void simpleAttributeSetTest() {
        CommandRunnerImpl runner = locator.getService(CommandRunnerImpl.class);
        assertNotNull(runner);

        // let's find our target
        NetworkListener listener = null;
        NetworkListeners service = locator.getService(NetworkListeners.class);
        for (NetworkListener l : service.getNetworkListener()) {
            if ("http-listener-1".equals(l.getName())) {
                listener = l;
                break;
            }
        }
        assertNotNull(listener);

        // Let's register a listener
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(listener);
        bean.addListener(this);

        // parameters to the command
        ParameterMap parameters = new ParameterMap();
        parameters.set("DEFAULT",
            "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.port=8090");
        // execute the set command.
        PlainTextActionReporter reporter = new PlainTextActionReporter();
        CommandInvocation invocation = runner.getCommandInvocation("set", reporter, adminSubject).parameters(parameters);
        invocation.execute();

        assertEquals(ExitCode.SUCCESS, reporter.getActionExitCode());
        assertEquals("", reporter.getMessage());

        // ensure events are delivered.
        locator.<Transactions>getService(Transactions.class).waitForDrain();

        // check the result.
        String port = listener.getPort();
        assertEquals("8090", port);

        // check we recevied the event
        assertNotNull(event);
        assertAll(
            () -> assertEquals("8080", event.getOldValue()),
            () -> assertEquals("8090", event.getNewValue()),
            () -> assertEquals("port", event.getPropertyName())
        );

    }

    @Override
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] propertyChangeEvents) {
        assertThat(propertyChangeEvents, arrayWithSize(1));
        event = propertyChangeEvents[0];
        return null;
    }
}
