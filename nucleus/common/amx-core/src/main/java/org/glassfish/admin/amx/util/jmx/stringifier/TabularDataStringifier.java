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

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.glassfish.admin.amx.util.stringifier.Stringifier;

public class TabularDataStringifier implements Stringifier
{
    public static final TabularDataStringifier DEFAULT = new TabularDataStringifier();

    public TabularDataStringifier()
    {
    }

    @Override
    public String stringify(Object o)
    {
        final StringBuffer buf = new StringBuffer();
        buf.append("Tabular data:\n");

        final TabularData data = (TabularData) o;

        int rowIndex = 0;
        for( final Object temp : data.values() )
        {
            final CompositeData item = (CompositeData)temp;
            final String s = CompositeDataStringifier.DEFAULT.stringify(item);

            // emit the row index followed by the row
            buf.append("[").append(rowIndex).append("] ");
            buf.append(s).append("\n");

            ++rowIndex;
        }

        return (buf.toString());
    }

}



















