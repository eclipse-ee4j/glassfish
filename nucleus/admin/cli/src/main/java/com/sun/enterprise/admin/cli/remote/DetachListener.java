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

package com.sun.enterprise.admin.cli.remote;

import com.sun.enterprise.admin.remote.RemoteRestAdminCommand;
import com.sun.enterprise.admin.remote.sse.GfSseInboundEvent;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.StringUtils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandEventBroker;
import org.glassfish.api.admin.AdminCommandState;

/**
 * Whenever a command is executed with --detach this class will close the Server Sent Events for detached commands and
 * give a job id.
 *
 * @author Bhakti Mehta
 */
public class DetachListener implements AdminCommandEventBroker.AdminCommandListener<GfSseInboundEvent> {

    private static final LocalStringsImpl strings = new LocalStringsImpl(DetachListener.class);

    private final Logger logger;
    private final RemoteRestAdminCommand rac;
    private final boolean terse;

    public DetachListener(Logger logger, RemoteRestAdminCommand rac, boolean terse) {
        this.logger = logger;
        this.rac = rac;
        this.terse = terse;
    }

    @Override
    public void onAdminCommandEvent(String name, GfSseInboundEvent event) {
        try {
            AdminCommandState acs = event.getData(AdminCommandState.class, "application/json");
            String id = acs.getId();
            if (StringUtils.ok(id)) {
                if (terse) {
                    rac.closeSse(id, ActionReport.ExitCode.SUCCESS);
                } else {
                    rac.closeSse(strings.get("detach.jobid", id), ActionReport.ExitCode.SUCCESS);
                }
            } else {
                logger.log(Level.SEVERE, strings.getString("detach.noid", "Command was started but id was not retrieved. Can not detach."));
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

}
