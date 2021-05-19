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
 * ConcurrencyGroupElement.java
 *
 * Created on February 29, 2000, 5:21 PM
 */

package com.sun.jdo.api.persistence.model.jdo;

/**
 *
 * @author raccah
 * @version %I%
 */
public class ConcurrencyGroupElement extends FieldGroupElement
{
    /** Create new ConcurrencyGroupElement with no implementation.
     * This constructor should only be used for cloning and archiving.
     */
    public ConcurrencyGroupElement ()
    {
        this(null, null);
    }

    /** Create new ConcurrencyGroupElement with the provided implementation.
     * The implementation is responsible for storing all properties of the
     * object.
     * @param impl the implementation to use
     * @param declaringClass the class to attach to
     */
    public ConcurrencyGroupElement (ConcurrencyGroupElement.Impl impl,
        PersistenceClassElement declaringClass)
    {
        super(impl, declaringClass);
    }

    /** @return implemetation factory for this concurrency group
     */
    final Impl getConcurrencyGroupImpl () { return (Impl)getImpl(); }

    /** Pluggable implementation of concurrency group elements.
     * @see ConcurrencyGroupElement
     */
    public interface Impl extends FieldGroupElement.Impl { }
}

