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

package org.glassfish.web.admin.monitor;

import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;

/**
 * Provider interface for JSP related probes.
 */
@ProbeProvider(moduleProviderName="glassfish", moduleName="web", probeProviderName="jsp")
public class JspProbeProvider {

    /**
     * Emits notification that a JSP has been accessed for the first time
     * and its corresponding Servlet has been loaded and initialized.
     *
     * @param jspUri The path (relative to the root of the application)
     * to the JSP that was loaded
     * @param appName The name of the application to which the JSP belongs
     * @param hostName The name of the virtual server on which the
     * application has been deployed
     */
    @Probe(name="jspLoadedEvent")
    public void jspLoadedEvent(
        @ProbeParam("jspUri") String jspUri,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}

    /**
     * Emits notification that a JSP whose source code has changed since
     * it was first deployed has been accessed again and was recompiled,
     * and its corresponding Servlet reloaded and reinitialized.
     *
     * @param jspUri The path (relative to the root of the application)
     * to the JSP that was reloaded
     * @param appName The name of the application to which the JSP belongs
     * @param hostName The name of the virtual server on which the
     * application has been deployed
     */
    @Probe(name="jspReloadedEvent")
    public void jspReloadedEvent(
        @ProbeParam("jspUri") String jspUri,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}

    /**
     * Emits notification that a JSP is being destroyed, that is, the
     * Servlet corresponding to the JSP is called at its destroy method
     * either because the JSP is being reloaded or because the application
     * to which the JSP belongs is being stopped (for example, as part of its
     * undeployment).
     *
     * @param jspUri The path (relative to the root of the application)
     * to the JSP that was destroyed
     * @param appName The name of the application to which the JSP belongs
     * @param hostName The name of the virtual server on which the
     * application has been deployed
     */
    @Probe(name="jspDestroyedEvent")
    public void jspDestroyedEvent(
        @ProbeParam("jspUri") String jspUri,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}

    /**
     * Emits notification that access to a JSP has resulted in an error.
     *
     * @param jspUri The path (relative to the root of the application)
     * to the JSP that produced the error
     * @param appName The name of the application to which the JSP belongs
     * @param hostName The name of the virtual server on which the
     * application has been deployed
     */
    @Probe(name="jspErrorEvent")
    public void jspErrorEvent(
        @ProbeParam("jspUri") String jspUri,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}
}
