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

package com.sun.jdo.spi.persistence.support.ejb.ejbqlc;

/**
 * An instance of this class denotes an identification variable as declared
 * in the from clause of an EJBQL query string. The compiler creates such an
 * instance when analysing the from clause and stores it in the symbol table.
 *
 * @author Michael Bouschen
 */
public class IdentificationVariable
{
    /** The name of the identification variable. */
    private String name;

    /** The type of the identification variable. */
    private Object typeInfo;

    /**
     * Creates an identification variable declaration for use during semantic
     * analysis.
     * @param name the name of the identification variable.
     * @param typeInfo the type of the identification variable.
     */
    public IdentificationVariable(String name, Object typeInfo)
    {
        this.name = name;
        this.typeInfo = typeInfo;
    }

    /**
     * Returns the name of the IdentificationVariable.
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the type of the IdentificationVariable.
     * @return the type
     */
    public Object getTypeInfo()
    {
        return typeInfo;
    }

}
