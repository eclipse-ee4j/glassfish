/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.connectors.admin.cli;

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;

import java.util.List;

import org.glassfish.connectors.config.WorkSecurityMap;

public class WorkSecurityMapHelper {
    static boolean doesResourceAdapterNameExist(String raName, Resources resources) {
        //check if the resource adapter exists.If it does not then throw an exception.
        boolean doesRAExist = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof WorkSecurityMap) {
                if (((WorkSecurityMap) resource).getResourceAdapterName().equals(raName)) {
                    doesRAExist = true;
                    break;
                }
            }
        }
        return doesRAExist;
    }

    static boolean doesMapNameExist(String raName, String mapname, Resources resources) {
        //check if the mapname exists for the given resource adapter name..
        List<WorkSecurityMap> maps = ConnectorsUtil.getWorkSecurityMaps(raName, resources);

        boolean doesMapNameExist = false;
        if (maps != null) {
            for (WorkSecurityMap wsm : maps) {
                String name = wsm.getName();
                if (name.equals(mapname)) {
                    doesMapNameExist = true;
                    break;
                }
            }
        }
        return doesMapNameExist;
    }

    static WorkSecurityMap getSecurityMap(String mapName, String raName, Resources resources) {
        List<WorkSecurityMap> maps = ConnectorsUtil.getWorkSecurityMaps(raName, resources);
        WorkSecurityMap map = null;
        if (maps != null) {
            for (WorkSecurityMap wsm : maps) {
                if (wsm.getName().equals(mapName)) {
                    map = wsm;
                    break;
                }
            }
        }
        return map;
    }

}
