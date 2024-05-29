/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.adapter;

import jakarta.inject.Singleton;

import org.glassfish.hk2.api.ServiceLocator;

/**
 * This is a bridge from one service locator to another, which is not related via parentage
 *
 * @author jwells
 */
@Singleton
public class LocatorBridge {
    private final ServiceLocator remoteLocator;

    public LocatorBridge(ServiceLocator remoteLocator) {
        this.remoteLocator = remoteLocator;
    }

    public ServiceLocator getRemoteLocator() {
        return remoteLocator;
    }

    @Override
    public String toString() {
        return "LocatorBridge(" + remoteLocator + "," + System.identityHashCode(this) + ")";
    }
}
