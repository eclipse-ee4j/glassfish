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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.common.util;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import jakarta.faces.context.FacesContext;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * This class is intended to replace, eventually, RestUtil. Whether or not that happens depends in large part on how the
 * new console is built. In the meantime, though, there is a need for REST-related utilities that behave in a manner not
 * supported by the current methods in RestUtil, so these need to be written. Rather than place these new methods beside
 * the existing ones, we will, for now, separate the two to maintain ease in identifying old vs new methods, as well as
 * helping to ensure that these methods remain purely REST-focused, free from the encroachment of view-related libraries
 * (see the pervasiveness of JSFTemplating types in RestUtil).
 *
 * @author jdlee
 */
public class RestUtil2 {

    @Handler(id = "rest.list",
            input = {
                    @HandlerInput(name="endpoint", type=String.class, required=true),
                    @HandlerInput(name="attrs", type=Map.class, required=false)},
            output = {
                    @HandlerOutput(name="result", type=List.class)})
    public static void sendRequestToCollectionResource(HandlerContext handlerCtx) {
        //Map<String, Object> attrs = (Map<String, Object>) handlerCtx.getInputValue("attrs");
        String endpoint = fixEndpoint((String) handlerCtx.getInputValue("endpoint"));

        Response resp = RestUtil.getJerseyClient().target(endpoint)
                .request(RestUtil.RESPONSE_TYPE)
                .cookie(new Cookie(RestUtil.REST_TOKEN_COOKIE, RestUtil.getRestToken()))
                .get(Response.class);
        if (!isSuccess(resp.getStatus())) {
            throw new RuntimeException(resp.readEntity(String.class));
        }

        List list = resp.readEntity(List.class);
        handlerCtx.setOutputValue("result", list);
    }

    @Handler(id = "rest.get",
            input = {
                    @HandlerInput(name="endpoint", type=String.class, required=true),
                    @HandlerInput(name="attrs", type=Map.class, required=false)},
            output = {
                    @HandlerOutput(name="result", type=Map.class)})
    public static void sendGetRequestToItemResource(HandlerContext handlerCtx) {
        Map<String, Object> attrs = (Map<String, Object>) handlerCtx.getInputValue("attrs");
        String endpoint = fixEndpoint((String) handlerCtx.getInputValue("endpoint"));

        Response resp = RestUtil.targetWithQueryParams(RestUtil.getJerseyClient().target(endpoint),
                RestUtil.buildMultivalueMap(attrs))
                .request(RestUtil.RESPONSE_TYPE)
                .cookie(new Cookie(RestUtil.REST_TOKEN_COOKIE, RestUtil.getRestToken()))
                .get(Response.class);
//        Response resp = makeRequest(endpoint, attrs).get(Response.class);

        Map map = resp.readEntity(Map.class);
        handlerCtx.setOutputValue("result", map);
    }

    @Handler(id = "rest.post",
            input = {
                    @HandlerInput(name="endpoint", type=String.class, required=true),
                    @HandlerInput(name="attrs", type=Map.class, required=false)},
            output = {
                    @HandlerOutput(name="result", type=String.class)})
    public static void sendPostRequest(HandlerContext handlerCtx) {
        Map<String, Object> attrs = (Map<String, Object>) handlerCtx.getInputValue("attrs");
        String endpoint = fixEndpoint((String) handlerCtx.getInputValue("endpoint"));

//        Response resp = makeRequest(endpoint, null).post(Response.class, attrs);

        Response resp = RestUtil.getJerseyClient().target(endpoint)
                .request(RestUtil.RESPONSE_TYPE)
                .cookie(new Cookie(RestUtil.REST_TOKEN_COOKIE, RestUtil.getRestToken()))
                .post(Entity.entity(attrs, MediaType.APPLICATION_JSON_TYPE), Response.class);

        if (!isSuccess(resp.getStatus())) {
            GuiUtil.getLogger().log(
                    Level.SEVERE,
                    GuiUtil.getCommonMessage("LOG_UPDATE_ENTITY_FAILED", new Object[]{endpoint, attrs}));
            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.error.checkLog"));
            return;
        }

        handlerCtx.setOutputValue("result", endpoint);
    }

    private static String fixEndpoint(String endpoint) {
        if (endpoint.startsWith("/")) {
            endpoint = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("REST_URL") + endpoint;
        }
        return endpoint;
    }

//    private static Builder makeRequest(String endpoint, Map<String, Object> queryParams) {
//        Target resource = RestUtil.getJerseyClient().target(endpoint);
//        if (queryParams != null) {
//            resource =  resource.queryParams(RestUtil.buildMultivalueMap(queryParams));
//        }
//        return resource
//                .type(MediaType.APPLICATION_JSON)
//                .cookie(new Cookie(RestUtil.REST_TOKEN_COOKIE, RestUtil.getRestToken()))
//                .accept(RestUtil.RESPONSE_TYPE);
//    }

    protected static boolean isSuccess(int status) {
        return (status >= 200) && (status <= 299);
    }
}
