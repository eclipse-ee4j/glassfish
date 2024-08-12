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

import java.lang.reflect.Array;

import javax.management.MBeanParameterInfo;

import org.glassfish.admin.amx.util.ClassUtil;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

public class MBeanFeatureInfoStringifier
{
    final MBeanFeatureInfoStringifierOptions mOptions;

    public static final MBeanFeatureInfoStringifierOptions DEFAULT =
            new MBeanFeatureInfoStringifierOptions(true, ",");

    static final String sOperationDelimiter = ",";

    MBeanFeatureInfoStringifier()
    {
        mOptions = DEFAULT;
    }

    MBeanFeatureInfoStringifier(MBeanFeatureInfoStringifierOptions options)
    {
        mOptions = options;
    }

    static String getPresentationTypeString(String type)
    {
        return (ClassUtil.getFriendlyClassname(type));
    }

    static String paramsToString(final MBeanParameterInfo[] params, final MBeanFeatureInfoStringifierOptions options)
    {
        String result;

        if (Array.getLength(params) != 0)
        {
            result = ArrayStringifier.stringify(params,
                    options.mArrayDelimiter,
                    new MBeanParameterInfoStringifier(options));
        }
        else
        {
            result = "void";
        }
        return (result);
    }

}
