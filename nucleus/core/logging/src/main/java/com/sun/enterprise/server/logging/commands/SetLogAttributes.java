/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.enterprise.config.serverbeans.Clusters;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.server.logging.LogManagerService;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.validation.ValidationException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.main.jul.cfg.GlassFishLogManagerProperty;
import org.glassfish.main.jul.cfg.LogProperty;
import org.glassfish.main.jul.formatter.GlassFishLogFormatter.GlassFishLogFormatterProperty;
import org.glassfish.main.jul.formatter.ODLLogFormatter;
import org.glassfish.main.jul.formatter.ODLLogFormatter.ODLFormatterProperty;
import org.glassfish.main.jul.formatter.OneLineFormatter;
import org.glassfish.main.jul.formatter.OneLineFormatter.OneLineFormatterProperty;
import org.glassfish.main.jul.formatter.UniformLogFormatter;
import org.glassfish.main.jul.formatter.UniformLogFormatter.UniformFormatterProperty;
import org.glassfish.main.jul.handler.ConsoleHandlerProperty;
import org.glassfish.main.jul.handler.FileHandlerProperty;
import org.glassfish.main.jul.handler.GlassFishLogHandler;
import org.glassfish.main.jul.handler.GlassFishLogHandlerProperty;
import org.glassfish.main.jul.handler.HandlerConfigurationHelper;
import org.glassfish.main.jul.handler.SimpleLogHandler;
import org.glassfish.main.jul.handler.SimpleLogHandler.SimpleLogHandlerProperty;
import org.glassfish.main.jul.handler.SyslogHandler;
import org.glassfish.main.jul.handler.SyslogHandlerProperty;
import org.jvnet.hk2.annotations.Service;


/**
 * Set Log Attributes Command
 * Updates one or more loggers' attributes
 * User: naman mehta
 * Date: Oct 21, 2010
 * Time: 11:48:20 AM
 */
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG})
@CommandLock(CommandLock.LockType.NONE)
@Service(name = "set-log-attributes")
@PerLookup
@I18n("set.log.attributes")
@RestEndpoints({
        @RestEndpoint(configBean = Domain.class,
                opType = RestEndpoint.OpType.POST,
                path = "set-log-attributes",
                description = "set-log-attributes")
})
public class SetLogAttributes implements AdminCommand {

    private static final String LINE_SEP = System.getProperty("line.separator");
    private static final Logger LOG = Logger.getLogger(SetLogAttributes.class.getName());
    private static final LocalStringManagerImpl LOCAL_STRINGS = new LocalStringManagerImpl(SetLogLevel.class);
    private static final Set<String> VALID_ATTRIBUTES;
    static {
        // the set of valid attribute keys affects Admin GUI! Try to save values in Logger settings.
        final Set<String> properties = new HashSet<>();
        Arrays.stream(GlassFishLogManagerProperty.values()).forEach(p -> properties.add(p.getPropertyName()));

        final Class<?>[] formatters = new Class<?>[] {
            UniformLogFormatter.class, ODLLogFormatter.class, OneLineFormatter.class
        };

        // all handlers with their own properties
        Arrays.stream(GlassFishLogHandlerProperty.values())
            .forEach(p -> properties.add(p.getPropertyFullName(GlassFishLogHandler.class)));
        Arrays.stream(SimpleLogHandlerProperty.values())
            .forEach(p -> properties.add(p.getPropertyFullName(SimpleLogHandler.class)));
        Arrays.stream(SyslogHandlerProperty.values())
            .forEach(p -> properties.add(p.getPropertyFullName(SyslogHandler.class)));

        // all formatters and their own properties
        Arrays.stream(UniformFormatterProperty.values())
            .forEach(p -> properties.add(p.getPropertyFullName(UniformLogFormatter.class)));
        Arrays.stream(ODLFormatterProperty.values())
            .forEach(p -> properties.add(p.getPropertyFullName(ODLLogFormatter.class)));
        Arrays.stream(OneLineFormatterProperty.values())
            .forEach(p -> properties.add(p.getPropertyFullName(OneLineFormatter.class)));
        for (Class<?> formatter : formatters) {
            Arrays.stream(GlassFishLogFormatterProperty.values())
                .forEach(p -> properties.add(p.getPropertyFullName(formatter)));
        }

        // and finally all formatters used with handlers
        final Class<?>[] handlersWithFormatter = new Class<?>[] {
            GlassFishLogHandler.class, SimpleLogHandler.class
        };
        final Set<LogProperty> formatterParameters = new HashSet<>();
        Arrays.stream(UniformFormatterProperty.values()).forEach(formatterParameters::add);
        Arrays.stream(ODLFormatterProperty.values()).forEach(formatterParameters::add);
        Arrays.stream(OneLineFormatterProperty.values()).forEach(formatterParameters::add);
        Arrays.stream(GlassFishLogFormatterProperty.values()).forEach(formatterParameters::add);
        Arrays.stream(FileHandlerProperty.values()).forEach(formatterParameters::add);

        for (LogProperty logProperty : formatterParameters) {
            for (Class<?> handler : handlersWithFormatter) {
                String formatterPrefix = HandlerConfigurationHelper.FORMATTER.getPropertyFullName(handler);
                properties.add(logProperty.getPropertyFullName(formatterPrefix));
            }
        }
        properties.add(ConsoleHandlerProperty.ENCODING.getPropertyFullName());
        properties.add(ConsoleHandlerProperty.FILTER.getPropertyFullName());
        properties.add(ConsoleHandlerProperty.FORMATTER.getPropertyFullName());
        properties.add(ConsoleHandlerProperty.LEVEL.getPropertyFullName());

        properties.add("java.util.logging.SimpleFormatter.format");
        VALID_ATTRIBUTES = Collections.unmodifiableSet(properties);
        LOG.log(Level.FINE, "Acceptable logging properties for the set-log-attribute command (except loggers): {0}",
            VALID_ATTRIBUTES);
    }

    @Param(name = "name_value", primary = true, separator = ':')
    Properties properties;

    @Param(optional = true)
    String target = SystemPropertyConstants.DAS_SERVER_NAME;

    @Param(optional = true, defaultValue = "true")
    boolean validate;

    @Inject
    LoggingConfigImpl loggingConfig;

    @Inject
    private LogManagerService logManager;

    @Inject
    Domain domain;

    @Inject
    Servers servers;

    @Inject
    Clusters clusters;


    @Override
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();
        final StringBuilder sbfSuccessMsg = new StringBuilder(LINE_SEP);
        boolean success = false;
        boolean invalidAttribute = false;
        Map<String, String> m = new HashMap<>();
        try {
            for (final Object key : properties.keySet()) {
                final String att_name = (String) key;
                final String att_value = (String) properties.get(att_name);
                // that is is a valid level
                if (validate) {
                    final boolean vlAttribute = isValid(att_name, att_value, report);
                    if (vlAttribute) {
                        m.put(att_name, att_value);
                        sbfSuccessMsg.append(LOCAL_STRINGS.getLocalString(
                            "set.log.attribute.properties",
                            "{0} logging attribute set with value {1}.",
                            att_name, att_value)).append(LINE_SEP);
                    } else {
                        invalidAttribute = true;
                        break;
                    }
                } else {
                    m.put(att_name, att_value);
                    sbfSuccessMsg.append(LOCAL_STRINGS.getLocalString(
                            "set.log.attribute.properties",
                            "{0} logging attribute set with value {1}.",
                            att_name, att_value)).append(LINE_SEP);
                }
            }

            if (invalidAttribute) {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            TargetInfo targetInfo = new TargetInfo(domain, target);
            String targetConfigName = targetInfo.getConfigName();
            boolean isDas = targetInfo.isDas();

            loggingConfig.updateLoggingProperties(m, targetConfigName);
            success = true;

            if (success) {
                String effectiveTarget = (isDas ? SystemPropertyConstants.DAS_SERVER_NAME : targetConfigName);
                sbfSuccessMsg.append(LOCAL_STRINGS.getLocalString(
                        "set.log.attribute.success",
                        "These logging attributes are set for {0}.", effectiveTarget)).append(LINE_SEP);
                report.setMessage(sbfSuccessMsg.toString());
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            } else {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                String msg = LOCAL_STRINGS.getLocalString("invalid.target.sys.props",
                        "Invalid target: {0}. Valid default target is a server named ''server'' (default) or cluster name.", target);
                report.setMessage(msg);
                return;
            }

        } catch (IOException e) {
            report.setMessage(LOCAL_STRINGS.getLocalString("set.log.attribute.failed",
                    "Could not set logging attributes for {0}.", target));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
    }


    private boolean isValid(final String att_name, final String att_value, final ActionReport report) {
        for (final String attrName : VALID_ATTRIBUTES) {
            if (attrName.equals(att_name)) {
                try {
                    logManager.validateLoggingProperty(att_name, att_value);
                    return true;
                } catch (ValidationException e) {
                    // Add in additional error message information if present
                    if (e.getMessage() != null) {
                        report.setMessage(e.getMessage() + "\n");
                        return false;
                    }
                }
            }
        }
        report.appendMessage(LOCAL_STRINGS.getLocalString("set.log.attribute.invalid",
            "Invalid logging attribute name {0} or value {1}.", att_name, att_value));
        return false;
    }
}
