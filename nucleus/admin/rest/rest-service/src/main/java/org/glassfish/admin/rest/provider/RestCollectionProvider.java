/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.Constants;
import org.glassfish.admin.rest.composite.RestCollection;
import org.glassfish.admin.rest.composite.RestModel;
import org.glassfish.admin.rest.composite.metadata.RestModelMetadata;
import org.glassfish.admin.rest.utils.JsonUtil;

/**
 * @author: jdlee
 */
@Provider
@Produces({ Constants.MEDIA_TYPE_JSON })
public class RestCollectionProvider extends BaseProvider<RestCollection> {
    public RestCollectionProvider() {
        super(RestCollection.class, Constants.MEDIA_TYPE_JSON_TYPE);
    }

    @Override
    public String getContent(RestCollection proxy) {
        StringBuilder sb = new StringBuilder();
        final List<String> wrapObjectHeader = requestHeaders.getRequestHeader("X-Wrap-Object");
        final List<String> skipMetadataHeader = requestHeaders.getRequestHeader("X-Skip-Metadata");
        boolean wrapObject = ((wrapObjectHeader != null) && (wrapObjectHeader.size() > 0));
        boolean skipMetadata = ((skipMetadataHeader != null) && (skipMetadataHeader.get(0).equalsIgnoreCase("true")));

        JSONArray models = new JSONArray();
        JSONArray metadata = new JSONArray();
        for (Map.Entry<RestModelMetadata, RestModel> entry : (Set<Map.Entry<RestModelMetadata, RestModel>>) proxy.entrySet()) {
            try {
                models.put(JsonUtil.getJsonObject(entry.getValue()));

                RestModelMetadata md = entry.getKey();
                JSONObject mdo = new JSONObject();
                mdo.put("id", md.getId());
                metadata.put(mdo);
            } catch (JSONException e) {
                e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
            }
        }
        JSONObject response = new JSONObject();
        try {
            response.put("items", models);
            if (!skipMetadata) {
                response.put("metadata", metadata);
            }
            sb.append(response.toString(getFormattingIndentLevel()));
        } catch (JSONException e) {
            e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
        }

        return (wrapObject ? " { items : " : "") + sb.toString() + (wrapObject ? "}" : "");
    }
}
