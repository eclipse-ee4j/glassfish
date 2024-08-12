/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.commands;

import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import org.glassfish.admin.payload.PayloadImpl;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.Job;
import org.glassfish.api.admin.JobManager;
import org.glassfish.api.admin.Payload.Outbound;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/** Retrieve outbound payload from finished managed job.
 *
 * @author mmares
 */
@Service(name = "_get-payload")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("getpayload")
@AccessRequired(resource="jobs/job/$jobID", action="read")
public class GetPayloadCommand implements AdminCommand {

    private final static LocalStringManagerImpl strings = new LocalStringManagerImpl(GetPayloadCommand.class);

    @Inject
    JobManager registry;

    @Param(primary=true, optional=false, multiple=false)
    String jobID;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport ar = context.getActionReport();
        Job job = registry.get(jobID);
        if (job == null) {
            ar.setActionExitCode(ActionReport.ExitCode.FAILURE);
            ar.setMessage(strings.getLocalString("getPayload.wrong.commandinstance.id", "Command instance {0} does not exist.", jobID));
            return;
        }
        Outbound jobPayload = job.getPayload();
        if (jobPayload == null) {
            ar.setMessage(strings.getLocalString("getPayload.nopayload", "Outbound payload does not exist."));
            return; //Just return. This is OK.
        }
        Outbound paylaod = context.getOutboundPayload();
        if ((paylaod instanceof PayloadImpl.Outbound) && (jobPayload instanceof PayloadImpl.Outbound)) {
            PayloadImpl.Outbound destination = (PayloadImpl.Outbound) paylaod;
            PayloadImpl.Outbound source = (PayloadImpl.Outbound) jobPayload;
            destination.getParts().addAll(source.getParts());
        } else {
            ar.setActionExitCode(ActionReport.ExitCode.FAILURE);
            ar.setMessage(strings.getLocalString("getPayload.unsupported", "Payload type is not supported. Can not download data."));
        }

    }


}
