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

package com.sun.enterprise.admin.util;

import com.sun.enterprise.util.io.DomainDirs;
import com.sun.enterprise.util.io.InstanceDirs;
import com.sun.enterprise.util.io.ServerDirs;

import java.io.File;
import java.io.IOException;

import org.glassfish.api.admin.CommandException;

/**
 * Based on the presence or absence of values for:
 * <ul>
 * <li>domain directory parent (e.g., ${installDir}/domains
 * <li>server name (domain or instance)
 * <li>node directory
 * <li>node name
 * </ul>
 *
 * select the correct directories object for the requested domain or node.
 *
 * @author Byron Nevins
 * @author Tim Quinn (just refactoring, rearranging the code to here)
 */
public class ServerDirsSelector {

    private File userSpecifiedDomainDirParent;
    private String userSpecifiedServerName;
    private String userSpecifiedNodeDir; // nodeDirRoot
    private String userSpecifiedNode;

    private DomainDirs domainDirs = null;
    private InstanceDirs instanceDirs = null;

    /**
     * Creates a selector for choosing the correct set of directories.
     *
     * @param domainDirParent parent of the domain directory file(s)
     * @param serverName name of the requested instance or domain
     * @param nodeDir path to the node directory
     * @param node name of the node
     * @return
     * @throws CommandException
     * @throws IOException
     */
    public static ServerDirsSelector getInstance(final File domainDirParent, final String serverName, final String nodeDir,
            final String node) throws CommandException, IOException {

        final ServerDirsSelector helper = new ServerDirsSelector(domainDirParent, serverName, nodeDir, node);

        helper.validateDomainOrInstance();
        return helper;
    }

    public ServerDirs dirs() {
        return selectDirs();
    }

    private ServerDirsSelector(final File domainDirParent, final String serverName, final String nodeDir, final String node) {
        userSpecifiedDomainDirParent = domainDirParent;
        userSpecifiedServerName = serverName;
        userSpecifiedNodeDir = nodeDir;
        userSpecifiedNode = node;
    }

    /**
     * make sure the parameters make sense for either an instance or a domain.
     */
    private void validateDomainOrInstance() throws CommandException, IOException {

        // case 1: since ddp is specified - it MUST be a domain
        if (userSpecifiedDomainDirParent != null) {
            domainDirs = new DomainDirs(userSpecifiedDomainDirParent, userSpecifiedServerName);
        }
        //case 2: if either of these are set then it MUST be an instance
        else if (userSpecifiedNode != null || userSpecifiedNodeDir != null) {
            instanceDirs = new InstanceDirs(userSpecifiedNodeDir, userSpecifiedNode, userSpecifiedServerName);
        }
        // case 3: nothing is specified -- use default domain as in v3.0
        else if (userSpecifiedServerName == null) {
            domainDirs = new DomainDirs(userSpecifiedDomainDirParent, userSpecifiedServerName);
        }
        // case 4: userSpecifiedServerName is set and the other 3 are all null
        // we need to figure out if it's a DAS or an instance
        else {
            try {
                domainDirs = new DomainDirs(userSpecifiedDomainDirParent, userSpecifiedServerName);
                return;
            } catch (IOException e) {
                // handled below
            }

            instanceDirs = new InstanceDirs(userSpecifiedNodeDir, userSpecifiedNode, userSpecifiedServerName);
        }
    }

    public boolean isInstance() {
        return instanceDirs != null;
    }

    private ServerDirs selectDirs() {
        if (isInstance())
            return instanceDirs.getServerDirs();
        else
            return domainDirs.getServerDirs();
    }
}
