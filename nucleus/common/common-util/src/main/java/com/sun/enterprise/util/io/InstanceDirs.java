/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util.io;

import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.glassfish.api.admin.CommandException;

import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;

/**
 * A class for keeping track of the directories that an instance lives in and under.
 * All the methods throw checked exception to avoid the inevitable NPE otherwise -
 * when working with invalid directories...
 *
 * Example:
 * new InstanceDirs(new File("/glassfish7/glassfish/nodes/mymachine/instance1"));
 *
 * getInstanceDir()   == /glassfish7/glassfish/nodes/mymachine/instance1
 * getNodeAgentDir()  == /glassfish7/glassfish/nodes/mymachine
 * getNodeAgentsDir() == /glassfish7/glassfish/nodes
 * getInstanceName()  == instance1
 *
 *
 * @author Byron Nevins
 * @since 3.1
 * Created: April 19, 2010
 */
public final class InstanceDirs {

    private final ServerDirs dirs;

    /**
     * This constructor is used when the instance dir is known
     *
     * @param instanceDir The instance's directory
     * @throws IOException If any error including not having a grandparent directory.
     */
    public InstanceDirs(File instanceDir) throws IOException {
        dirs = new ServerDirs(instanceDir);

        if (dirs.getServerGrandParentDir() == null) {
            throw new IOException(ServerDirs.strings.get("InstanceDirs.noGrandParent", dirs.getServerDir()));
        }
    }

    /**
     * This constructor handles 0, 1, 2 or 3 null args.
     * It is smart enough to figure out many defaults.
     *
     * @param nodeDirParentPath E.g. install-dir/nodes
     * @param nodeDirName E.g. install-dir/nodes/localhost
     * @param instanceName E.g. i1
     */
    public InstanceDirs(String nodeDirParentPath, String nodeDirName, String instanceName) throws IOException {
        if (!StringUtils.ok(nodeDirParentPath)) {
            nodeDirParentPath = getNodeDirRootDefault();
        }

        final File nodeDirParent = new File(nodeDirParentPath);
        if (!nodeDirParent.isDirectory()) {
            dirs = null;
            throw new IOException(Strings.get("InstanceDirs.noNodeParent", nodeDirParent));
        }

        final File nodeDir;
        if (StringUtils.ok(nodeDirName)) {
            nodeDir = new File(nodeDirParent, nodeDirName);
        } else {
            nodeDir = getTheOneAndOnlyNode(nodeDirParent);
        }
        if (!nodeDir.isDirectory()) {
            dirs = null;
            throw new IOException(Strings.get("InstanceDirs.badNodeDir", nodeDir));
        }

        final File instanceDir;
        if (StringUtils.ok(instanceName)) {
            instanceDir = new File(nodeDir, instanceName);
        } else {
            instanceDir = getTheOneAndOnlyInstance(nodeDir);
        }
        if (!instanceDir.isDirectory()) {
            dirs = null;
            throw new IOException(Strings.get("InstanceDirs.badInstanceDir", instanceDir));
        }
        dirs = new ServerDirs(instanceDir);
    }


    /** Look for subdirs in the parent dir -- there must be one and only one */
    private File getTheOneAndOnlyNode(File parent) throws IOException {
        File[] files = parent.listFiles(f -> f != null && f.isDirectory());
        if (files == null || files.length < 1) {
            throw new IOException(Strings.get("InstanceDirs.noNodes", parent));
        }
        if (files.length > 1) {
            throw new IOException(Strings.get("InstanceDirs.tooManyNodes", parent, files.length));
        }
        return files[0];
    }

    private File getTheOneAndOnlyInstance(File nodeDir) throws IOException {
        // look for subdirs in the parent dir -- there must be one and only one

        File[] files = nodeDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f != null && f.isDirectory() && !"agent".equals(f.getName());
            }
        });

        // ERROR:  No instance dirs
        if (files == null || files.length < 1) {
            throw new IOException(
                    Strings.get("InstanceDirs.noInstances", nodeDir));
        }
        // ERROR:  more than one instance dir
        if (files.length > 1) {
            throw new IOException(
                    Strings.get("InstanceDirs.tooManyInstances", files.length, nodeDir));
        }

        // the usual case -- one instance dir
        return files[0];
    }

    /**
     * Return the default value for nodeDirRoot, first checking if com.sun.aas.agentRoot
     * was specified in asenv.conf and returning this value. If not specified,
     * then the default value is the {GlassFish_Install_Root}/nodes.
     * nodeDirRoot is the parent directory of the node(s).
     *
     * @return String default nodeDirRoot - parent directory of node(s)
     * @throws CommandException if the GlassFish install root is not found
     */
    private String getNodeDirRootDefault() throws IOException {
        String nodeDirDefault = System.getProperty(
                SystemPropertyConstants.AGENT_ROOT_PROPERTY);

        if (StringUtils.ok(nodeDirDefault)) {
            return nodeDirDefault;
        }

        String installRootPath = getInstallRootPath();
        return installRootPath + "/" + "nodes";
    }

    /**
     * Gets the GlassFish installation root (using property com.sun.aas.installRoot),
     *
     * @return path of GlassFish install root
     * @throws IOException if the GlassFish install root is not found
     */
    protected String getInstallRootPath() throws IOException {
        String installRootPath = System.getProperty(INSTALL_ROOT.getSystemPropertyName());

        if (!StringUtils.ok(installRootPath)) {
            throw new IOException("noInstallDirPath");
        }

        return installRootPath;
    }

    /**
     * Create a InstanceDir from the more general ServerDirs instance.
     * along with getServerDirs() you can convert freely back and forth
     *
     * @param aServerDir
     */
    public InstanceDirs(ServerDirs sd) {
        dirs = sd;
    }

    public String getInstanceName() {
        return dirs.getServerName();
    }

    public File getInstanceDir() {
        return dirs.getServerDir();
    }

    public File getNodeAgentDir() {
        return dirs.getServerParentDir();
    }

    public File getNodeAgentsDir() {
        return dirs.getServerGrandParentDir();
    }

    public ServerDirs getServerDirs() {
        return dirs;
    }

    public File getDasPropertiesFile() {
        return dirs.getDasPropertiesFile();
    }

    @Override
    public String toString() {
        return super.toString() + "[" + dirs + "]";
    }
}
