/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.restconnector;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public final class Constants {
    private Constants() {
    }

    public static final String REST_ADMIN_ADAPTER = "RestAdminAdapter";
    public static final String REST_MANAGEMENT_ADAPTER = "RestManagementAdapter";
    public static final String REST_MONITORING_ADAPTER = "RestMonitoringAdapter";
    public static final String REST_COMMAND_ADAPTER = "RestCommandAdapter";
    public static final String REST_ADMIN_CONTEXT_ROOT = "/admin";
    public static final String REST_MANAGEMENT_CONTEXT_ROOT = "/management";
    public static final String REST_MONITORING_CONTEXT_ROOT = "/monitoring";
    public static final String REST_COMMAND_CONTEXT_ROOT = "/command";
}
