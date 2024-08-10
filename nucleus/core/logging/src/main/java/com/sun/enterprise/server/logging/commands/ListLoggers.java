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

package com.sun.enterprise.server.logging.commands;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.server.logging.LoggerInfoMetadata;
import com.sun.enterprise.util.ColumnFormatter;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

@ExecuteOn({RuntimeType.DAS})
@Service(name = "list-loggers")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.loggers")
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.GET,
        path="list-loggers",
        description="list-loggers")
})
public class ListLoggers implements AdminCommand {

    private static final String UNKNOWN = "?";

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListLoggers.class);

    @Param(optional=true, name="_internal", defaultValue="false")
    private boolean listInternalLoggers;

    @Inject
    private LoggerInfoMetadata loggerInfoMetadataService;

    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();
        String header_name = localStrings.getLocalString("list.loggers.header.name", "Logger Name");
        String header_subsystem = localStrings.getLocalString("list.loggers.header.subsystem", "Subsystem");
        String header_description = localStrings.getLocalString("list.loggers.header.description", "Logger Description");

        ColumnFormatter colFormatter = new ColumnFormatter(new String[]{header_name, header_subsystem, header_description});

        // An option to specify client locale should be supported. However, it probably
        // should not be specific to this command. For now, localize using the default locale.
        Locale locale = Locale.getDefault();

        try {
            Set<String> loggers = loggerInfoMetadataService.getLoggerNames();
            Set<String> sortedLoggers = new TreeSet<String>(loggers);
            // The following Map & List are used to hold the REST data
            Map<String, String> loggerSubsystems = new TreeMap<String, String>();
            Map<String, String> loggerDescriptions = new TreeMap<String, String>();
            List<String> loggerList = new ArrayList<String>();

            for (String logger : sortedLoggers) {
                String subsystem = loggerInfoMetadataService.getSubsystem(logger);
                String desc = loggerInfoMetadataService.getDescription(logger, locale);
                boolean published = loggerInfoMetadataService.isPublished(logger);
                if (subsystem == null) subsystem = UNKNOWN;
                if (desc == null) desc = UNKNOWN;
                if (published || listInternalLoggers) {
                    colFormatter.addRow(new Object[]{logger, subsystem, desc});
                    loggerSubsystems.put(logger, subsystem);
                    loggerDescriptions.put(logger, desc); //Needed for REST xml and JSON output
                    loggerList.add(logger); //Needed for REST xml and JSON output
                }
            }

            report.appendMessage(colFormatter.toString());
            report.appendMessage(System.getProperty("line.separator"));

            // Populate the extraProperties data structure for REST...
            Properties restData = new Properties();
            restData.put("loggerSubsystems", loggerSubsystems);
            restData.put("loggerDescriptions", loggerDescriptions);
            restData.put("loggers", loggerList);
            report.setExtraProperties(restData);

        } catch (Exception ex) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ex.printStackTrace(new PrintStream(out));
            report.setMessage(localStrings.getLocalString("list.loggers.failed",
                    "Error listing loggers: {0}", out.toString()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

}
