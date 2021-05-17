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

package org.glassfish.ejb.security.application;

import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;

/**
 *
 * @author nithyasubramanian
 */
@ProbeProvider(moduleProviderName="glassfish",moduleName="security", probeProviderName="ejb")
public class EjbSecurityProbeProvider {

    @Probe(name="securityManagerCreationEvent")
    public void securityManagerCreationEvent(
            @ProbeParam("appName") String appName){}

    @Probe(name="securityManagerCreationStartedEvent")
    public void securityManagerCreationStartedEvent(
            @ProbeParam("appName") String appName){}

    @Probe(name="securityManagerCreationEndedEvent")
    public void securityManagerCreationEndedEvent(
            @ProbeParam("appName") String appName){}

    @Probe(name="securityManagerDestructionEvent")
    public void securityManagerDestructionEvent(
            @ProbeParam("appName") String appName
            ) {}

    @Probe(name="securityManagerDestructionStartedEvent")
    public void securityManagerDestructionStartedEvent(
            @ProbeParam("appName") String appName
            ) {}

    @Probe(name="securityManagerDestructionEndedEvent")
    public void securityManagerDestructionEndedEvent(
            @ProbeParam("appName") String appName
            ) {}


    @Probe(name="policyDestructionEvent")
    public void policyDestructionEvent(
            @ProbeParam("contextId") String contextId
            ) {}

    @Probe(name="policyDestructionStartedEvent")
    public void policyDestructionStartedEvent(
            @ProbeParam("appName") String appName
            ) {}

    @Probe(name="policyDestructionEndedEvent")
    public void policyDestructionEndedEvent(
            @ProbeParam("appName") String appName
            ) {}

}
