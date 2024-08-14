/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.Payload;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;

/**
 * Bootstraps secure admin on a new instance by downloading the minimum files
 * required for the client to offer client authentication using a cert.
 *
 * @author Tim Quinn
 */
@Service(name="_bootstrap-secure-admin")
@PerLookup
@ExecuteOn(value={RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="_bootstrap-secure-admin",
        description="_bootstrap-secure-admin")
})
public class BootstrapSecureAdminCommand implements AdminCommand, PostConstruct {

    private final static String DOWNLOADED_FILE_MIME_TYPE = "application/octet-stream";
    private final static String DOWNLOAD_DATA_REQUEST_NAME = "secure-admin";

    private File[] bootstrappedFiles = null;

    @Inject
    private ServerEnvironment env;


    @Override
    public void postConstruct() {
        bootstrappedFiles = new File[] {
            env.getJKS(),
            env.getTrustStore()
                };
    }

    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final Payload.Outbound outboundPayload = context.getOutboundPayload();
        final File instanceRoot = env.getInstanceRoot();

        try {
            for (File f : bootstrappedFiles) {
                outboundPayload.attachFile(
                        DOWNLOADED_FILE_MIME_TYPE,
                        instanceRoot.toURI().relativize(f.toURI()),
                        DOWNLOAD_DATA_REQUEST_NAME,
                        f);
            }
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (IOException ex) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(ex);
        }
    }
}
