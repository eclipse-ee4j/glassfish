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

package com.sun.enterprise.web.jsp;

import com.sun.enterprise.web.VirtualServer;
import com.sun.enterprise.web.WebModule;

import org.glassfish.jsp.api.JspProbeEmitter;
import org.glassfish.web.admin.monitor.JspProbeProvider;

/**
 * Implementation of JspProbeEmitter interface that delegates each probe
 * event to the JspProbeProvider.
 *
 * @author jluehe
 */
public class JspProbeEmitterImpl implements JspProbeEmitter {

    private String monitoringNodeName;

    // The id of the virtual server on which the web module has been
    // deployed
    private String vsId;

    private JspProbeProvider jspProbeProvider;

    /**
     * Constructor.
     *
     * @param webModule the web module on whose behalf this
     * JspProbeEmitterImpl emits jsp related probe events
     */
    public JspProbeEmitterImpl(WebModule webModule) {
        this.monitoringNodeName = webModule.getMonitoringNodeName();
        if (webModule.getParent() != null) {
            this.vsId = ((VirtualServer) webModule.getParent()).getID();
        }
        this.jspProbeProvider = webModule.getWebContainer().getJspProbeProvider();
    }

    public void jspLoadedEvent(String jspUri) {
        jspProbeProvider.jspLoadedEvent(jspUri, monitoringNodeName, vsId);
    }

    public void jspReloadedEvent(String jspUri) {
        jspProbeProvider.jspReloadedEvent(jspUri, monitoringNodeName, vsId);
    }

    public void jspDestroyedEvent(String jspUri) {
        jspProbeProvider.jspDestroyedEvent(jspUri, monitoringNodeName, vsId);
    }

    public void jspErrorEvent(String jspUri) {
        jspProbeProvider.jspErrorEvent(jspUri, monitoringNodeName, vsId);
    }
}
