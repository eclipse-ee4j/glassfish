/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.web.cache.mapping;

public class Constants {
    /** field scope
     */
    public static final int SCOPE_CONTEXT_ATTRIBUTE = 1;
    public static final int SCOPE_REQUEST_HEADER = 2;
    public static final int SCOPE_REQUEST_PARAMETER = 3;
    public static final int SCOPE_REQUEST_COOKIE = 4;
    public static final int SCOPE_REQUEST_ATTRIBUTE = 5;
    public static final int SCOPE_SESSION_ATTRIBUTE = 6;
    public static final int SCOPE_SESSION_ID = 7;

    // field match expression
    public static final int MATCH_EQUALS = 1;
    public static final int MATCH_GREATER = 2;
    public static final int MATCH_LESSER = 3;
    public static final int MATCH_NOT_EQUALS = 4;
    public static final int MATCH_IN_RANGE = 5;
}
