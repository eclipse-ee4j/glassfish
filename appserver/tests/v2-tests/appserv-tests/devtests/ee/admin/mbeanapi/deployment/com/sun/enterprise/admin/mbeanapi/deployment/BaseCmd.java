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

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.j2ee.J2EEDomain;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.client.TLSParams;

/**
 */
public abstract class BaseCmd implements Cmd, SinkCmd
{
    public static final String kConnectionSource = "connectionSource";

    private final CmdEnv _cmdEnv;

    protected BaseCmd(CmdEnv cmdEnv)
    {
        if (cmdEnv == null)
        {
            throw new IllegalArgumentException();
        }
        _cmdEnv = cmdEnv;
    }

    public void setPipedData(Object o)
    {
        if (o instanceof AppserverConnectionSource)
        {
            setConnectionSource((AppserverConnectionSource)o);
        }
        else
        {
            throw new IllegalArgumentException(
                "setPipedData: Support only AppserverConnectionSource for now");
        }
    }

    protected CmdEnv getCmdEnv()
    {
        return _cmdEnv;
    }

    protected boolean isConnected()
    {
        return _cmdEnv.get(kConnectionSource) != null;
    }

    protected AppserverConnectionSource getConnectionSource()
    {
        return (AppserverConnectionSource)_cmdEnv.get(kConnectionSource);
    }

    protected void setConnectionSource(AppserverConnectionSource cs)
    {
        _cmdEnv.put(kConnectionSource, cs);
    }

    protected final DomainRoot getDomainRoot() throws Exception
    {
        return getConnectionSource().getDomainRoot();
    }

    protected final DomainConfig getDomainConfig() throws Exception
    {
       return getDomainRoot().getDomainConfig();
    }

    protected final J2EEDomain getJ2EEDomain() throws Exception
    {
       return getDomainRoot().getJ2EEDomain();
    }

    protected final TLSParams getTLSParams()
    {
        return Env.useTLS() ? Env.getTLSParams() : null;
    }

    protected final QueryMgr getQueryMgr() throws Exception
    {
       return getDomainRoot().getQueryMgr();
    }
}
