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
import org.jvnet.hk2.config.TransactionFailure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MailJunit5Extension.class)
public class CreateMailResourceTest {

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
        assertNotNull(resources);
        parameters = new ParameterMap();
        context = new AdminCommandContextImpl(
                LogDomains.getLogger(CreateMailResourceTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        assertNotNull(cr);
        adminSubject = mockGenerator.createAsadminSubject();
    }

    @AfterEach
    public void tearDown() throws TransactionFailure {
        DeleteMailResource deleteCommand = locator.getService(DeleteMailResource.class);
        parameters = new ParameterMap();
        parameters.set("jndi_name", "mail/MyMailSession");

        cr.getCommandInvocation("delete-mail-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(deleteCommand);
        parameters = new ParameterMap();
        parameters.set("jndi_name", "dupRes");
        cr.getCommandInvocation("delete-mail-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(deleteCommand);
    }

    /**
     * Test of execute method, of class CreateMailResource.
     * asadmin create-mail-resource --mailuser=test --mailhost=localhost
     * --fromaddress=test@sun.com mail/MyMailSession
     */
    @Test
    public void testExecuteSuccess() {
        parameters.set("mailhost", "localhost");
        parameters.set("mailuser", "test");
        parameters.set("fromaddress", "test@sun.com");
        parameters.set("jndi_name", "mail/MyMailSession");
        CreateMailResource command = locator.getService(CreateMailResource.class);
        assertNotNull(command);
        cr.getCommandInvocation("create-mail-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(command);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof MailResource) {
                MailResource r = (MailResource) resource;
                if (r.getJndiName().equals("mail/MyMailSession")) {
                    assertEquals("localhost", r.getHost());
                    assertEquals("test", r.getUser());
                    assertEquals("test@sun.com", r.getFrom());
                    assertEquals("true", r.getEnabled());
                    assertEquals("false", r.getDebug());
                    assertEquals("imap", r.getStoreProtocol());
                    assertEquals("com.sun.mail.imap.IMAPStore", r.getStoreProtocolClass());
                    assertEquals("smtp", r.getTransportProtocol());
                    assertEquals("com.sun.mail.smtp.SMTPTransport", r.getTransportProtocolClass());
                    isCreated = true;
                    logger.fine("MailResource config bean mail/MyMailSession is created.");
                    break;
                }
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());
        Servers servers = locator.getService(Servers.class);
        boolean isRefCreated = false;
        for (Server server : servers.getServer()) {
            if (server.getName().equals(SystemPropertyConstants.DAS_SERVER_NAME)) {
                for (ResourceRef ref : server.getResourceRef()) {
                    if (ref.getRef().equals("mail/MyMailSession")) {
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
     * Test of execute method, of class CreateMailResource.
     * asadmin create-mail-resource --mailuser=test --mailhost=localhost
     * --fromaddress=test@sun.com dupRes
     * asadmin create-mail-resource --mailuser=test --mailhost=localhost
     * --fromaddress=test@sun.com dupRes
     */
    @Test
    public void testExecuteFailDuplicateResource() {
        parameters.set("mailhost", "localhost");
        parameters.set("mailuser", "test");
        parameters.set("fromaddress", "test@sun.com");
        parameters.set("jndi_name", "dupRes");
        CreateMailResource command1 = locator.getService(CreateMailResource.class);
        assertNotNull(command1);
        cr.getCommandInvocation("create-mail-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(command1);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof MailResource) {
                MailResource jr = (MailResource) resource;
                if (jr.getJndiName().equals("dupRes")) {
                    isCreated = true;
                    logger.fine("MailResource config bean dupRes is created.");
                    break;
                }
            }
        }
        assertTrue(isCreated);

        CreateMailResource command2 = locator.getService(CreateMailResource.class);
        cr.getCommandInvocation("create-mail-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(command2);
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
        int numDupRes = 0;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof MailResource) {
                MailResource jr = (MailResource) resource;
                if (jr.getJndiName().equals("dupRes")) {
                    numDupRes = numDupRes + 1;
                }
            }
        }
        assertEquals(1, numDupRes);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class CreateMailResource when enabled has no value
     * asadmin create-mail-resource --mailuser=test --mailhost=localhost
     * --fromaddress=test@sun.com  --enabled=false --debug=true
     * --storeprotocol=pop
     * --storeprotocolclass=com.sun.mail.pop.POPStore
     * --transprotocol=lmtp
     * --transprotocolclass=com.sun.mail.lmtop.LMTPTransport
     * mail/MyMailSession
     */
    @Test
    public void testExecuteWithOptionalValuesSet() {
        parameters.set("mailhost", "localhost");
        parameters.set("mailuser", "test");
        parameters.set("fromaddress", "test@sun.com");
        parameters.set("enabled", "false");
        parameters.set("debug", "true");
        parameters.set("storeprotocol", "pop");
        parameters.set("storeprotocolclass", "com.sun.mail.pop.POPStore");
        parameters.set("transprotocol", "lmtp");
        parameters.set("transprotocolclass", "com.sun.mail.lmtp.LMTPTransport");
        parameters.set("jndi_name", "mail/MyMailSession");
        CreateMailResource command = locator.getService(CreateMailResource.class);
        assertNotNull(command);
        cr.getCommandInvocation("create-mail-resource", context.getActionReport(), adminSubject).parameters(parameters).execute(command);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof MailResource) {
                MailResource r = (MailResource) resource;
                if (r.getJndiName().equals("mail/MyMailSession")) {
                    //expect enabled for the resource to be true as resource-ref's enabled
                    //would be set to false
                    assertEquals("true", r.getEnabled());
                    assertEquals("true", r.getDebug());
                    assertEquals("pop", r.getStoreProtocol());
                    assertEquals("com.sun.mail.pop.POPStore", r.getStoreProtocolClass());
                    assertEquals("lmtp", r.getTransportProtocol());
                    assertEquals("com.sun.mail.lmtp.LMTPTransport", r.getTransportProtocolClass());
                    isCreated = true;
                    break;
                }
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }
}
