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

package com.sun.enterprise.util.cluster;

import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.util.StringUtils;

/**
 * Why go through the painful process of creating Paths and hard-coding in filenames
 * in more than one place?  Let's do it in here and only in here
 * @author Byron Nevins
 */
public final class Paths {
    public final static String DAS_PROPS_FILENAME =  "das.properties";
    public final static String DAS_PROPS_SUBPATH =  "agent/config/" + DAS_PROPS_FILENAME;

    // TODO: the Node class ought to do this!
    public final static String getNodesDir(final Node node) {
        if (node == null) // don't do that!
            throw new NullPointerException();

        String nodesDir = node.getNodeDirAbsoluteUnixStyle();

        if (nodesDir == null)
            nodesDir = node.getInstallDirUnixStyle() + "/glassfish/nodes";

        return nodesDir;
    }

    public final static String getNodeDir(final Node node) {
        return getNodesDir(node) + "/" + node.getName();
    }

    public final static String getDasPropsPath(final Node node) {
        return getNodeDir(node) + "/" + DAS_PROPS_SUBPATH;
    }

    public static String getInstanceDirPath(final Node node, final String instanceName) {
        if (!StringUtils.ok(instanceName))
            throw new IllegalArgumentException(); // don't do that!

        return getNodeDir(node) + "/" + instanceName;
    }

    private Paths() {
        // all static methods
    }
}
