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

import com.sun.enterprise.admin.util.InstanceStateService;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.cluster.SyncRequest;

import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.InstanceState;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

/**
 * Synchronize files.  Accepts an XML document containing files
 * and mod times and sends the client new versions of anything
 * that's out of date.
 *
 * @author Bill Shannon
 */
@Service(name="_synchronize-files")
@PerLookup
@CommandLock(CommandLock.LockType.EXCLUSIVE)
@I18n("synchronize.command")
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="_synchronize-files",
        description="_synchronize-files")
})
public class SynchronizeFiles implements AdminCommand {

    @Param(name = "file_list", primary = true)
    private File fileList;

    @Inject @Optional
    private Applications applications;

    @Inject @Optional
    private Servers servers;

    @Inject
    private InstanceStateService stateService;

    @Inject
    private ServerSynchronizer sync;

    private Logger logger;

    private final static LocalStringManagerImpl strings =
        new LocalStringManagerImpl(SynchronizeFiles.class);

    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        logger = context.getLogger();
        SyncRequest sr = null;
        try {
            /*
            try {
            BufferedInputStream in =
                new BufferedInputStream(new FileInputStream(fileList));
            byte[] buf = new byte[8192];
            int n = in.read(buf);
            System.out.write(buf, 0, n);
            in.close();
            } catch (IOException ex) {}
            */
            // read the input document
            JAXBContext jc = JAXBContext.newInstance(SyncRequest.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            unmarshaller.setSchema(null);       // XXX - needed?
            sr = (SyncRequest)unmarshaller.unmarshal(fileList);
            if (logger.isLoggable(Level.FINER))
                logger.finer("SynchronizeFiles: synchronize dir " + sr.dir);
        } catch (Exception ex) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("SynchronizeFiles: Exception reading request");
                logger.fine(ex.toString());
            }
            report.setActionExitCode(ExitCode.FAILURE);
            report.setMessage(
                        strings.getLocalString("sync.exception.reading",
                            "SynchronizeFiles: Exception reading request"));
            report.setFailureCause(ex);
            return;
        }

        try {
            // verify the server instance is valid
            Server server = null;
            if (servers != null)
                server = servers.getServer(sr.instance);
            if (server == null) {
                report.setActionExitCode(ExitCode.FAILURE);
                report.setMessage(
                        strings.getLocalString("sync.unknown.instance",
                            "Unknown server instance: {0}", sr.instance));
                return;
            }

            sync.synchronize(server, sr, context.getOutboundPayload(), report,
                                logger);
            stateService.setState(server.getName(), InstanceState.StateType.NO_RESPONSE, true);
            stateService.removeFailedCommandsForInstance(server.getName());
        } catch (Exception ex) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("SynchronizeFiles: Exception processing request");
                logger.fine(ex.toString());
            }
            report.setActionExitCode(ExitCode.FAILURE);
            report.setMessage(
                        strings.getLocalString("sync.exception.processing",
                            "SynchronizeFiles: Exception processing request"));
            report.setFailureCause(ex);
        }
    }
}
