/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

DROP TABLE JOBSTATUS;
DROP TABLE STEPSTATUS;
DROP TABLE CHECKPOINTDATA;
DROP TABLE JOBINSTANCEDATA;
DROP TABLE EXECUTIONINSTANCEDATA;
DROP TABLE STEPEXECUTIONINSTANCEDATA;

CREATE TABLE JOBINSTANCEDATA(
  jobinstanceid serial not null PRIMARY KEY,
  name          character varying (512),
  apptag        VARCHAR(512)
);

CREATE TABLE EXECUTIONINSTANCEDATA(
  jobexecid     serial not null PRIMARY KEY,
  jobinstanceid bigint not null REFERENCES JOBINSTANCEDATA (jobinstanceid),
  createtime    timestamp,
  starttime     timestamp,
  endtime       timestamp,
  updatetime    timestamp,
  parameters    bytea,
  batchstatus   character varying (512),
  exitstatus    character varying (512)
);

CREATE TABLE STEPEXECUTIONINSTANCEDATA(
    stepexecid       serial not null PRIMARY KEY,
    jobexecid        bigint not null REFERENCES EXECUTIONINSTANCEDATA (jobexecid),
    batchstatus      character varying (512),
    exitstatus       character varying (512),
    stepname         character varying (512),
    readcount        integer,
    writecount       integer,
    commitcount      integer,
    rollbackcount    integer,
    readskipcount    integer,
    processskipcount integer,
    filtercount      integer,
    writeskipcount   integer,
    startTime        timestamp,
    endTime          timestamp,
    persistentData   bytea
);

CREATE TABLE JOBSTATUS (
  id        bigint not null REFERENCES JOBINSTANCEDATA (jobinstanceid),
  obj       bytea
);

CREATE TABLE STEPSTATUS(
  id        bigint not null REFERENCES STEPEXECUTIONINSTANCEDATA (stepexecid),
  obj       bytea
);

CREATE TABLE CHECKPOINTDATA(
  id        character varying (512),
  obj       bytea
);
