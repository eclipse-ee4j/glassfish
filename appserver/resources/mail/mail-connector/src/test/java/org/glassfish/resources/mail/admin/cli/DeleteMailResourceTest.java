/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resources.mail.admin.cli;

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
import org.glassfish.resources.mail.config.MailResource;
import org.glassfish.resources.mail.test.MailJunit5Extension;
import org.glassfish.tests.utils.mock.MockGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MailJunit5Extension.class)
public class DeleteMailResourceTest {
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
        parameters = new ParameterMap();
        resources = locator.<Domain>getService(Domain.class).getResources();
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(DeleteMailResourceTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        adminSubject = mockGenerator.createAsadminSubject();
    }

    @AfterEach
    public void tearDown() throws TransactionFailure {
        SingleConfigCode<Resources> configCode = resourcesProxy -> {
            Resource target = null;
            for (Resource resource : resourcesProxy.getResources()) {
                if (resource instanceof MailResource) {
                    MailResource r = (MailResource) resource;
                    if (r.getJndiName().equals("mail/MyMailSession")) {
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
     * Test of execute method, of class DeleteMailResource.
     * asadmin create-mail-resource --mailuser=test --mailhost=localhost
     * --fromaddress=test@sun.com mail/MyMailSession
     * delete-mail-resource mail/MyMailSession
     */
    @Test
    public void testExecuteSuccessDefaultTarget() {
        parameters.set("mailhost", "localhost");
        parameters.set("mailuser", "test");
        parameters.set("fromaddress", "test@sun.com");
        parameters.set("jndi_name", "mail/MyMailSession");
        org.glassfish.resources.mail.admin.cli.CreateMailResource createCommand = locator.getService(org.glassfish.resources.mail.admin.cli.CreateMailResource.class);
        assertTrue(createCommand != null);
        cr.getCommandInvocation("create-mail-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(createCommand);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        parameters = new ParameterMap();
        parameters.set("jndi_name", "mail/MyMailSession");
        org.glassfish.resources.mail.admin.cli.DeleteMailResource deleteCommand = locator.getService(org.glassfish.resources.mail.admin.cli.DeleteMailResource.class);
        assertTrue(deleteCommand != null);
        cr.getCommandInvocation("delete-mail-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(deleteCommand);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isDeleted = true;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof MailResource) {
                MailResource jr = (MailResource) resource;
                if (jr.getJndiName().equals("mail/MyMailSession")) {
                    isDeleted = false;
                    logger.fine("MailResource config bean mail/MyMailSession is deleted.");
                    break;
                }
            }
        }
        assertTrue(isDeleted);
        logger.fine("msg: " + context.getActionReport().getMessage());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        Servers servers = locator.getService(Servers.class);
        boolean isRefDeleted = true;
        for (Server server : servers.getServer()) {
            if (server.getName().equals(SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)) {
                for (ResourceRef ref : server.getResourceRef()) {
                    if (ref.getRef().equals("mail/MyMailSession")) {
                        isRefDeleted = false;
                        break;
                    }
                }
            }
        }
        assertTrue(isRefDeleted);
    }

    /**
     * Test of execute method, of class DeleteMailResource.
     * delete-mail-resource doesnotexist
     */
    @Test
    public void testExecuteFailDoesNotExist() {
        parameters.set("jndi_name", "doesnotexist");
        org.glassfish.resources.mail.admin.cli.DeleteMailResource deleteCommand = locator.getService(org.glassfish.resources.mail.admin.cli.DeleteMailResource.class);
        cr.getCommandInvocation("delete-mail-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(deleteCommand);
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
        logger.fine("msg: " + context.getActionReport().getMessage());
    }
}
