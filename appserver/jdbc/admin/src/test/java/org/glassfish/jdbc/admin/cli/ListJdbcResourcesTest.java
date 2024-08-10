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

package org.glassfish.jdbc.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.MessagePart;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandContextImpl;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jdbc.admin.cli.test.JdbcAdminJunit5Extension;
import org.glassfish.jdbc.config.JdbcResource;
import org.glassfish.tests.utils.mock.MockGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jennifer
 */
@ExtendWith(JdbcAdminJunit5Extension.class)
public class ListJdbcResourcesTest {
    @Inject
    private ServiceLocator habitat;
    @Inject
    private Logger logger;
    @Inject
    private MockGenerator mockGenerator;
    @Inject
    private CommandRunner cr;

    private Resources resources;
    private int origNum;
    private ParameterMap parameters;
    private CreateJdbcResource createCommand;
    private DeleteJdbcResource deleteCommand;
    private ListJdbcResources listCommand;
    private AdminCommandContext context;
    private Subject adminSubject;


    @BeforeEach
    public void setUp() {
        parameters = new ParameterMap();
        resources = habitat.<Domain>getService(Domain.class).getResources();
        for (Resource resource : resources.getResources()) {
            if (resource instanceof JdbcResource) {
                origNum = origNum + 1;
            }
        }
        adminSubject = mockGenerator.createAsadminSubject();
    }

    /**
     * Test of execute method, of class ListJdbcResources.
     * list-jdbc-resources
     */
    @Test
    public void testExecuteSuccessListOriginal() {
        listCommand = habitat.getService(ListJdbcResources.class);
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(ListJdbcResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("list-jdbc-resources", context.getActionReport(), adminSubject).parameters(parameters).execute(listCommand);

        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertThat(list, hasSize(origNum));

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }

    /**
     * Test of execute method, of class ListJdbcResource.
     * list-jdbc-resources server
     */
    @Test
    public void testExecuteSuccessValidTargetOperand() {
        // List the original set of JDBC Resources
        //Get an instance of the ListJdbcResources command
        listCommand = habitat.getService(ListJdbcResources.class);

        parameters.add("DEFAULT", "server");

        context = new AdminCommandContextImpl(
                LogDomains.getLogger(ListJdbcResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("list-jdbc-resources", context.getActionReport(), adminSubject).parameters(parameters).execute(listCommand);

        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertThat(list, hasSize(origNum));

        // Check the exit code is Success
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }

    /**
     * Test of execute method, of class ListJdbcResource.
     * create-jdbc-resource --connectionpoolid DerbyPool bob
     * list-jdbc-resources
     */
    @Test
    public void testExecuteSuccessListBob() {
        // Create JDBC Resource bob
        assertTrue(resources!=null);

        //Get an instance of the CreateJdbcResource command
        createCommand = habitat.getService(CreateJdbcResource.class);
        assertTrue(createCommand!=null);

        parameters.add("connectionpoolid", "DerbyPool");
        parameters.add("DEFAULT", "bob");

        context = new AdminCommandContextImpl(
                LogDomains.getLogger(ListJdbcResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());

        cr.getCommandInvocation("create-jdbc-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(createCommand);

        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        // List JDBC Resources and check if bob is in the list
        //Get an instance of the ListJdbcResources command
        listCommand = habitat.getService(ListJdbcResources.class);
        parameters = new ParameterMap();
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(ListJdbcResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("list-jdbc-resources", context.getActionReport(), adminSubject).parameters(parameters).execute(listCommand);

        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();

        assertThat(list, hasSize(origNum + 1));

        List<String> listStr = new java.util.ArrayList();
        for (MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertTrue(listStr.contains("bob"));

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }

    /**
     * Test of execute method, of class ListJdbcResource.
     * delete-jdbc-resource bob
     * list-jdbc-resources
     */
    @Test
    public void testExecuteSuccessListNoBob() {
        // Create JDBC Resource bob
        assertTrue(resources!=null);

        //Get an instance of the CreateJdbcResource command
        createCommand = habitat.getService(CreateJdbcResource.class);
        assertTrue(createCommand!=null);

        parameters.add("connectionpoolid", "DerbyPool");
        parameters.add("DEFAULT", "bob2");

        context = new AdminCommandContextImpl(
                LogDomains.getLogger(ListJdbcResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());

        cr.getCommandInvocation("create-jdbc-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(createCommand);

        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        // Delete JDBC Resource bob
        //assertTrue(resources!=null);

        //Get an instance of the CreateJdbcResource command
        deleteCommand = habitat.getService(DeleteJdbcResource.class);
        assertTrue(deleteCommand!=null);

        parameters = new ParameterMap();
        parameters.add("DEFAULT", "bob2");

        cr.getCommandInvocation("delete-jdbc-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(deleteCommand);

        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        // List JDBC Resources and check if bob is in the list
        //Get an instance of the ListJdbcResources command
        listCommand = habitat.getService(ListJdbcResources.class);
        parameters = new ParameterMap();
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(ListJdbcResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("list-jdbc-resources", context.getActionReport(), adminSubject).parameters(parameters).execute(listCommand);

        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();

        int numResources = 0;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof JdbcResource) {
                numResources = numResources + 1;
            }
        }

        assertThat(list, hasSize(origNum));

        List<String> listStr = new ArrayList<>();
        for (MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertFalse(listStr.contains("bob2"));

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }

   /**
     * Test of execute method, of class ListJdbcResource.
     * list-jdbc-resources invalid
     */
    @Test
    public void testExecuteFailInvalidTargetOperand() {
        // List the original set of JDBC Resources
        //Get an instance of the ListJdbcResources command
        listCommand = habitat.getService(ListJdbcResources.class);

        parameters.add("DEFAULT", "invalid");

        context = new AdminCommandContextImpl(
                LogDomains.getLogger(ListJdbcResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());

        //Call CommandRunnerImpl.doCommand(..) to execute the command
       cr.getCommandInvocation("list-jdbc-resources", context.getActionReport(), adminSubject).parameters(parameters).execute(listCommand);

        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertThat(list, hasSize(0));

        // Check the exit code is FAILURE
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
    }

    /**
     * Test of execute method, of class ListJdbcResource.
     * list-jdbc-resources --invalid invalid
     */
    @Test
    public void testExecuteFailInvalidOption() {
        listCommand = habitat.getService(ListJdbcResources.class);
        parameters.add("invalid", "invalid");
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(ListJdbcResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());

        cr.getCommandInvocation("list-jdbc-resources", context.getActionReport(), adminSubject).parameters(parameters).execute(listCommand);

        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertThat(list, hasSize(1));

        for (MessagePart mp : list) {
            assertEquals("Usage: list-jdbc-resources [target] ", mp.getMessage());
        }
        // Check the exit code is FAILURE
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
    }

}
