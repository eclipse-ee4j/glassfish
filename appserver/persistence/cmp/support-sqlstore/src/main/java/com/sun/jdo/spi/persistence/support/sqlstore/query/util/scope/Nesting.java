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
 * Nesting.java
 *
 * Created on March 8, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore.query.util.scope;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class is used to handle the hidden definition
 * of an identifier. If an identifier is declared its old
 * definition is hidden and stored in the actual nesting.
 *
 * @author  Michael Bouschen
 * @version 0.1
 */
public class Nesting
{
    /**
     * List of idents with hidden definitions in the actual scope.
     */
    protected List idents;

    /**
     * List of hidden definitions; each definition in this list
     * corresponds to an identifier of the list idents.
     */
    protected List hiddenDefs;

    /**
     * Creates an new nesting.
     */
    public Nesting()
    {
        this.idents = new ArrayList();
        this.hiddenDefs = new ArrayList();
    }

    /**
     * Adds a hidden definition to this.
     * @param ident name of the identifier
     * @param hidden the hidden definition of ident
     */
    public void add(String ident, Definition hidden)
    {
        idents.add(ident);
        hiddenDefs.add(hidden);
    }

    /**
     * Returns an enumeration of idents with hidden definitions.
     */
    public Iterator getIdents()
    {
        return idents.iterator();
    }

    /**
     * Returns an enumeration of hidden definitions.
     */
    public Iterator getHiddenDefinitions()
    {
        return hiddenDefs.iterator();
    }
}
