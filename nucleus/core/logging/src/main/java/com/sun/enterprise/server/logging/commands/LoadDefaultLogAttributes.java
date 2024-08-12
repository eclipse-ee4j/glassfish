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

package com.sun.enterprise.server.logging.commands;

import com.sun.common.util.logging.LoggingConfigImpl;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

@Service(name = "_load-default-log-attributes")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("load.default.log.attributes")
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class, opType=RestEndpoint.OpType.POST, path="load-default-log-attributes")
})
public class LoadDefaultLogAttributes implements AdminCommand {

    @Inject
    LoggingConfigImpl loggingConfig;

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(LoadDefaultLogAttributes.class);

    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final String target = "default template";

        try {
            HashMap<String, String> props = null;
            props = (HashMap<String, String>) loggingConfig.getDefaultLoggingProperties();

            List<String> keys = new ArrayList<String>();
            keys.addAll(props.keySet());
            Collections.sort(keys);
            Iterator<String> it2 = keys.iterator();
            // The following Map & List are used to hold the REST data
            Map<String, String> logAttributes = new HashMap<String, String>();

            while (it2.hasNext()) {
                String name = it2.next();
                if (!name.endsWith(".level") && !name.equals(".level")) {
                    final ActionReport.MessagePart part = report.getTopMessagePart()
                            .addChild();
                    part.setMessage(name + "\t" + "<" + props.get(name) + ">");
                    logAttributes.put(name, props.get(name));
                }
            }
            Properties restData = new Properties();
            restData.put("logAttributes", logAttributes);
            report.setExtraProperties(restData);

        } catch (IOException ex) {
            report.setMessage(localStrings.getLocalString("get.log.attribute.failed",
                    "Could not get logging attributes for {0}.", target));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
