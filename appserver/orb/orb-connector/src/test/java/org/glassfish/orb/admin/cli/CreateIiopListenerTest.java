/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.orb.admin.cli;

import com.sun.enterprise.v3.common.PropsFileActionReporter;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;

import java.util.List;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandContextImpl;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.orb.admin.config.IiopListener;
import org.glassfish.orb.admin.config.IiopService;
import org.glassfish.orb.admin.test.OrbJunitExtension;
import org.glassfish.tests.utils.mock.MockGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(OrbJunitExtension.class)
public class CreateIiopListenerTest {

    @Inject
    private ServiceLocator services;
    @Inject
    private Logger logger;
    @Inject
    private MockGenerator mockGenerator;

    @Inject
    private IiopService iiopService;
    @Inject
    private CommandRunner cr;

    private ParameterMap parameters;
    private AdminCommandContext context;
    private Subject adminSubject;

    public static void checkActionReport(ActionReport report) {
        if (ActionReport.ExitCode.SUCCESS.equals(report.getActionExitCode())) {
            return;
        }

        Throwable reason = report.getFailureCause();
        assertNotNull(reason,
            "Action failed with exit code " + report.getActionExitCode() + " and message " + report.getMessage());
        fail(reason);
    }


    @BeforeEach
    public void setUp() {
        parameters = new ParameterMap();
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(CreateIiopListenerTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        adminSubject = mockGenerator.createAsadminSubject();
    }

    @AfterEach
    public void tearDown() throws TransactionFailure {
        SingleConfigCode<IiopService> configCode = iiopServiceProxy -> {
            List<IiopListener> listenerList = iiopServiceProxy.getIiopListener();
            for (IiopListener listener : listenerList) {
                String currListenerId = listener.getId();
                if (currListenerId != null && currListenerId.equals("iiop_1")) {
                    listenerList.remove(listener);
                    break;
                }
            }
            return listenerList;
        };
        ConfigSupport.apply(configCode, iiopService);
        parameters = new ParameterMap();
    }



    /**
     * Test of execute method, of class CreateIiopListener.
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 --enabled=true --securityenabled=true iiop_1
     */
    @Test
    public void testExecuteSuccess() {
        parameters.set("listeneraddress", "localhost");
        parameters.set("iiopport", "4440");
        parameters.set("listener_id", "iiop_1");
        parameters.set("enabled", "true");
        parameters.set("securityenabled", "true");
        CreateIiopListener command = services.getService(CreateIiopListener.class);
        cr.getCommandInvocation("create-iiop-listener", context.getActionReport(), adminSubject).parameters(parameters).execute(command);
        checkActionReport(context.getActionReport());
        boolean isCreated = false;
        List<IiopListener> listenerList = iiopService.getIiopListener();
        for (IiopListener listener : listenerList) {
            if (listener.getId().equals("iiop_1")) {
                assertEquals("localhost", listener.getAddress());
                assertEquals("true", listener.getEnabled());
                assertEquals("4440", listener.getPort());
                assertEquals("true", listener.getSecurityEnabled());
                isCreated = true;
                logger.fine("IIOPListener name iiop_1 is created.");
                break;
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class CreateIiopListener.
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 iiop_1
     */

    @Test
    public void testExecuteSuccessDefaultValues() {
        parameters.set("listeneraddress", "localhost");
        parameters.set("iiopport", "4440");
        parameters.set("listener_id", "iiop_1");
        CreateIiopListener command = services.getService(CreateIiopListener.class);
        cr.getCommandInvocation("create-iiop-listener", context.getActionReport(), adminSubject).parameters(parameters).execute(command);
        checkActionReport(context.getActionReport());
        boolean isCreated = false;
        List<IiopListener> listenerList = iiopService.getIiopListener();
        for (IiopListener listener : listenerList) {
            if (listener.getId().equals("iiop_1")) {
                assertEquals("localhost", listener.getAddress());
                assertEquals("4440", listener.getPort());
                isCreated = true;
                logger.fine("IIOPListener name iiop_1 is created.");
                break;
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }


    /**
     * Test of execute method, of class CreateIiopListener.
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 iiop_1
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 iiop_1
     */
    @Test
    public void testExecuteFailDuplicateListener() {
        parameters.set("listeneraddress", "localhost");
        parameters.set("iiopport", "4440");
        parameters.set("listener_id", "iiop_1");
        CreateIiopListener command1 = services.getService(CreateIiopListener.class);
        cr.getCommandInvocation("create-iiop-listener", context.getActionReport(), adminSubject).parameters(parameters).execute(command1);
        checkActionReport(context.getActionReport());
        boolean isCreated = false;
        List<IiopListener> listenerList = iiopService.getIiopListener();
        for (IiopListener listener : listenerList) {
            if (listener.getId().equals("iiop_1")) {
                assertEquals("localhost", listener.getAddress());
                assertEquals("4440", listener.getPort());
                isCreated = true;
                logger.fine("IIOPListener name iiop_1 is created.");
                break;
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());

        CreateIiopListener command2 = services.getService(CreateIiopListener.class);
        cr.getCommandInvocation("create-iiop-listener", context.getActionReport(), adminSubject).parameters(parameters).execute(command2);
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
        int numDupRes = 0;
        listenerList = iiopService.getIiopListener();
        for (IiopListener listener : listenerList) {
            if (listener.getId().equals("iiop_1")) {
                numDupRes = numDupRes + 1;
            }
        }
        assertEquals(1, numDupRes);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class CreateIiopListener with same iiop port number
     * and listener address.
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 iiop_1
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 iiop_2
     */
    @Test
    public void testExecuteFailForSamePortAndListenerAddress() {
        parameters.set("listeneraddress", "localhost");
        parameters.set("iiopport", "4440");
        parameters.set("listener_id", "iiop_1");
        CreateIiopListener command = services.getService(CreateIiopListener.class);
        cr.getCommandInvocation("create-iiop-listener", context.getActionReport(), adminSubject).parameters(parameters).execute(command);
        checkActionReport(context.getActionReport());
        boolean isCreated = false;
        List<IiopListener> listenerList = iiopService.getIiopListener();
        for (IiopListener listener : listenerList) {
            if (listener.getId().equals("iiop_1")) {
                assertEquals("localhost", listener.getAddress());
                assertEquals("4440", listener.getPort());
                isCreated = true;
                logger.fine("IIOPListener name iiop_1 is created.");
                break;
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());

        parameters = new ParameterMap();
        parameters.set("listener_id", "iiop_2");
        parameters.set("iiopport", "4440");
        parameters.set("listeneraddress", "localhost");
        cr.getCommandInvocation("create-iiop-listener", context.getActionReport(), adminSubject).parameters(parameters).execute(command);
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
        logger.fine("msg: " + context.getActionReport().getMessage());

    }

    /**
     * Test of execute method, of class CreateIiopListener when enabled set to junk
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 --enabled=junk iiop_1
     */
    //@Test
    public void testExecuteFailInvalidOptionEnabled() {
        parameters.set("listeneraddress", "localhost");
        parameters.set("iiopport", "4440");
        parameters.set("listener_id", "iiop_1");
        parameters.set("enabled", "junk");
        CreateIiopListener command = services.getService(CreateIiopListener.class);
        cr.getCommandInvocation("create-iiop-listener", context.getActionReport(), adminSubject).parameters(parameters).execute(command);
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
    }

    /**
     * Test of execute method, of class CreateIiopListener when enabled has no value
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 --enable iiop_1
     */
    @Test
    public void testExecuteSuccessNoValueOptionEnabled() {
        parameters.set("listeneraddress", "localhost");
        parameters.set("iiopport", "4440");
        parameters.set("listener_id", "iiop_1");
        parameters.set("enabled", "");
        CreateIiopListener command = services.getService(CreateIiopListener.class);
        cr.getCommandInvocation("create-iiop-listener", context.getActionReport(), adminSubject).parameters(parameters).execute(command);
        checkActionReport(context.getActionReport());
        boolean isCreated = false;
        List<IiopListener> listenerList = iiopService.getIiopListener();
        for (IiopListener listener : listenerList) {
            if (listener.getId().equals("iiop_1")) {
                assertEquals("localhost", listener.getAddress());
                assertEquals("true", listener.getEnabled());
                assertEquals("4440", listener.getPort());
                isCreated = true;
                logger.fine("IIOPListener name iiop_1 is created.");
                break;
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class CreateIiopListener when enabled has no value
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 --securityenabled iiop_1
     */
    @Test
    public void testExecuteSuccessNoValueOptionSecurityEnabled() {
        parameters.set("listeneraddress", "localhost");
        parameters.set("iiopport", "4440");
        parameters.set("listener_id", "iiop_1");
        parameters.set("securityenabled", "");
        CreateIiopListener command = services.getService(CreateIiopListener.class);
        cr.getCommandInvocation("create-iiop-listener", context.getActionReport(), adminSubject).parameters(parameters).execute(command);
        checkActionReport(context.getActionReport());
        boolean isCreated = false;
        List<IiopListener> listenerList = iiopService.getIiopListener();
        for (IiopListener listener : listenerList) {
            if (listener.getId().equals("iiop_1")) {
                assertEquals("localhost", listener.getAddress());
                assertEquals("true", listener.getSecurityEnabled());
                assertEquals("4440", listener.getPort());
                isCreated = true;
                logger.fine("IIOPListener name iiop_1 is created.");
                break;
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }
}
