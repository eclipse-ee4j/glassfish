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

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.logging.LogDomains;


import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandContextImpl;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jdbc.config.JdbcResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import org.glassfish.api.ActionReport;
import org.glassfish.tests.utils.Utils;
import org.glassfish.tests.utils.ConfigApiTest;
import org.jvnet.hk2.config.DomDocument;

/**
 *
 * @author Jennifer
 */
//@Ignore // temporarily disabled
public class DeleteJdbcResourceTest extends ConfigApiTest {
    ServiceLocator habitat = Utils.instance.getHabitat(this);
    private Resources resources = habitat.<Domain>getService(Domain.class).getResources();
    private DeleteJdbcResource deleteCommand = null;
    private ParameterMap parameters = new ParameterMap();
    private AdminCommandContext context = null;
    private CommandRunner cr = habitat.getService(CommandRunner.class);

    @Override
    public DomDocument getDocument(ServiceLocator habitat) {

        return new TestDocument(habitat);
    }

    /**
     * Returns the DomainTest file name without the .xml extension to load the test configuration
     * from.
     *
     * @return the configuration file name
     */
    public String getFileName() {
        return "DomainTest";
    }

    @Before
    public void setUp() {
        assertTrue(resources!=null);

        // Create a JDBC Resource jdbc/foo for each test
        CreateJdbcResource createCommand = habitat.getService(CreateJdbcResource.class);
        assertTrue(createCommand!=null);

        parameters.add("connectionpoolid", "DerbyPool");
        parameters.add("DEFAULT", "jdbc/foo");

        context = new AdminCommandContextImpl(
                LogDomains.getLogger(DeleteJdbcResourceTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());

        cr.getCommandInvocation("create-jdbc-resource", context.getActionReport(), adminSubject()).parameters(parameters).execute(createCommand);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        // Setup for delete-jdbc-resource
        parameters = new ParameterMap();
        deleteCommand = habitat.getService(DeleteJdbcResource.class);
        assertTrue(deleteCommand!=null);
    }

    @After
    public void tearDown() {
        // Cleanup any leftover jdbc/foo resource - could be success or failure depending on the test
        parameters = new ParameterMap();
        parameters.add("DEFAULT", "jdbc/foo");
        cr.getCommandInvocation("delete-jdbc-resource", context.getActionReport(), adminSubject()).parameters(parameters).execute(deleteCommand);
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
        cr.getCommandInvocation("delete-jdbc-resource", context.getActionReport(), adminSubject()).parameters(parameters).execute(deleteCommand);

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
            if (server.getName().equals(SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)) {
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
        parameters.add("target", "server");
        parameters.add("DEFAULT", "jdbc/foo");

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("delete-jdbc-resource", context.getActionReport(), adminSubject()).parameters(parameters).execute(deleteCommand);

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
            if (server.getName().equals(SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)) {
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
        cr.getCommandInvocation("delete-jdbc-resource", context.getActionReport(), adminSubject()).parameters(parameters).execute(deleteCommand);

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
    @Ignore
    @Test
    public void testExecuteFailNoOperand() {
        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("delete-jdbc-resource", context.getActionReport(), adminSubject()).parameters(parameters).execute(deleteCommand);

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
    @Ignore
    @Test
    public void testExecuteFailInvalidOption() {
        // Set operand
        parameters.add("invalid", "");
        parameters.add("DEFAULT", "jdbc/foo");

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("delete-jdbc-resource", context.getActionReport(), adminSubject()).parameters(parameters).execute(deleteCommand);

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
    @Ignore
    //disabling the test.
    //in v3, this test was expecting the Command to return failure code.
    //in 3.1 --target validation is done by CLI framework (as part of command replication)
    //as of now command replication is not enabled by default and as a result,
    //the modified command does not fail when an invalid target is specified
    public void testExecuteFailInvalidTarget() {
        // Set operand
        parameters.add("target", "invalid");
        parameters.add("DEFAULT", "jdbc/foo");

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("delete-jdbc-resource", context.getActionReport(), adminSubject()).parameters(parameters).execute(deleteCommand);

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
        //assertFalse(isDeleted);

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
            if (server.getName().equals(SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)) {
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
