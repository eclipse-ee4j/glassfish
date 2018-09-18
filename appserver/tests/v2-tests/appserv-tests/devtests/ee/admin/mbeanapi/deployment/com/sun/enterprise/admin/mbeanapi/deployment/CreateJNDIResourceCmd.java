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

import com.sun.appserv.management.config.JNDIResourceConfig;

/**
 */
public class CreateJNDIResourceCmd extends BaseResourceCmd
    implements SourceCmd
{
    public static final String kJNDILookupName      = "JNDILookupName";
    public static final String kResType             = "ResType";
    public static final String kFactoryClass        = "FactoryClass";

    public CreateJNDIResourceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        final JNDIResourceConfig resource = getDomainConfig().createJNDIResourceConfig(
            getJNDIName(), getJNDILookupName(), getResType(),
            getFactoryClass(), getOptional());
        return resource;
    }

    private String getJNDILookupName()
    {
        return (String)getCmdEnv().get(kJNDILookupName);
    }

    private String getFactoryClass()
    {
        return (String)getCmdEnv().get(kFactoryClass);
    }

    private String getResType()
    {
        return (String)getCmdEnv().get(kResType);
    }
}
