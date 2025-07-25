/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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
package org.glassfish.commandrecorder.admingui;

import com.sun.enterprise.v3.admin.AdminCommandJob;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.extras.commandlogger.AdminCommandLogger;

import static org.glassfish.extras.commandlogger.AdminCommandLogger.LogMode.NO_COMMAND;
import static org.glassfish.extras.commandlogger.AdminCommandLogger.LogMode.WRITE_COMMANDS;

@Named
@RequestScoped
public class CommandRecorder {

    @Inject
    Instance<CommandRunner<AdminCommandJob>> commandRunnerProvider;

    @Inject
    Instance<ActionReport> actionReportProvider;

    @Inject
    Instance<Subject> subjectProvider;

    public boolean isEnabled() {
        return !NO_COMMAND.equals(AdminCommandLogger.LogMode.get());
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            setSystemProperty(WRITE_COMMANDS);
        } else {
            setSystemProperty(NO_COMMAND);
        }
    }


    private void setSystemProperty(AdminCommandLogger.LogMode propertyValue) {
        final CommandInvocation<?> commandInvocation = commandRunnerProvider.get()
            .getCommandInvocation("create-system-properties", actionReportProvider.get(), subjectProvider.get());
        commandInvocation.parameters(parameters(propertyValue)).execute();
    }

    public void toggle() {
        setEnabled(!isEnabled());
    }

    public String getToggleButtonTitle() {
        return (isEnabled() ? "Disable" : "Enable") + " logging commands";
    }

    private ParameterMap parameters(AdminCommandLogger.LogMode propertyValue) {
        ParameterMap data = new ParameterMap();
        data.add("target", "server");
        data.add("DEFAULT", AdminCommandLogger.LogMode.PROPERTY_NAME + "=" + propertyValue.name());
        return data;
    }
}
