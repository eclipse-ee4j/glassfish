/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment;

import org.glassfish.deployment.common.Descriptor;

/**
 * This class encapsulates information in the runAs-specified-identity
 * XML element as well as the runtime principal to be used.
 *
 * @author Sanjeev Krishnan
 */
public final class RunAsIdentityDescriptor extends Descriptor {

    private static final long serialVersionUID = 1L;
    private String roleName = "";
    private String principal = "";

    public RunAsIdentityDescriptor() {
    }


    public RunAsIdentityDescriptor(String description) {
        super("no name", description);
    }


    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }


    public String getRoleName() {
        return roleName;
    }


    public void setPrincipal(String principal) {
        this.principal = principal;
    }


    public String getPrincipal() {
        return principal;
    }
}
