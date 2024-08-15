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

package org.glassfish.resources.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.jvnet.hk2.annotations.Service;

/**
 * Create add-resources Command
 *
 */
@TargetType(value={CommandTarget.DAS,CommandTarget.DOMAIN, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE })
@ExecuteOn(RuntimeType.ALL)
@Service(name="add-resources")
@PerLookup
@I18n("add.resources")
@RestEndpoints({
    @RestEndpoint(configBean=Resources.class,
        opType=RestEndpoint.OpType.POST,
        path="add-resources",
        description="add-resources")
})
public class AddResources implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(AddResources.class);

    @Param(optional=true)
    private String target = SystemPropertyConstants.DAS_SERVER_NAME;

    @Param(name="xml_file_name", primary=true)
    private File xmlFile;

    @Inject
    private Domain domain;

    @Inject
    private ResourceFactory resourceFactory;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        // Check if the path xmlFile exists
        if (!xmlFile.exists()) {
            report.setMessage(localStrings.getLocalString("FileNotFound",
                "The system cannot find the path specified: {0}", xmlFile.getName()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            final ArrayList results = ResourcesManager.createResources(
                    domain.getResources(), xmlFile, target, resourceFactory);
            final Iterator resultsIter = results.iterator();
            report.getTopMessagePart().setChildrenType("Command");
            boolean isSuccess = false;
            while (resultsIter.hasNext()) {
                ResourceStatus rs = ((ResourceStatus) resultsIter.next());
                final String msgToAdd = rs.getMessage();
                if ((msgToAdd != null) && (!msgToAdd.equals(""))) {
                    final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                    part.setMessage(msgToAdd);
                }
                if (rs.getStatus() == ResourceStatus.SUCCESS || rs.isAlreadyExists())
                    isSuccess = true;
            }
            report.setActionExitCode(
                    (isSuccess)?ActionReport.ExitCode.SUCCESS:ActionReport.ExitCode.FAILURE);
            if (!isSuccess)
                report.setMessage(localStrings.getLocalString("add.resources.failed",
                                                "add-resources <{0}> failed", xmlFile.getName()));

        } catch (Exception ex) {
            Logger.getLogger(AddResources.class.getName()).log(Level.SEVERE, "Something went wrong in add-resources", ex);
            report.setMessage(localStrings.getLocalString("add.resources.failed",
                                                "add-resources <{0}> failed", xmlFile.getName()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            //Need to fix, doesn't show the error from exception, though it writes in the log
            report.setFailureCause(ex);
        }
    }
}
