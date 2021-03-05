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

package com.sun.enterprise.security.jacc.provider;

import java.security.Principal;
import java.util.BitSet;
import java.util.Set;

import javax.security.auth.Subject;

/**
 *
 * @author monzillo
 */
public interface JACCRoleMapper {

    String HANDLER_KEY = "simple.jacc.provider.RoleMapper";
    String CLASS_NAME = "simple.jacc.provider.JACCRoleMapper.class";

    Set<String> getDeclaredRoles(String pcid);

    boolean isSubjectInRole(String pcid, Subject s, String roleName) throws SecurityException;

    boolean arePrincipalsInRole(String pcid, Principal[] principals, String roleName) throws SecurityException;

    Set<String> getRolesOfSubject(String pcid, Subject s) throws SecurityException, UnsupportedOperationException;

    Set<String> getRolesOfPrincipals(String pcid, Principal[] principals) throws SecurityException, UnsupportedOperationException;

    BitSet getRolesOfSubject(String pcid, String roles[], Subject s) throws SecurityException, UnsupportedOperationException;

    BitSet getRolesOfPrincipals(String pcid, String roles[], Principal[] principals)
            throws SecurityException, UnsupportedOperationException;

    Set<Principal> getPrincipalsInRole(String pcid, String roleName) throws SecurityException, UnsupportedOperationException;
}
