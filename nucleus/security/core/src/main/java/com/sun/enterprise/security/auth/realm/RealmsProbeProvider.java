/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.auth.realm;

import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;

/**
 *
 * @author nithyasubramanian
 */
@ProbeProvider(moduleProviderName = "glassfish", moduleName = "security", probeProviderName = "realm")
public class RealmsProbeProvider {

    @Probe(name = "realmAddedEvent")
    public void realmAddedEvent(@ProbeParam("realmName") String realmName) {
    }

    @Probe(name = "realmRemovedEvent")
    public void realmRemovedEvent(@ProbeParam("realmName") String realmName) {
    }

}
