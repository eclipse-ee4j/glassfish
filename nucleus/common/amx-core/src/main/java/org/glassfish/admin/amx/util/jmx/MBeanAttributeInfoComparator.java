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

import javax.management.MBeanAttributeInfo;

import org.glassfish.admin.amx.util.jmx.stringifier.MBeanAttributeInfoStringifier;
import org.glassfish.admin.amx.util.jmx.stringifier.MBeanFeatureInfoStringifierOptions;

/**
Caution: this Comparator may be inconsistent with equals() because it ignores the description.
 */
public final class MBeanAttributeInfoComparator implements java.util.Comparator<MBeanAttributeInfo>, Serializable
{
    private static final MBeanAttributeInfoStringifier ATTRIBUTE_INFO_STRINGIFIER =
            new MBeanAttributeInfoStringifier(new MBeanFeatureInfoStringifierOptions(false, ","));

    public static final MBeanAttributeInfoComparator INSTANCE = new MBeanAttributeInfoComparator();

    private MBeanAttributeInfoComparator()
    {
    }

    @Override
    public int compare(final MBeanAttributeInfo o1, final MBeanAttributeInfo o2)
    {
        final String s1 = ATTRIBUTE_INFO_STRINGIFIER.stringify(o1);
        final String s2 = ATTRIBUTE_INFO_STRINGIFIER.stringify(o2);

        return (s1.compareTo(s2));
    }

    @Override
    public boolean equals(Object other)
    {
        return (other instanceof MBeanAttributeInfoComparator);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

}
