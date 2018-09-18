/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.remote;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ParameterMap;

/**
 *
 * @author martinmares
 */
public class ParamsWithPayload {
    
    private final RestPayloadImpl.Outbound payloadOutbound;
    private final RestPayloadImpl.Inbound payloadInbound;
    private final ActionReport actionReport;
    private final ParameterMap parameters;

    public ParamsWithPayload(RestPayloadImpl.Inbound payloadInbound, ParameterMap parameters, ActionReport actionReport) {
        this.payloadInbound = payloadInbound;
        this.parameters = parameters;
        this.payloadOutbound = null;
        this.actionReport = actionReport;
    }

    public ParamsWithPayload(RestPayloadImpl.Outbound payloadOutbound, ParameterMap parameters) {
        this.payloadOutbound = payloadOutbound;
        this.payloadInbound = null;
        this.parameters = parameters;
        this.actionReport = null;
    }
    
    public ParamsWithPayload(RestPayloadImpl.Outbound payloadOutbound, ActionReport actionReport) {
        this.payloadOutbound = payloadOutbound;
        this.payloadInbound = null;
        this.parameters = null;
        this.actionReport = actionReport;
    }

    public RestPayloadImpl.Outbound getPayloadOutbound() {
        return payloadOutbound;
    }

    public RestPayloadImpl.Inbound getPayloadInbound() {
        return payloadInbound;
    }

    public ParameterMap getParameters() {
        return parameters;
    }

    public ActionReport getActionReport() {
        return actionReport;
    }

    
}
