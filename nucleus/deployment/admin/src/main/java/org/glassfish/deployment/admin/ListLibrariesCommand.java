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

package org.glassfish.deployment.admin;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.Param;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestEndpoint;
import org.jvnet.hk2.annotations.Service;

import org.glassfish.hk2.api.PerLookup;
import jakarta.inject.Inject;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.config.serverbeans.Domain;

import java.io.File;
import org.glassfish.api.admin.AccessRequired;

@Service(name="list-libraries")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn(value={RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class, opType= RestEndpoint.OpType.GET, path="list-libraries", description="List libraries")
})
@AccessRequired(resource=DeploymentCommandUtils.LIBRARY_SECURITY_RESOURCE_PREFIX + "/$type", action="read")
public class ListLibrariesCommand implements AdminCommand {

    @Param(optional=true, acceptableValues="common, ext, app")
    String type = "common";

    @Inject
    ServerEnvironment env;

    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        File libDir = env.getLibPath();

        if (type.equals("ext")) {
            libDir = new File(libDir, "ext");
        } else if (type.equals("app")) {
            libDir = new File(libDir, "applibs");
        }

        ActionReport.MessagePart part = report.getTopMessagePart();

        // list the library files from the appropriate library directory
        for (File libFile : FileUtils.listFiles(libDir)) {
            if (libFile.isFile()) {
                ActionReport.MessagePart childPart = part.addChild();
                childPart.setMessage(libFile.getName());
            }
        }
    }
}
