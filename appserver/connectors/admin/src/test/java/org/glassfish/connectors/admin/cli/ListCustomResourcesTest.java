/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;

import java.util.List;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.MessagePart;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandContextImpl;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.connectors.admin.cli.test.ConnectorsAdminJunit5Extension;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.resources.admin.cli.ListCustomResources;
import org.glassfish.resources.config.CustomResource;
import org.glassfish.tests.utils.mock.MockGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ConnectorsAdminJunit5Extension.class)
public class ListCustomResourcesTest {

    @Inject
    private ServiceLocator locator;
    @Inject
    private CommandRunner<?> cr;
    @Inject
    private MockGenerator mockGenerator;
    private AdminCommandContext context;
    private int origNum;
    private ParameterMap parameters;
    private Subject adminSubject;

    @BeforeEach
    public void setUp() {
        parameters = new ParameterMap();
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(ListCustomResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        Resources resources = locator.<Domain>getService(Domain.class).getResources();
        assertNotNull(resources);
        for (Resource resource : resources.getResources()) {
            if (resource instanceof CustomResource) {
                origNum = origNum + 1;
            }
        }
        adminSubject = mockGenerator.createAsadminSubject();
    }

    /**
     * Test of execute method, of class ListCustomResources.
     * list-custom-resources
     */
    @Test
    public void testExecuteSuccessListOriginal() {
        ListCustomResources listCommand = locator.getService(ListCustomResources.class);
        cr.getCommandInvocation("list-custom-resources", context.getActionReport(), adminSubject).parameters(parameters).execute(listCommand);
        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        if (origNum == 0) {
            //Nothing to list.
        } else {
            assertEquals(origNum, list.size());
        }
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }

    /**
     * Test of execute method, of class ListCustomResources.
     * create-custom-resource ---restype=topic --factoryclass=javax.naming.spi.ObjectFactory
     * Resource1
     * list-custom-resources
     */
    @Test
    public void testExecuteSuccessListResource() {

        createCustomResource();

        ParameterMap parameters = new ParameterMap();
        org.glassfish.resources.admin.cli.ListCustomResources listCommand = locator.getService(org.glassfish.resources.admin.cli.ListCustomResources.class);
        cr.getCommandInvocation("list-custom-resources", context.getActionReport(), adminSubject).parameters(parameters).execute(listCommand);

        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertEquals(origNum + 1, list.size());
        List<String> listStr = new java.util.ArrayList<>();
        for (MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertTrue(listStr.contains("custom_resource1"));
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        deleteCustomResource();
    }


    private void deleteCustomResource() {
        org.glassfish.resources.admin.cli.DeleteCustomResource deleteCommand = locator.getService(org.glassfish.resources.admin.cli.DeleteCustomResource.class);
        assertNotNull(deleteCommand);
        ParameterMap  parameters = new ParameterMap();
        parameters.set("jndi_name", "custom_resource1");
        cr.getCommandInvocation("delete-custom-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(deleteCommand);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }

    private void createCustomResource() {
        org.glassfish.resources.admin.cli.CreateCustomResource createCommand = locator.getService(org.glassfish.resources.admin.cli.CreateCustomResource.class);
        assertNotNull(createCommand);
        ParameterMap parameters = new ParameterMap();
        parameters.set("restype", "topic");
        parameters.set("factoryclass", "javax.naming.spi.ObjectFactory");
        parameters.set("jndi_name", "custom_resource1");
        cr.getCommandInvocation("create-custom-resource", context.getActionReport(), adminSubject)
            .parameters(parameters).execute(createCommand);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }


    /**
     * Test of execute method, of class ListCustomResources.
     * delete-custom-resource Resource1
     * list-Custom-resources
     */
    @Test
    public void testExecuteSuccessListNoResource() {

        createCustomResource();

        org.glassfish.resources.admin.cli.ListCustomResources listCommand = locator.getService(org.glassfish.resources.admin.cli.ListCustomResources.class);
        cr.getCommandInvocation("list-custom-resources", context.getActionReport(), adminSubject).parameters(parameters).execute(listCommand);

        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertEquals(origNum + 1, list.size());
        origNum = origNum + 1; //as we newly created a resource after test "setup".

        deleteCustomResource();

        ParameterMap parameters = new ParameterMap();
        listCommand = locator.getService(org.glassfish.resources.admin.cli.ListCustomResources.class);
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(ListCustomResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        cr.getCommandInvocation("list-custom-resources", context.getActionReport(), adminSubject).parameters(parameters).execute(listCommand);

        list = context.getActionReport().getTopMessagePart().getChildren();
        if ((origNum - 1) == 0) {
            //Nothing to list.
        } else {
            assertEquals(origNum - 1, list.size());
        }
        List<String> listStr = new java.util.ArrayList<>();
        for (MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertFalse(listStr.contains("custom_resource1"));
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }
}
