/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.internal.data;

import org.glassfish.api.container.Sniffer;
import org.glassfish.hk2.api.ServiceLocator;

import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Singleton;

import jakarta.inject.Inject;
import java.util.*;

/**
 * The container Registry holds references to the currently running containers.
 *
 * @author Jerome Dochez
 */
@Service
@Singleton
public class ContainerRegistry {

    @Inject
    ServiceLocator habitat;
    
    Map<String, EngineInfo> containers = new HashMap<String, EngineInfo>();


    public synchronized void addContainer(String name, EngineInfo info) {
        containers.put(name, info);
        info.setRegistry(this);
    }

    public List<Sniffer> getStartedContainersSniffers() {

        ArrayList<Sniffer> sniffers = new ArrayList<Sniffer>();

        for (EngineInfo info : getContainers() ) {
            sniffers.add(info.getSniffer());
        }
        return sniffers;
    }

    public synchronized EngineInfo getContainer(String containerType) {
        return containers.get(containerType);
    }

    public synchronized EngineInfo removeContainer(EngineInfo container) {
        for (Map.Entry<String, EngineInfo> entry : containers.entrySet()) {
            if (entry.getValue().equals(container)) {
                return containers.remove(entry.getKey());
            }
        }
        return null;
    }

    public Iterable<EngineInfo> getContainers() {
        ArrayList<EngineInfo> copy = new ArrayList<EngineInfo>(containers.size());
        copy.addAll(containers.values());
        return copy;
    }
        
}
