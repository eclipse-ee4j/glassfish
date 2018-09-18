/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;

/**
 */
public abstract class BaseResourceCmd extends BaseCmd
{
    public static final String kJNDIName    = "JNDIName";
    public static final String kOptional    = "Optional";
    public static final String kTarget      = "Target";
    public static final String kRACName     = "resourceAdapterName";


    public BaseResourceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    protected String getJNDIName()
    {
        return (String)getCmdEnv().get(kJNDIName);
    }

    protected String getTarget()
    {
        return (String)getCmdEnv().get(kTarget);
    }

    protected Map getOptional()
    {
        return (Map)getCmdEnv().get(kOptional);
    }
}
