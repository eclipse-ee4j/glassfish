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

package org.glassfish.kernel.embedded;

import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Inject;
import org.glassfish.internal.embedded.LifecycleException;
import org.glassfish.internal.embedded.Port;
import org.glassfish.internal.embedded.admin.CommandExecution;
import org.glassfish.internal.embedded.admin.CommandParameters;
import org.glassfish.internal.embedded.admin.EmbeddedAdminContainer;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.ActionReport;

import java.util.List;
import java.util.ArrayList;
import com.sun.enterprise.v3.common.PlainTextActionReporter;
import org.glassfish.internal.api.InternalSystemAdministrator;

/**
 * Implementation of the embedded command execution
 *
 * @author Jerome Dochez
 */
@Service
public class EmbeddedAdminCtrImpl implements EmbeddedAdminContainer {

    @Inject
    CommandRunner runner;

    @Inject
    private InternalSystemAdministrator kernelIdentity;

    private final static List<Sniffer> empty = new ArrayList<Sniffer>();

    public List<Sniffer> getSniffers() {
        return empty;
    }

    public void bind(Port port, String protocol) {

    }

    public void start() throws LifecycleException {

    }

    public void stop() throws LifecycleException {

    }

    public CommandExecution execute(String commandName, CommandParameters params) {
        ParameterMap props = params.getOptions();
        if (params.getOperands().size() > 0) {
            for (String op : params.getOperands())
                props.add("DEFAULT", op);
        }
        final ActionReport report = new PlainTextActionReporter();
        CommandExecution ce = new CommandExecution() {

            public ActionReport getActionReport() {
                return report;
            }

            public ActionReport.ExitCode getExitCode() {
                return report.getActionExitCode();
            }

            public String getMessage() {
                return report.getMessage();
            }
        };
        runner.getCommandInvocation(commandName, report, kernelIdentity.getSubject()).parameters(props).execute();
        return ce;
    }

    public void bind(Port port) {

    }

}
