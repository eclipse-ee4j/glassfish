/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.naming.impl;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

/**
 * This is a Wrapper for {@link SerialContext}.
 * This is used by {@link SerialInitContextFactory} when NamingManager is set
 * up with an InitialContextFactoryBuilder. The reason for having this class
 * is described below:
 * When there is no builder setup, {@link InitialContext} uses a discovery
 * mechanism to handle URL strings as described in
 * {@link NamingManager#getURLContext(String, java.util.Hashtable)}. But,
 * when a builder is set up, it by-passes this logic and delegates to whatever
 * Context is returned by
 * builder.createInitialContextFactory(env).getInitialContext(env).
 * In our case, this results in SerialContext, which does not know how to handle
 * all kinds of URL strings. So, we want to returns a WrapperSerialContext
 * that delegates to appropriate URLContext whenever possible.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class WrappedSerialContext extends InitialContext
{
    /*
     * Implementation Note:
     * It extends InitialContext and overrides getURLOrDefaultInitCtx methods.
     * This is a very sensitive class. Take extreme precautions while changing.
     */

    // Not for public use.
    /* prackage */ WrappedSerialContext(Hashtable environment,
                                        SerialContext serialContext)
                                        throws NamingException
    {
        super(environment);
        defaultInitCtx = serialContext; // this is our default context
        gotDefault = true;
    }

    @Override
    protected void init(Hashtable environment) throws NamingException
    {
        // Don't bother merging with application resources  or system
        // properties, as that has already happened when user called
        // new InitialContext. So, just store it.
        myProps = environment;
    }

    @Override
    protected Context getDefaultInitCtx() throws NamingException
    {
        return defaultInitCtx;
    }

    @Override
    protected Context getURLOrDefaultInitCtx(String name) throws NamingException
    {
        String scheme = getURLScheme(name);
        if (scheme != null)
        {
            Context ctx = NamingManager.getURLContext(scheme, myProps);
            if (ctx != null)
            {
                return ctx;
            }
        }
        return getDefaultInitCtx();
    }

    @Override
    protected Context getURLOrDefaultInitCtx(Name name) throws NamingException
    {
        if (name.size() > 0)
        {
            String first = name.get(0);
            String scheme = getURLScheme(first);
            if (scheme != null)
            {
                Context ctx = NamingManager.getURLContext(scheme, myProps);
                if (ctx != null)
                {
                    return ctx;
                }
            }
        }
        return getDefaultInitCtx();
    }

    /**
     * Return URL scheme component from this string. Returns null if there
     * is no scheme.
     *
     * @param str
     * @return
     * @see javax.naming.spi.NamingManager#getURLScheme
     */
    private static String getURLScheme(String str)
    {
        // Implementation is copied from
        // javax.naming.spi.NamingManager#getURLScheme
        int colon_posn = str.indexOf(':');
        int slash_posn = str.indexOf('/');

        if (colon_posn > 0 && (slash_posn == -1 || colon_posn < slash_posn))
        {
            return str.substring(0, colon_posn);
        }
        return null;
    }

}
