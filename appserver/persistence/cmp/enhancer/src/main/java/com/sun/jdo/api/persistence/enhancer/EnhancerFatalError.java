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

package com.sun.jdo.api.persistence.enhancer;


/**
 * Thrown to indicate that the class-file enhancer failed to perform an
 * operation due to a serious error.  The enhancer is not guaranteed to
 * be in a consistent state anymore.
 */
public class EnhancerFatalError
    extends Exception
{
    /**
     * An optional nested exception.
     */
    public final Throwable nested;

    /**
     * Constructs an <code>EnhancerFatalError</code> with no detail message.
     */
    public EnhancerFatalError()
    {
        this.nested = null;
    }

    /**
     * Constructs an <code>EnhancerFatalError</code> with the specified
     * detail message.
     */
    public EnhancerFatalError(String msg)
    {
        super(msg);
        this.nested = null;
    }

    /**
     * Constructs an <code>EnhancerFatalError</code> with an optional
     * nested exception.
     */
    public EnhancerFatalError(Throwable nested)
    {
        super(nested.toString());
        this.nested = nested;
    }

    /**
     * Constructs an <code>EnhancerFatalError</code> with the specified
     * detail message and an optional nested exception.
     */
    public EnhancerFatalError(String msg, Throwable nested)
    {
        super(msg);
        this.nested = nested;
    }
}
