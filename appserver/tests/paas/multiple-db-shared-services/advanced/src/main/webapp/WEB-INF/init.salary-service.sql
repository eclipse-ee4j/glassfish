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

CREATE TABLE SALARY (
    emp_id    integer not null,
    emp_sal   float(7, 2)
);

INSERT INTO SALARY VALUES(101, 11111.12);
INSERT INTO SALARY VALUES(102, 11221.15);
INSERT INTO SALARY VALUES(101, 31411.16);
INSERT INTO SALARY VALUES(101, 14135.72);
INSERT INTO SALARY VALUES(101, 61114.18);

