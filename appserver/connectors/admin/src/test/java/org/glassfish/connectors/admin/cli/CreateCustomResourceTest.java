/*
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
import org.glassfish.connectors.admin.cli.test.ConnectorsAdminJunit5Extension;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.resources.admin.cli.CreateCustomResource;
import org.glassfish.resources.admin.cli.DeleteCustomResource;
import org.glassfish.resources.config.CustomResource;
import org.glassfish.tests.utils.mock.MockGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.TransactionFailure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ConnectorsAdminJunit5Extension.class)
public class CreateCustomResourceTest {

    @Inject
    private ServiceLocator locator;
    @Inject
    private Logger logger;
    @Inject
    private MockGenerator mockGenerator;
    @Inject
    private CreateCustomResource command;
    @Inject
    private CommandRunner cr;
    private Resources resources;
    private AdminCommandContext context;
    private Subject adminSubject;


    @BeforeEach
    public void setUp() {
        assertNotNull(command);
        resources = locator.<Domain>getService(Domain.class).getResources();
        assertNotNull(resources);
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(CreateCustomResourceTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        adminSubject = mockGenerator.createAsadminSubject();
    }

    @AfterEach
    public void tearDown() throws TransactionFailure {
        DeleteCustomResource deleteCommand = locator.getService(DeleteCustomResource.class);
        ParameterMap parameters = new ParameterMap();
        parameters.set("jndi_name", "sample_custom_resource");
        cr.getCommandInvocation("delete-custom-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(deleteCommand);
        parameters = new ParameterMap();
        parameters.set("jndi_name", "dupRes");
        cr.getCommandInvocation("delete-custom-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(deleteCommand);
    }

    /**
     * Test of execute method, of class CreateCustomResource.
     * asadmin create-custom-resource --restype=topic --factoryclass=javax.naming.spi.ObjectFactory
     * sample_custom_resource
     */
    @Test
    public void testExecuteSuccess() {
        ParameterMap parameters = new ParameterMap();
        parameters.set("restype", "topic");
        parameters.set("factoryclass", "javax.naming.spi.ObjectFactory");
        parameters.set("jndi_name", "sample_custom_resource");

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("create-custom-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(command);

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        //Check that the resource was created
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof CustomResource) {
                CustomResource r = (CustomResource) resource;
                if (r.getJndiName().equals("sample_custom_resource")) {
                    assertEquals("topic", r.getResType());
                    assertEquals("javax.naming.spi.ObjectFactory", r.getFactoryClass());
                    assertEquals("true", r.getEnabled());
                    isCreated = true;
                    logger.fine("Custom Resource config bean sample_custom_resource is created.");
                    break;
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
                    if (ref.getRef().equals("sample_custom_resource")) {
                        assertEquals("true", ref.getEnabled());
                        isRefCreated = true;
                        break;
                    }
                }
            }
        }
        assertTrue(isRefCreated);
    }

    /**
     * Test of execute method, of class CreateCustomResource.
     * asadmin create-custom-resource --restype=topic --factoryclass=javax.naming.spi.ObjectFactory
     * dupRes
     * asadmin create-custom-resource --restype=topic --factoryclass=javax.naming.spi.ObjectFactory
     * dupRes
     */
    @Test
    public void testExecuteFailDuplicateResource() {
        ParameterMap parameters = new ParameterMap();
        parameters.set("restype", "topic");
        parameters.set("factoryclass", "javax.naming.spi.ObjectFactory");
        parameters.set("jndi_name", "dupRes");

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("create-custom-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(command);

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        //Check that the resource was created
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof CustomResource) {
                CustomResource jr = (CustomResource) resource;
                if (jr.getJndiName().equals("dupRes")) {
                    isCreated = true;
                    logger.fine("Custom Resource config bean dupRes is created.");
                    break;
                }
            }
        }
        assertTrue(isCreated);

        //Try to create a duplicate resource dupRes. Get a new instance of the command.
        CreateCustomResource command2 = locator.getService(CreateCustomResource.class);
        cr.getCommandInvocation("create-custom-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(command2);

        // Check the exit code is FAILURE
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());

        //Check that the 2nd resource was NOT created
        int numDupRes = 0;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof CustomResource) {
                CustomResource jr = (CustomResource) resource;
                if (jr.getJndiName().equals("dupRes")) {
                    numDupRes = numDupRes + 1;
                }
            }
        }
        assertEquals(1, numDupRes);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }


    /**
     * Test of execute method, of class CreateCustomResource.
     * asadmin create-custom-resource --restype=topic --factoryclass=javax.naming.spi.ObjectFactory
     * --enabled=false --description=Administered Object sample_custom_resource
     */
    @Test
    public void testExecuteWithOptionalValuesSet() {
        ParameterMap parameters = new ParameterMap();
        parameters.set("restype", "topic");
        parameters.set("factoryclass", "javax.naming.spi.ObjectFactory");
        parameters.set("enabled", "false");
        parameters.set("description", "Administered Object");
        parameters.set("jndi_name", "sample_custom_resource");

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("create-custom-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(command);

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        //Check that the resource was created
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof CustomResource) {
                CustomResource r = (CustomResource) resource;
                if (r.getJndiName().equals("sample_custom_resource")) {
                    assertEquals("topic", r.getResType());
                    assertEquals("javax.naming.spi.ObjectFactory", r.getFactoryClass());
                    //expect enabled for the resource to be true as resource-ref's enabled
                    //would be set to false
                    assertEquals("true", r.getEnabled());
                    assertEquals("Administered Object", r.getDescription());
                    isCreated = true;
                    logger.fine("Custom Resource config bean sample_custom_resource is created.");
                    break;
                }
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class CreateCustomResource.
     * asadmin create-custom-resource --factoryclass=javax.naming.spi.ObjectFactory
     * sample_custom_resource
     */
    @Test
    public void testExecuteFailInvalidResType() throws TransactionFailure {
        ParameterMap parameters = new ParameterMap();
        parameters.set("factoryclass", "javax.naming.spi.ObjectFactory");
        parameters.set("jndi_name", "sample_custom_resource");

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("create-custom-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(command);

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
    }


}
