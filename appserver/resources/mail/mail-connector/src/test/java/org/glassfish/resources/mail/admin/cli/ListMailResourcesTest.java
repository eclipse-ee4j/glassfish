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

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.MessagePart;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandContextImpl;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.resources.mail.config.MailResource;
import org.glassfish.resources.mail.test.MailJunit5Extension;
import org.glassfish.tests.utils.mock.MockGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MailJunit5Extension.class)
public class ListMailResourcesTest {

    @Inject
    private ServiceLocator locator;
    @Inject
    private MockGenerator mockGenerator;
    @Inject
    private CommandRunner cr;

    private int origNum;
    private ParameterMap parameters;
    private AdminCommandContext context;
    private Subject adminSubject;

    @BeforeEach
    public void setUp() {
        parameters = new ParameterMap();
        cr = locator.getService(CommandRunner.class);
        assertNotNull(cr);
        Resources resources = locator.<Domain>getService(Domain.class).getResources();
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(ListMailResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        for (Resource resource : resources.getResources()) {
            if (resource instanceof MailResource) {
                origNum = origNum + 1;
            }
        }
        adminSubject = mockGenerator.createAsadminSubject();
    }

    /**
     * Test of execute method, of class ListMailResources.
     * list-mail-resources
     */
    @Test
    public void testExecuteSuccessListOriginal() {
        ListMailResources listCommand = locator.getService(ListMailResources.class);
        cr.getCommandInvocation("list-mail-resources", context.getActionReport(), adminSubject).parameters(parameters).execute(listCommand);
        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertThat(list, hasSize(origNum));
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
        ListMailResources listCommand = locator.getService(ListMailResources.class);
        assertNotNull(listCommand);
        cr.getCommandInvocation("list-mail-resources", context.getActionReport(), adminSubject).parameters(parameters).execute(listCommand);
        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertEquals(origNum + 1, list.size());
        List<String> listStr = new ArrayList<>();
        for (MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertThat(listStr, containsInRelativeOrder("mailresource"));
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        deleteMailResource();
    }

    private void createMailResource() {
        parameters = new ParameterMap();
        parameters.set("mailhost", "localhost");
        parameters.set("mailuser", "test");
        parameters.set("fromaddress", "test@sun.com");
        parameters.set("jndi_name", "mailresource");
        CreateMailResource createCommand = locator.getService(CreateMailResource.class);
        assertNotNull(createCommand);
        cr.getCommandInvocation("create-mail-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(createCommand);
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
        ListMailResources listCommand = locator.getService(ListMailResources.class);
        cr.getCommandInvocation("list-mail-resources", context.getActionReport(), adminSubject).parameters(parameters).execute(listCommand);

        List<ActionReport.MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertThat(list, hasSize(origNum + 1));
        origNum = origNum + 1; //as we newly created a resource after test "setup".

        deleteMailResource();
        parameters = new ParameterMap();
        listCommand = locator.getService(ListMailResources.class);
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(ListMailResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        cr.getCommandInvocation("list-mail-resources", context.getActionReport(), adminSubject).parameters(parameters).execute(listCommand);
        list = context.getActionReport().getTopMessagePart().getChildren();
        assertThat(list, hasSize(origNum - 1));
        List<String> listStr = new ArrayList<>();
        for (MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertFalse(listStr.contains("mailresource"));
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }

    private void deleteMailResource() {
        parameters = new ParameterMap();
        parameters.set("jndi_name", "mailresource");
        DeleteMailResource deleteCommand = locator.getService(DeleteMailResource.class);
        assertTrue(deleteCommand != null);
        cr.getCommandInvocation("delete-mail-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(deleteCommand);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }
}
