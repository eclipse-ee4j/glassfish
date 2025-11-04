/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.resources.admin.cli;

import com.sun.enterprise.config.serverbeans.BindableResource;
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

import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandContextImpl;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.resources.admin.cli.test.ResourcesJunit5Extension;
import org.glassfish.resources.config.ExternalJndiResource;
import org.glassfish.tests.utils.mock.MockGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.TransactionFailure;

import static org.glassfish.api.ActionReport.ExitCode.FAILURE;
import static org.glassfish.api.ActionReport.ExitCode.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(ResourcesJunit5Extension.class)
public class CreateJndiResourceTest {

    @Inject
    private ServiceLocator habitat;
    @Inject
    private Logger logger;
    @Inject
    private MockGenerator mockGenerator;
    @Inject
    private CommandRunner cr;
    @Inject
    private Server server;

    private Resources resources;
    private ParameterMap parameters;
    private AdminCommandContext context;
    private Subject adminSubject;

    @BeforeEach
    public void setUp() {
        resources = habitat.<Domain>getService(Domain.class).getResources();
        parameters = new ParameterMap();
        context = new AdminCommandContextImpl(
            LogDomains.getLogger(CreateJndiResourceTest.class, LogDomains.ADMIN_LOGGER), new PropsFileActionReporter());
        adminSubject = mockGenerator.createAsadminSubject();
    }

    @AfterEach
    public void tearDown() throws TransactionFailure {
        DeleteJndiResource deleteCommand = habitat.getService(DeleteJndiResource.class);
        parameters = new ParameterMap();
        parameters.set("jndi_name", "sample_jndi_resource");
        cr.getCommandInvocation("delete-jndi-resource", context.getActionReport(), adminSubject).parameters(parameters)
            .execute(deleteCommand);
        parameters = new ParameterMap();
        parameters.set("jndi_name", "dupRes");
        cr.getCommandInvocation("delete-jndi-resource", context.getActionReport(), adminSubject).parameters(parameters)
            .execute(deleteCommand);
    }

    /**
     * Test of execute method, of class CreateJndiResource.
     * asadmin create-jndi-resource --restype=queue --factoryclass=sampleClass --jndilookupname=sample_jndi
     * sample_jndi_resource
     */
    @Test
    public void testExecuteSuccess() {
        parameters.set("jndilookupname", "sample_jndi");
        parameters.set("restype", "queue");
        parameters.set("factoryclass", "sampleClass");
        parameters.set("jndi_name", "sample_jndi_resource");
        CreateJndiResource command = habitat.getService(CreateJndiResource.class);
        cr.getCommandInvocation("create-jndi-resource", context.getActionReport(), adminSubject).parameters(parameters)
            .execute(command);
        assertEquals(SUCCESS, context.getActionReport().getActionExitCode(), context.getActionReport().getMessage());
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof ExternalJndiResource) {
                ExternalJndiResource r = (ExternalJndiResource) resource;
                if (r.getJndiName().equals("sample_jndi_resource")) {
                    assertEquals("queue", r.getResType());
                    assertEquals("sample_jndi", r.getJndiLookupName());
                    assertEquals("sampleClass", r.getFactoryClass());
                    assertEquals("true", r.getEnabled());
                    isCreated = true;
                    logger.fine("Jndi Resource config bean sample_jndi_resource is created.");
                    break;
                }
            }
        }
        assertTrue(isCreated);
        Servers servers = habitat.getService(Servers.class);
        boolean isRefCreated = false;
        for (Server server : servers.getServer()) {
            if (server.getName().equals(SystemPropertyConstants.DAS_SERVER_NAME)) {
                for (ResourceRef ref : server.getResourceRef()) {
                    if (ref.getRef().equals("sample_jndi_resource")) {
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
     * Test of execute method, of class CreateJndiResource.
     * asadmin create-jndi-resource --restype=queue --factoryclass=sampleClass --jndilookupname=sample_jndi
     * dupRes
     * asadmin create-jndi-resource --restype=queue --factoryclass=sampleClass --jndilookupname=sample_jndi
     * dupRes
     */
    @Test
    public void testExecuteFailDuplicateResource() {
        parameters.set("jndilookupname", "sample_jndi");
        parameters.set("restype", "queue");
        parameters.set("factoryclass", "sampleClass");
        parameters.set("jndi_name", "dupRes");
        CreateJndiResource command1 = habitat.getService(CreateJndiResource.class);
        cr.getCommandInvocation("create-jndi-resource", context.getActionReport(), adminSubject).parameters(parameters)
            .execute(command1);
        assertEquals(SUCCESS, context.getActionReport().getActionExitCode(), context.getActionReport().getMessage());
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof BindableResource) {
                BindableResource jr = (BindableResource) resource;
                if (jr.getJndiName().equals("dupRes")) {
                    isCreated = true;
                    logger.fine("Jndi Resource config bean dupRes is created.");
                    break;
                }
            }
        }
        assertTrue(isCreated);

        CreateJndiResource command2 = habitat.getService(CreateJndiResource.class);
        cr.getCommandInvocation("create-jndi-resource", context.getActionReport(), adminSubject).parameters(parameters)
            .execute(command2);
        assertEquals(FAILURE, context.getActionReport().getActionExitCode(), context.getActionReport().getMessage());
        int numDupRes = 0;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof BindableResource) {
                BindableResource jr = (BindableResource) resource;
                if (jr.getJndiName().equals("dupRes")) {
                    numDupRes = numDupRes + 1;
                }
            }
        }
        assertEquals(1, numDupRes);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class CreateJndiResource.
     * asadmin create-jndi-resource --restype=queue --factoryclass=sampleClass --jndilookupname=sample_jndi
     * --enabled=false --description=External JNDI Resource
     * sample_jndi_resource
     */
    @Test
    public void testExecuteWithOptionalValuesSet() {
        parameters.set("jndilookupname", "sample_jndi");
        parameters.set("restype", "queue");
        parameters.set("factoryclass", "sampleClass");
        parameters.set("enabled", "false");
        parameters.set("description", "External JNDI Resource");
        parameters.set("jndi_name", "sample_jndi_resource");
        CreateJndiResource command = habitat.getService(CreateJndiResource.class);
        cr.getCommandInvocation("create-jndi-resource", context.getActionReport(), adminSubject).parameters(parameters)
            .execute(command);
        assertEquals(SUCCESS, context.getActionReport().getActionExitCode(), context.getActionReport().getMessage());
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof ExternalJndiResource) {
                ExternalJndiResource r = (ExternalJndiResource) resource;
                if (r.getJndiName().equals("sample_jndi_resource")) {
                    assertEquals("queue", r.getResType());
                    assertEquals("sampleClass", r.getFactoryClass());
                    assertEquals("sample_jndi", r.getJndiLookupName());
                    //expect enabled for the resource to be true as resource-ref's enabled
                    //would be set to false
                    assertEquals("true", r.getEnabled());
                    assertEquals("External JNDI Resource", r.getDescription());
                    isCreated = true;
                    logger.fine("Jndi Resource config bean sample_jndi_resource is created.");
                    break;
                }
            }
        }
        assertTrue(isCreated);
        ResourceRef ref = server.getResourceRef(SimpleJndiName.of("sample_jndi_resource"));
        assertNotNull(ref);
        assertEquals("false", ref.getEnabled());
    }

    /**
     * Test of execute method, of class CreateJndiResource.
     * asadmin create-jndi-resource --factoryclass=sampleClass --jndilookupname=sample_jndi
     * sample_jndi_resource
     */
    @Test
    public void testExecuteFailInvalidResType() {
        parameters.set("factoryclass", "sampleClass");
        parameters.set("jndilookupname", "sample_jndi");
        parameters.set("jndi_name", "sample_jndi_resource");
        CreateJndiResource command = habitat.getService(CreateJndiResource.class);
        cr.getCommandInvocation("create-jndi-resource", context.getActionReport(), adminSubject).parameters(parameters)
            .execute(command);
        assertEquals(FAILURE, context.getActionReport().getActionExitCode(), context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class CreateJndiResource.
     * asadmin create-jndi-resource --factoryclass=sampleClass --restype=queue
     * sample_jndi_resource
     */
    @Test
    public void testExecuteFailInvalidJndiLookupName() {
        parameters.set("factoryclass", "sampleClass");
        parameters.set("restype", "queue");
        parameters.set("jndi_name", "sample_jndi_resource");
        CreateJndiResource command = habitat.getService(CreateJndiResource.class);
        cr.getCommandInvocation("create-jndi-resource", context.getActionReport(), adminSubject).parameters(parameters)
            .execute(command);
        assertEquals(FAILURE, context.getActionReport().getActionExitCode(), context.getActionReport().getMessage());
    }
}
