/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util.cluster;

import java.util.List;

public final class NodeInfo {

    private final String host;
    private final String name;
    private final String installDir;
    private final String instancesList;
    private final String type;

    private static final String NAME = Strings.get("ListNode.name");
    private static final String TYPE = Strings.get("ListNode.type");
    private static final String HOST = Strings.get("ListNode.host");
    private static final String INSTALLDIR = Strings.get("ListNode.installDir");
    private static final String INSTANCESLIST = Strings.get("ListNode.instancesList");

    public NodeInfo(String name1, String host1, String installDir1, String type1, String instancesList1){
        this.name = name1;
        this.host = host1;
        this.installDir = installDir1;
        this.type = type1;
        this.instancesList = instancesList1;
    }


    public final String getHost() {
        return host;
    }

    public final String getType() {
        return type;
    }

    public final String getName() {
        return name;
    }

    public final String getInstallDir() {
        return installDir;
    }

    public final String getInstancesList() {
        return instancesList;
    }

    public static String format(List<NodeInfo> infos) {
        int longestName = NAME.length();
        int longestType = TYPE.length();
        int longestInstallDir = INSTALLDIR.length();
        int longestHost = HOST.length();
        int longestInstancesList = INSTANCESLIST.length();

        int nullStringLen = 4;
        for (NodeInfo info : infos) {
            int namel = info.getName().length();
            int hostl = info.getHost() != null ? info.getHost().length(): nullStringLen;
            int type1 = info.getType() != null ? info.getType().length(): nullStringLen;
            int installDir1 = info.getInstallDir() != null ? info.getInstallDir().length() : nullStringLen;
            int instancesList1 = info.getInstancesList() != null ? info.getInstancesList().length() : nullStringLen;

            if (namel > longestName)
                longestName = namel;
            if (hostl > longestHost)
                longestHost = hostl;
            if (type1 > longestType)
                longestType = type1;
            if (installDir1 > longestInstallDir)
                longestInstallDir = installDir1;
            if (instancesList1 > longestInstancesList)
                longestInstancesList = instancesList1;
        }

        longestName += 2;
        longestHost += 2;
        longestType += 2;
        longestInstallDir += 2;
        longestInstancesList += 2;

        StringBuilder sb = new StringBuilder();

        String formattedLine =
                "%-" + longestName
                + "s %-" + longestType
                + "s %-" + longestHost
                + "s %-" + longestInstallDir
                + "s %-" + longestInstancesList
                + "s";

        sb.append(String.format(formattedLine, NAME, TYPE, HOST, INSTALLDIR, INSTANCESLIST));
        sb.append('\n');

        // no linefeed at the end!!!
        boolean first = true;
        for (NodeInfo info : infos) {
            if (first)
                first = false;
            else
                sb.append('\n');

            sb.append(String.format(formattedLine, info.getName(), info.getType(),  info.getHost(), info.getInstallDir(), info.getInstancesList()));
        }

        return sb.toString();
    }



}
