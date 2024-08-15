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

package org.glassfish.admin.monitor.cli;

import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.util.Iterator;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * Return the version and build number
 *
 * @author Prashanth Abbagani
 */
@Service(name = "monitor")
@PerLookup
@I18n("monitor.command")
public class Monitor implements AdminCommand {

    @Param(optional = true)
    private String type;

    @Param(optional = true)
    private String filter;

    @Inject
    private ServiceLocator habitat;

    final private LocalStringManagerImpl localStrings = new LocalStringManagerImpl(Monitor.class);

    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        MonitorContract mContract = null;
        for (MonitorContract m : habitat.<MonitorContract>getAllServices(MonitorContract.class)) {
            if ((m.getName()).equals(type)) {
                mContract = m;
                break;
            }
        }
        if (mContract != null) {
            mContract.process(report, filter);
            return;
        }
        if (habitat.getAllServices(MonitorContract.class).size() != 0) {
            StringBuffer buf = new StringBuffer();
            Iterator<MonitorContract> contractsIterator = habitat.<MonitorContract>getAllServices(MonitorContract.class).iterator();
            while (contractsIterator.hasNext()) {
                buf.append(contractsIterator.next().getName());
                if (contractsIterator.hasNext()) {
                    buf.append(", ");
                }
            }
            String validTypes = buf.toString();
            report.setMessage(localStrings.getLocalString("monitor.type.error",
                    "No type exists in habitat for the given monitor type {0}. " + "Valid types are: {1}", type, validTypes));
        } else {
            report.setMessage(
                    localStrings.getLocalString("monitor.type.invalid", "No type exists in habitat for the given monitor type {0}", type));
        }

        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
    }
}
