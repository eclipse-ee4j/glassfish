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
import org.glassfish.api.ActionReport.MessagePart;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.config.DomDocument;

import java.util.ArrayList;
import java.util.List;


public class ListIiopListenersTest extends org.glassfish.tests.utils.ConfigApiTest {

    private ServiceLocator services;
    private int origNum;
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
        IiopService iiopService = services.getService(IiopService.class);
        parameters = new ParameterMap();
        cr = services.getService(CommandRunner.class);
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(ListIiopListenersTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        List<IiopListener> listenerList = iiopService.getIiopListener();
        origNum = listenerList.size();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class ListIiopListeners.
     * list-iiop-listeners
     */
    @Test
    public void testExecuteSuccessListOriginal() {
        ListIiopListeners listCommand = services.getService(ListIiopListeners.class);
        cr.getCommandInvocation("list-iiop-listeners", context.getActionReport(), adminSubject()).parameters(parameters).execute(listCommand);
        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertEquals(origNum, list.size());
        CreateIiopListenerTest.checkActionReport(context.getActionReport());
    }

    /**
     * Test of execute method, of class ListIiopListeners.
     * list-iiop-listeners server
     */
    @Test
    public void testExecuteSuccessValidTargetOperand() {
        ListIiopListeners listCommand = services.getService(ListIiopListeners.class);
        parameters.set("DEFAULT", "server");
        cr.getCommandInvocation("list-iiop-listeners", context.getActionReport(), adminSubject()).parameters(parameters).execute(listCommand);               
        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertEquals(origNum, list.size());
        CreateIiopListenerTest.checkActionReport(context.getActionReport());
    }

    /**
     * Test of execute method, of class ListIiopListeners.
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 listener
     * list-iiop-listeners
     * delete-iiop-listener listener
     */
    @Test
    public void testExecuteSuccessListListener() {
        parameters.set("listeneraddress", "localhost");
        parameters.set("iiopport", "4440");
        parameters.set("listener_id", "listener");
        CreateIiopListener createCommand = services.getService(CreateIiopListener.class);
        cr.getCommandInvocation("create-iiop-listener", context.getActionReport(), adminSubject()).parameters(parameters).execute(createCommand);
        CreateIiopListenerTest.checkActionReport(context.getActionReport());
        parameters = new ParameterMap();
        ListIiopListeners listCommand = services.getService(ListIiopListeners.class);
        cr.getCommandInvocation("list-iiop-listeners", context.getActionReport(), adminSubject()).parameters(parameters).execute(listCommand);               
        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertEquals(origNum + 1, list.size());
        List<String> listStr = new ArrayList<String>();
        for (MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertTrue(listStr.contains("listener"));
        CreateIiopListenerTest.checkActionReport(context.getActionReport());
        parameters = new ParameterMap();
        parameters.set("listener_id", "listener");
        DeleteIiopListener deleteCommand = services.getService(DeleteIiopListener.class);
        cr.getCommandInvocation("delete-iiop-listener", context.getActionReport(), adminSubject()).parameters(parameters).execute(deleteCommand);               
        CreateIiopListenerTest.checkActionReport(context.getActionReport());
    }

    /**
     * Test of execute method, of class ListIiopListener.
     * list-iiop-listeners
     */
    @Test
    public void testExecuteSuccessListNoListener() {       
        parameters = new ParameterMap();
        ListIiopListeners listCommand = services.getService(ListIiopListeners.class);
        cr.getCommandInvocation("list-iiop-listeners", context.getActionReport(), adminSubject()).parameters(parameters).execute(listCommand);    
        CreateIiopListenerTest.checkActionReport(context.getActionReport());
        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertEquals(origNum, list.size());
        List<String> listStr = new ArrayList<String>();
        for (MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertFalse(listStr.contains("listener"));
    }
}
