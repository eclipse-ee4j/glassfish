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

package org.glassfish.orb.admin.cli;

import org.glassfish.api.admin.AdminCommandContextImpl;
import org.glassfish.orb.admin.config.IiopListener;
import org.glassfish.orb.admin.config.IiopService;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import java.beans.PropertyVetoException;
import java.util.List;


public class DeleteIiopListenerTest extends org.glassfish.tests.utils.ConfigApiTest {

    private ServiceLocator services;
    private IiopService iiopService;
    private ParameterMap parameters;
    private CommandRunner cr;
    private AdminCommandContext context;

    public String getFileName() {
        return "DomainTest";
    }

    public DomDocument getDocument(ServiceLocator services) {
        return new TestDocument(services);
    }

    @Before
    public void setUp() {
        services = getHabitat();
        iiopService = services.getService(IiopService.class);
        parameters = new ParameterMap();
        cr = services.getService(CommandRunner.class);
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(DeleteIiopListenerTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
    }

    @After
    public void tearDown() throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<IiopService>() {
            public Object run(IiopService param) throws PropertyVetoException,
                    TransactionFailure {
                List<IiopListener> listenerList = param.getIiopListener();
                for (IiopListener listener : listenerList) {
                    String currListenerId = listener.getId();
                    if (currListenerId != null && currListenerId.equals
                            ("iiop_1")) {
                        listenerList.remove(listener);
                        break;
                    }
                }
                return listenerList;
            }
        }, iiopService);
    }


    /**
     * Test of execute method, of class DeleteIiopListener.
     * delete-iiop-listener iiop_1
     */
    @Test
    public void testExecuteSuccessDefaultTarget() {
        parameters.set("listeneraddress", "localhost");
        parameters.set("iiopport", "4440");
        parameters.set("listener_id", "iiop_1");
        CreateIiopListener createCommand = services.getService(CreateIiopListener.class);
        cr.getCommandInvocation("create-iiop-listener", context.getActionReport(), adminSubject()).parameters(parameters).execute(createCommand);  
        CreateIiopListenerTest.checkActionReport(context.getActionReport());
        parameters = new ParameterMap();
        parameters.set("listener_id", "iiop_1");
        DeleteIiopListener deleteCommand = services.getService(DeleteIiopListener.class);
        cr.getCommandInvocation("delete-iiop-listener", context.getActionReport(), adminSubject()).parameters(parameters).execute(deleteCommand);               

        CreateIiopListenerTest.checkActionReport(context.getActionReport());
        boolean isDeleted = true;
        List<IiopListener> listenerList = iiopService.getIiopListener();
        for (IiopListener listener : listenerList) {
            if (listener.getId().equals("iiop_1")) {
                isDeleted = false;
                logger.fine("IIOPListener name iiop_1 is not deleted.");
                break;
            }
        }
        assertTrue(isDeleted);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class DeleteIiopListener.
     * delete-iiop-listener doesnotexist
     */
    @Test
    public void testExecuteFailDoesNotExist() {
        parameters.set("DEFAULT", "doesnotexist");
        DeleteIiopListener deleteCommand = services.getService(DeleteIiopListener.class);
        cr.getCommandInvocation("delete-iiop-listener", context.getActionReport(), adminSubject()).parameters(parameters).execute(deleteCommand);               
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
        logger.fine("msg: " + context.getActionReport().getMessage());
    }
}
