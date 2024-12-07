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

package org.glassfish.deployment.admin;

import com.sun.enterprise.config.serverbeans.Domain;

import java.io.File;
import java.util.logging.Level;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * Instance-only command which makes sure that a deployment directory seems to
 * be the same when viewed from this instance as when viewed from the DAS.
 * <p>
 * The DAS computes a checksum for the deployment directory as it sees it and
 * passes it as a parameter to this command.  This command (on each instance)
 * computes a checksum for the path passed to it.  If the checksums agree
 * then we conclude that the DAS and this instance saw the same files in the
 * directory and this command reports success; otherwise this command reports
 * failure.
 *
 * @author Tim Quinn
 */
@Service(name="_instanceValidateRemoteDirDeployment")
@PerLookup
@ExecuteOn(value={RuntimeType.INSTANCE})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="_instanceValidateRemoteDirDeployment",
        description="_instanceValidateRemoteDirDeployment")
})
@AccessRequired(resource="domain", action="read")
public class InstanceValidateRemoteDirDeploymentCommand implements AdminCommand {

    private static final LocalStringsImpl localStrings =
            new LocalStringsImpl(InstanceValidateRemoteDirDeploymentCommand.class);

    @Param(primary=true)
    private File path;

    @Param
    private String checksum;

    @Override
    public void execute(AdminCommandContext context) {
        context.getLogger().log(Level.FINE,
                "Running _instanceValidateRemoteDirDeployment with directory {0} and expected checksum {1}",
                new Object[]{path.getAbsolutePath(), checksum});
        final ActionReport report = context.getActionReport();

        try {
            final long myChecksum = DeploymentUtils.checksum(path);
            final long dasChecksum = Long.parseLong(checksum);
            if (dasChecksum == myChecksum) {
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            } else {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.getTopMessagePart().setMessage(
                        localStrings.get("deploy.remoteDirDeployChecksumMismatch",
                        path.getAbsolutePath()));
            }
        } catch (IllegalArgumentException ex) {
            /*
             * If the path is not a directory then DeploymentUtils.checksum
             * throws an IllegalArgumentException.
             */
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.getTopMessagePart().setMessage(ex.getMessage());
        }
    }
}
