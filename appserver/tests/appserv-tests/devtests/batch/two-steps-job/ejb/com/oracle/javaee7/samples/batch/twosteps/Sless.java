/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.oracle.javaee7.samples.batch.twosteps;

import jakarta.ejb.Remote;

@Remote
public interface Sless {

    public long submitJob();

    public String getJobExitStatus(long executionId);

    public boolean wasEjbCreateCalled();

}

