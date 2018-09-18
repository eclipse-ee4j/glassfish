/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.admin;

/**
 * Resolves password alias expressions of the form ${ALIAS=aliasName} using
 * an internal password alias store while also accepting passwords themselves
 * which are not translated.
 * 
 * @author tjquinn
 */
public interface PasswordAliasResolver {
   
    /**
     * Returns the password from the argument, processing (if present)
     * an expression of the form ${ALIAS=aliasName} using
     * a PasswordAliasStore.  If the argument is not such an expression
     * then the returned value is the character array
     * for that string.  If the argument is an alias expression then
     * the alias name is resolved using an internal
     * PasswordAliasStore and the corresponding password is returned.
     *
     * @param aliasExpressionOrPassword either a password or a password
     * alias expression
     * @return the resolved password
     */
    char[] resolvePassword(String aliasExpressionOrPassword);
}
