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
 * ErrorType.java
 *
 * Created on March 8, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore.query.util.type;

/**
 * This class represents the internal error type used during semantic analysis.
 *
 * @author  Michael Bouschen
 * @version 0.1
 */
public class ErrorType
    extends Type
{
    /**
     *
     */
    ErrorType()
    {
        super("jdoErrorType", void.class); //NOI18N
    }

    /**
     *
     */
    public boolean isCompatibleWith(Type type)
    {
        return true;
    }

    /**
     *
     */
        public boolean isOrderable()
        {
                return true;
        }
}
