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

package com.sun.enterprise.connectors.work.monitor;

import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;


/**
 * Provider interface for Connector Work Management Related Probes
 *
 * @author Jagadish Ramu.
 */

@ProbeProvider(moduleProviderName = "glassfish", moduleName = "jca", probeProviderName = "work-management")
public class WorkManagementProbeProvider {


    @Probe(name = "workSubmitted")
    public void workSubmitted(
            @ProbeParam("raName") String raName
    ) {}

    @Probe(name = "workQueued")
    public void workQueued(
            @ProbeParam("raName") String raName
    ) {}

    @Probe(name = "workWaitedFor")
    public void workWaitedFor(
            @ProbeParam("raName") String raName,
            @ProbeParam("elapsedTime") long elapsedTime
    ) {}

    @Probe(name = "workDequeued")
    public void workDequeued(
            @ProbeParam("raName") String raName
    ) {}

    @Probe(name = "workProcessingStarted")
    public void workProcessingStarted(
            @ProbeParam("raName") String raName
    ) {}

    @Probe(name = "workProcessingCompleted")
    public void workProcessingCompleted(
            @ProbeParam("raName") String raName
    ) {}

    @Probe(name = "workProcessed")
    public void workProcessed(
            @ProbeParam("raName") String raName
    ) {}

    @Probe(name = "workTimedOut")
    public void workTimedOut(
            @ProbeParam("raName") String raName
    ) {}

}
