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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.composite.CompositeUtil;
import org.glassfish.admin.rest.resources.CommandResult;
import org.glassfish.admin.rest.utils.JsonUtil;

/**
 * Provider for {@code AdminCommandState} event object used by {@code /management} interface.
 *
 * @author martinmares
 */
@Provider
@Produces({ MediaType.APPLICATION_JSON, "application/x-javascript" })
public class AdminCommandStateCmdResultJsonProvider extends AdminCommandStateJsonProvider {

    @Override
    protected void addActionReporter(ActionReporter ar, JSONObject json) throws JSONException {
        if (ar != null) {
            CommandResult cr = CompositeUtil.instance().getModel(CommandResult.class);
            cr.setMessage(ar.getMessage());
            cr.setProperties(ar.getTopMessagePart().getProps());
            Properties props = ar.getExtraProperties();
            if (props != null) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    map.put(entry.getKey().toString(), entry.getValue());
                }
                cr.setExtraProperties(map);
            }
            json.put("command-result", (JSONObject) JsonUtil.getJsonObject(cr));
        }
    }

}
