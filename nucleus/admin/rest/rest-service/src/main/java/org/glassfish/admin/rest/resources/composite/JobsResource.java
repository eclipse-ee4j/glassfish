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

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.glassfish.admin.rest.composite.CompositeResource;
import org.glassfish.admin.rest.composite.CompositeUtil;
import org.glassfish.admin.rest.model.RestCollectionResponseBody;
import org.glassfish.admin.rest.utils.StringUtil;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ParameterMap;
import org.jvnet.hk2.annotations.Service;

/**
 * This resource queries for the system for all detached jobs.
 * <h2>Example Interactions</h2>
 * <h4>View the detached jobs</h4>
 *
 * <div class="codeblock"> $ curl --user admin:admin123 -v \ -H Accept:application/vnd.oracle.glassfish+json \ -H
 * Content-Type:application/vnd.oracle.glassfish+json \ -H X-Requested-By:MyClient \
 * http://localhost:4848/management/jobs
 *
 * HTTP/1.1 200 OK { "items": [{ "exitCode": "COMPLETED", "jobId": "1", "jobName": "load-sdp", "jobState": "COMPLETED",
 * "executionDate": "Wed Jan 02 11:36:38 CST 2013", "message": "SDP loaded with name nucleusSDP.", "user": "admin" }],
 * "metadata": [{"id": "http:\/\/localhost:4848\/management\/jobs\/id\/1"}] } </div>
 *
 * @author jdlee
 */
@Service
@Path("/jobs")
public class JobsResource extends CompositeResource {

    /**
     * The GET method on this resource returns a list of Job entities that represent each recent or current job known to
     * this GlassFish instance.
     * <p>
     * Roles: PaasAdmin, TenantAdmin
     * <p>
     *
     * @param currentUser Optional query parameter to restrict the set of returns {@link Job} objects to those for the
     * current user
     * @return A collection of Job entities which contains information for each job resource. For each job returned, the
     * <code>jobId</code> field can be used to format the URI to interact with a specific job.
     * @throws Exception
     */
    @GET
    public RestCollectionResponseBody<Job> getItems(@QueryParam("currentUser") @DefaultValue("false") final boolean currentUser,
            @QueryParam(INCLUDE) final String include, @QueryParam(EXCLUDE) final String exclude) throws Exception {
        RestCollectionResponseBody<Job> rb = restCollectionResponseBody(Job.class, "job", null); // there is no parent resource
        ActionReport ar = executeReadCommand(getCommandName(), getParameters());
        Collection<Map<String, Object>> jobMaps = (List<Map<String, Object>>) ar.getExtraProperties().get("jobs");
        if (jobMaps != null) {
            for (Map<String, Object> jobMap : jobMaps) {
                if (currentUser && !StringUtil.compareStrings((String) jobMap.get(ListJobsCommand.USER), this.getAuthenticatedUser())) {
                    continue;
                }
                if (jobMap == null) {
                    continue;
                }
                Job model = constructJobModel(jobMap);
                rb.addItem(filterModel(Job.class, model, include, exclude, "jobId"), model.getJobId());
            }
        }
        return rb;
    }

    @Path("id/{jobId}")
    public JobResource getJobResource() {
        return getSubResource(JobResource.class);
    }

    protected String getCommandName() {
        return "list-jobs";
    }

    protected ParameterMap getParameters() {
        return new ParameterMap();
    }

    static Job constructJobModel(Map<String, Object> jobMap) {
        Job model = CompositeUtil.instance().getModel(Job.class);
        model.setJobId((String) jobMap.get(ListJobsCommand.ID));
        model.setJobName((String) jobMap.get(ListJobsCommand.NAME));
        model.setExecutionDate(jobMap.get(ListJobsCommand.DATE).toString());
        model.setCompletionDate(jobMap.get(ListJobsCommand.COMPLETION_DATE).toString());
        model.setJobState(jobMap.get(ListJobsCommand.STATE).toString());
        model.setExitCode((String) jobMap.get(ListJobsCommand.CODE));
        model.setMessage((String) jobMap.get(ListJobsCommand.MESSAGE));
        model.setUser((String) jobMap.get(ListJobsCommand.USER));
        return model;
    }
}
