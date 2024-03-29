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

import javax.management.MBeanOperationInfo;

import org.glassfish.admin.amx.util.jmx.stringifier.MBeanOperationInfoStringifier;

/**
Caution: this Comparator may be inconsistent with equals() because it ignores the description.
 */
public final class MBeanOperationInfoComparator
        implements java.util.Comparator<MBeanOperationInfo>, Serializable
{
    public static final MBeanOperationInfoComparator INSTANCE = new MBeanOperationInfoComparator();

    private MBeanOperationInfoComparator()
    {
    }

    @Override
    public int compare(final MBeanOperationInfo info1, final MBeanOperationInfo info2)
    {
        // we just want to sort based on name and signature; there can't be two operations with the
        // same name and same signature, so as long as we include the name and signature the
        // sorting will always be consistent.
        int c = info1.getName().compareTo(info2.getName());
        if (c == 0)
        {
            // names the same, subsort on signature, first by number of params
            c = info1.getSignature().length - info2.getSignature().length;
            if (c == 0)
            {
                // names the same, subsort on signature, first by number of params
                c = MBeanOperationInfoStringifier.getSignature(info1).compareTo(
                        MBeanOperationInfoStringifier.getSignature(info2));
            }

        }

        return (c);
    }

    @Override
    public boolean equals(Object other)
    {
        return (other instanceof MBeanOperationInfoComparator);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

}
