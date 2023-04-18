/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.web.plugin.common;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Engine;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.web.config.serverbeans.EnvEntry;
import org.glassfish.web.config.serverbeans.WebModuleConfig;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Sets the value of an env-entry customization for a web application.
 *
 * @author tjquinn
 */
@Service(name = "set-web-env-entry")
@I18n("setWebEnvEntry.command")
@PerLookup
@RestEndpoints({
        @RestEndpoint(
            configBean = Application.class,
            opType = RestEndpoint.OpType.POST,
            path = "set-web-env-entry",
            description = "set-web-env-entry",
            params = {@RestParam(name = "id", value = "$parent")}
        )
})
public class SetWebEnvEntryCommand extends WebEnvEntryCommand {

    @Param(name = "name")
    private String name;

    @Param(name = "value", optional = true)
    private String value;

    @Param(name = "type", optional = true)
    private String envEntryType;

    @Param(name = "description", optional = true)
    private String description;

    @Param(name = "ignoreDescriptorItem", optional = true)
    private Boolean ignoreDescriptorItem;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();

        try {
            final Engine engine = engine(report);
            if (engine != null) {
                setEnvEntry(engine,
                    name, description, ignoreDescriptorItem, value, envEntryType,
                    report);
            }
        } catch (Exception e) {
            fail(report, e, "errSetEnvEntry", "Error setting env entry");
        }

    }

    private void setEnvEntry(final Engine owningEngine,
            final String name,
            final String description,
            final Boolean ignoreDescriptorItem,
            final String value,
            final String envEntryType,
            final ActionReport report) throws PropertyVetoException, TransactionFailure {

        WebModuleConfig config = WebModuleConfig.webModuleConfig(owningEngine);
        if (config == null) {
            createEnvEntryOnNewWMC(owningEngine, name, value, envEntryType,
                    description, ignoreDescriptorItem);
        } else {
            EnvEntry entry = config.getEnvEntry(name);
            if (entry == null) {
                if (isTypeOrIgnorePresent(ignoreDescriptorItem, envEntryType, report)) {
                    createEnvEntryOnExistingWMC(config, name,
                            value, envEntryType, description, ignoreDescriptorItem);
                }
            } else {
                modifyEnvEntry(entry, value, envEntryType, description,
                            ignoreDescriptorItem);
                succeed(report, "setWebEnvEntryOverride",
                        "Previous env-entry setting of {0} for application/module {1} was overridden.",
                        name, appNameAndOptionalModuleName());
            }
        }
    }

    private boolean isTypeOrIgnorePresent(final Boolean ignoreDescriptorItem,
            final String envEntryType,
            final ActionReport report) {
        final boolean result = (ignoreDescriptorItem != null || envEntryType != null);
        if ( ! result) {
            fail(report, "valueOrIgnoreReqd",
                    "The set-web-env-entry command for a new setting requires either the --value option or the --ignoreDescriptorItem option.");
        }
        return result;
    }

    private void createEnvEntryOnNewWMC(final Engine owningEngine,
            final String name,
            final String value,
            final String envEntryType,
            final String description,
            final Boolean ignoreDescriptorItem) throws PropertyVetoException, TransactionFailure {

        ConfigSupport.apply(new SingleConfigCode<Engine>() {

            @Override
            public Object run(Engine e) throws PropertyVetoException, TransactionFailure {
                final WebModuleConfig config = e.createChild(WebModuleConfig.class);
                e.getApplicationConfigs().add(config);
                final EnvEntry envEntry = config.createChild(EnvEntry.class);
                config.getEnvEntry().add(envEntry);
                set(envEntry, name, value, envEntryType, description, ignoreDescriptorItem);
                return config;
             }
        }, owningEngine);
    }

    private void createEnvEntryOnExistingWMC(final WebModuleConfig config,
            final String name,
            final String value,
            final String envEntryType,
            final String description,
            final Boolean ignoreDescriptorItem) throws PropertyVetoException, TransactionFailure {

        ConfigSupport.apply(new SingleConfigCode<WebModuleConfig>() {
            @Override
            public Object run(WebModuleConfig cf) throws PropertyVetoException, TransactionFailure {
                final EnvEntry envEntry = cf.createChild(EnvEntry.class);
                cf.getEnvEntry().add(envEntry);
                set(envEntry, name, value, envEntryType, description, ignoreDescriptorItem);
                return envEntry;
             }
        }, config);
    }

    private void modifyEnvEntry(final EnvEntry envEntry,
            final String value,
            final String envEntryType,
            final String description,
            final Boolean ignoreDescriptorItem) throws PropertyVetoException, TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<EnvEntry>() {

            @Override
            public Object run(EnvEntry ee) throws PropertyVetoException, TransactionFailure {
                set(ee, ee.getEnvEntryName(), value, envEntryType, description, ignoreDescriptorItem);
                return ee;
            }
        }, envEntry);
    }

    private void set(final EnvEntry envEntry,
            final String name,
            final String value,
            final String envEntryType,
            final String description,
            final Boolean ignoreDescriptorItem) throws PropertyVetoException, TransactionFailure {

        final String candidateFinalValue = (value == null) ? envEntry.getEnvEntryValue() : value;
        final String candidateFinalType = (envEntryType == null) ? envEntry.getEnvEntryType() : envEntryType;

        if (value != null || envEntryType != null) {
            if (candidateFinalValue == null || candidateFinalType == null) {

                final String fmt = localStrings.getLocalString("valueAndTypeRequired",
                        "Both a valid --type and --value are required; one is missing");
                throw new IllegalArgumentException(fmt);
            }
            try {
                EnvEntry.validateValue(candidateFinalType, candidateFinalValue);
            } catch (IllegalArgumentException ex) {
                final String fmt = localStrings.getLocalString("valueTypeMismatch",
                        "Cannot assign value {0} to an env-entry of type {1}",
                        candidateFinalValue, candidateFinalType);
                final String valueOrType = (value != null ? "--value" : "--type");
                throw new PropertyVetoException(fmt + " - " + ex.getLocalizedMessage(),
                        new PropertyChangeEvent(envEntry, valueOrType,
                            envEntry.getEnvEntryType(), envEntryType));
            }
        }

        envEntry.setEnvEntryName(name);
        if (envEntryType != null) {
            envEntry.setEnvEntryType(envEntryType);
        }
        if (value != null) {
            envEntry.setEnvEntryValue(value);
        }

        if (envEntryType != null) {
            envEntry.setEnvEntryType(envEntryType);
        }

        if (description != null) {
            envEntry.setDescription(description);
        }
        if (ignoreDescriptorItem != null) {
            envEntry.setIgnoreDescriptorItem(ignoreDescriptorItem.toString());
        }
    }
}
