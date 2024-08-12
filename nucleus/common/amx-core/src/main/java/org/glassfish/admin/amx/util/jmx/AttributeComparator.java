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

package org.glassfish.admin.amx.util.jmx;

import java.io.Serializable;

import javax.management.Attribute;

public final class AttributeComparator implements java.util.Comparator<Attribute>, Serializable
{
    public static final AttributeComparator INSTANCE = new AttributeComparator();

    private AttributeComparator()
    {
    }

    @Override
    public int compare(Attribute attr1, Attribute attr2)
    {
        int result = attr1.getName().compareTo(attr2.getName());
        if (result == 0)
        {
            result = attr1.getValue().toString().compareTo(attr2.getValue().toString());
        }

        return (result);
    }

    @Override
    public boolean equals(Object other)
    {
        return (other instanceof AttributeComparator);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
}
