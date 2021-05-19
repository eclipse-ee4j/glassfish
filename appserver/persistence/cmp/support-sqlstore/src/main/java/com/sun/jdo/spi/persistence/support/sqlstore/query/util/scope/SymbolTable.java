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
 * Created on March 8, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore.query.util.scope;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;

/**
 * The symbol table handling declared identifies.
 *
 * @author  Michael Bouschen
 * @version 0.1
 */
public class SymbolTable
{
    /**
     * The actual scope level.
     */
    protected int actualScope = 0;

    /**
     * Stack of old definitions.
     */
    protected Stack nestings = new Stack();

    /**
     * The table of declared identifier (symbols).
     */
    protected Hashtable symbols = new Hashtable();

    /**
     * Opens a new scope.
     * Prepare everything to handle old definitions when
     * a identifier declaration is hidden.
     */
    public void enterScope()
    {
        actualScope++;
        nestings.push(new Nesting());
    }

    /**
     * Closes the actual scope.
     * Hidden definitions are reinstalled.
     */
    public void leaveScope()
    {
        forgetNesting((Nesting)nestings.pop());
        actualScope--;
    }

    /**
     * Returns the level of the actual scope.
     * @return actual scope level.
     */
    public int getActualScope()
    {
        return actualScope;
    }

    /**
     * Add identifier to the actual scope.
     * If the identifier was already declared in the actual
     * scope the symbol table is NOT changed and the old definition
     * is returned. Otherwise a possible definition of a lower
     * level scope is saved in the actual nesting and the new definition
     * is stored in the symbol table. This allows to reinstall the old
     * definition when the sctaul scope is closed.
     * @param   ident   identifier to be declared
     * @param   def new definition of identifier
     * @return  the old definition if the identifier was already declared
     *          in the actual scope; null otherwise
     */
    public Definition declare(String ident, Definition def)
    {
        Definition old = (Definition)symbols.get(ident);
        def.setScope(actualScope);
        if ((old == null) || (old.getScope() < actualScope))
        {
            Nesting nest = (Nesting)nestings.peek();
            nest.add(ident, old); // save old definition in nesting
            symbols.put(ident, def); // install new definition as actual definition
            return null;
        }
        else
        {
            return old;
        }
    }

    /**
     * Checks whether the specified identifier is declared.
     * @param ident the name of identifier to be tested
     * @return true if the identifier is declared;
     * false otherwise.
     */
    public boolean isDeclared(String ident)
    {
        return (getDefinition(ident) != null);
    }

    /**
     * Checks the symbol table for the actual definition
     * of the specified identifier. If the identifier is
     * declared the definition is returned, otherwise null.
     * @param ident the name of identifier
     * @return the actual definition of ident is declared;
     * null otherise.
     */
    public Definition getDefinition(String ident)
    {
        return (Definition)symbols.get(ident);
    }

    /**
     * Internal method to reinstall the old definitions.
     * The method is called when a scope is closed.
     * For all identifier that were declared in the
     * closed scope their former definition (that was hidden)
     * is reinstalled.
     * @param nesting list of hidden definitions
     */
    protected void forgetNesting(Nesting nesting)
    {
        String ident = null;
        Definition hidden = null;

        Iterator idents = nesting.getIdents();
        Iterator hiddenDefs = nesting.getHiddenDefinitions();

        while (idents.hasNext())
        {
            ident = (String) idents.next();
            hidden = (Definition) hiddenDefs.next();
            if (hidden == null)
            {
                symbols.remove(ident);
            }
            else
            {
                symbols.put(ident, hidden);
            }
        }
    }
}
