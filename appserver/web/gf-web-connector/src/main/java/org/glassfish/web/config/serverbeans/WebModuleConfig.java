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

package org.glassfish.web.config.serverbeans;

import com.sun.enterprise.config.serverbeans.ApplicationConfig;
import com.sun.enterprise.config.serverbeans.Engine;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.DuckTyped;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;


/**
 * Corresponds to the web-module-config element used for recording web
 * module configuration customizations.
 *
 * @author tjquinn
 */
@Configured
public interface WebModuleConfig extends ConfigBeanProxy, ApplicationConfig {

    /**
     * Returns the env-entry objects, if any.
     * @return the env-entry objects
     */
    @Element
    public List<EnvEntry> getEnvEntry();

    /**
     * Returns the context-param objects, if any.
     * @return the context-param objects
     */
    @Element
    public List<ContextParam> getContextParam();

    @DuckTyped public EnvEntry getEnvEntry(final String name);

    @DuckTyped public ContextParam getContextParam(final String name);

    @DuckTyped public void deleteEnvEntry(final String name) throws PropertyVetoException, TransactionFailure;

    @DuckTyped public void deleteContextParam(final String name) throws PropertyVetoException, TransactionFailure;

    @DuckTyped public List<EnvEntry> envEntriesMatching(final String nameOrNull);

    @DuckTyped public List<ContextParam> contextParamsMatching(final String nameOrNull);

    public class Duck {

        public static EnvEntry getEnvEntry(final WebModuleConfig instance,
                final String name) {
            for (EnvEntry entry : instance.getEnvEntry()) {
                if (entry.getEnvEntryName().equals(name)) {
                    return entry;
                }
            }
            return null;
        }

        public static ContextParam getContextParam(final WebModuleConfig instance,
                final String name){
            for (ContextParam param : instance.getContextParam()) {
                if (param.getParamName().equals(name)) {
                    return param;
                }
            }
            return null;
        }

        public static void deleteEnvEntry(final WebModuleConfig instance,
                final String name) throws PropertyVetoException, TransactionFailure {
            final EnvEntry entry = instance.getEnvEntry(name);
            if (entry == null) {
                return;
            }
            ConfigSupport.apply(new SingleConfigCode<WebModuleConfig>(){

                @Override
                public Object run(WebModuleConfig config) throws PropertyVetoException, TransactionFailure {
                    return config.getEnvEntry().remove(entry);
                }

                }, instance);
        }

        public static void deleteContextParam(final WebModuleConfig instance,
                final String name) throws PropertyVetoException, TransactionFailure {
            final ContextParam param = instance.getContextParam(name);
            if (param == null) {
                return;
            }
            ConfigSupport.apply(new SingleConfigCode<WebModuleConfig>(){

                @Override
                public Object run(WebModuleConfig config) throws PropertyVetoException, TransactionFailure {
                    return config.getContextParam().remove(param);
                }

                }, instance);

        }



        public static List<EnvEntry> envEntriesMatching(final WebModuleConfig instance,
                final String nameOrNull) {
            List<EnvEntry> result;
            if (nameOrNull != null) {
                final EnvEntry entry = getEnvEntry(instance, nameOrNull);
                if (entry == null) {
                    result = Collections.emptyList();
                } else {
                    result = new ArrayList<EnvEntry>();
                    result.add(entry);
                }
            } else {
                result = instance.getEnvEntry();
            }
            return Collections.unmodifiableList(result);
        }

        public static List<ContextParam> contextParamsMatching(final WebModuleConfig instance,
                final String nameOrNull) {
            List<ContextParam> result;
            if (nameOrNull != null) {
                final ContextParam param = getContextParam(instance, nameOrNull);
                if (param == null) {
                    result = Collections.emptyList();
                } else {
                    result = new ArrayList<ContextParam>();
                    result.add(param);
                }
            } else {
                result = instance.getContextParam();
            }
            return Collections.unmodifiableList(result);

        }

        public static WebModuleConfig webModuleConfig(final Engine engine) {
            return (WebModuleConfig) engine.getApplicationConfig();
        }
    }

}
