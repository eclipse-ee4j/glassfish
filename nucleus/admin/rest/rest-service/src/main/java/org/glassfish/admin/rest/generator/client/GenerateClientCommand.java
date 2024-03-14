/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.generator.client;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.Payload;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoint.OpType;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;

import jakarta.inject.Inject;
import org.glassfish.api.admin.AccessRequired;

/**
 *
 * @author jdlee
 */
// TODO: This command is not quite ready yet, so we disable it until
//@Service(name = "__generate-rest-client")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn({ RuntimeType.DAS })
@TargetType({ CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG,
        CommandTarget.CLUSTERED_INSTANCE })
@RestEndpoints({ @RestEndpoint(configBean = Domain.class, opType = OpType.GET, path = "client", description = "Generate REST client") })
@AccessRequired(resource = "domain/rest-client", action = "read")
public class GenerateClientCommand implements AdminCommand {
    @Inject
    ServiceLocator habitat;

    @Param
    private String outputDir;

    @Param(shortName = "lang", optional = true, defaultValue = "java")
    private String languages;

    private final static LocalStringManager localStrings = new LocalStringManagerImpl(GenerateClientCommand.class);

    @Override
    public void execute(AdminCommandContext context) {
        List<ClientGenerator> generators = new ArrayList<ClientGenerator>();

        for (String lang : languages.split(",")) {
            ClientGenerator gen = null;
            if ("java".equalsIgnoreCase(lang)) {
                gen = new JavaClientGenerator(habitat);
            } else if ("python".equalsIgnoreCase(lang)) {
                gen = new PythonClientGenerator(habitat);
            }

            if (gen != null) {
                generators.add(gen);
                gen.generateClasses();
            }
        }

        Logger logger = context.getLogger();
        try {
            Payload.Outbound outboundPayload = context.getOutboundPayload();
            Properties props = new Properties();
            /*
             * file-xfer-root is used as a URI, so convert backslashes.
             */
            props.setProperty("file-xfer-root", outputDir.replace('\\', '/'));
            for (ClientGenerator gen : generators) {
                for (Map.Entry<String, URI> entry : gen.getArtifact().entrySet()) {
                    final URI artifact = entry.getValue();
                    outboundPayload.attachFile("application/octet-stream", new URI(entry.getKey()), "files", props, new File(artifact));
                }
                List<String> messages = gen.getMessages();
                if (!messages.isEmpty()) {
                    ActionReport ar = context.getActionReport();
                    for (String msg : messages) {
                        ar.addSubActionsReport().appendMessage(msg);
                    }
                }
            }
        } catch (Exception e) {
            final String errorMsg = localStrings.getLocalString("download.errDownloading", "Error while downloading generated files");
            logger.log(Level.SEVERE, errorMsg, e);
            ActionReport report = context.getActionReport();

            report = report.addSubActionsReport();
            report.setActionExitCode(ExitCode.WARNING);
            report.setMessage(errorMsg);
            report.setFailureCause(e);
        }
    }
}
