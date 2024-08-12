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

package org.glassfish.admin.amx.util.jmx.stringifier;

import java.util.Iterator;

import javax.management.AttributeList;

import org.glassfish.admin.amx.util.stringifier.IteratorStringifier;
import org.glassfish.admin.amx.util.stringifier.Stringifier;

public final class AttributeListStringifier implements Stringifier
{
    final String mDelim;

    public final static AttributeListStringifier DEFAULT = new AttributeListStringifier();

    public AttributeListStringifier()
    {
        this("\n");
    }

    public AttributeListStringifier(String delim)
    {
        mDelim = delim;

    }

    public String stringify(Object o)
    {
        final AttributeList attrList = (AttributeList) o;
        final Iterator iter = attrList.iterator();

        return (IteratorStringifier.stringify(iter, mDelim));
    }

}
