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

import javax.management.MBeanOperationInfo;

import org.glassfish.admin.amx.util.stringifier.Stringifier;

public class MBeanOperationInfoStringifier
        extends MBeanFeatureInfoStringifier implements Stringifier
{
    public static final MBeanOperationInfoStringifier DEFAULT = new MBeanOperationInfoStringifier();

    public MBeanOperationInfoStringifier()
    {
        super();
    }

    public MBeanOperationInfoStringifier(MBeanFeatureInfoStringifierOptions options)
    {
        super(options);
    }

    public static String getImpact(MBeanOperationInfo info)
    {
        String impactStr = null;

        switch (info.getImpact())
        {
            default:
                impactStr = "unknown";
                break;
            case MBeanOperationInfo.INFO:
                impactStr = "info";
                break;
            case MBeanOperationInfo.ACTION:
                impactStr = "action";
                break;
            case MBeanOperationInfo.ACTION_INFO:
                impactStr = "action-info";
                break;
        }

        return (impactStr);
    }

    public static String getSignature(MBeanOperationInfo info)
    {
        return (getSignature(info, MBeanFeatureInfoStringifierOptions.DEFAULT));
    }

    public static String getSignature(MBeanOperationInfo info, MBeanFeatureInfoStringifierOptions options)
    {
        return (paramsToString(info.getSignature(), options));
    }

    public static String getDescription(MBeanOperationInfo info)
    {
        return (sOperationDelimiter + "\"" + info.getDescription() + "\"");
    }

    public String stringify(Object o)
    {
        assert (o != null);
        final MBeanOperationInfo op = (MBeanOperationInfo) o;

        String result = getPresentationTypeString(op.getReturnType()) + " " + op.getName() + "(";

        // create the signature string
        result = result + getSignature(op, mOptions) + ")";

        String impactStr = getImpact(op);

        result = result + sOperationDelimiter + "impact=" + impactStr;

        if (mOptions.mIncludeDescription)
        {
            result = result + getDescription(op);
        }

        return (result);
    }

}
