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

/*
 * SymbolTable.java
 *
 * Created on November 19, 2001
 */

package com.sun.jdo.spi.persistence.support.ejb.ejbqlc;

import java.util.HashMap;
import java.util.Map;

/**
 * The symbol table handling declared identifies.
 *
 * @author  Michael Bouschen
 */
public class SymbolTable
{
    /**
     * The table of declared identifier (symbols).
     */
    protected Map symbols = new HashMap();

    /**
     * This method adds the specified identifier to this SymbolTable.
     * The specified decl object provides details anbout the declaration.
     * If this SymbolTable already defines an identifier with the same name,
     * the SymbolTable is not changed and the existing declaration is returned.
     * Otherwise <code>null</code> is returned.
     * @param   ident   identifier to be declared
     * @param   decl new definition of identifier
     * @return  the old definition if the identifier was already declared;
     * <code>null</code> otherwise
     */
    public Object declare(String ident, Object decl)
    {
        Object old = symbols.get(ident);
        if (old == null) {
            symbols.put(ident.toUpperCase(), decl);
        }
        return old;
    }

    /**
     * Checks whether the specified identifier is declared.
     * @param ident the name of identifier to be tested
     * @return <code>true</code> if the identifier is declared;
     * <code>false</code> otherwise.
     */
    public boolean isDeclared(String ident)
    {
        return (getDeclaration(ident) != null);
    }

    /**
     * Checks the symbol table for the actual declaration of the specified
     * identifier. The method returns the declaration object if available or
     * <code>null</code> for an undeclared identifier.
     * @param ident the name of identifier
     * @return the declaration object if ident is declared;
     * <code>null</code> otherise.
     */
    public Object getDeclaration(String ident)
    {
        return symbols.get(ident.toUpperCase());
    }

}
