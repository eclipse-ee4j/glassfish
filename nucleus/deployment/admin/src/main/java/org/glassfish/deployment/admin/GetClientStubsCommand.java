/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.deployment.admin;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.util.Collection;
import java.util.logging.Logger;
import jakarta.inject.Inject;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.deployment.common.Artifacts;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.deployment.versioning.VersioningSyntaxException;
import org.glassfish.deployment.versioning.VersioningUtils;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

/**
 *
 * @author tjquinn
 */
@Service(name="get-client-stubs")
@I18n("get.client.stubs")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@RestEndpoints({
    @RestEndpoint(configBean=Application.class,
        opType=RestEndpoint.OpType.GET,
        path="get-client-stubs",
        description="Get Client Stubs",
        params={
            @RestParam(name="appname", value="$parent")
        })
})
public class GetClientStubsCommand implements AdminCommand, AdminCommandSecurity.Preauthorization {

    private final static String APPNAME = "appname";

    private final static LocalStringManager localStrings =
            new LocalStringManagerImpl(GetClientStubsCommand.class);

    @Inject
    private Applications apps;

    @Param(name = APPNAME, optional=false)
    private String appname = null;

    @Param(primary=true)
    private String localDir;

    @AccessRequired.To("read")
    private Application matchingApp = null;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        for (Application app : apps.getApplications()) {
            if (app.getName().equals(appname)) {
                matchingApp = app;
                return true;
            }
        }
        context.getActionReport().failure(context.getLogger(), localStrings.getLocalString(
            getClass(),
            "get-client-stubs.noSuchApp",
            "Application {0} was not found",
            new Object[] {appname}));
        return false;
    }

    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();
        Collection<Artifacts.FullAndPartURIs> artifactInfo = DeploymentUtils.downloadableArtifacts(matchingApp).getArtifacts();

        try {
            VersioningUtils.checkIdentifier(appname);
        } catch (VersioningSyntaxException ex) {
            report.failure(logger,ex.getMessage());
            return;
        }

        if (artifactInfo.size() == 0) {
            report.setMessage(localStrings.getLocalString(
                getClass(),
                "get-client-stubs.noStubApp",
                "there are no files to retrieve for application {0}",
                new Object[] {appname}));
            return;
        }

        try {
            DeployCommand.retrieveArtifacts(context, matchingApp, localDir);
        } catch (Exception e) {
            report.setFailureCause(e);
            report.failure(logger, localStrings.getLocalString(
                    getClass(),
                    "get-client-stubs.errorPrepDownloadedFiles",
                    "Error preparing for download"), e);
        }
    }
}
