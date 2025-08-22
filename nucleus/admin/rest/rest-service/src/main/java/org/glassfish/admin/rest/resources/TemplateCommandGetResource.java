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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.admin.rest.Constants;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.jersey.media.sse.SseFeature;

/**
 *
 * @author ludovic champenois ludo@dev.java.net Code moved from generated classes to here. Gen code inherits from this
 * template class that contains the logic for mapped commands RS Resources
 *
 */
public class TemplateCommandGetResource extends TemplateExecCommand {

    public TemplateCommandGetResource(String resourceName, String commandName, String commandMethod, boolean b) {
        super(resourceName, commandName, commandMethod, "GET", commandName, b);
    }

    @GET
    @Produces({ "text/html", MediaType.APPLICATION_JSON + ";qs=0.5", MediaType.APPLICATION_XML + ";qs=0.5",
            MediaType.APPLICATION_FORM_URLENCODED + ";qs=0.5" })
    public Response processGetLegacyFormat() {
        return executeCommandLegacyFormat(prepareParameters());
    }

    @GET
    @Produces(Constants.MEDIA_TYPE_JSON + ";qs=0.5")
    public CommandResult processGet() {
        return executeCommand(prepareParameters());
    }

    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS + ";qs=0.5")
    public Response processSseGet() {
        return executeSseCommand(prepareParameters());
    }

    private ParameterMap prepareParameters() {
        ParameterMap data = new ParameterMap();
        processCommandParams(data);
        addQueryString(uriInfo.getQueryParameters(), data);
        purgeEmptyEntries(data);
        adjustParameters(data);
        return data;
    }
}
