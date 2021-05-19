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
 * JDOQueryException.java
 *
 * Created on March 28, 2000
 */

package com.sun.jdo.api.persistence.support;

/**
 *
 * @author  Michael Bouschen
 * @version 0.1
 */
public class JDOQueryException
    extends JDOUserException
{
    /**
     * Creates a new <code>JDOQueryException</code> without detail message.
     */
    public JDOQueryException()
    {
    }

    /**
     * Constructs a new <code>JDOQueryException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public JDOQueryException(String msg)
    {
        super(msg);
    }

    /**
      * Constructs a new <code>JDOQueryException</code> with the specified detail message
      * and nested Exception.
      * @param msg the detail message.
      * @param nested the nested <code>Exception</code>.
      */
    public JDOQueryException(String msg, Exception nested)
    {
        super(msg, nested);
    }
}
