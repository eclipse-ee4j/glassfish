/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.impl;

import jakarta.inject.Singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.glassfish.flashlight.datatree.TreeNode;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Harpreet Singh
 */

@Service
@Singleton
public class MonitoringRuntimeDataRegistryImpl implements MonitoringRuntimeDataRegistry {

    protected Map<String, TreeNode> children = new ConcurrentHashMap<String, TreeNode>();

    public MonitoringRuntimeDataRegistryImpl() {
    }

    public void add(String name, TreeNode node) {
        if (name != null)
            children.put(name, node);
        else {
            throw new RuntimeException("MonitoringRuntimeDataRegistry does not take null keys");
        }
    }

    public void remove(String name) {
        if (name != null)
            children.remove(name);
    }

    public TreeNode get(String name) {
        TreeNode node = (name != null) ? children.get(name) : null;
        return node;
    }
}
