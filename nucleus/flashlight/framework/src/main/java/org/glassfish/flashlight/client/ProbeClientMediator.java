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

package org.glassfish.flashlight.client;


import java.util.Collection;
import java.util.List;

import org.glassfish.flashlight.provider.FlashlightProbe;
import org.jvnet.hk2.annotations.Contract;

/**
 * @author Mahesh Kannan
 *         Date: Jan 27, 2008
 */
@Contract
public interface ProbeClientMediator {

    public Collection<ProbeClientMethodHandle> registerListener(Object listener);

    public Collection<ProbeClientMethodHandle> registerListener(Object listener, String invokerId);

    public void transformProbes(Object listener, List<FlashlightProbe> probes);
}
