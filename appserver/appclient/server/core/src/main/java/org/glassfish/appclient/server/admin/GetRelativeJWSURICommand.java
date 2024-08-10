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

package org.glassfish.appclient.server.admin;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.appclient.server.core.AppClientDeployer;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Returns the path part (not host or port) of the URI for launching
 * an app client using Java Web Start.
 * <p>
 * Used primarily from the admin console to support the Java Web Start
 * client launch feature.
 *
 * @author Tim Quinn
 */
@Service(name="_get-relative-jws-uri")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn(value={RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Application.class,
        opType=RestEndpoint.OpType.GET,
        path="_get-relative-jws-uri",
        description="Get Relative JWS URI",
        params={
            @RestParam(name="appname", value="$parent")
        },
        useForAuthorization=true)
})
public class GetRelativeJWSURICommand implements AdminCommand {

    private static final String APPNAME_OPTION = "appname";
    private static final String MODULENAME_OPTION = "modulename";
    private static final String URI_PROPERTY_NAME = "relative-uri";

    private final static LocalStringManager localStrings =
            new LocalStringManagerImpl(GetRelativeJWSURICommand.class);

    @Param(name = APPNAME_OPTION, optional=false)
    public String appname;

    @Param(name = MODULENAME_OPTION, optional=false)
    public String modulename;

    @Inject
    private AppClientDeployer appClientDeployer;

    @Inject
    private Applications apps;

    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        final Application app = apps.getApplication(appname);
        if (app != null) {
            Module appClient = app.getModule(modulename);
            if (appClient == null) {
                appClient = app.getModule(modulename + ".jar");
            }
            if (appClient != null) {
                String result = appClient.getPropertyValue("jws.user.friendly.path");
                /*
                 * For stand-alone app clients the property is stored at the
                 * application level instead of the module level.
                 */
                if (result == null) {
                    result = app.getPropertyValue("jws.user.friendly.path");
                }
                if (result != null) {
                    report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
                    report.getTopMessagePart().addProperty(URI_PROPERTY_NAME, result);
                    report.setMessage(result);
                    return;
                }
            }
        }
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        report.setMessage(localStrings.getLocalString(
                this.getClass(),
                "getreljwsuri.appOrModuleNotFound",
                "Could not find application {0}, module {1}",
                new Object[] {appname, modulename}));
    }

}
