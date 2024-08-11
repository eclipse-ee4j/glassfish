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

import com.sun.enterprise.universal.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Normally I'd have named this class "Cluster" but we don't want it to clash
 * with the config class with the same name.  This name makes life easier.
 *
 * This class is only used in this package.  It is more like an old-fashioned
 * "C" struct.  Help yourself to the data variables!
 *
 * A cluster in domain.xml contains 2 things that we care about:
 * <ul>
 * <li> a list of servers that the cluster owns
 * <li> a list of system properties
 *
 * created April 9, 2011
 * @author Byron Nevins
 */
final class ParsedCluster {
    ParsedCluster(String theName) {
        name = theName;
    }

    // forced to make this public...
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Cluster: ");
        sb.append(name).append('\n');
        sb.append("properties: ").append(CollectionUtils.toString(sysProps)).append('\n');
        sb.append("server names: ").append(CollectionUtils.toString(serverNames)).append('\n');
        return sb.toString();
    }

    Map<String, String> getMySysProps(String serverName) {
        if(serverNames.contains(serverName))
            return sysProps;
        return null;
    }

    final Map<String, String> sysProps = new HashMap<String, String>();
    final List<String> serverNames = new ArrayList<String>();
    private final String name;

}
