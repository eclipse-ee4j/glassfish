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

package org.glassfish.admin.amx.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
Implements the Iterator interface over an Enumeration
 */
public final class EnumerationIterator implements Iterator
{
    private final Enumeration mEnum;

    public EnumerationIterator(Enumeration enumIn)
    {
        mEnum = enumIn;
    }

    public boolean hasNext()
    {
        return (mEnum.hasMoreElements());
    }

    public Object next()
    {
        return (mEnum.nextElement());
    }

    public void remove()
    {
        throw new UnsupportedOperationException("");
    }

}
