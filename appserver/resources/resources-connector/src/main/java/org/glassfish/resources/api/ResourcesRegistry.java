/*
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourcesRegistry {

    private static Map<String, Map<String, Resources>> resourceConfigurations =
            new ConcurrentHashMap<String, Map<String, Resources>>();


    public static Resources getResources(String appName, String moduleName){
        Map<String, Resources> allResources = resourceConfigurations.get(appName);
        if(allResources != null){
            return allResources.get(moduleName);
        }
        return null;
    }

    public static Map<String, Resources> getResources(String appName){
        return resourceConfigurations.get(appName);
    }

    public static void putResources(String appName, Map<String, Resources> allResources){
        resourceConfigurations.put(appName, allResources);
    }

    public static Map<String, Resources> remove(String appName){
        return resourceConfigurations.remove(appName);
    }

}
