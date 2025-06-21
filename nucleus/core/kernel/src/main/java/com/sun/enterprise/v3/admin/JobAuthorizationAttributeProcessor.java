/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.v3.admin;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Subject;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AuthorizationPreprocessor;
import org.glassfish.api.admin.Job;
import org.glassfish.api.admin.progress.JobInfo;
import org.jvnet.hk2.annotations.Service;

/**
 * Attaches a user attribute to job resources for authorization.
 *
 * @author Tim Quinn
 * @author Bhakti Mehta
 */
@Service
@Singleton
public class JobAuthorizationAttributeProcessor implements AuthorizationPreprocessor {

    private final static String USER_ATTRIBUTE_NAME = "user";

    public final static String JOB_RESOURCE_NAME_PREFIX_NO_SLASH = "jobs/job";
    public final static String JOB_RESOURCE_NAME_PREFIX = JOB_RESOURCE_NAME_PREFIX_NO_SLASH + '/';

    public final static Pattern JOB_PATTERN = Pattern.compile("(?:" + JOB_RESOURCE_NAME_PREFIX_NO_SLASH + "(?:/(\\d*))?)");

    @Inject
    private JobManagerService jobManager;

    @Inject
    private DefaultJobManagerFile defaultJobFile;

    @Override
    public void describeAuthorization(Subject subject, String resourceName, String action, AdminCommand command,
            Map<String, Object> context, Map<String, String> subjectAttributes, Map<String, String> resourceAttributes,
            Map<String, String> actionAttributes) {
        final Matcher m = JOB_PATTERN.matcher(resourceName);
        if (!m.matches() || (m.groupCount() == 0)) {
            /*
             * The resource name pattern did not match for including a job ID, so we will not be able to attach a user attribute to
             * the resource.
             */
            return;
        }
        final String jobID = m.group(1);
        final Job job = jobManager.get(jobID);

        /*
         * This logic might run before any validation in the command has run, in which case the job ID would be invalid and the
         * job manager and/or the completed jobs store might not know about the job.
         */
        final String userID;
        if (job == null || job.getSubjectUsernames().isEmpty()) {
            final JobInfo jobInfo = jobManager.getCompletedJobForId(jobID, defaultJobFile.getFile());
            userID = jobInfo == null ? null : jobInfo.user;
        } else {
            userID = job.getSubjectUsernames().get(0);
        }
        if (userID != null) {
            resourceAttributes.put(USER_ATTRIBUTE_NAME, userID);
        }
    }
}
