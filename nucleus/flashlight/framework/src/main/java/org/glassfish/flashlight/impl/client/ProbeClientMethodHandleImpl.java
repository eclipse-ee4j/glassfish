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

package org.glassfish.flashlight.impl.client;

import org.glassfish.flashlight.client.ProbeClientInvoker;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;
import org.glassfish.flashlight.provider.FlashlightProbe;

/**
 * @author Mahesh Kannan
 * @author Byron Nevins
 */
public class ProbeClientMethodHandleImpl
        implements ProbeClientMethodHandle {

    private int clientMethodId;
    private boolean enabled = true;
    private ProbeClientInvoker clientMethodInvoker;
    private FlashlightProbe probe;

    public ProbeClientMethodHandleImpl(int id, ProbeClientInvoker invoker, FlashlightProbe probe) {
        this.clientMethodId = id;
        this.clientMethodInvoker = invoker;
        this.probe = probe;
    }

    @Override
    public int getId() {
        return clientMethodId;
    }

    @Override
    public synchronized boolean isEnabled() {
        return enabled;
    }

    @Override
    public synchronized void enable() {
        probe.addInvoker(clientMethodInvoker);
        enabled = true;
        ProbeProviderClassFileTransformer.transform(probe.getProviderClazz());
    }

    @Override
    public synchronized void disable() {
        probe.removeInvoker(clientMethodInvoker);
        enabled = false;
        ProbeProviderClassFileTransformer.untransform(probe.getProviderClazz());
    }
}
