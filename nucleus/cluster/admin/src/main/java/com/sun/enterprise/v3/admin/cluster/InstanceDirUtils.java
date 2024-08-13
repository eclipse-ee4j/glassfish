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

package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.util.io.InstanceDirs;

import java.io.File;
import java.io.IOException;

import org.glassfish.internal.api.ServerContext;


final class InstanceDirUtils{
    Node node;
    ServerContext serverContext;


    InstanceDirUtils(Node node, ServerContext serverContext){
        this.node = node;
        this.serverContext = serverContext;
    }

    /**
     * Returns the directory for the selected instance that is on the local
     * system.
     * @param instanceName name of the instance
     * @return File for the local file system location of the instance directory
     * @throws IOException
     */
    File getLocalInstanceDir(String instance) throws IOException {
        /*
         * Pass the node directory parent and the node directory name explicitly
         * or else InstanceDirs will not work as we want if there are multiple
         * nodes registered on this node.
         *
         * If the configuration recorded an explicit directory for the node,
         * then use it.  Otherwise, use the default node directory of
         * ${installDir}/glassfish/nodes/${nodeName}.
         */
        String nodeDir = node.getNodeDirAbsolute();
        final File nodeDirFile = (nodeDir != null ?
            new File(nodeDir) :
            defaultLocalNodeDirFile());
        InstanceDirs instanceDirs = new InstanceDirs(nodeDirFile.toString(), node.getName(), instance);
        return instanceDirs.getInstanceDir();
    }

    File defaultLocalNodeDirFile() {
        /*
         * The "nodes" directory we want to use is a child of
         * the install directory.
         *
         * The installDir field contains the installation directory which the
         * administrator specified, if s/he specified one, when the target node
         * was first created.  It is null if the administrator did not specify
         * an installation directory for the node.  In that case we should
         * use the DAS's install directory (because this method applies in the
         * local instance case).
         */
        String installDir = node.getInstallDir();
        final File nodeParentDir = (
                installDir == null
                    ? serverContext.getInstallRoot()
                    : new File(installDir, "glassfish"));
        return new File(nodeParentDir, "nodes");
    }
}
