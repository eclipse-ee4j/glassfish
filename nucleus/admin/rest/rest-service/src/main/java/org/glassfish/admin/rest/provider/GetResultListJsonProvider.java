/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.RestLogging;
import org.glassfish.admin.rest.results.GetResultList;
import org.jvnet.hk2.config.Dom;

import static org.glassfish.admin.rest.provider.ProviderUtil.KEY_CHILD_RESOURCES;
import static org.glassfish.admin.rest.provider.ProviderUtil.KEY_COMMANDS;
import static org.glassfish.admin.rest.provider.ProviderUtil.KEY_ENTITY;
import static org.glassfish.admin.rest.provider.ProviderUtil.KEY_METHODS;
import static org.glassfish.admin.rest.provider.ProviderUtil.getElementLink;
import static org.glassfish.admin.rest.provider.ProviderUtil.getJsonForMethodMetaData;

/**
 *
 * @author Rajeshwar Patil
 * @author Luvdovic Champenois ludo@dev.java.net
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class GetResultListJsonProvider extends BaseProvider<GetResultList> {

    public GetResultListJsonProvider() {
        super(GetResultList.class, MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    public String getContent(GetResultList proxy) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(KEY_ENTITY, new JSONObject());
            obj.put(KEY_METHODS, getJsonForMethodMetaData(proxy.getMetaData()));
            if (proxy.getDomList().size() > 0) {
                obj.put(KEY_CHILD_RESOURCES, getResourcesLinks(proxy.getDomList()));
            }
            if (proxy.getCommandResourcesPaths().length > 0) {
                obj.put(KEY_COMMANDS, getCommandLinks(proxy.getCommandResourcesPaths()));
            }
        } catch (JSONException ex) {
            RestLogging.restLogger.log(Level.SEVERE, null, ex);
        }

        return obj.toString();
    }

    private JSONArray getResourcesLinks(List<Dom> proxyList) {
        JSONArray array = new JSONArray();
        for (Map.Entry<String, String> link : getResourceLinks(proxyList).entrySet()) {
            array.put(link.getValue());
        }
        return array;
    }

    private JSONArray getCommandLinks(String[][] commandResourcesPaths) throws JSONException {
        JSONArray array = new JSONArray();

        //TODO commandResourcePath is two dimensional array. It seems the second e.x. see DomainResource#getCommandResourcesPaths().
        //The second dimension POST/GET etc. does not seem to be used. Discussed with Ludo. Need to be removed in a separate checkin.
        for (String[] commandResourcePath : commandResourcesPaths) {
            array.put(getElementLink(uriInfo, commandResourcePath[0]));
        }
        return array;
    }
}
