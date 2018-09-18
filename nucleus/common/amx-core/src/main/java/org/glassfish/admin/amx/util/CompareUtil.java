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

/**
Provides a variety of useful utilities for comparing objects.
 */
public final class CompareUtil
{
    private CompareUtil()
    {
        // disallow instantiation
    }

    public static boolean objectsEqual(Object o1, Object o2)
    {
        boolean equal = o1 == o2;

        if (!equal)
        {
            if (o1 == null)
            {
                // o1 is null, but o2 is not
                equal = false;
            }
            else
            {
                equal = o1.equals(o2);
                if (!equal)
                {
                    if (ClassUtil.objectIsArray(o1))
                    {
                        equal = ArrayUtil.arraysEqual(o1, o2);
                    }
                }
            }
        }

        return (equal);
    }

}

