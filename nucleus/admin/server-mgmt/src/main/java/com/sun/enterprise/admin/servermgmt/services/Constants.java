/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.servermgmt.services;

/**
 * A place for everything. Everything in its place
 *
 * @author bnevins
 */
class Constants {
    ///////////////////////////////////////////////////////////////////////////
    /////           Token  Names   ////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    static final String DATE_CREATED_TN = "DATE_CREATED";
    static final String AS_ADMIN_PATH_TN = "AS_ADMIN_PATH";
    static final String CREDENTIALS_TN = "CREDENTIALS";
    static final String SERVICE_NAME_TN = "NAME";
    static final String SERVICE_TYPE_TN = "TYPE";
    static final String CFG_LOCATION_TN = "LOCATION";
    static final String ENTITY_NAME_TN = "ENTITY_NAME";
    static final String DISPLAY_NAME_TN = "DISPLAY_NAME";
    static final String AS_ADMIN_USER_TN = "AS_ADMIN_USER";
    static final String AS_ADMIN_PASSWORD_TN = "AS_ADMIN_PASSWORD";
    static final String AS_ADMIN_MASTERPASSWORD_TN = "AS_ADMIN_MASTERPASSWORD";
    //static final String PASSWORD_FILE_PATH_TN        = "PASSWORD_FILE_PATH";
    static final String OS_USER_TN = "OS_USER";
    static final String START_COMMAND_TN = "START_COMMAND";
    static final String RESTART_COMMAND_TN = "RESTART_COMMAND";
    static final String STOP_COMMAND_TN = "STOP_COMMAND";
    static final String LOCATION_ARGS_START_TN = "LOCATION_ARGS_START";
    static final String LOCATION_ARGS_STOP_TN = "LOCATION_ARGS_STOP";
    static final String LOCATION_ARGS_RESTART_TN = "LOCATION_ARGS_RESTART";
    static final String CREDENTIALS_START_TN = "CREDENTIALS_START";
    static final String CREDENTIALS_STOP_TN = "CREDENTIALS_STOP";
    static final String SERVICEUSER_STOP_TN = "SERVICEUSER_STOP";
    static final String SERVICEUSER_START_TN = "SERVICEUSER_START";
    ///////////////////////////////////////////////////////////////////////////
    /////           Other Constants     ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    static final String START_ARG_START = "<startargument>";
    static final String STOP_ARG_START = "<stopargument>";
    static final String START_ARG_END = "</startargument>";
    static final String STOP_ARG_END = "</stopargument>";
    static final String TRACE_PREPEND = "TRACE:  ";
    static final String DRYRUN_PREPEND = "DRYRUN:  ";
    static final String README = "PlatformServices.log";
    static final String ETC = "/etc";
    static final String INITD = "/etc/init.d";
    static final String REGEXP_PATTERN_BEGIN = "[KS][0-9][0-9]?";
}
