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

import com.sun.enterprise.admin.util.ClusterOperationUtil;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.Supplemental;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.deployment.Deployment;
import org.jvnet.hk2.annotations.Service;

/**
 * Makes sure that, if a deployment is a directory deployment to a non-DAS
 * target, that all targeted instances "see" the same files in the specified
 * deployment directory as the DAS sees.  If so, the DeployCommand runs as normal.  If not,
 * this supplemental command reports failure which prevents the DeployCommand
 * from running because it would have tried to deploy different files to
 * different instances.
 *
 * @author Tim Quinn
 */
@Service(name="_validateRemoteDirDeployment")
@Supplemental(value="deploy", on=Supplemental.Timing.Before, ifFailure=FailurePolicy.Error)
@PerLookup
@ExecuteOn(value={RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="_validateRemoteDirDeployment",
        description="_validateRemoteDirDeployment")
})
@AccessRequired(resource=DeploymentCommandUtils.APPLICATION_RESOURCE_NAME, action="write")
public class ValidateRemoteDirDeploymentCommand extends DeployCommandParameters
        implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ValidateRemoteDirDeploymentCommand.class);

    @Inject
    private ArchiveFactory archiveFactory;

    @Inject
    private ServiceLocator habitat;

    @Inject
    private Deployment deployment;

    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();

        /*
         * This supplemental command should run only if the deployment
         * underway is a directory deployment, if the archive exists, and
         * only if the target includes a non-DAS target.
         */
        final ReadableArchive archive = archive(logger, report);
        if (archive == null) {
            /*
             * This is a little weird.  We cannot read the archive the user
             * specified.  Eventually, the deploy command will find this out
             * also.  But if we return a failure from here then the
             * command framework will report that a supplemental command has
             * failed and the deploy command will never have a chance to nicely
             * complain about the missing archive.  So, from this supplemental
             * command we'll report success so the deploy command will
             * be run.
             */
            reportSuccess(report);
            return;
        }

        final File source = new File(archive.getURI().getSchemeSpecificPart());
        try {
            archive.close();
        } catch (IOException ex) {
            report.failure(logger, ex.getLocalizedMessage(), ex);
        }
        if ( ! source.isDirectory()) {
            /*
             * This is not a directory deployment, so we're done.
             */
            reportSuccess(report);
            return;
        }

        if (target == null) {
            target = deployment.getDefaultTarget(name, origin, _classicstyle);
        }

        final TargetInfo targetInfo = new TargetInfo(target);
        if ( ! targetInfo.containsNonDAS()) {
            reportSuccess(report);
            return;
        }

        /*
         * There is at least one non-DAS target.  Compute the checksum as seen
         * here on the DAS.
         */
        final long checksum = DeploymentUtils.checksum(source);

        /*
         * Replicate the hidden validateRemoteDirDeployment command on the
         * targets, passing the URI for the directory and the checksum.
         */
        final ParameterMap paramMap = new ParameterMap();
        paramMap.add("checksum", Long.toString(checksum));
        paramMap.add("DEFAULT", path.toURI().getSchemeSpecificPart());

        ActionReport.ExitCode replicateResult = ClusterOperationUtil.replicateCommand(
                "_instanceValidateRemoteDirDeployment",
                FailurePolicy.Error,
                FailurePolicy.Ignore,
                FailurePolicy.Ignore,
                targetInfo.targetNames(),
                context,
                paramMap,
                habitat);

        report.setActionExitCode(replicateResult);

    }

    /**
     * Opens and returns an archive for the injected "path" parameter.  If there
     * is any problem the method updates the action report accordingly.
     * @param logger
     * @param report
     * @return
     */
    private ReadableArchive archive(final Logger logger, final ActionReport report) {
        try {
            return archiveFactory.openArchive(path, this);
        } catch (IOException e) {
            final String msg = localStrings.getLocalString("deploy.errOpeningArtifact",
                    "deploy.errOpeningArtifact", path.getAbsolutePath());
            if (logReportedErrors) {
                report.failure(logger, msg, e);
            } else {
                report.setMessage(msg + path.getAbsolutePath() + e.toString());
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            }
            return null;
        }
    }

    private void reportSuccess(final ActionReport report) {
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        report.setMessage("");
    }

    /**
     * Collects some information about the target(s) specified by the
     * "target" parameter.
     */
    private static class TargetInfo {

        private boolean containsNonDAS = false;
        private final List<String> targetNames = new ArrayList<String>();

        private TargetInfo(final String targetExpr) {
            for (String targetName : targetExpr.split(",")) {
                targetNames.add(targetName);
                containsNonDAS |= ( ! DeploymentUtils.isDASTarget(targetName));
            }
        }

        private boolean containsNonDAS() {
            return containsNonDAS;
        }

        private List<String> targetNames() {
            return targetNames;
        }
    }
}
