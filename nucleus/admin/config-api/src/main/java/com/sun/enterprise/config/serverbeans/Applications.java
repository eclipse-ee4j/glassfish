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

package com.sun.enterprise.config.serverbeans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.glassfish.api.admin.RestRedirect;
import org.glassfish.api.admin.config.ApplicationName;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

@Configured
public interface Applications extends ConfigBeanProxy {

    /**
     * Gets the value of the Application property. Objects of the following
     * type(s) are allowed in the list {@link Application }
     */
    @Element("*")
    @RestRedirect(opType = RestRedirect.OpType.PUT, commandName = "deploy")
    List<ApplicationName> getModules();

    /**
     * Gets a subset of {@link #getModules()} that has the given type.
     */
    default <T> List<T> getModules(Class<T> type) {
        List<T> modules = new ArrayList<>();
        for (Object module : getModules()) {
            if (type.isInstance(module)) {
                modules.add(type.cast(module));
            }
        }
        // you have to return an unmodifiable list since this list
        // is not the real list of elements as maintained by this config bean
        return Collections.unmodifiableList(modules);
    }

    default <T> T getModule(Class<T> type, String moduleID) {
        if (moduleID == null) {
            return null;
        }

        for (ApplicationName module : getModules()) {
            if (type.isInstance(module) && module.getName().equals(moduleID)) {
                return type.cast(module);
            }
        }
        return null;
    }

    default List<Application> getApplications() {
        return getModules(Application.class);
    }

    /**
     * Return the application with the given module ID (name), or {@code null} if no such application exists.
     *
     * @param moduleID the module ID of the application
     * @return the {@code Application} object, or {@code null} if no such app
     */
    default Application getApplication(String moduleID) {
        if (moduleID == null) {
            return null;
        }

        for (ApplicationName module : getModules()) {
            if (module instanceof Application && module.getName().equals(moduleID)) {
                return (Application) module;
            }
        }
        return null;
    }

    default List<Application> getApplicationsWithSnifferType(String snifferType) {
        return getApplicationsWithSnifferType(snifferType, false);
    }

    default List<Application> getApplicationsWithSnifferType(String snifferType, boolean onlyStandaloneModules) {
        List<Application> applications = new ArrayList<>();
        for (Application app : getModules(Application.class)) {
            if (app.containsSnifferType(snifferType)) {
                if (onlyStandaloneModules) {
                    if (app.isStandaloneModule()) {
                        applications.add(app);
                    }
                } else {
                    applications.add(app);
                }
            }
        }
        return Collections.unmodifiableList(applications);
    }
}
