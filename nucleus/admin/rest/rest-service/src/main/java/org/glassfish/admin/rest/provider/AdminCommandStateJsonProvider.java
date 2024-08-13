/*
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

package org.glassfish.admin.rest.provider;

import com.sun.enterprise.v3.common.ActionReporter;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

import java.lang.reflect.Type;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.api.admin.AdminCommandState;

/**
 *
 * @author mmares
 */
@Provider
@Produces({ MediaType.APPLICATION_JSON, "application/x-javascript" })
public class AdminCommandStateJsonProvider extends BaseProvider<AdminCommandState> {

    private static final ActionReportJson2Provider actionReportJsonProvider = new ActionReportJson2Provider();

    public AdminCommandStateJsonProvider() {
        super(AdminCommandState.class, MediaType.APPLICATION_JSON_TYPE, new MediaType("application", "x-javascript"));
    }

    @Override
    protected boolean isGivenTypeWritable(Class<?> type, Type genericType) {
        return desiredType.isAssignableFrom(type);
    }

    @Override
    public String getContent(AdminCommandState proxy) {
        try {
            return processState(proxy).toString();
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    public JSONObject processState(AdminCommandState state) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("state", state.getState().name());
        result.put("id", state.getId());
        result.put("empty-payload", state.isOutboundPayloadEmpty());
        ActionReporter ar = (ActionReporter) state.getActionReport();
        addActionReporter(ar, result);
        return result;
    }

    protected void addActionReporter(ActionReporter ar, JSONObject json) throws JSONException {
        if (ar != null) {
            json.put("action-report", actionReportJsonProvider.processReport(ar));
        }
    }

}
