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

package com.sun.enterprise.deployment.web;

    /** I represent the information about how the web application's data should be protected.
    ** @author Danny Coward
    */
public interface UserDataConstraint {
    public static String NONE_TRANSPORT = "NONE";
    public static String INTEGRAL_TRANSPORT = "INTEGRAL";
    public static String CONFIDENTIAL_TRANSPORT = "CONFIDENTIAL";
    // JACC specific
    public static String CLEAR = "CLEAR";
    public String getDescription();
    public void setDescription(String description);

    public String getTransportGuarantee();
    public String[] getUnacceptableTransportGuarantees();
    public void setTransportGuarantee(String transportGuarantee);
}

