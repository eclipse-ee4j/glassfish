/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.tests.progress;

import com.sun.enterprise.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ManagedJob;
import org.glassfish.api.admin.Payload.Outbound;
import org.glassfish.api.admin.Progress;
import org.glassfish.api.admin.ProgressStatus;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Doing progress and send some payload.
 *
 * @author mmares
 */
@Service(name = "progress-payload")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("progress")
@Progress(totalStepCount=5)
@ManagedJob
public class ProgressPayloadCommand implements AdminCommand {

    private static final Logger LOG = Logger.getLogger(ProgressPayloadCommand.class.getName());

    @Param(name = "down", multiple = false, primary = true, optional = true)
    String down;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        ProgressStatus ps = context.getProgressStatus();
        for (int i = 0; i < 4; i++) {
            doSomeLogic();
            ps.progress(1);
        }
        //Prepare payload
        Outbound out = context.getOutboundPayload();
        StringBuilder msg = new StringBuilder();
        if (down == null || down.isEmpty()) {
            msg.append("No file requested.");
        } else {
            msg.append("You are requesting for ").append(down).append('.').append(StringUtils.EOL);
            File f = new File(down);
            if (!f.exists()) {
                msg.append("But it does not exist!");
            } else {
                try {
                    String canonicalPath = f.getCanonicalPath();
                    canonicalPath = canonicalPath.replace('\\', '/');
                    if (canonicalPath.charAt(1) == ':') {
                        canonicalPath = canonicalPath.substring(2);
                    }
                    if (f.isDirectory()) {
                        msg.append("It is directory - recursive download");
                        out.attachFile("application/octet-stream", URI.create(canonicalPath), f.getName(), f, true);
                    } else {
                        out.attachFile("application/octet-stream", URI.create(canonicalPath), f.getName(), f);
                    }
                } catch (IOException ex) {
                    report.failure(LOG, "Can not append " + f.getAbsolutePath());
                }
            }
        }
        if (report.getActionExitCode() == ActionReport.ExitCode.SUCCESS) {
            report.setMessage(msg.toString());
        }
        //Return
        ps.progress(1);
        ps.complete("Finished");
    }

    private void doSomeLogic() {
        try {
            Thread.sleep(250L);
        } catch (Exception ex) {
        }
    }

}
