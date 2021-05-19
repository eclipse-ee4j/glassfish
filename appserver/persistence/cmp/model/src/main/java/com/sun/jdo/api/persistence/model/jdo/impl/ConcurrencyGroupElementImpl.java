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
 * ConcurrencyGroupElementImpl.java
 *
 * Created on March 2, 2000, 6:37 PM
 */

package com.sun.jdo.api.persistence.model.jdo.impl;

import com.sun.jdo.api.persistence.model.jdo.ConcurrencyGroupElement;

/**
 *
 * @author raccah
 * @version %I%
 */
public class ConcurrencyGroupElementImpl extends FieldGroupElementImpl
    implements ConcurrencyGroupElement.Impl
{
    /** Create new ConcurrencyGroupElementImpl with no corresponding name.
     * This constructor should only be used for cloning and archiving.
     */
    public ConcurrencyGroupElementImpl ()
    {
        this(null);
    }

    /** Creates new ConcurrencyGroupElementImpl with the corresponding name
     * @param name the name of the element
     */
    public ConcurrencyGroupElementImpl (String name)
    {
        super(name);
    }
}
