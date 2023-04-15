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

package org.glassfish.web.config.serverbeans;

import com.sun.enterprise.config.serverbeans.ApplicationConfig;
import com.sun.enterprise.config.serverbeans.Engine;

import java.util.Collections;
import java.util.List;

import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
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
     * Returns the {@code env-entry} objects, if any.
     *
     * @return the {@code env-entry} objects
     */
    @Element
    List<EnvEntry> getEnvEntry();

    /**
     * Returns the {@code context-param} objects, if any.
     *
     * @return the {@code context-param} objects
     */
    @Element
    List<ContextParam> getContextParam();

    default EnvEntry getEnvEntry(final String name) {
        for (EnvEntry entry : getEnvEntry()) {
            if (entry.getEnvEntryName().equals(name)) {
                return entry;
            }
        }
        return null;
    }

    default ContextParam getContextParam(final String name) {
        for (ContextParam param : getContextParam()) {
            if (param.getParamName().equals(name)) {
                return param;
            }
        }
        return null;
    }

    default void deleteEnvEntry(final String name) throws TransactionFailure {
        final EnvEntry entry = getEnvEntry(name);
        if (entry == null) {
            return;
        }
        ConfigSupport.apply(config -> config.getEnvEntry().remove(entry), this);
    }

    default void deleteContextParam(final String name) throws TransactionFailure {
        final ContextParam param = getContextParam(name);
        if (param == null) {
            return;
        }
        ConfigSupport.apply(config -> config.getContextParam().remove(param), this);
    }

    default List<EnvEntry> envEntriesMatching(final String nameOrNull) {
        List<EnvEntry> result;
        if (nameOrNull != null) {
            final EnvEntry entry = getEnvEntry(nameOrNull);
            if (entry == null) {
                result = List.of();
            } else {
                result = List.of(entry);
            }
        } else {
            result = Collections.unmodifiableList(getEnvEntry());
        }
        return result;
    }

    default List<ContextParam> contextParamsMatching(final String nameOrNull) {
        List<ContextParam> result;
        if (nameOrNull != null) {
            final ContextParam param = getContextParam(nameOrNull);
            if (param == null) {
                result = List.of();
            } else {
                result = List.of(param);
            }
        } else {
            result = Collections.unmodifiableList(getContextParam());
        }
        return result;
    }

    static WebModuleConfig webModuleConfig(final Engine engine) {
        return (WebModuleConfig) engine.getApplicationConfig();
    }
}
