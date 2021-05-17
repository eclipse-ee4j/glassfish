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

package org.glassfish.security.common;

import org.glassfish.api.admin.PasswordAliasResolver;
import org.glassfish.api.admin.PasswordAliasStore;

/**
 * Provides password alias resolution, using an internal password alias store
 * to actually resolve an alias if one is specified.
 *
 * @author tjquinn
 */
public class PasswordAliasResolverImpl implements PasswordAliasResolver {

    private static final String ALIAS_TOKEN = "ALIAS";
    private static final String STARTER = "${" + ALIAS_TOKEN + "="; //no space is allowed in starter
    private static final String ENDER = "}";

    private final PasswordAliasStore store;
    public PasswordAliasResolverImpl(final PasswordAliasStore store) {
        this.store = store;
    }

    @Override
    public char[] resolvePassword(String aliasExpressionOrPassword) {
        final String alias = getAlias(aliasExpressionOrPassword);
        if (alias != null) {
            return store.get(alias);
        }
        return aliasExpressionOrPassword.toCharArray();
    }

    /**
     * check if a given property name matches AS alias pattern ${ALIAS=aliasname}.
     * if so, return the aliasname, otherwise return null.
     * @param propName The property name to resolve. ex. ${ALIAS=aliasname}.
     * @return The aliasname or null.
     */
    private static String getAlias(String pwOrAliasExpression)
    {
       String aliasName=null;

       pwOrAliasExpression = pwOrAliasExpression.trim();
       if (pwOrAliasExpression.startsWith(STARTER) && pwOrAliasExpression.endsWith(ENDER) ) {
           pwOrAliasExpression = pwOrAliasExpression.substring(STARTER.length() );
           int lastIdx = pwOrAliasExpression.length() - 1;
           if (lastIdx > 1) {
              pwOrAliasExpression = pwOrAliasExpression.substring(0,lastIdx);
              if (pwOrAliasExpression!=null) {
                   aliasName = pwOrAliasExpression.trim();
               }
           }
       }
       return aliasName;
    }
}
