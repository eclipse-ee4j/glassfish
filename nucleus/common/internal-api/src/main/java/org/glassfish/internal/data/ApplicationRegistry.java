/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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


import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for deployed Applications
 *
 * TODO : dochez
 * this class needs to go, I think we should use the configured tree (applications)
 * to store this list. This could be achieve once hk2 configured support Transient
 * objects attachment.
 */
@Service
@Singleton
public class ApplicationRegistry {

    private Map<String, ApplicationInfo> apps = new HashMap<String, ApplicationInfo>();

    public synchronized void add(String name, ApplicationInfo info) {
        apps.put(name, info);
    }

    public ApplicationInfo get(String name) {
        return apps.get(name);
    }

    public synchronized void remove(String name) {

        apps.remove(name);
    }

    public Set<String> getAllApplicationNames() {
        return apps.keySet();
    }

}
