/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandContextImpl;
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
import org.glassfish.tests.utils.ConfigApiTest;

public class ListJndiResourcesTest extends ConfigApiTest {

    private ServiceLocator habitat;
    private int origNum = 0;
    private ParameterMap parameters;
    AdminCommandContext context;
    CommandRunner cr;

    public DomDocument getDocument(ServiceLocator habitat) {
        return new TestDocument(habitat);
    }

    public String getFileName() {
        return "DomainTest";
    }

    @Before
    public void setUp() {
        habitat = getHabitat();
        cr = habitat.getService(CommandRunner.class);
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(ListJndiResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        parameters = new ParameterMap();
        Resources resources = habitat.<Domain>getService(Domain.class).getResources();
        for (Resource resource : resources.getResources()) {
            if (resource instanceof org.glassfish.resources.config.ExternalJndiResource) {
                origNum = origNum + 1;
            }
        }
    }

    @After
    public void tearDown() {

    }

    /**
     * Test of execute method, of class ListJndiResources.
     * list-jndi-resources
     */

    @Test
    public void testExecuteSuccessListOriginal() {
        org.glassfish.resources.admin.cli.ListJndiResources listCommand = habitat.getService(org.glassfish.resources.admin.cli.ListJndiResources.class);
        cr.getCommandInvocation("list-jndi-resources", context.getActionReport(), adminSubject()).parameters(parameters).execute(listCommand);
        List<ActionReport.MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        if (origNum == 0) {
            //Nothing to list.
        } else {
            assertEquals(origNum, list.size());
        }
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
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
        org.glassfish.resources.admin.cli.ListJndiResources listCommand = habitat.getService(org.glassfish.resources.admin.cli.ListJndiResources.class);
        cr.getCommandInvocation("list-jndi-resources", context.getActionReport(), adminSubject()).parameters(parameters).execute(listCommand);
        List<ActionReport.MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertEquals(origNum + 1, list.size());
        List<String> listStr = new ArrayList<String>();
        for (ActionReport.MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertTrue(listStr.contains("resource"));
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        deleteJndiResource();
    }


    private void createJndiResource() {
        parameters = new ParameterMap();
        parameters.set("restype", "topic");
        parameters.set("jndilookupname", "sample_jndi");
        parameters.set("factoryclass", "javax.naming.spi.ObjectFactory");
        parameters.set("jndi_name", "resource");
        org.glassfish.resources.admin.cli.CreateJndiResource createCommand = habitat.getService(org.glassfish.resources.admin.cli.CreateJndiResource.class);
        cr.getCommandInvocation("create-jndi-resource", context.getActionReport(), adminSubject()).parameters(parameters).execute(createCommand);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }

    private void deleteJndiResource() {
        parameters = new ParameterMap();
        parameters.set("jndi_name", "resource");
        org.glassfish.resources.admin.cli.DeleteJndiResource deleteCommand = habitat.getService(org.glassfish.resources.admin.cli.DeleteJndiResource.class);
        cr.getCommandInvocation("delete-jndi-resource", context.getActionReport(), adminSubject()).parameters(parameters).execute(deleteCommand);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
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
        org.glassfish.resources.admin.cli.ListJndiResources listCommand = habitat.getService(org.glassfish.resources.admin.cli.ListJndiResources.class);
        cr.getCommandInvocation("list-jndi-resources", context.getActionReport(), adminSubject()).parameters(parameters).execute(listCommand);

        List<ActionReport.MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertEquals(origNum + 1, list.size());
        origNum = origNum + 1; //as we newly created a resource after test "setup".


        deleteJndiResource();

        ParameterMap parameters = new ParameterMap();
        listCommand = habitat.getService(org.glassfish.resources.admin.cli.ListJndiResources.class);
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(ListJndiResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        cr.getCommandInvocation("list-jndi-resources", context.getActionReport(), adminSubject()).parameters(parameters).execute(listCommand);

        list = context.getActionReport().getTopMessagePart().getChildren();

        if ((origNum - 1) == 0) {
            //Nothing to list.
        } else {
            assertEquals(origNum - 1, list.size());
        }
        List<String> listStr = new ArrayList<String>();
        for (ActionReport.MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertFalse(listStr.contains("resource"));
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }
}
