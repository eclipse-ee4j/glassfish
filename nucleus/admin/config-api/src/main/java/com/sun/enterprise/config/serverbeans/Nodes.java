/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.config.serverbeans;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.Objects;

import org.glassfish.api.I18n;
import org.glassfish.config.support.Create;
import org.glassfish.config.support.Delete;
import org.glassfish.config.support.TypeAndNameResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.Element;

/**
 * Nodes configuration.
 *
 * <p>Maintain a list of {@link Node} active configurations.
 */
@Configured
public interface Nodes extends ConfigBeanProxy {

    /**
     * Sets the value of the {@code freeze} attribute on the nodes list. If the nodes list
     * is frozen then no new nodes are allowed to be created.
     *
     * @param freeze allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setFreeze(String freeze) throws PropertyVetoException;

    /**
     * Check if nodes list is frozen.
     *
     * <p>That is prevent creation of new nodes.
     *
     * @return value of freeze
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getFreeze();

    /**
     * Return the list of nodes currently configured.
     *
     * @return list of {@link Node}
     */
    @Element
    @Create(
            value = "_create-node",
            decorator = Node.Decorator.class,
            i18n = @I18n("_create.node.command")
    )
    @Delete(
            value = "_delete-node",
            resolver = TypeAndNameResolver.class,
            decorator = Node.DeleteDecorator.class,
            i18n = @I18n("delete.node.command")
    )
    List<Node> getNode();

    /**
     * Return the default local node, {@literal localhost-<domain_name>},
     * or {@code null} if no such node exists.
     *
     * @return the {@link Node} object, or {@code null} if no such node
     */
    default Node getDefaultLocalNode() {
        ServiceLocator habitat = Objects.requireNonNull(Dom.unwrap(this)).getHabitat();
        Domain domain = habitat.getService(Domain.class);
        for (Node node : getNode()) {
            if (node.getName().equals("localhost-" + domain.getName())) {
                return node;
            }
        }
        return null;
    }

    /**
     * Return the node with the given {@code name}, or {@code null} if no such node exists.
     *
     * @param name the name of the node
     * @return the {@link Node} object, or {@code null} if no such node
     */
    default Node getNode(String name) {
        if (name == null) {
            return null;
        }
        for (Node node : getNode()) {
            if (node.getName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Can we create a node?
     *
     * @return {@code true} if node creation is allowed, else {@code false}
     */
    default boolean nodeCreationAllowed() {
        return !Boolean.parseBoolean(getFreeze());
    }
}
