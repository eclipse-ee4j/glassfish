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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author mmares
 */
@Provider
@Produces({ MediaType.APPLICATION_JSON, "application/x-javascript" })
public class ActionReportJson2Provider extends ActionReportJsonProvider {

    @Override
    protected JSONObject processReport(ActionReporter ar) throws JSONException {
        JSONObject result = super.processReport(ar);
        String combinedMessage = result.optString("message");
        String msg = decodeEol(ar.getTopMessagePart().getMessage());
        if (combinedMessage != null && !combinedMessage.equals(msg)) {
            result.put("top_message", msg);
        }
        if (ar.getFailureCause() != null) {
            result.put("failure_cause", ar.getFailureCause().getLocalizedMessage());
        }
        return result;
    }

}
