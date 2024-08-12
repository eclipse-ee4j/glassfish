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

import java.beans.PropertyVetoException;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.web.config.serverbeans.ContextParam;
import org.glassfish.web.config.serverbeans.WebModuleConfig;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 *
 * @author tjquinn
 */
@Service(name = "set-web-context-param")
@I18n("setWebContextParam.command")
@PerLookup
@RestEndpoints({
        @RestEndpoint(
                configBean = Application.class,
                opType = RestEndpoint.OpType.POST,
                path = "set-web-context-param",
                description = "set-web-context-param",
                params = {@RestParam(name = "id", value = "$parent")}
        )
})
public class SetWebContextParamCommand extends WebModuleConfigCommand {

    @Param(name = "name")
    private String name;

    @Param(name = "value", optional = true)
    private String value;

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
                setContextParam(engine,
                    name, value, description, ignoreDescriptorItem, report);
            }
        } catch (Exception e) {
            fail(report, e, "errSetContextParam", "Error setting context param");
        }
    }

    private void setContextParam(final Engine owningEngine,
            final String paramName,
            final String paramValue,
            final String description,
            final Boolean ignoreDescriptorItem,
            final ActionReport report) throws PropertyVetoException, TransactionFailure {

        WebModuleConfig config = WebModuleConfig.webModuleConfig(owningEngine);
        if (config == null) {
            createContextParamOnNewWMC(owningEngine, paramName, paramValue,
                    description, ignoreDescriptorItem);
        } else {
            ContextParam cp = config.getContextParam(paramName);
            if (cp == null) {
                createContextParamOnExistingWMC(config, paramName,
                        paramValue, description, ignoreDescriptorItem);
            } else {
                modifyContextParam(cp, paramValue, description,
                            ignoreDescriptorItem);
                succeed(report, "setWebContextParamOverride",
                        "Previous context-param setting of {0} for application/module {1} was overridden.",
                        name, appNameAndOptionalModuleName());
            }
        }
    }

    private void createContextParamOnNewWMC(
            final Engine owningEngine,
            final String paramName,
            final String paramValue,
            final String description,
            final Boolean ignoreDescriptorItem) throws PropertyVetoException, TransactionFailure {


        ConfigSupport.apply(new SingleConfigCode<Engine>() {

            @Override
            public Object run(Engine e) throws PropertyVetoException, TransactionFailure {
                final WebModuleConfig config = e.createChild(WebModuleConfig.class);
                e.getApplicationConfigs().add(config);
                final ContextParam newParam = config.createChild(ContextParam.class);
                config.getContextParam().add(newParam);
                set(newParam, paramName, paramValue, description, ignoreDescriptorItem);
                return config;
            }
        }, owningEngine);

    }

    private void createContextParamOnExistingWMC(
            final WebModuleConfig config,
            final String paramName,
            final String paramValue,
            final String description,
            final Boolean ignoreDescriptorItem) throws PropertyVetoException, TransactionFailure {

        ConfigSupport.apply(new SingleConfigCode<WebModuleConfig>() {

            @Override
            public Object run(WebModuleConfig cf) throws PropertyVetoException, TransactionFailure {
                final ContextParam param = cf.createChild(ContextParam.class);
                cf.getContextParam().add(param);
                set(param, paramName, paramValue, description, ignoreDescriptorItem);
                return param;
            }
        }, config);
    }

    private void modifyContextParam(
            final ContextParam param,
            final String paramValue,
            final String description,
            final Boolean ignoreDescriptorItem) throws PropertyVetoException, TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<ContextParam>() {

            @Override
            public Object run(ContextParam cp) throws PropertyVetoException, TransactionFailure {
                set(cp, cp.getParamName(), paramValue, description, ignoreDescriptorItem);
                return cp;
            }
        }, param);
    }

    private void set(final ContextParam param,
            final String paramName,
            final String paramValue,
            final String description,
            final Boolean ignoreDescriptorItem) throws PropertyVetoException, TransactionFailure {
        if (ignoreDescriptorItem != null) {
            param.setIgnoreDescriptorItem(ignoreDescriptorItem.toString());
        }
        if (description != null) {
            param.setDescription(description);
        }
        if (paramValue != null) {
            param.setParamValue(paramValue);
        }
        if (paramName != null) {
            param.setParamName(paramName);
        }
    }

 }
