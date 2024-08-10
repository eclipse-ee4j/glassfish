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

package org.glassfish.admin.rest.resources.composite;

import com.sun.enterprise.v3.admin.commands.ListJobsCommand;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.glassfish.admin.rest.composite.CompositeResource;
import org.glassfish.admin.rest.model.RestModelResponseBody;
import org.glassfish.admin.rest.utils.StringUtil;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ParameterMap;

/**
 * This resource is used to view the current state of a specific job.
 * <h2>Example Interactions</h2>
 * <h4>View a specific detached job</h4>
 *
 * <div class="codeblock"> $ curl --user admin:admin123 -v \ -H Accept:application/vnd.oracle.glassfish+json \ -H
 * Content-Type:application/vnd.oracle.glassfish+json \ -H X-Requested-By:MyClient \
 * http://localhost:4848/management/jobs/id/1
 *
 * HTTP/1.1 200 OK { "exitCode": "COMPLETED", "jobId": "1", "jobName": "load-sdp", "jobState": "COMPLETED",
 * "executionDate": "Wed Jan 02 11:36:38 CST 2013", "message": "SDP loaded with name nucleusSDP.", "user": "admin" }
 * </div>
 *
 * @author jdlee
 */
public class JobResource extends CompositeResource {

    /**
     * Retrieve information about the specific job identified by the resource URL.
     * <p>
     * <b>Roles: PaasAdmin, TenantAdmin</b>
     *
     * @param jobId
     * @return the {@link Job} entity which contains information about the job id specified.
     */
    @GET
    public RestModelResponseBody<Job> getItem(@PathParam("jobId") String jobId) throws Exception {
        ActionReport ar = executeReadCommand(getCommandName(), getParameters());
        Collection<Map<String, Object>> jobMaps = (List<Map<String, Object>>) ar.getExtraProperties().get("jobs");
        if (jobMaps != null) {
            for (Map<String, Object> jobMap : jobMaps) {
                if (StringUtil.compareStrings(jobId, (String) jobMap.get(ListJobsCommand.ID))) {
                    Job model = JobsResource.constructJobModel(jobMap);
                    return restModelResponseBody(Job.class, getCollectionChildParentUri(), model);
                }
            }
        }

        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    protected String getCommandName() {
        return "list-jobs";
    }

    protected ParameterMap getParameters() {
        return new ParameterMap();
    }
}
