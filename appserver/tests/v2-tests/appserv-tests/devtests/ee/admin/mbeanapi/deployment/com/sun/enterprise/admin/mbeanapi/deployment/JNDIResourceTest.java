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
import java.util.HashMap;

import com.sun.appserv.management.config.JNDIResourceConfig;


/**
 */
public class JNDIResourceTest extends BaseTest
{
    private final Cmd target;

    static final String kJNDIName       = "myJNDIResource";
    static final String kJNDILookupName = "jndi/myJNDIResource";
    static final String kResType        = "abc";
    static final String kFactoryClass   = "a.b.c";
    static final String kObjectType     = "user";

    public JNDIResourceTest(final String user,
        final String password, final String host, final int port,
        final String jndiName, final String jndiLookupName,
        final String resType, final String factoryClass)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final CreateJNDIResourceCmd createCmd =
                cmdFactory.createCreateJNDIResourceCmd(jndiName,
                        jndiLookupName, resType, factoryClass, getOptional());

        final DeleteJNDIResourceCmd deleteCmd =
                cmdFactory.createDeleteJNDIResourceCmd(jndiName);

        final PipeCmd p1 = new PipeCmd(connectCmd, createCmd);
        final PipeCmd p2 = new PipeCmd(p1, new VerifyCreateCmd());
        final PipeCmd p3 = new PipeCmd(connectCmd, deleteCmd);

        final CmdChainCmd chainCmd = new CmdChainCmd();
        chainCmd.addCmd(p2);
        chainCmd.addCmd(p3);

        target = chainCmd;
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }


    public static void main(String[] args) throws Exception
    {
        new JNDIResourceTest(
                "admin", "adminadmin", "localhost", 8686,
                kJNDIName, kJNDILookupName, kResType, kFactoryClass).run();
    }

    private Map getOptional()
    {
        final Map optional = new HashMap();
        //optional.put(CreateResourceKeys.RESOURCE_OBJECT_TYPE_KEY,
            //kObjectType);
        return optional;
    }

    private final class VerifyCreateCmd implements Cmd, SinkCmd
    {
        private JNDIResourceConfig res;

        private VerifyCreateCmd()
        {
        }

        public void setPipedData(Object o)
        {
            res = (JNDIResourceConfig)o;
        }

        public Object execute() throws Exception
        {
            System.out.println(
                "JNDIName="+res.getJNDIName());
            System.out.println("JNDILookupName="+res.getJNDILookupName());
            System.out.println("ObjectType="+res.getObjectType());
            System.out.println("FactoryClass="+res.getFactoryClass());
            System.out.println("ResType="+res.getResType());

            return new Integer(0);
        }

    }
}
