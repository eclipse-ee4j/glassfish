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
 * Variable.java
 *
 * Created on March 8, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore.query.util.scope;

import com.sun.jdo.spi.persistence.support.sqlstore.query.util.type.Type;

import org.glassfish.persistence.common.I18NHelper;

/**
 * An object of class Variable is used if an identifier
 * is declared as variable.
 *
 * @author  Michael Bouschen
 * @version 0.1
 */
public class Variable
  extends Definition
{
    /**
     *
     */
    public Variable(Type type)
    {
        super(type);
    }

    /**
     *
     */
    public String getName()
    {
        return I18NHelper.getMessage(messages, "scope.variable.getname.name"); //NOI18N
    }

    /**
     *
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("Variable("); //NOI18N
        buf.append(scope);
        buf.append(", "); //NOI18N
        buf.append(type);
        buf.append(")"); //NOI18N
        return buf.toString();
    }
}
