/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.oracle.javaee7.samples.batch.simple.batchlet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

import jakarta.annotation.PostConstruct;

import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.JobExecution;
import jakarta.ejb.Stateless;
import jakarta.ejb.EJB;

@Stateless
public class SlessBean
    implements Sless {

    boolean ejbCreateCalled = false;

    public void ejbCreate() {
        this.ejbCreateCalled = true;
    }

    public long submitJob() {
        try {
          JobOperator jobOperator = BatchRuntime.getJobOperator();

          Properties props = new Properties();
          for (int i=0; i<9; i++)
                props.put(i, i);
          return  jobOperator.start("simpleBatchletJob", props);
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
    }

    public String getJobExitStatus(long executionId) {
        try {
        JobOperator jobOperator = BatchRuntime.getJobOperator();
        JobExecution jobExecution = jobOperator.getJobExecution(executionId);
        return jobExecution.getExitStatus();
        } catch (Exception ex) {}
        return "-1";
    }


    public boolean wasEjbCreateCalled() {
        return ejbCreateCalled;
    }


}
