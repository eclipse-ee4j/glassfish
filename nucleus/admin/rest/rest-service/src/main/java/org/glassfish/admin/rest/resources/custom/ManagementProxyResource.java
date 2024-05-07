/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package org.glassfish.admin.rest.resources.custom;

import com.sun.enterprise.util.Utility;
import jakarta.inject.Inject;
import org.glassfish.admin.rest.results.ActionReportResult;
import org.glassfish.admin.rest.utils.ProxyImpl;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.jvnet.hk2.config.Dom;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import org.glassfish.admin.rest.utils.Util;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ServerContext;

/**
 * @author Mitesh Meswani
 */
@Produces({ "text/html", MediaType.APPLICATION_JSON + ";qs=0.5", MediaType.APPLICATION_XML + ";qs=0.5" })
@Path("domain/proxy/{path:.*}")
public class ManagementProxyResource {
    @Context
    protected UriInfo uriInfo;

    @Context
    protected ServiceLocator habitat;

    @Inject
    private ServerContext serverContext;

    @GET
    public ActionReportResult proxyRequest() {

        RestActionReporter ar = new RestActionReporter();
        ar.setActionDescription("Proxied Data");
        ar.setSuccess();

        ActionReportResult result = new ActionReportResult(ar);

        /* Jersey client is not accessible from the current context classloader,
            which is kernel OSGi bundle CL. We need to run it within the common classloader as
            the context classloader
        */
        Properties proxiedResponse = Utility.runWithContextClassLoader(serverContext.getCommonClassLoader(),
                () -> {
                    return new ManagementProxyImpl()
                            .proxyRequest(uriInfo, Util.getJerseyClient(), habitat);
                });
        ar.setExtraProperties(proxiedResponse);
        return result;
    }

    private static class ManagementProxyImpl extends ProxyImpl {
        private static int TARGET_INSTANCE_NAME_PATH_INDEX = 2; //pathSegments == { "domain", "proxy", "instanceName", ....}

        @Override
        public UriBuilder constructTargetURLPath(UriInfo sourceUriInfo, URL responseURLReceivedFromTarget) {
            return sourceUriInfo.getBaseUriBuilder().replacePath(responseURLReceivedFromTarget.getFile());
        }

        @Override
        public UriBuilder constructForwardURLPath(UriInfo sourceUriInfo) {
            // The sourceURI is of the form /mangement/domain/proxy/<instanceName>/forwardSegment1/forwardSegment2/....
            // The forwardURI constructed is of the form /mangement/domain/forwardSegment1/forwardSegment2/....
            List<PathSegment> sourcePathSegments = sourceUriInfo.getPathSegments();
            List<PathSegment> forwardPathSegmentsHead = sourcePathSegments.subList(0, TARGET_INSTANCE_NAME_PATH_INDEX - 1); //path that precedes proxy/<instancenName>
            List<PathSegment> forwardPathSegmentsTail = sourcePathSegments.subList(TARGET_INSTANCE_NAME_PATH_INDEX + 1,
                    sourcePathSegments.size()); //path that follows <instanceName>
            UriBuilder forwardUriBuilder = sourceUriInfo.getBaseUriBuilder(); // Gives /management/domain
            for (PathSegment pathSegment : forwardPathSegmentsHead) { //append domain
                forwardUriBuilder.segment(pathSegment.getPath());
            }

            for (PathSegment pathSegment : forwardPathSegmentsTail) { //append forwardSegment1/forwardSegment2/....
                forwardUriBuilder.segment(pathSegment.getPath());
            }
            return forwardUriBuilder;
        }

        @Override
        public String extractTargetInstanceName(UriInfo uriInfo) {
            return uriInfo.getPathSegments().get(TARGET_INSTANCE_NAME_PATH_INDEX).getPath();
        }
    }

    public void setEntity(Dom p) {
        // ugly no-op hack to keep the generated code happy.
    }

}
