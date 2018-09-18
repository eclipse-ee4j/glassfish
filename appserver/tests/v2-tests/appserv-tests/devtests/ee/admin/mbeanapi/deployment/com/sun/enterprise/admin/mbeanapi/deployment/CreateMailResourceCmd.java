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

import com.sun.appserv.management.config.MailResourceConfig;

/**
 */
public class CreateMailResourceCmd extends BaseResourceCmd
    implements SourceCmd
{
    public static final String kHost    = "Host";
    public static final String kUser    = "User";
    public static final String kFrom    = "From";

    public CreateMailResourceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        final MailResourceConfig resource = getDomainConfig().createMailResourceConfig(
            getJNDIName(), getHost(), getUser(), getFrom(), getOptional());
        return resource;
    }

    private String getHost()
    {
        return (String)getCmdEnv().get(kHost);
    }

    private String getUser()
    {
        return (String)getCmdEnv().get(kUser);
    }

    private String getFrom()
    {
        return (String)getCmdEnv().get(kFrom);
    }
}
