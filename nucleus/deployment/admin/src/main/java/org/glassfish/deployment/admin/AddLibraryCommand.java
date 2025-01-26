/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.v3.server.CommonClassLoaderServiceImpl;
import com.sun.enterprise.v3.server.DomainXmlPersistence;

import jakarta.inject.Inject;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.config.UnprocessedConfigListener;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.UnprocessedChangeEvent;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import static org.glassfish.deployment.admin.DeploymentCommandUtils.renameUploadedFileOrCopyInPlaceFile;

@Service(name="add-library")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn(value={RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class, opType= RestEndpoint.OpType.POST, path="add-library", description="Install library")
})
@AccessRequired(resource=DeploymentCommandUtils.LIBRARY_SECURITY_RESOURCE_PREFIX + "$type", action="create")
public class AddLibraryCommand implements AdminCommand {

    @Param(primary=true, multiple=true)
    File[] files = null;

    @Param(optional=true, acceptableValues="common, app")
    String type = "common";

    @Inject
    ServerEnvironment env;

    @Inject
    DomainXmlPersistence dxp;

    @Inject
    UnprocessedConfigListener ucl;

    @Inject
    CommonClassLoaderServiceImpl commonCLService;

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(AddLibraryCommand.class);

    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();

        File libDir = env.getLibPath();
        boolean typeCommon = true;

        if (type.equals("app")) {
            libDir = new File(libDir, "applibs");
            typeCommon = false;
        }

        // rename or copy the library file to the appropriate
        // library directory
        try {
            List<UnprocessedChangeEvent> unprocessed = new ArrayList<>();
            StringBuilder msg = new StringBuilder();
            for (File libraryFile : files) {
                if (libraryFile.exists()) {
                    File resultFile = renameUploadedFileOrCopyInPlaceFile(libDir, libraryFile,
                        env.getApplicationRepositoryPath());
                    if (typeCommon) {
                        commonCLService.addToClassPath(resultFile.toURI().toURL());
                    } else {
                        PropertyChangeEvent pe = new PropertyChangeEvent(libDir, "add-library", null, resultFile);
                        UnprocessedChangeEvent uce = new UnprocessedChangeEvent(pe, "add-library");
                        unprocessed.add(uce);
                    }
                } else {
                    msg.append(
                        localStrings.getLocalString("lfnf", "Library file not found", libraryFile.getAbsolutePath()));
                }
            }
            if (msg.length() > 0) {
                logger.log(Level.WARNING, msg.toString());
                report.setActionExitCode(ActionReport.ExitCode.WARNING);
                report.setMessage(msg.toString());
            }

            // set the restart required flag
            UnprocessedChangeEvents uces = new UnprocessedChangeEvents(unprocessed);
            List<UnprocessedChangeEvents> ucesList = new ArrayList<>();
            ucesList.add(uces);
            ucl.unprocessedTransactedEvents(ucesList);

            // touch the domain.xml so instances restart will synch
            // over the libraries.
            dxp.touch();

        } catch (Exception e) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(e.getMessage());
        }
    }
}
