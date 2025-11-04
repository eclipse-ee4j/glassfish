/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ConnectorsAdminJunit5Extension.class)
public class DeleteCustomResourceTest {
    @Inject
    private ServiceLocator habitat;
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
        resources = habitat.<Domain>getService(Domain.class).getResources();
        parameters = new ParameterMap();
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(DeleteCustomResourceTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        adminSubject = mockGenerator.createAsadminSubject();
    }

    @AfterEach
    public void tearDown() throws TransactionFailure {
        SingleConfigCode<Resources> configCode = resourcesBean -> {
            Resource target = null;
            for (Resource resource : resourcesBean.getResources()) {
                if (resource instanceof CustomResource) {
                    CustomResource r = (CustomResource) resource;
                    if (r.getJndiName().equals("sample_custom_resource")) {
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
     * Test of execute method, of class DeleteCustomResource.
     * delete-custom-resource sample_custom_resource
     */
    @Test
    public void testExecuteSuccessDefaultTarget() {
        CreateCustomResource createCommand = habitat.getService(CreateCustomResource.class);
        assertNotNull(createCommand);
        parameters.set("restype", "topic");
        parameters.set("factoryclass", "javax.naming.spi.ObjectFactory");
        parameters.set("jndi_name", "sample_custom_resource");
        cr.getCommandInvocation("create-custom-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(createCommand);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        parameters = new ParameterMap();
        DeleteCustomResource deleteCommand = habitat.getService(DeleteCustomResource.class);
        assertTrue(deleteCommand != null);
        parameters.set("jndi_name", "sample_custom_resource");
        cr.getCommandInvocation("delete-custom-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(deleteCommand);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isDeleted = true;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof CustomResource) {
                CustomResource jr = (CustomResource) resource;
                if (jr.getJndiName().equals("sample_custom_resource")) {
                    isDeleted = false;
                    logger.fine("CustomResource config bean sample_custom_resource is deleted.");
                    break;
                }
            }
        }
        assertTrue(isDeleted);
        logger.fine("msg: " + context.getActionReport().getMessage());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        Servers servers = habitat.getService(Servers.class);
        boolean isRefDeleted = true;
        for (Server server : servers.getServer()) {
            if (server.getName().equals(SystemPropertyConstants.DAS_SERVER_NAME)) {
                for (ResourceRef ref : server.getResourceRef()) {
                    if (ref.getRef().equals("sample_custom_resource")) {
                        isRefDeleted = false;
                        break;
                    }
                }
            }
        }
        assertTrue(isRefDeleted);
    }

    /**
     * Test of execute method, of class DeleteCustomResource.
     * delete-custom-resource doesnotexist
     */
    @Test
    public void testExecuteFailDoesNotExist() {
        DeleteCustomResource deleteCommand = habitat.getService(DeleteCustomResource.class);
        assertNotNull(deleteCommand);
        parameters.set("jndi_name", "doesnotexist");
        cr.getCommandInvocation("delete-custom-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(deleteCommand);
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

}
