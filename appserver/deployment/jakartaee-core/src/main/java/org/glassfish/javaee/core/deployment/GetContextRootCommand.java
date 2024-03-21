/*
 * Copyright (c) 2010, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.javaee.core.deployment;

import org.glassfish.api.Param;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import org.glassfish.deployment.common.DeploymentProperties;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.*;
import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;

import org.glassfish.hk2.api.PerLookup;


/**
 * Get context root command
 */
@Service(name="_get-context-root")
@ExecuteOn(value={RuntimeType.DAS})
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@RestEndpoints({
    @RestEndpoint(configBean=com.sun.enterprise.config.serverbeans.Application.class,
        opType=RestEndpoint.OpType.GET,
        path="get-context-root",
        description="Get Context Root",
        params={
            @RestParam(name="appname", value="$parent")
        })
})
public class GetContextRootCommand implements AdminCommand {

    @Param(primary=true)
    private String modulename = null;

    @Param(optional=true)
    private String appname = null;

    @Inject
    ApplicationRegistry appRegistry;

    /**
     * Entry point from the framework into the command execution
     * @param context context for the command.
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        ActionReport.MessagePart part = report.getTopMessagePart();

        ApplicationInfo appInfo = appRegistry.get(appname);
        if (appInfo != null) {
            Application app = appInfo.getMetaData(Application.class);
            if (app != null) {
                BundleDescriptor bundleDesc = app.getModuleByUri(modulename);
                if (bundleDesc != null &&
                    bundleDesc instanceof WebBundleDescriptor) {
                    String contextRoot = ((WebBundleDescriptor)bundleDesc).getContextRoot();
                    part.addProperty(DeploymentProperties.CONTEXT_ROOT, contextRoot);
                    part.setMessage(contextRoot);
                }
            }
        }
    }
}
