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

import javax.management.MBeanAttributeInfo;

import org.glassfish.admin.amx.util.stringifier.Stringifier;

public class MBeanAttributeInfoStringifier extends MBeanFeatureInfoStringifier implements Stringifier
{
    public final static MBeanAttributeInfoStringifier DEFAULT =
            new MBeanAttributeInfoStringifier();

    public MBeanAttributeInfoStringifier()
    {
        super();
    }

    public MBeanAttributeInfoStringifier(MBeanFeatureInfoStringifierOptions options)
    {
        super(options);
    }

    public String stringify(Object o)
    {
        MBeanAttributeInfo attr = (MBeanAttributeInfo) o;
        String result = attr.getName() + ":";
        if (attr.isReadable())
        {
            result = result + "r";
        }
        if (attr.isWritable())
        {
            result = result + "w";
        }
        result = result + mOptions.mArrayDelimiter + getPresentationTypeString(attr.getType());

        if (mOptions.mIncludeDescription)
        {
            result = result + ",\"" + attr.getDescription() + "\"";
        }

        return (result);
    }

}
