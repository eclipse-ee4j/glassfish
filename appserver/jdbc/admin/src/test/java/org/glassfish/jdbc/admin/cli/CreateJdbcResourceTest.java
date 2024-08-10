/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jennifer
 */
@ExtendWith(JdbcAdminJunit5Extension.class)
public class CreateJdbcResourceTest {
    @Inject
    private ServiceLocator locator;
    @Inject
    private Logger logger;
    @Inject
    private MockGenerator mockGenerator;
    @Inject
    private CommandRunner cr;
    @Inject
    private CreateJdbcResource command;

    private Resources resources;
    private ParameterMap parameters = new ParameterMap();
    private AdminCommandContext context;
    private Subject adminSubject;

    @BeforeEach
    public void setUp() {
        assertNotNull(command);
        resources = locator.<Domain>getService(Domain.class).getResources();
        assertNotNull(resources);

        // Set the options and operand to pass to the command
        parameters.set("connectionpoolid", "DerbyPool");
        parameters.set("enabled", "true");
        parameters.set("description", "my resource");
        parameters.set("DEFAULT", "jdbc/foo");

        context = new AdminCommandContextImpl(
                LogDomains.getLogger(CreateJdbcResourceTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());

        adminSubject = mockGenerator.createAsadminSubject();
    }

    @AfterEach
    public void tearDown() throws TransactionFailure {
        // Delete the created resource
        SingleConfigCode<Resources> configCode = resourcesBean -> {
            Resource target = null;
            // TODO: this causes NoSuchElementException but really
            // it should have caused ConcurrentModificationException, because the iteration
            // and removal runs at the same time.
            for (Resource resource : resourcesBean.getResources()) {
                if (resource instanceof JdbcResource) {
                    JdbcResource jr = (JdbcResource) resource;
                    if (jr.getJndiName().equals("jdbc/foo") || jr.getJndiName().equals("dupRes")
                        || jr.getJndiName().equals("jdbc/sun") || jr.getJndiName().equals("jdbc/alldefaults")
                        || jr.getJndiName().equals("jdbc/junk")) {
                        target = resource;
                        break;
                    }
                }
            }
            if (target != null) {
                resourcesBean.getResources().remove(target);
            }
            return null;
        };
        ConfigSupport.apply(configCode, resources);
    }

    /**
     * Test of execute method, of class CreateJdbcResource.
     * asadmin create-jdbc-resource --connectionpoolid DerbyPool --enabled=true
     *        --description "my resource" jdbc/foo
     */
    @Test
    public void testExecuteSuccess() {
        // Set operand
        parameters.set("DEFAULT", "jdbc/foo");

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("create-jdbc-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(command);

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        //Check that the resource was created
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof JdbcResource) {
                JdbcResource jr = (JdbcResource)resource;
                if (jr.getJndiName().equals("jdbc/foo")) {
                    assertEquals("DerbyPool", jr.getPoolName());
                    assertEquals("true", jr.getEnabled());
                    assertEquals("my resource", jr.getDescription());
                    isCreated = true;
                    logger.fine("JdbcResource config bean jdbc/foo is created.");
                    continue;
                }
            }
        }
        assertTrue(isCreated);

        logger.fine("msg: " + context.getActionReport().getMessage());

        // Check resource-ref created
        Servers servers = locator.getService(Servers.class);
        boolean isRefCreated = false;
        for (Server server : servers.getServer()) {
            if (server.getName().equals(SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)) {
                for (ResourceRef ref : server.getResourceRef()) {
                    if (ref.getRef().equals("jdbc/foo")) {
                        assertEquals("true", ref.getEnabled());
                        isRefCreated = true;
                        continue;
                    }
                }
            }
        }
        assertTrue(isRefCreated);
    }

    /**
     * Test of execute method, of class CreateJdbcResource.
     * asadmin create-jdbc-resource --connectionpoolid DerbyPool jdbc/alldefaults
     */
    @Test
    public void testExecuteSuccessDefaultValues() {
        // Only pass the required option and operand
        parameters = new ParameterMap();
        parameters.set("connectionpoolid", "DerbyPool");
        parameters.set("DEFAULT", "jdbc/alldefaults");


        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("create-jdbc-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(command);

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        //Check that the resource was created
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof JdbcResource) {
                JdbcResource jr = (JdbcResource)resource;
                if (jr.getJndiName().equals("jdbc/alldefaults")) {
                    assertEquals("DerbyPool", jr.getPoolName());
                    assertEquals("true", jr.getEnabled());
                    assertNull(jr.getDescription());
                    isCreated = true;
                    logger.fine("JdbcResource config bean jdbc/alldefaults is created.");
                    continue;
                }
            }
        }
        assertTrue(isCreated);

        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class CreateJdbcResource.
     * asadmin create-jdbc-resource --connectionpoolid DerbyPool --enabled=true
     *         --description "my resource" dupRes
     * asadmin create-jdbc-resource --connectionpoolid DerbyPool --enabled=true
     *         --description "my resource" dupRes
     */
    @Test
    public void testExecuteFailDuplicateResource() {
        // Set operand
        parameters.set("DEFAULT", "dupRes");

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("create-jdbc-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(command);

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        //Check that the resource was created
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof JdbcResource) {
                JdbcResource jr = (JdbcResource)resource;
                if (jr.getJndiName().equals("dupRes")) {
                    isCreated = true;
                    logger.fine("JdbcResource config bean dupRes is created.");
                    continue;
                }
            }
        }
        assertTrue(isCreated);

        //Try to create a duplicate resource dupRes. Get a new instance of the command.
        CreateJdbcResource command2 = locator.getService(CreateJdbcResource.class);
        cr.getCommandInvocation("create-jdbc-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(command2);

        // Check the exit code is FAILURE
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());

        //Check that the 2nd resource was NOT created
        int numDupRes = 0;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof JdbcResource) {
                JdbcResource jr = (JdbcResource)resource;
                if (jr.getJndiName().equals("dupRes")) {
                    numDupRes = numDupRes + 1;
                }
            }
        }
        assertEquals(1, numDupRes);

        // Check the error message
        assertEquals("A JdbcResource by name dupRes already exists with resource-ref in target server.", context.getActionReport().getMessage());
        logger.fine("msg: " + context.getActionReport().getMessage());
    }


    /**
     * Test of execute method, of class CreateJdbcResource when specified
     * connectionpoolid does not exist.
     * asadmin create-jdbc-resource --connectionpoolid xxxxxx --enabled=true
     *         --description "my resource" jdbc/nopool
     */
    @Test
    public void testExecuteFailInvalidConnPoolId() {
        // Set invalid connectionpoolid
        parameters.set("connectionpoolid", "xxxxxx");
        parameters.set("DEFAULT", "jdbc/nopool");

        // Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("create-jdbc-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(command);

        // Check the exit code is Failure
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());

        //Check that the resource was NOT created
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof JdbcResource) {
                JdbcResource jr = (JdbcResource)resource;
                if (jr.getJndiName().equals("jdbc/nopool")) {
                    isCreated = true;
                    logger.fine("JdbcResource config bean jdbc/nopool is created.");
                }
            }
        }
        assertFalse(isCreated);

        // Check the error message
        assertEquals("Attribute value (pool-name = xxxxxx) is not found in list of jdbc connection pools.",
        context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class CreateJdbcResource when enabled set to junk
     * asadmin create-jdbc-resource --connectionpoolid DerbyPool --enabled=junk
     *         --description "my resource" jdbc/junk
     */
    @Test
    public void testExecuteFailInvalidOptionEnabled() {
        // Set invalid enabled option value: --enabled junk
        parameters.set("enabled", "junk");
        parameters.set("DEFAULT", "jdbc/junk");

        // Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("create-jdbc-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(command);

        // Check the exit code is Failure
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
        // Don't check error message.  Error message being set by CommandRunnerImpl.
    }

    /**
     * Test of execute method, of class CreateJdbcResource when enabled has no value
     * asadmin create-jdbc-resource --connectionpoolid DerbyPool --enabled
     *         --description "my resource" jdbc/sun
     */
    @Test
    public void testExecuteSuccessNoValueOptionEnabled() {
        // Set enabled without a value:  --enabled
        parameters.set("enabled", "");
        parameters.set("DEFAULT", "jdbc/sun");

        // Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("create-jdbc-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(command);

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        // Check that the resource was created with enabled set to true
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof JdbcResource) {
                JdbcResource jr = (JdbcResource)resource;
                if (jr.getJndiName().equals("jdbc/sun")) {
                    assertEquals("DerbyPool", jr.getPoolName());
                    assertEquals("true", jr.getEnabled());
                    assertEquals("my resource", jr.getDescription());
                    isCreated = true;
                    continue;
                }
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }
}
