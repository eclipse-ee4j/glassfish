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
 * Definition.java
 *
 * Created on March 8, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore.query.util.scope;

import com.sun.jdo.spi.persistence.support.sqlstore.query.util.type.Type;

import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;

/**
 * Super class of all possible identifier definitions
 *
 * @author  Michael Bouschen
 * @version 0.1
 */
public abstract class Definition
{
    /**
     * I18N support
     */
    protected final static ResourceBundle messages =
            I18NHelper.loadBundle(Definition.class);

    /**
     * Scope level of the definition
     */
    protected int scope;

    /**
     * Type of the identifier
     */
    protected Type type;

    /**
     * Creates a new definition.
     * A definition contains at least the type of the identifier.
     * @param type type of the declared identifier
     */
    public Definition (Type type)
    {
        this.type = type;
    }

    /**
     * Set the scope of the identifier's definition.
     */
    public void setScope(int scope)
    {
        this.scope = scope;
    }

    /**
     * Returns the scope of the identifier's definition.
     */
    public int getScope()
    {
        return scope;
    }

    /**
     * Returns the type of the identifiers's definition.
     */
    public Type getType()
    {
        return type;
    }

    /**
     * Returns the name of the definition.
     */
    public abstract String getName();
}
