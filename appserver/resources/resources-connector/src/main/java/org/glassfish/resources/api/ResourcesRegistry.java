/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resources.api;

import com.sun.enterprise.config.serverbeans.Resources;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourcesRegistry {

    private static final Map<String, Map<String, Resources>> RESOURCE_CONFIGURATIONS = new ConcurrentHashMap<>();

    public static Resources getResources(String appName, String moduleName) {
        Map<String, Resources> allResources = RESOURCE_CONFIGURATIONS.get(appName);
        if (allResources != null) {
            return allResources.get(moduleName);
        }
        return null;
    }


    public static Map<String, Resources> getResources(String appName) {
        return RESOURCE_CONFIGURATIONS.get(appName);
    }


    public static void putResources(String appName, Map<String, Resources> allResources) {
        RESOURCE_CONFIGURATIONS.put(appName, allResources);
    }


    public static Map<String, Resources> remove(String appName) {
        return RESOURCE_CONFIGURATIONS.remove(appName);
    }
}
