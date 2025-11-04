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
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.resources.admin.cli.test.ResourcesJunit5Extension;
import org.glassfish.tests.utils.mock.MockGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static org.glassfish.api.ActionReport.ExitCode.FAILURE;
import static org.glassfish.api.ActionReport.ExitCode.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(ResourcesJunit5Extension.class)
public class DeleteJndiResourceTest {
    @Inject
    private ServiceLocator locator;
    @Inject
    private Logger logger;
    @Inject
    private MockGenerator mockGenerator;
    @Inject
    private CommandRunner cr;

    private Resources resources;
    private ParameterMap parameters;
    private AdminCommandContext context;
    private Subject adminSubject;

    @BeforeEach
    public void setUp() {
        resources = locator.<Domain>getService(Domain.class).getResources();
        parameters = new ParameterMap();
        cr = locator.getService(CommandRunner.class);
        context = new AdminCommandContextImpl(
            LogDomains.getLogger(DeleteJndiResourceTest.class, LogDomains.ADMIN_LOGGER), new PropsFileActionReporter());
        adminSubject = mockGenerator.createAsadminSubject();
    }

    @AfterEach
    public void tearDown() throws TransactionFailure {
        parameters = new ParameterMap();
        SingleConfigCode<Resources> configCode = resourcesProxy -> {
            Resource target = null;
            for (Resource resource : resourcesProxy.getResources()) {
                if (resource instanceof BindableResource) {
                    BindableResource r = (BindableResource) resource;
                    if (r.getJndiName().equals("sample_jndi_resource") ||
                            r.getJndiName().equals("dupRes")) {
                        target = resource;
                        break;
                    }
                }
            }
            if (target != null) {
                resourcesProxy.getResources().remove(target);
            }
            return null;
        };
        ConfigSupport.apply(configCode, resources);
    }

    /**
     * Test of execute method, of class DeleteJndiResource.
     * asadmin create-jndi-resource --restype=queue --factoryclass=sampleClass --jndilookupname=sample_jndi
     * sample_jndi_resource
     * delete-jndi-resource sample_jndi_resource
     */
    @Test
    public void testExecuteSuccessDefaultTarget() {
        parameters.set("restype", "topic");
        parameters.set("jndilookupname", "sample_jndi");
        parameters.set("factoryclass", "javax.naming.spi.ObjectFactory");
        parameters.set("jndi_name", "sample_jndi_resource");
        CreateJndiResource createCommand = locator.getService(CreateJndiResource.class);
        cr.getCommandInvocation("create-jndi-resource", context.getActionReport(), adminSubject)
            .parameters(parameters).execute(createCommand);
        assertEquals(SUCCESS, context.getActionReport().getActionExitCode(), context.getActionReport().getMessage());
        parameters = new ParameterMap();
        parameters.set("jndi_name", "sample_jndi_resource");
        DeleteJndiResource deleteCommand = locator.getService(DeleteJndiResource.class);
        cr.getCommandInvocation("delete-jndi-resource", context.getActionReport(), adminSubject)
            .parameters(parameters).execute(deleteCommand);
        assertEquals(SUCCESS, context.getActionReport().getActionExitCode(), context.getActionReport().getMessage());
        boolean isDeleted = true;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof BindableResource) {
                BindableResource jr = (BindableResource) resource;
                if (jr.getJndiName().equals("sample_jndi_resource")) {
                    isDeleted = false;
                    logger.fine("Jndi Resource config bean sample_jndi_resource is created.");
                    break;
                }
            }
        }
        assertTrue(isDeleted);
        Servers servers = locator.getService(Servers.class);
        boolean isRefDeleted = true;
        for (Server server : servers.getServer()) {
            if (server.getName().equals(SystemPropertyConstants.DAS_SERVER_NAME)) {
                for (ResourceRef ref : server.getResourceRef()) {
                    if (ref.getRef().equals("sample_jndi_resource")) {
                        isRefDeleted = false;
                        break;
                    }
                }
            }
        }
        assertTrue(isRefDeleted);
    }

    /**
     * Test of execute method, of class DeleteJndiResource.
     * delete-jndi-resource doesnotexist
     */
    @Test
    public void testExecuteFailDoesNotExist() {
        parameters.set("jndi_name", "doesnotexist");
        DeleteJndiResource deleteCommand = locator.getService(DeleteJndiResource.class);
        cr.getCommandInvocation("delete-jndi-resource", context.getActionReport(), adminSubject)
            .parameters(parameters).execute(deleteCommand);
        assertEquals(FAILURE, context.getActionReport().getActionExitCode(), context.getActionReport().getMessage());
    }
}
