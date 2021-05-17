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
 * StringType.java
 *
 * Created on April 19, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore.query.util.type;

import com.sun.jdo.spi.persistence.utility.FieldTypeEnumeration;

/**
 *
 * @author  Michael Bouschen
 * @version 0.1
 */
public class StringType
    extends ClassType
{
    /**
     *
     */
    public StringType(TypeTable typetab)
    {
        super("java.lang.String", String.class, FieldTypeEnumeration.STRING, typetab); //NOI18N
    }

    /**
     * String defines an ordering.
     */
    public boolean isOrderable()
    {
        return true;
    }
}


