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

import javax.management.MBeanConstructorInfo;

import org.glassfish.admin.amx.util.stringifier.Stringifier;

public class MBeanConstructorInfoStringifier extends MBeanFeatureInfoStringifier implements Stringifier
{
    public static final MBeanConstructorInfoStringifier DEFAULT =
            new MBeanConstructorInfoStringifier();

    public MBeanConstructorInfoStringifier()
    {
        super();
    }

    public MBeanConstructorInfoStringifier(MBeanFeatureInfoStringifierOptions options)
    {
        super(options);
    }

    public String stringify(Object o)
    {
        final MBeanConstructorInfo constructor = (MBeanConstructorInfo) o;

        final String name = constructor.getName();
        final int lastDot = name.lastIndexOf(".");
        final String abbreviatedName = name.substring(lastDot + 1, name.length());

        final String params = "(" +
                              paramsToString(constructor.getSignature(), mOptions) + ")";

        return (abbreviatedName + params);
    }

}
