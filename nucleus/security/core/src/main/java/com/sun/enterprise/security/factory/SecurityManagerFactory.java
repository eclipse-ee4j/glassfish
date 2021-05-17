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

package com.sun.enterprise.security.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author monzillo
 */
public class SecurityManagerFactory {

    protected SecurityManagerFactory() {
      //do not allow creation of this base class directly
    }
    /**
     * find SecurityManager by policy context identifier and name (as
     * secondary key). There is a separate SecurityManager for each EJB of
     * a module; they all share the same policy context identifier, and are
     * differentiated by (ejb) name. There is one SecurityManager per web
     * module/policy context identifier.
     *
     * @param iD2sMmap maps policy context id (as key) to subordinate map
     * (as value) of (module) name (as key) to SecurityManagers (as value).
     * @param ctxId the policy context identifier (i.e., the primary key of the
     * lookup
     * @param name the name of the component (e.g., the ejb name when looking up
     * an EJBSecurityManager. for WebSecurityManagers this value should be null
     * @param remove boolean indicating whether the corresponding SecurityManager
     * is to be deleted from the map.
     * @return the selected SecurityManager or null.
     */
    public<T> T getManager(Map<String, Map<String, T>> iD2sMmap,
            String ctxId, String name, boolean remove) {

        T manager = null;

        synchronized (iD2sMmap) {
            Map<String, T> managerMap = iD2sMmap.get(ctxId);
            if (managerMap != null) {
                manager = managerMap.get(name);
                if (remove) {
                    managerMap.remove(name);
                    if (managerMap.isEmpty()) {
                        iD2sMmap.remove(ctxId);
                    }
                }
            }

        }

        return manager;
    }

    /**
     * Get all SecurityManagers associated with a policy context identifier.
     * EJBs from the same jar, all share the same policy context identifier, but
     * each have their own SecurityManager. WebSecurityManager(s)
     * map 1-to-1 to policy context identifier.
     *
     * @param iD2sMmap maps policy context id (as key) to subordinate map
     * (as value) of (module) name (as key) to SecurityManagers (as value).
     * @param ctxId the policy context identifier (i.e., the lookup key).
     * @param remove boolean indicating whether the corresponding
     * SecurityManager(s) are to be deleted from the map.
     * @return a non-empty ArrayList containing the selected managers, or null.
     */
    public <T> ArrayList<T> getManagers(Map<String, Map<String, T>> iD2sMmap,
            String ctxId, boolean remove) {

        ArrayList<T> managers = null;

        synchronized (iD2sMmap) {
            Map<String, T> managerMap = iD2sMmap.get(ctxId);

            if (managerMap != null && !managerMap.isEmpty()) {
                managers = new ArrayList(managerMap.values());
            }
            if (remove) {
                iD2sMmap.remove(ctxId);
            }
        }
        return managers;
    }

    /**
     * Get (Web or EJB) SecurityManagers associated with an application.
     * Note that the WebSecurityManager and EJBSecurityManager classes manage
     * separate maps for their respectibe security manager types.
     *
     * @param iD2sMmap maps policy context id (as key) to subordinate map
     * (as value) of (module) name (as key) to SecurityManagers (as value).
     * @param app2iDmap maps appName (as key) to list of policy context
     * identifiers (as value).
     * @param appName the application name, (i.e., the lookup key)
     * @param remove boolean indicating whether the corresponding mappings
     * are to be removed from the app2iDmap and aiD2sMmap.
     * @return a non-empty ArrayList containing the selected managers, or null.
     */
    public <T> ArrayList<T> getManagersForApp(Map<String, Map<String, T>> iD2sMmap,
            Map<String, ArrayList<String>> app2iDmap, String appName, boolean remove) {

        ArrayList<T> managerList = null;
        String[] ctxIds = getContextsForApp(app2iDmap, appName, remove);
        if (ctxIds != null) {
            ArrayList<T> managers = null;
            synchronized (iD2sMmap) {
                for (String id : ctxIds) {
                    managers = getManagers(iD2sMmap, id, remove);
                    if (managers != null) {
                        if (managerList == null) {
                            managerList = new ArrayList<T>();
                        }
                        managerList.addAll(managers);
                    }
                }
            }
        }

        return managerList;
    }

    /**
     * Get (EJB or Web) Policy context identifiers for app.
     *
     * @param app2iDmap maps appName (as key) to list of policy context
     * identifiers (as value).
     * @param appName the application name, (i.e., the lookup key).
     * @param remove boolean indicating whether the corresponding mappings
     * are to be removed from the app2iDmap.
     * @return a non-zero length array containing the selected
     * policy context identifiers, or null.
     */
    public <T> String[] getContextsForApp(Map<String, ArrayList<String>> app2iDmap,
            String appName, boolean remove) {

        String[] ctxIds = null;

        synchronized (app2iDmap) {

            ArrayList<String> ctxList = app2iDmap.get(appName);
            if (ctxList != null && !ctxList.isEmpty()) {
                ctxIds = ctxList.toArray(new String[ctxList.size()]);
            }
            if (remove) {
                app2iDmap.remove(appName);
            }
        }

        return ctxIds;
    }

    /**
     * In iD2sMmap, maps manager to  ctxId and name, and in app2iDmap,
     * includes ctxID in values mapped to appName.
     *
     * @param iD2sMmap maps policy context id (as key) to subordinate map
     * (as value) of (module) name (as key) to SecurityManagers (as value).
     * @param app2iDmap maps appName (as key) to list of policy context
     * identifiers (as value).
     * @param ctxId the policy context identifier
     * @param name the component name (the EJB name or null for web modules)
     * @param appName the application name
     * @param manager the SecurityManager
     */
    public <T> void addManagerToApp(Map<String, Map<String, T>> iD2sMmap,
            Map<String, ArrayList<String>> app2iDmap,
            String ctxId, String name, String appName, T manager) {

        synchronized (iD2sMmap) {

            Map<String, T> managerMap = iD2sMmap.get(ctxId);

            if (managerMap == null) {
                managerMap = new HashMap<String, T>();
                iD2sMmap.put(ctxId, managerMap);
            }

            managerMap.put(name, manager);
        }
        synchronized (app2iDmap) {
            ArrayList<String> ctxList = app2iDmap.get(appName);

            if (ctxList == null) {
                ctxList = new ArrayList<String>();
                app2iDmap.put(appName, ctxList);
            }

            if (!ctxList.contains(ctxId)) {
                ctxList.add(ctxId);
            }
        }
    }
}
