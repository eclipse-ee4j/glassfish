/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.cli.remote;

import com.sun.enterprise.admin.remote.RemoteRestAdminCommand;
import com.sun.enterprise.admin.remote.sse.GfSseInboundEvent;
import com.sun.enterprise.util.StringUtils;

import java.io.IOException;
import java.lang.System.Logger;

import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.AdminCommandEventBroker.AdminCommandListener;
import org.glassfish.api.admin.AdminCommandState;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.TRACE;

/**
 * Whenever a command is executed with --detach this class will close the Server Sent Events for detached commands and
 * give a job id.
 *
 * @author Bhakti Mehta
 */
public class DetachListener implements AdminCommandListener<GfSseInboundEvent> {

    private static final Logger LOG = System.getLogger(DetachListener.class.getName());

    private final RemoteRestAdminCommand command;
    private final boolean terse;

    public DetachListener(RemoteRestAdminCommand rac, boolean terse) {
        this.command = rac;
        this.terse = terse;
    }

    @Override
    public void onAdminCommandEvent(String name, GfSseInboundEvent event) {
        LOG.log(TRACE, "onAdminCommandEvent(name={0}, event={1})", name, event);
        try {
            final AdminCommandState acs = event.getData(AdminCommandState.class, "application/json");
            final String id = acs.getId();
            if (StringUtils.ok(id)) {
                if (terse) {
                    command.closeSse(id, ExitCode.SUCCESS);
                } else {
                    command.closeSse("Job ID: " + id, ExitCode.SUCCESS);
                }
            } else {
                LOG.log(ERROR, "Command was started but id was not retrieved. Cannot detach.");
            }
        } catch (IOException ex) {
            LOG.log(ERROR, "Failed to retrieve event data.", ex);
        }
    }
}
