/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.universal.collections.ManifestUtils;

import jakarta.inject.Inject;

/**
 * Create data structures that describe the command.
 *
 * @author Jerome Dochez
 *
 */
@Service(name = "_list-descriptors")
@PerLookup
@I18n("list.commands")
@AccessRequired(resource = "domain", action = "dump")
public class ListCommandDescriptorsCommand implements AdminCommand {
    @Inject
    ServiceLocator habitat;

    @Override
    public void execute(AdminCommandContext context) {
        setAdminCommands();
        sort();

        for (AdminCommand cmd : adminCmds) {
            cliCmds.add(reflect(cmd));
        }
        ActionReport report = context.getActionReport();
        StringBuilder sb = new StringBuilder();
        sb.append("ALL COMMANDS: ").append(EOL);
        for (CLICommand cli : cliCmds) {
            sb.append(cli.toString()).append(EOL);
        }
        report.setMessage(sb.toString());
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private CLICommand reflect(AdminCommand cmd) {
        CLICommand cliCmd = new CLICommand(cmd);

        for (Field field : cmd.getClass().getDeclaredFields()) {
            final Param param = field.getAnnotation(Param.class);
            if (param == null) {
                continue;
            }

            Option option = new Option(param, field);
            cliCmd.options.add(option);
        }
        return cliCmd;
    }

    private void setAdminCommands() {
        adminCmds = new ArrayList<>();
        for (AdminCommand command : habitat.<AdminCommand>getAllServices(AdminCommand.class)) {
            adminCmds.add(command);
        }
    }

    private void sort() {
        Collections.sort(adminCmds, new Comparator<AdminCommand>() {
            @Override
            public int compare(AdminCommand c1, AdminCommand c2) {
                Service service1 = c1.getClass().getAnnotation(Service.class);
                Service service2 = c2.getClass().getAnnotation(Service.class);

                String name1 = (service1 != null) ? service1.name() : "";
                String name2 = (service2 != null) ? service2.name() : "";

                return name1.compareTo(name2);
            }
        });
    }

    private static boolean ok(String s) {
        return s != null && s.length() > 0 && !s.equals("null");
    }

    private List<AdminCommand> adminCmds;
    private List<CLICommand> cliCmds = new LinkedList<>();
    private final static String EOL = ManifestUtils.EOL_TOKEN;

    private static class CLICommand {
        CLICommand(AdminCommand adminCommand) {
            this.adminCommand = adminCommand;
            Service service = adminCommand.getClass().getAnnotation(Service.class);
            name = (service != null) ? service.name() : "";
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("CLI Command: name=").append(name);
            sb.append(" class=").append(adminCommand.getClass().getName()).append(EOL);

            for (Option opt : options) {
                sb.append(opt.toString()).append(EOL);
            }
            return sb.toString();
        }

        AdminCommand adminCommand;
        String name;
        List<Option> options = new LinkedList<>();
    }

    private static class Option {
        Option(Param p, Field f) {
            final Class<?> ftype = f.getType();
            name = p.name();

            if (!ok(name)) {
                name = f.getName();
            }

            required = !p.optional();
            operand = p.primary();
            defaultValue = p.defaultValue();
            type = ftype;
        }

        @Override
        public String toString() {
            String s = "   Option:" + " name=" + name + " required=" + required + " operand=" + operand + " defaultValue=" + defaultValue
                    + " type=" + type.getName();
            return s;
        }

        private boolean required;
        private boolean operand;
        private String name;
        private String defaultValue;
        private Class<?> type;
    }
}
