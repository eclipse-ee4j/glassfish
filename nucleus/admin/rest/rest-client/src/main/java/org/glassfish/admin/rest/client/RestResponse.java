/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.client;

import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.glassfish.admin.rest.client.utils.Util;

/**
 *
 * @author jasonlee
 */
public class RestResponse {
    private String message;
    private int status;
    private Map<String, Object> extraProperties;
    private List children;
    private Map<String, String> properties;

    public RestResponse(Response response) {
        Map<String, Object> responseMap = Util.processJsonMap(response.readEntity(String.class));

        status = response.getStatus();
        message = (String) responseMap.get("message");
        extraProperties = (Map) responseMap.get("extraProperties");
        children = (List) responseMap.get("children");
        Map respProps = (Map) responseMap.get("properties");
        if (respProps != null) {
            this.properties = respProps;
        }
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return (status >= 200) && (status <= 299);
    }

    public Map<String, Object> getExtraProperties() {
        return Collections.unmodifiableMap(extraProperties);
    }

    public List getChildren() {
        return children;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
