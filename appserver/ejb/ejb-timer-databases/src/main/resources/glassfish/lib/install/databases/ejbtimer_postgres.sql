/*
 * Copyright:qw:wq (c) 2019 Fujitsu Limited and/or its affiliates. All rights
 * reserved.
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

CREATE TABLE "EJB__TIMER__TBL" (
"CREATIONTIMERAW"      BIGINT                 NOT NULL,
"BLOB"                 BYTEA,
"TIMERID"              CHARACTER VARYING(255) NOT NULL,
"CONTAINERID"          BIGINT                 NOT NULL,
"OWNERID"              CHARACTER VARYING(255),
"STATE"                INTEGER                NOT NULL,
"PKHASHCODE"           INTEGER                NOT NULL,
"INTERVALDURATION"     BIGINT                 NOT NULL,
"INITIALEXPIRATIONRAW" BIGINT                 NOT NULL,
"LASTEXPIRATIONRAW"    BIGINT                 NOT NULL,
"SCHEDULE"             CHARACTER VARYING(255),
"APPLICATIONID"        BIGINT                 NOT NULL,
CONSTRAINT "EJB__TIMER__TBL_pkey" PRIMARY KEY ("TIMERID")
)
