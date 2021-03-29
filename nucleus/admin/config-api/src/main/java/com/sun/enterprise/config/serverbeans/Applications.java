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

package com.sun.enterprise.config.serverbeans;

import org.glassfish.api.admin.RestRedirect;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.DuckTyped;
import org.jvnet.hk2.config.Element;

import java.util.*;
import org.glassfish.api.admin.config.ApplicationName;

@Configured
public interface Applications extends ConfigBeanProxy {

    /**
     * Gets the value of the MbeanorApplication property. Objects of the following type(s) are allowed in the list
     * {@link Application }
     */
    @Element("*")
    @RestRedirect(opType = RestRedirect.OpType.PUT, commandName = "deploy")
    public List<ApplicationName> getModules();

    /**
     * Gets a subset of {@link #getModules()} that has the given type.
     */
    @DuckTyped
    <T> List<T> getModules(Class<T> type);

    @DuckTyped
    <T> T getModule(Class<T> type, String moduleID);

    @DuckTyped
    List<Application> getApplications();

    /**
     * Return the application with the given module ID (name), or null if no such application exists.
     *
     * @param moduleID the module ID of the application
     * @return the Application object, or null if no such app
     */
    @DuckTyped
    Application getApplication(String moduleID);

    @DuckTyped
    List<Application> getApplicationsWithSnifferType(String snifferType);

    @DuckTyped
    List<Application> getApplicationsWithSnifferType(String snifferType, boolean onlyStandaloneModules);

    public class Duck {
        public static <T> List<T> getModules(Applications apps, Class<T> type) {
            List<T> modules = new ArrayList<T>();
            for (Object module : apps.getModules()) {
                if (type.isInstance(module)) {
                    modules.add(type.cast(module));
                }
            }
            // you have to return an umodifiable list since this list
            // is not the real list of elements as maintained by this config bean
            return Collections.unmodifiableList(modules);
        }

        public static <T> T getModule(Applications apps, Class<T> type, String moduleID) {
            if (moduleID == null) {
                return null;
            }

            for (ApplicationName module : apps.getModules())
                if (type.isInstance(module) && module.getName().equals(moduleID))
                    return type.cast(module);

            return null;

        }

        public static List<Application> getApplications(Applications apps) {
            return getModules(apps, Application.class);
        }

        public static Application getApplication(Applications apps, String moduleID) {
            if (moduleID == null) {
                return null;
            }

            for (ApplicationName module : apps.getModules())
                if (module instanceof Application && module.getName().equals(moduleID))
                    return (Application) module;

            return null;
        }

        public static List<Application> getApplicationsWithSnifferType(Applications apps, String snifferType) {
            return getApplicationsWithSnifferType(apps, snifferType, false);
        }

        public static List<Application> getApplicationsWithSnifferType(Applications apps, String snifferType,
                boolean onlyStandaloneModules) {
            List<Application> result = new ArrayList<Application>() {

            };

            List<Application> applications = getModules(apps, Application.class);

            for (Application app : applications) {
                if (app.containsSnifferType(snifferType)) {
                    if (onlyStandaloneModules) {
                        if (app.isStandaloneModule()) {
                            result.add(app);
                        }
                    } else {
                        result.add(app);
                    }
                }
            }

            return Collections.unmodifiableList(result);
        }
    }
}
