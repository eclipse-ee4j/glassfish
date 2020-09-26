/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.oracle.javaee7.samples.batch.cmt.chunk;

import jakarta.ejb.Remote;
import java.util.Collection;
import java.util.Map;
import java.util.List;

@Remote
public interface JobSubmitter {

    public String nextId();

    public long submitJob(String jobName);

    public Collection<String> listJobs(boolean useLongFormat);

    public Collection<String> listJobExecutions(boolean useLongFormat, long... executinIds);

    public Map<String, String> toMap(long executionId);

    public String getJobExitStatus(long executionId);

    public List<Long> getAllExecutionIds(String jobName);

}

