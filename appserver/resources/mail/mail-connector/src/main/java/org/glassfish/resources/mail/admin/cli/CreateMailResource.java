/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resources.api.ResourceAttributes;
import org.jvnet.hk2.annotations.Service;

/**
 * Create Java Mail Resource
 *
 */
@TargetType(value={CommandTarget.DAS,CommandTarget.DOMAIN, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE })
@RestEndpoints({
        @RestEndpoint(configBean=Resources.class,
                opType=RestEndpoint.OpType.POST,
                path="create-mail-resource",
                description="create-mail-resource")
})
@org.glassfish.api.admin.ExecuteOn(RuntimeType.ALL)
@Service(name="create-mail-resource")
@PerLookup
@I18n("create.mail.resource")
public class CreateMailResource implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(CreateMailResource.class);

    @Param(name="mailhost", alias="host")
    private String mailHost;

    @Param(name="mailuser", alias="user")
    private String mailUser;

    @Param(name="fromaddress",alias="from")
    private String fromAddress;

    @Param(name="jndi_name", primary=true)
    private String jndiName;

    @Param(name="storeprotocol", optional=true, defaultValue="imap", alias="storeProtocol")
    private String storeProtocol;

    @Param(name="storeprotocolclass", optional=true, defaultValue="com.sun.mail.imap.IMAPStore", alias="storeProtocolClass")
    private String storeProtocolClass;

    @Param(name="transprotocol", optional=true, defaultValue="smtp", alias="transportProtocol")
    private String transportProtocol;

    @Param(name="transprotocolclass", optional=true, defaultValue="com.sun.mail.smtp.SMTPTransport", alias="transportProtocolClass")
    private String transportProtocolClass;

    @Param(optional=true, defaultValue="true")
    private Boolean enabled;

    @Param(optional=true, defaultValue="false")
    private Boolean debug;

    @Param(name="property", optional=true, separator=':')
    private Properties properties;

    @Param(optional=true,
    defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    private String target;

    @Param(optional=true)
    private String description;


    @Inject
    private Domain domain;

    @Inject
    private org.glassfish.resources.mail.admin.cli.MailResourceManager mailResMgr;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the parameter names and the values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        ResourceAttributes attributes = new ResourceAttributes();
        attributes.set(org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME, jndiName);
        attributes.set(org.glassfish.resources.admin.cli.ResourceConstants.MAIL_HOST, mailHost);
        attributes.set(org.glassfish.resources.admin.cli.ResourceConstants.MAIL_USER, mailUser);
        attributes.set(org.glassfish.resources.admin.cli.ResourceConstants.MAIL_FROM_ADDRESS, fromAddress);
        attributes.set(org.glassfish.resources.admin.cli.ResourceConstants.MAIL_STORE_PROTO, storeProtocol);
        attributes.set(org.glassfish.resources.admin.cli.ResourceConstants.MAIL_STORE_PROTO_CLASS, storeProtocolClass);
        attributes.set(org.glassfish.resources.admin.cli.ResourceConstants.MAIL_TRANS_PROTO, transportProtocol);
        attributes.set(org.glassfish.resources.admin.cli.ResourceConstants.MAIL_TRANS_PROTO_CLASS, transportProtocolClass);
        attributes.set(org.glassfish.resources.admin.cli.ResourceConstants.MAIL_DEBUG, debug.toString());
        attributes.set(org.glassfish.resources.admin.cli.ResourceConstants.ENABLED, enabled.toString());
        attributes.set(ServerTags.DESCRIPTION, description);

        ResourceStatus rs;

        try {
            rs = mailResMgr.create(domain.getResources(), attributes, properties, target);
        } catch(Exception e) {
            Logger.getLogger(CreateMailResource.class.getName()).log(Level.SEVERE,
                    "Unable to create Mail Resource " + jndiName, e);
            String def = "Mail resource: {0} could not be created";
            report.setMessage(localStrings.getLocalString("create.mail.resource.fail",
                    def, jndiName) + " " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        ActionReport.ExitCode ec = ActionReport.ExitCode.SUCCESS;
        if (rs.getStatus() == ResourceStatus.FAILURE) {
            ec = ActionReport.ExitCode.FAILURE;
            if (rs.getMessage() == null) {
                 report.setMessage(localStrings.getLocalString("create.mail.resource.fail",
                    "Unable to create Mail Resource {0}.", jndiName));

            }
            if (rs.getException() != null) {
                report.setFailureCause(rs.getException());
            }
        }
        if(rs.getMessage() != null){
            report.setMessage(rs.getMessage());
        }
        report.setActionExitCode(ec);
    }
}
