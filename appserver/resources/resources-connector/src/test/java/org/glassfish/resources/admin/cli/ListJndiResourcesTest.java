/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resources.admin.cli;

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
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandContextImpl;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.resources.admin.cli.test.ResourcesJunit5Extension;
import org.glassfish.resources.config.ExternalJndiResource;
import org.glassfish.tests.utils.mock.MockGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.glassfish.api.ActionReport.ExitCode.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ResourcesJunit5Extension.class)
public class ListJndiResourcesTest {

    @Inject
    private ServiceLocator locator;
    @Inject
    private CommandRunner cr;
    @Inject
    private MockGenerator mockGenerator;

    private int origNum;
    private ParameterMap parameters;
    private AdminCommandContext context;
    private Subject adminSubject;


    @BeforeEach
    public void setUp() {
        cr = locator.getService(CommandRunner.class);
        context = new AdminCommandContextImpl(
            LogDomains.getLogger(ListJndiResourcesTest.class, LogDomains.ADMIN_LOGGER), new PropsFileActionReporter());
        parameters = new ParameterMap();
        Resources resources = locator.<Domain>getService(Domain.class).getResources();
        for (Resource resource : resources.getResources()) {
            if (resource instanceof ExternalJndiResource) {
                origNum = origNum + 1;
            }
        }
        adminSubject = mockGenerator.createAsadminSubject();
    }


    /**
     * Test of execute method, of class ListJndiResources.
     * list-jndi-resources
     */

    @Test
    public void testExecuteSuccessListOriginal() {
        ListJndiResources listCommand = locator.getService(ListJndiResources.class);
        cr.getCommandInvocation("list-jndi-resources", context.getActionReport(), adminSubject).parameters(parameters)
            .execute(listCommand);
        List<ActionReport.MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertEquals(origNum, list.size());
        assertEquals(SUCCESS, context.getActionReport().getActionExitCode(), context.getActionReport().getMessage());
    }

/**
     * Test of execute method, of class ListJndiResources.
     * create-jndi-resource ---restype=topic --factoryclass=javax.naming.spi.ObjectFactory --jndilookupname=sample_jndi
     * resource
     * list-jndi-resources
     */

    @Test
    public void testExecuteSuccessListResource() {
        createJndiResource();
        parameters = new ParameterMap();
        ListJndiResources listCommand = locator.getService(ListJndiResources.class);
        cr.getCommandInvocation("list-jndi-resources", context.getActionReport(), adminSubject).parameters(parameters)
            .execute(listCommand);
        List<ActionReport.MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertEquals(origNum + 1, list.size());
        List<String> listStr = new ArrayList<>();
        for (ActionReport.MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertTrue(listStr.contains("resource"));
        assertEquals(SUCCESS, context.getActionReport().getActionExitCode(), context.getActionReport().getMessage());

        deleteJndiResource();
    }


    private void createJndiResource() {
        parameters = new ParameterMap();
        parameters.set("restype", "topic");
        parameters.set("jndilookupname", "sample_jndi");
        parameters.set("factoryclass", "javax.naming.spi.ObjectFactory");
        parameters.set("jndi_name", "resource");
        CreateJndiResource createCommand = locator.getService(CreateJndiResource.class);
        cr.getCommandInvocation("create-jndi-resource", context.getActionReport(), adminSubject).parameters(parameters)
            .execute(createCommand);
        assertEquals(SUCCESS, context.getActionReport().getActionExitCode(), context.getActionReport().getMessage());
    }

    private void deleteJndiResource() {
        parameters = new ParameterMap();
        parameters.set("jndi_name", "resource");
        DeleteJndiResource deleteCommand = locator.getService(DeleteJndiResource.class);
        cr.getCommandInvocation("delete-jndi-resource", context.getActionReport(), adminSubject).parameters(parameters)
            .execute(deleteCommand);
        assertEquals(SUCCESS, context.getActionReport().getActionExitCode(), context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class ListJndiResource.
     * delete-jndi-resource resource
     * list-jndi-resources
     */
    @Test
    public void testExecuteSuccessListNoResource() {

        createJndiResource();

        parameters = new ParameterMap();
        ListJndiResources listCommand = locator.getService(ListJndiResources.class);
        cr.getCommandInvocation("list-jndi-resources", context.getActionReport(), adminSubject).parameters(parameters)
            .execute(listCommand);

        List<ActionReport.MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertEquals(origNum + 1, list.size());
        origNum = origNum + 1; //as we newly created a resource after test "setup".

        deleteJndiResource();

        ParameterMap params = new ParameterMap();
        listCommand = locator.getService(ListJndiResources.class);
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(ListJndiResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        cr.getCommandInvocation("list-jndi-resources", context.getActionReport(), adminSubject).parameters(params)
            .execute(listCommand);

        list = context.getActionReport().getTopMessagePart().getChildren();

        assertEquals(origNum - 1, list.size());
        List<String> listStr = new ArrayList<>();
        for (ActionReport.MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertFalse(listStr.contains("resource"));
        assertEquals(SUCCESS, context.getActionReport().getActionExitCode(), context.getActionReport().getMessage());
    }
}
