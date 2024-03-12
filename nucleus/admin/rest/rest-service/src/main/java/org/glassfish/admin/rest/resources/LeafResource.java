/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;

import org.glassfish.admin.rest.provider.MethodMetaData;
import org.glassfish.admin.rest.results.ActionReportResult;
import org.glassfish.admin.rest.results.OptionsResult;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;

import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.admin.rest.utils.ResourceUtil;
import org.glassfish.admin.rest.utils.Util;
import org.jvnet.hk2.config.Dom;

import static org.glassfish.admin.rest.utils.Util.decode;

/**
 * @author Ludovic Champenois
 */
public abstract class LeafResource extends AbstractResource {

    protected LeafContent entity;
    protected Dom parent;
    protected String tagName;

    public final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(LeafResource.class);

    /** Creates a new instance of xxxResource */
    public LeafResource() {
    }

    public void setEntity(LeafContent p) {
        entity = p;
    }

    public LeafContent getEntity() {
        return entity;
    }

    public void setParentAndTagName(Dom parent, String tagName) {
        this.parent = parent;
        this.tagName = tagName;
        entity = new LeafContent();
        entity.name = tagName;//parent.leafElements(tagName);
        entity.value = parent.leafElement(tagName);

    }

    @GET
    @Produces({ "text/html", MediaType.APPLICATION_JSON + ";qs=0.5", MediaType.APPLICATION_XML + ";qs=0.5",
            MediaType.APPLICATION_FORM_URLENCODED + ";qs=0.5" })
    public ActionReportResult get(@QueryParam("expandLevel") @DefaultValue("1") int expandLevel) {
        if (getEntity() == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return buildActionReportResult();
    }

    @POST //create
    @Produces({ "text/html", MediaType.APPLICATION_JSON + ";qs=0.5", MediaType.APPLICATION_XML + ";qs=0.5",
            MediaType.APPLICATION_FORM_URLENCODED + ";qs=0.5" })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED })
    public ActionReportResult create(HashMap<String, String> data) {
        //hack-1 : support delete method for html
        //Currently, browsers do not support delete method. For html media,
        //delete operations can be supported through POST. Redirect html
        //client POST request for delete operation to DELETE method.
        if ((data.containsKey("operation")) && (data.get("operation").equals("__deleteoperation"))) {
            data.remove("operation");
            return delete(data);
        }
        return null;
        //TODO

        ////        String postCommand = getPostCommand();
        ////        final Map<String, String> payload = processData(data, postCommand);
        ////
        ////        return runCommand(postCommand, payload, "rest.resource.create.message",
        ////            "\"{0}\" created successfully.", "rest.resource.post.forbidden","POST on \"{0}\" is forbidden.");
    }

    @DELETE //delete
    @Produces({ "text/html", MediaType.APPLICATION_JSON + ";qs=0.5", MediaType.APPLICATION_XML + ";qs=0.5",
            MediaType.APPLICATION_FORM_URLENCODED + ";qs=0.5" })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED })
    public ActionReportResult delete(HashMap<String, String> data) {
        ResourceUtil.addQueryString(uriInfo.getQueryParameters(), data);

        return null;//TODOTODO

    }

    @OPTIONS
    @Produces({ MediaType.APPLICATION_JSON + ";qs=0.5", "text/html", MediaType.APPLICATION_XML + ";qs=0.5" })
    public ActionReportResult options() {
        return buildActionReportResult();
    }

    protected ActionReportResult buildActionReportResult() {
        RestActionReporter ar = new RestActionReporter();
        final String typeKey = (decode(getName()));
        ar.setActionDescription(typeKey);
        ar.getExtraProperties().put("entityLeaf", getEntity());

        OptionsResult optionsResult = new OptionsResult(Util.getResourceName(uriInfo));
        Map<String, MethodMetaData> mmd = getMethodMetaData();
        optionsResult.putMethodMetaData("GET", mmd.get("GET"));
        optionsResult.putMethodMetaData("POST", mmd.get("POST"));

        ResourceUtil.addMethodMetaData(ar, mmd);
        ActionReportResult r = new ActionReportResult(ar, optionsResult);
        r.setLeafContent(entity);
        return r;
    }

    protected Map<String, MethodMetaData> getMethodMetaData() {
        Map<String, MethodMetaData> mmd = new TreeMap<String, MethodMetaData>();
        //GET meta data
        mmd.put("GET", new MethodMetaData());

        return mmd;
    }

    protected String getName() {
        return Util.getResourceName(uriInfo);
    }

    public static class LeafContent {

        public String name;
        public String value;
    }

}
