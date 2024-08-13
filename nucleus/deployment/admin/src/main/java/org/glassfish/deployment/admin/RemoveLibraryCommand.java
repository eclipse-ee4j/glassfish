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

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.v3.server.DomainXmlPersistence;

import jakarta.inject.Inject;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
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

@Service(name="remove-library")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn(value={RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class, opType= RestEndpoint.OpType.DELETE, path="remove-library", description="Uninstall library")
})
public class RemoveLibraryCommand implements AdminCommand, AdminCommandSecurity.AccessCheckProvider {

    @Param(primary=true, multiple=true)
    String[] names = null;

    @Param(optional=true, acceptableValues="common, ext, app")
    String type = "common";

    @Inject
    ServerEnvironment env;

    @Inject
    DomainXmlPersistence dxp;

    @Inject
    UnprocessedConfigListener ucl;

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(RemoveLibraryCommand.class);

    @Override
    public Collection<? extends AccessCheck> getAccessChecks() {
        final List<AccessRequired.AccessCheck> accessChecks = new ArrayList<AccessRequired.AccessCheck>();
        for (String libName : names) {
            accessChecks.add(new AccessCheck(DeploymentCommandUtils.LIBRARY_SECURITY_RESOURCE_PREFIX + "/" + type + "/" + libName, "delete"));
        }
        return accessChecks;
    }


    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();

        File libDir = env.getLibPath();

        if (type.equals("ext")) {
            libDir = new File(libDir, "ext");
        } else if (type.equals("app")) {
            libDir = new File(libDir, "applibs");
        }

        try {
            List<UnprocessedChangeEvent> unprocessed =
                new ArrayList<UnprocessedChangeEvent>();

            // delete the file from the appropriate library directory
            StringBuffer msg = new StringBuffer();
            for (String libraryName : names) {
                File libraryFile = new File(libDir, libraryName);
                if (libraryFile.exists()) {
                    boolean isDeleted = FileUtils.deleteFile(libraryFile);
                    if (!isDeleted) {
                        msg.append(localStrings.getLocalString("lfnd","Could not remove library file", libraryFile.getAbsolutePath()));
                    } else {
                        PropertyChangeEvent pe = new PropertyChangeEvent(libDir,
                            "remove-library", libraryFile, null);
                        UnprocessedChangeEvent uce = new UnprocessedChangeEvent(
                            pe, "remove-library");
                        unprocessed.add(uce);
                    }
                } else {
                    msg.append(localStrings.getLocalString("lfnf","Library file not found", libraryFile.getAbsolutePath()));
                }
            }
            if (msg.length() > 0) {
                logger.log(Level.WARNING, msg.toString());
                report.setActionExitCode(ActionReport.ExitCode.WARNING);
                report.setMessage(msg.toString());
            }

            // set the restart required flag
            UnprocessedChangeEvents uces = new UnprocessedChangeEvents(
                unprocessed);
            List<UnprocessedChangeEvents> ucesList =
                new ArrayList<UnprocessedChangeEvents>();
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
