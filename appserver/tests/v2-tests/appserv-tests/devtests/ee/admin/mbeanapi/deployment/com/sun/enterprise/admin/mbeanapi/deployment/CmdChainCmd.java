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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 */
public class CmdChainCmd implements Cmd
{
    private final List _cmds = new ArrayList();

    public CmdChainCmd()
    {
    }

    public CmdChainCmd(List cmds)
    {
        _cmds.addAll(cmds);
    }

    public void addCmd(Cmd cmd)
    {
        _cmds.add(cmd);
    }

    public Object execute() throws Exception
    {
        final Iterator it = _cmds.iterator();
        while (it.hasNext())
        {
            ((Cmd)it.next()).execute();
        }
        return new Integer(0);
    }
}
