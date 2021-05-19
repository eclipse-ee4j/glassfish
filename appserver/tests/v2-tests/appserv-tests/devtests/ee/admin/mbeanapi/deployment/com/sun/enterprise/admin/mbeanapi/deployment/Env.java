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
import java.util.Collections;

import com.sun.appserv.management.client.TLSParams;
import com.sun.appserv.management.client.TrustStoreTrustManager;
import com.sun.appserv.management.client.TrustAnyTrustManager;
import com.sun.appserv.management.client.HandshakeCompletedListenerImpl;

/**
 */
public class Env
{
    public static final String USE_TLS_SYSTEM_PROPERTY = "useTLS";

    private Env()
    {
    }

    public static CmdFactory getCmdFactory()
    {
        return new CmdFactory();
    }

    public static TLSParams getTLSParams()
    {
        final TrustStoreTrustManager trustMgr =  TrustStoreTrustManager.
            getSystemInstance();

        // WBN -- NPE below if this is null, which is very likely if the System Props aren't
        //setup


        final HandshakeCompletedListenerImpl handshakeCompletedListener =
                new HandshakeCompletedListenerImpl();

        if(trustMgr == null)
        {
            javax.net.ssl.X509TrustManager tm = TrustAnyTrustManager.getInstance();
            return new TLSParams(tm, handshakeCompletedListener);
        }


        trustMgr.setPrompt(true);
        return new TLSParams(trustMgr, handshakeCompletedListener);
    }

    public static boolean useTLS()
    {
        // WBN -- if useTLS return false -- deployments fail 100% of the time.
        // so I'm switching this to always return true...
        return true;
        //final String useTLS = System.getProperty(USE_TLS_SYSTEM_PROPERTY);
        //return ((useTLS != null) &&
                //(useTLS.equals("true") || useTLS.equals("TRUE")));
    }
}
