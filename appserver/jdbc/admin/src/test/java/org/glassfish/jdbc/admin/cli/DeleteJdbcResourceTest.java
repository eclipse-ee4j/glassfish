/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;

import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandContextImpl;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jdbc.admin.cli.test.JdbcAdminJunit5Extension;
import org.glassfish.jdbc.config.JdbcResource;
import org.glassfish.tests.utils.mock.MockGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Jennifer
 */
@ExtendWith(JdbcAdminJunit5Extension.class)
public class DeleteJdbcResourceTest {
    @Inject
    private ServiceLocator habitat;
    @Inject
    private Logger logger;
    @Inject
    private MockGenerator mockGenerator;
    @Inject
    private CommandRunner cr;
    @Inject
    private DeleteJdbcResource deleteCommand;

    private Resources resources;
    private ParameterMap parameters = new ParameterMap();
    private AdminCommandContext context;
    private Subject adminSubject;

    @BeforeEach
    public void setUp() {
        resources = habitat.<Domain>getService(Domain.class).getResources();
        assertNotNull(resources);

        // Create a JDBC Resource jdbc/foo for each test
        CreateJdbcResource createCommand = habitat.getService(CreateJdbcResource.class);
        assertNotNull(createCommand);

        parameters.add("connectionpoolid", "DerbyPool");
        parameters.add("DEFAULT", "jdbc/foo");
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(DeleteJdbcResourceTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        adminSubject = mockGenerator.createAsadminSubject();
        cr.getCommandInvocation("create-jdbc-resource", context.getActionReport(), adminSubject).parameters(parameters)
            .execute(createCommand);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        // Setup for delete-jdbc-resource
        parameters = new ParameterMap();
        assertNotNull(deleteCommand);
    }

    @AfterEach
    public void tearDown() {
        // Cleanup any leftover jdbc/foo resource - could be success or failure depending on the test
        parameters = new ParameterMap();
        parameters.add("DEFAULT", "jdbc/foo");
        cr.getCommandInvocation("delete-jdbc-resource", context.getActionReport(), adminSubject).parameters(parameters)
            .execute(deleteCommand);
    }

    /**
     * Test of execute method, of class DeleteJdbcResource.
     * delete-jdbc-resource jdbc/foo
     */
    @Test
    public void testExecuteSuccessDefaultTarget() {
        // Set operand
        parameters.add("DEFAULT", "jdbc/foo");

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("delete-jdbc-resource", context.getActionReport(), adminSubject).parameters(parameters)
            .execute(deleteCommand);

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        //Check that the resource was deleted
        boolean isDeleted = true;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof JdbcResource) {
                JdbcResource jr = (JdbcResource)resource;
                if (jr.getJndiName().equals("jdbc/foo")) {
                    isDeleted = false;
                    logger.fine("JdbcResource config bean jdbc/foo is deleted.");
                    continue;
                }
            }
        }
        assertTrue(isDeleted);
        logger.fine("msg: " + context.getActionReport().getMessage());

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        //Check that the resource ref was deleted
        Servers servers = habitat.getService(Servers.class);
        boolean isRefDeleted = true;
        for (Server server : servers.getServer()) {
            if (server.getName().equals(SystemPropertyConstants.DAS_SERVER_NAME)) {
                for (ResourceRef ref : server.getResourceRef()) {
                    if (ref.getRef().equals("jdbc/foo")) {
                        isRefDeleted = false;
                        continue;
                    }
                }
            }
        }
        assertTrue(isRefDeleted);
    }

    /**
     * Test of execute method, of class DeleteJdbcResource.
     * delete-jdbc-resource --target server jdbc/foo
     */
    @Test
    public void testExecuteSuccessTargetServer() {
        // Set operand
        parameters.add("DEFAULT", "jdbc/foo");

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("delete-jdbc-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(deleteCommand);

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        //Check that the resource was deleted
        boolean isDeleted = true;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof JdbcResource) {
                JdbcResource jr = (JdbcResource)resource;
                if (jr.getJndiName().equals("jdbc/foo")) {
                    isDeleted = false;
                    logger.fine("JdbcResource config bean jdbc/foo is deleted.");
                    continue;
                }
            }
        }
        assertTrue(isDeleted);
        logger.fine("msg: " + context.getActionReport().getMessage());

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        //Check that the resource ref was deleted
        Servers servers = habitat.getService(Servers.class);
        boolean isRefDeleted = true;
        for (Server server : servers.getServer()) {
            if (server.getName().equals(SystemPropertyConstants.DAS_SERVER_NAME)) {
                for (ResourceRef ref : server.getResourceRef()) {
                    if (ref.getRef().equals("jdbc/foo")) {
                        isRefDeleted = false;
                        continue;
                    }
                }
            }
        }
        assertTrue(isRefDeleted);
    }

    /**
     * Test of execute method, of class DeleteJdbcResource.
     * delete-jdbc-resource doesnotexist
     */
    @Test
    public void testExecuteFailDoesNotExist() {
        // Set operand
        parameters.add("DEFAULT", "doesnotexist");

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("delete-jdbc-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(deleteCommand);

        // Check the exit code is FAILURE
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());

        // Check the error message
        assertEquals("A JDBC resource named doesnotexist does not exist.", context.getActionReport().getMessage());
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class DeleteJdbcResource.
     * delete-jdbc-resource
     */
    @Test
    @Disabled("Results in 'Cannot find jndiName in delete-jdbc-resource command model, file a bug'")
    public void testExecuteFailNoOperand() {
        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("delete-jdbc-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(deleteCommand);

        // Check the exit code is FAILURE
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
        // Check the error message
        assertEquals("Operand required.", context.getActionReport().getMessage());
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class DeleteJdbcResource.
     * delete-jdbc-resource --invalid jdbc/foo
     */
    @Test
    @Disabled("The action report error message contains weird characters (EOL, percents)")
    public void testExecuteFailInvalidOption() {
        // Set operand
        parameters.add("invalid", "");
        parameters.add("DEFAULT", "jdbc/foo");

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("delete-jdbc-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(deleteCommand);

        // Check the exit code is FAILURE
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
        // Check the error message
        assertEquals(" Invalid option: invalid", context.getActionReport().getMessage());
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class DeleteJdbcResource.
     * delete-jdbc-resource --target invalid jdbc/foo
     */
    @Test
    public void testExecuteFailInvalidTarget() {
        // Set operand
        parameters.add("target", "invalid");
        parameters.add("DEFAULT", "jdbc/foo");

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("delete-jdbc-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(deleteCommand);

        //Check that the resource was NOT deleted
        boolean isDeleted = true;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof JdbcResource) {
                JdbcResource jr = (JdbcResource)resource;
                if (jr.getJndiName().equals("jdbc/foo")) {
                    isDeleted = false;
                    logger.fine("JdbcResource config bean jdbc/foo is not deleted.");
                    continue;
                }
            }
        }
        // Need bug fix in DeleteJdbcResource before uncommenting assertion
        assertFalse(isDeleted);

        // Check the exit code is FAILURE
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
        // Check the error message
        // Need bug fix in DeleteJdbcResource before uncommenting assertion
        //assertEquals(" Invalid target: invalid", context.getActionReport().getMessage());
        logger.fine("msg: " + context.getActionReport().getMessage());

        //Check that the resource ref was NOT deleted
        Servers servers = habitat.getService(Servers.class);
        boolean isRefDeleted = true;
        for (Server server : servers.getServer()) {
            if (server.getName().equals(SystemPropertyConstants.DAS_SERVER_NAME)) {
                for (ResourceRef ref : server.getResourceRef()) {
                    if (ref.getRef().equals("jdbc/foo")) {
                        isRefDeleted = false;
                        continue;
                    }
                }
            }
        }
        assertFalse(isRefDeleted);
    }
}
