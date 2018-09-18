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

package com.sun.enterprise.universal.xml;

import java.util.HashMap;
import java.util.Map;

import static com.sun.enterprise.util.StringUtils.ok;

/**
 * MiniXmlParser is getting way too long and complicated.  All new code should go
 * into new and/or different classes.
 *
 * This class is for organizing system-property's from several different parts of the
 * config.
 * The system-property order of priority, from highest to lowest is:
 * 1. <server>
 * 2. <cluster> -- if applicable
 * 3. <config>
 * 4. <domain>
 * ref: http://java.net/jira/browse/GLASSFISH-16121
 * created April 8, 2011
 * @author Byron Nevins
 */
class SysPropsHandler {
    enum Type {
        SERVER, CLUSTER, CONFIG, DOMAIN
    };

    Map<String, String> getCombinedSysProps() {
        Map<String, String> map = new HashMap<String, String>(domain);
        map.putAll(config);
        map.putAll(cluster);
        map.putAll(server);

        return map;
    }

    // perhaps a bit inefficient.
    // TODO go through the maps one after the next.
    String get(String key) {
        return getCombinedSysProps().get(key);
    }

    // TODO these 2 add methods could be made more efficient.  Probably not worth
    // the effort.

    void add(Type type, Map<String, String> map) {
        if (type == null || map == null)
            return; // TODO : throw ????

        switch (type) {
            case SERVER:
                server.putAll(map);
                break;
            case CLUSTER:
                cluster.putAll(map);
                break;
            case CONFIG:
                config.putAll(map);
                break;
            case DOMAIN:
                domain.putAll(map);
                break;
            default:
                throw new IllegalArgumentException("unknown type");
       }
    }
    void add(Type type, String name, String value) {
        if (type == null || !ok(name))
            //throw new NullPointerException();
            return; // TODO : throw ????

        switch (type) {
            case SERVER:
                server.put(name, value);
                break;
            case CLUSTER:
                cluster.put(name, value);
                break;
            case CONFIG:
                config.put(name, value);
                break;
            case DOMAIN:
                domain.put(name, value);
                break;
            default:
                throw new IllegalArgumentException("unknown type");
        }
    }
    private Map<String, String> server = new HashMap<String, String>();
    private Map<String, String> cluster = new HashMap<String, String>();
    private Map<String, String> config = new HashMap<String, String>();
    private Map<String, String> domain = new HashMap<String, String>();
}
