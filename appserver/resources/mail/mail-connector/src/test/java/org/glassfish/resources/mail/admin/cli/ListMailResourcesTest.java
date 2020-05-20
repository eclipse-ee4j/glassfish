/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resources.mail.admin.cli;


import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.MessagePart;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandContextImpl;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.resources.mail.config.MailResource;
import org.glassfish.tests.utils.ConfigApiTest;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.config.DomDocument;

import java.util.ArrayList;
import java.util.List;

public class ListMailResourcesTest extends ConfigApiTest {

    private ServiceLocator habitat;
    private int origNum = 0;
    private ParameterMap parameters;
    private AdminCommandContext context;
    private CommandRunner cr;

    @Override
    public DomDocument getDocument(ServiceLocator habitat) {
        return new TestDocument(habitat);
    }

    public String getFileName() {
        return "DomainTest";
    }

    @Before
    public void setUp() {
        habitat = getHabitat();
        parameters = new ParameterMap();
        cr = habitat.getService(CommandRunner.class);
        assertTrue(cr != null);
        Resources resources = habitat.<Domain>getService(Domain.class).getResources();
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(ListMailResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        for (Resource resource : resources.getResources()) {
            if (resource instanceof MailResource) {
                origNum = origNum + 1;
            }
        }
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class ListMailResources.
     * list-mail-resources
     */
    @Test
    public void testExecuteSuccessListOriginal() {
        org.glassfish.resources.mail.admin.cli.ListMailResources listCommand = habitat.getService(org.glassfish.resources.mail.admin.cli.ListMailResources.class);
        cr.getCommandInvocation("list-mail-resources", context.getActionReport(), adminSubject()).parameters(parameters).execute(listCommand);
        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        if (origNum == 0) {
            //Nothing to list
        } else {
            assertEquals(origNum, list.size());
        }
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }


    /**
     * Test of execute method, of class ListMailResource.
     * create-mail-resource --mailuser=test --mailhost=localhost
     * --fromaddress=test@sun.com mailresource
     * list-mail-resources
     */
    @Test
    public void testExecuteSuccessListMailResource() {
        createMailResource();

        parameters = new ParameterMap();
        org.glassfish.resources.mail.admin.cli.ListMailResources listCommand = habitat.getService(org.glassfish.resources.mail.admin.cli.ListMailResources.class);
        assertTrue(listCommand != null);
        cr.getCommandInvocation("list-mail-resources", context.getActionReport(), adminSubject()).parameters(parameters).execute(listCommand);
        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertEquals(origNum + 1, list.size());
        List<String> listStr = new ArrayList<String>();
        for (MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertTrue(listStr.contains("mailresource"));
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        deleteMailResource();
    }

    private void createMailResource() {
        parameters = new ParameterMap();
        parameters.set("mailhost", "localhost");
        parameters.set("mailuser", "test");
        parameters.set("fromaddress", "test@sun.com");
        parameters.set("jndi_name", "mailresource");
        CreateMailResource createCommand = habitat.getService(CreateMailResource.class);
        assertTrue(createCommand != null);
        cr.getCommandInvocation("create-mail-resource", context.getActionReport(), adminSubject()).parameters(parameters).execute(createCommand);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }


    /**
     * Test of execute method, of class ListJdbcResource.
     * delete-mail-resource mailresource
     * list-mail-resources
     */
    @Test
    public void testExecuteSuccessListNoMailResource() {
        createMailResource();

        parameters = new ParameterMap();
        org.glassfish.resources.mail.admin.cli.ListMailResources listCommand = habitat.getService(org.glassfish.resources.mail.admin.cli.ListMailResources.class);
        cr.getCommandInvocation("list-mail-resources", context.getActionReport(), adminSubject()).parameters(parameters).execute(listCommand);

        List<ActionReport.MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertEquals(origNum + 1, list.size());
        origNum = origNum + 1; //as we newly created a resource after test "setup".

        deleteMailResource();
        parameters = new ParameterMap();
        listCommand = habitat.getService(org.glassfish.resources.mail.admin.cli.ListMailResources.class);
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(ListMailResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        cr.getCommandInvocation("list-mail-resources", context.getActionReport(), adminSubject()).parameters(parameters).execute(listCommand);
        list = context.getActionReport().getTopMessagePart().getChildren();
        if ((origNum - 1) == 0) {
            //Nothing to list.
        } else {
            assertEquals(origNum - 1, list.size());
        }
        List<String> listStr = new ArrayList<String>();
        for (MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertFalse(listStr.contains("mailresource"));
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }

    private void deleteMailResource() {
        parameters = new ParameterMap();
        parameters.set("jndi_name", "mailresource");
        DeleteMailResource deleteCommand = habitat.getService(DeleteMailResource.class);
        assertTrue(deleteCommand != null);
        cr.getCommandInvocation("delete-mail-resource", context.getActionReport(), adminSubject()).parameters(parameters).execute(deleteCommand);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }
}
