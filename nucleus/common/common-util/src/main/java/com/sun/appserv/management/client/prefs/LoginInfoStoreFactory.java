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

package com.sun.appserv.management.client.prefs;

import java.util.Arrays;
import com.sun.appserv.management.client.prefs.LoginInfo;
import com.sun.appserv.management.client.prefs.LoginInfoStoreFactory;

/** A factory class to create instances of LoginInfoStore.
 * @since Appserver 9.0
 */
public class LoginInfoStoreFactory {

    /** Private constructor.
     */
    private LoginInfoStoreFactory() {
    }

    /** Returns the store that is represented by given class name. The parameter must
     * implement the {@link LoginInfoStore} interface. If a null is passed, an instance of the default
     * store {@link MemoryHashLoginInfoStore} is returned.
     * @param storeImplClassName fully qualified name of the class implementing LoginInfoStore. May be null.
     * @return the instance of LoginInfoStore of your choice
     * @throws IllegalArgumentException if the parameter does not implement LoginInfoStore
     * @throws StoreException if the construction of default store results in problems
     * @throws ClassNotFoundException if the given class could not be loaded
     */
    public static LoginInfoStore getStore(final String storeImplClassName)
        throws StoreException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        LoginInfoStore store = null;
        if (storeImplClassName == null)
            store = getDefaultStore();
        else
            store = getCustomStore(storeImplClassName);
        return ( store );
    }

    public static LoginInfoStore getDefaultStore() throws StoreException {
        return ( new MemoryHashLoginInfoStore() );
    }

    private static LoginInfoStore getCustomStore(final String icn)
        throws ClassNotFoundException, IllegalAccessException, InstantiationException{
        final Class ic  = Class.forName(icn);
        final String in = LoginInfoStore.class.getName();
        if (ic == null || !isStore(ic))
            throw new IllegalArgumentException("Class: " + ic.getName() + " does not implement: " + in);
        final LoginInfoStore store = (LoginInfoStore) ic.newInstance();
        return ( store );
    }

    private static boolean isStore(final Class c) {
        final Class[] ifs = c.getInterfaces();
        final Class sc    = LoginInfoStore.class;
        return ( Arrays.asList(ifs).contains(sc) );
    }
}
