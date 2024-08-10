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
import java.util.Set;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import org.glassfish.admin.amx.util.stringifier.SmartStringifier;
import org.glassfish.admin.amx.util.stringifier.Stringifier;

public class CompositeDataStringifier implements Stringifier
{
    public static final CompositeDataStringifier DEFAULT = new CompositeDataStringifier();

    public CompositeDataStringifier()
    {
    }

    public String stringify(Object o)
    {
        final StringBuffer buf = new StringBuffer();
        buf.append("Composite data:\n");

        final CompositeData data = (CompositeData) o;
        final CompositeType type = data.getCompositeType();

        final Set keySet = type.keySet();
        final Iterator iter = keySet.iterator();
        while (iter.hasNext())
        {
            final String key = (String) iter.next();
            final Object item = data.get(key);

            final String s = SmartStringifier.toString(item);
            buf.append(key + "=" + s + "\n");
        }

        return (buf.toString());
    }

}



















