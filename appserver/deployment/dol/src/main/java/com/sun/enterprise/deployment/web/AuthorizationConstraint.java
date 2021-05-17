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

import java.util.Enumeration;

/**
 * This is the descriptor for the authorization constraint element in
 * the DTD.
 * @author Danny Coward
 */

public interface AuthorizationConstraint extends WebDescriptor {
    public static String BASIC_METHOD = "basic";
    public static String FORM_METHOD = "form";
    public static String MUTUAL_METHOD = "mutual";

    /**
     * Return the security roles involved in this constraint.
     * @return the enumeration of security roles.
     */
    public Enumeration getSecurityRoles();

    /**
     * Add a security role to the constraint.
     * @param the security role.
     */
    public void addSecurityRole(SecurityRole securityRole);
}
