/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.serverbeans;

import com.sun.enterprise.config.serverbeans.customvalidators.NotDuplicateTargetName;
import com.sun.enterprise.config.serverbeans.customvalidators.NotTargetKeyword;
import com.sun.enterprise.config.util.ConfigApiLoggerInfo;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.net.NetUtils;

import jakarta.inject.Inject;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.Named;
import org.glassfish.api.admin.config.ReferenceContainer;
import org.glassfish.config.support.CreationDecorator;
import org.glassfish.config.support.DeletionDecorator;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.TransactionFailure;

import static org.glassfish.config.support.Constants.NAME_REGEX;
import static org.glassfish.config.support.Constants.NAME_SERVER_REGEX;

/**
 * A cluster defines a homogeneous set of server instances that share the same
 * applications, resources, and configuration.
 */
@Configured
@NotDuplicateTargetName(message = "{node.duplicate.name}", payload = Node.class)
public interface Node extends ConfigBeanProxy, Named, ReferenceContainer, RefContainer, Payload {

    /**
     * Sets the node {@code name}.
     *
     * @param name node name
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "name", primary = true)
    @Override
    void setName(String name) throws PropertyVetoException;

    @NotTargetKeyword(message = "{node.reserved.name}", payload = Node.class)
    @Pattern(regexp = NAME_SERVER_REGEX, message = "{node.invalid.name}", payload = Node.class)
    @Override
    String getName();

    /**
     * Points to the parent directory of the node(s) directory.
     *
     * @return path location of {@code node-dir}
     */
    @Attribute
    String getNodeDir();

    /**
     * Sets the value of the {@code node-dir}, top-level parent directory of node(s).
     *
     * @param nodeDir allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "nodedir", optional = true)
    void setNodeDir(String nodeDir) throws PropertyVetoException;

    /**
     * Points to a named host.
     *
     * @return a named host name
     */
    @Attribute
    @Pattern(regexp = NAME_REGEX, message = "{nodehost.invalid.name}", payload = Node.class)
    String getNodeHost();

    /**
     * Sets the value of the {@code nodeHost} property.
     *
     * @param nodeHost allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "nodehost", optional = true)
    void setNodeHost(String nodeHost) throws PropertyVetoException;

    /**
     * Points to a GlassFish installation root.
     *
     * @return value of {@code install-dir}
     */
    @Attribute
    String getInstallDir();

    /**
     * Sets the value of {@code install-dir}, the GlassFish installation root.
     *
     * @param installDir allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "installdir", optional = true)
    void setInstallDir(String installDir) throws PropertyVetoException;

    @Attribute()
    String getType();

    /**
     * Sets the value of {@code type} of this node.
     *
     * @param type allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "type")
    void setType(String type) throws PropertyVetoException;

    /**
     * Specifies the Windows domain if applicable
     *
     * @return the Windows domain name.
     */
    @Attribute
    @Pattern(regexp = NAME_REGEX, message = "{windowsdomain.invalid.name}", payload = Node.class)
    String getWindowsDomain();

    /**
     * Sets the value of the Windows domain property.
     *
     * @param windowsDomain allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "windowsdomain", optional = true)
    void setWindowsDomain(String windowsDomain) throws PropertyVetoException;

    /**
     * Check if node is frozen and we should not allow new instances to be created on the node.
     *
     * @return {@code true} if node is frozen
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getFreeze();

    /**
     * Sets the value of the {@code freeze} property.
     *
     * @param freeze {@code true} to freeze node and not allow instances to be created
     *
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setFreeze(String freeze) throws PropertyVetoException;

    @Element
    SshConnector getSshConnector();

    void setSshConnector(SshConnector connector);

    /**
     * Returns the install dir with separators as forward slashes.
     *
     * <p>This is needed to run commands over SSH tools on Windows where
     * the backslashes are interpreted as escape chars.
     *
     * @return the install dir with separators as forward slashes
     */
    default String getInstallDirUnixStyle() {
        String installDir = getInstallDir();
        if (installDir == null) {
            return null;
        }
        return installDir.replaceAll("\\\\", "/");
    }

    /**
     * Returns the node dir with separators as forward slashes.
     *
     * <p>This is needed to run commands over SSH tools on Windows where
     * the backslashes are interpreted as escape chars.
     *
     * @return the node dir with separators as forward slashes
     */
    default String getNodeDirUnixStyle() {
        String nodeDir = getNodeDir();
        if (nodeDir == null) {
            return null;
        }
        return nodeDir.replaceAll("\\\\", "/");
    }

    /**
     * Returns the node dir as an absolute path.
     *
     * <p>If the node dir path in the {@code node} element is relative this will make it
     * absolute relative to the node's installdir.
     *
     * @return the node's {@code nodeDir} as an absolute path, {@code null} if no nodedir.
     */
    default String getNodeDirAbsolute() {
        // If nodedir is relative make it absolute relative to installRoot
        String nodeDir = getNodeDir();
        if (nodeDir == null || nodeDir.length() == 0) {
            return null;
        }

        File nodeDirFile = new File(nodeDir);
        if (nodeDirFile.isAbsolute()) {
            return nodeDir;
        }
        // node-dir is relative. Make it absolute. We root it under the
        // GlassFish root install directory.
        File installRootFile = new File(getInstallDir(), "glassfish");
        return new File(installRootFile, nodeDir).getPath();
    }

    default String getNodeDirAbsoluteUnixStyle() {
        String nodeDirAbsolute = getNodeDirAbsolute();
        if (nodeDirAbsolute == null) {
            return null;
        }
        return nodeDirAbsolute.replaceAll("\\\\", "/");
    }

    /**
     * Is a node being used by any server instance?
     *
     * @return true if node is referenced by any server instance, else false.
     */
    default boolean nodeInUse() {
        //check if node is referenced by an instance
        String nodeName = getName();
        ServiceLocator habitat = Objects.requireNonNull(Dom.unwrap(this)).getHabitat();
        List<Server> servers = habitat.getService(Servers.class).getServer();
        if (servers != null) {
            for (Server server : servers) {
                if (nodeName.equals(server.getNodeRef())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * True if this is the default local node.
     *
     * <p>Example: {@code localhost-domain1}.
     *
     * @return {@code true} if this is the default local node, {@code false} otherwise
     */
    default boolean isDefaultLocalNode() {
        ServiceLocator habitat = Objects.requireNonNull(Dom.unwrap(this)).getHabitat();
        Domain domain = habitat.getService(Domain.class);
        if (getName().equals("localhost-" + domain.getName())) {
            return true;
        }
        return false;
    }

    /**
     * True if the node's {@code nodeHost} is local to this.
     *
     * @return {@code true} if the node's host is local to this, {@code false} otherwise
     */
    default boolean isLocal() {
        // Short circuit common case for efficiency
        ServiceLocator habitat = Objects.requireNonNull(Dom.unwrap(this)).getHabitat();
        Domain domain = habitat.getService(Domain.class);
        if (getName().equals("localhost-" + domain.getName())) {
            return true;
        }

        String nodeHost = getNodeHost();
        if (nodeHost == null || nodeHost.isEmpty()) {
            return false;
        }

        return NetUtils.isThisHostLocal(nodeHost);
    }

    /**
     * Does the node allow instance creation?
     *
     * @return {@code true} if node allows instance creation, else {@code false}
     */
    default boolean instanceCreationAllowed() {
        return !Boolean.parseBoolean(getFreeze());
    }

    @Service
    @PerLookup
    class Decorator implements CreationDecorator<Node> {

        @Param(name = "nodedir", optional = true)
        String nodedir = null;

        @Param(name = "nodehost", optional = true)
        String nodehost = null;

        @Param(name = "installdir", optional = true)
        String installdir = null;

        @Param(name = "type")
        String type = null;

        @Param(name = "sshport", optional = true)
        String sshPort = null;

        @Param(name = "sshnodehost", optional = true)
        String sshHost = null;

        @Param(name = "sshuser", optional = true)
        String sshuser = null;

        @Param(name = "sshkeyfile", optional = true)
        String sshkeyfile;

        @Param(name = "sshpassword", optional = true)
        String sshpassword;

        @Param(name = "sshkeypassphrase", optional = true)
        String sshkeypassphrase;

        @Param(name = "windowsdomain", optional = true)
        String windowsdomain;

        @Inject
        ServiceLocator habitat;

        @Inject
        ServerEnvironment env;

        @Inject
        Domain domain;

        @Inject
        Nodes nodes;

        /**
         * Decorates the newly CRUD created cluster configuration instance.
         *
         * <p>Tasks :
         * <ul>
         * <li>ensures that it references an existing configuration</li>,
         * <li>creates a new config from the default-config if no config-ref was provided.</li>,
         * <li>>check for deprecated parameters</p>.
         *
         * @param context administration command context
         * @param instance newly created configuration element
         */
        @Override
        public void decorate(AdminCommandContext context, final Node instance) throws TransactionFailure, PropertyVetoException {

            LocalStringManagerImpl localStrings = new LocalStringManagerImpl(Node.class);

            /* 16034: see if instance creation is turned off on node */
            if (!nodes.nodeCreationAllowed()) {
                throw new TransactionFailure(
                        localStrings.getLocalString("nodeCreationNotAllowed", "Node creation is disabled. No new nodes may be created."));
            }
            // If these options were passed a value of the empty string then
            // we want to make sure they are null in the Node. The
            // admin console often passes the empty string instead of null.
            // See bug 14873
            if (!StringUtils.ok(nodedir)) {
                instance.setNodeDir(null);
            }
            if (!StringUtils.ok(installdir)) {
                instance.setInstallDir(null);
            }
            if (!StringUtils.ok(nodehost)) {
                instance.setNodeHost(null);
            }
            if (!StringUtils.ok(windowsdomain)) {
                instance.setWindowsDomain(null);
            }

            //only create-node-ssh and update-node-ssh should be changing the type to SSH
            instance.setType(type);

            if (type.equals("CONFIG"))
                return;

            SshConnector sshC = instance.createChild(SshConnector.class);

            SshAuth sshA = sshC.createChild(SshAuth.class);
            if (StringUtils.ok(sshuser)) {
                sshA.setUserName(sshuser);
            }
            if (StringUtils.ok(sshkeyfile)) {
                sshA.setKeyfile(sshkeyfile);
            }
            if (StringUtils.ok(sshpassword)) {
                sshA.setPassword(sshpassword);
            }
            if (StringUtils.ok(sshkeypassphrase)) {
                sshA.setKeyPassphrase(sshkeypassphrase);
            }
            sshC.setSshAuth(sshA);

            if (StringUtils.ok(sshPort)) {
                sshC.setSshPort(sshPort);
            }

            if (StringUtils.ok(sshHost)) {
                sshC.setSshHost(sshHost);
            }

            instance.setSshConnector(sshC);
        }
    }

    @Service
    @PerLookup
    class DeleteDecorator implements DeletionDecorator<Nodes, Node> {

        @Inject
        private Domain domain;

        @Inject
        Nodes nodes;

        @Inject
        Servers servers;

        @Inject
        private ServerEnvironment env;

        @Override
        public void decorate(AdminCommandContext context, Nodes parent, Node child) throws PropertyVetoException, TransactionFailure {
            Logger logger = ConfigApiLoggerInfo.getLogger();
            LocalStringManagerImpl localStrings = new LocalStringManagerImpl(Node.class);
            String nodeName = child.getName();

            if (nodeName.equals("localhost-" + domain.getName())) { // can't delete localhost node
                final String msg = localStrings.getLocalString("Node.localhost", "Cannot remove Node {0}. ", child.getName());

                logger.log(Level.SEVERE, ConfigApiLoggerInfo.cannotRemoveNode, child.getName());
                throw new TransactionFailure(msg);
            }

            List<Node> nodeList = nodes.getNode();

            // See if any servers are using this node
            List<Server> serversOnNode = servers.getServersOnNode(child);
            int n = 0;
            if (serversOnNode != null && serversOnNode.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (Server server : serversOnNode) {
                    if (n > 0)
                        sb.append(", ");
                    sb.append(server.getName());
                    n++;
                }

                final String msg = localStrings.getLocalString("Node.referencedByInstance",
                        "Node {0} referenced in server instance(s): {1}.  Remove instances before removing node.", child.getName(),
                        sb.toString());
                logger.log(Level.SEVERE, ConfigApiLoggerInfo.referencedByInstance, new Object[] { child.getName(), sb.toString() });
                throw new TransactionFailure(msg);
            }

            nodeList.remove(child);
        }
    }
}
