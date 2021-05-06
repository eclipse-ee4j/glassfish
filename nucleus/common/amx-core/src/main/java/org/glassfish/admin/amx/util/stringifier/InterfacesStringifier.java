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

package org.glassfish.admin.amx.util.stringifier;

/**
 * Stringifies an object based on specified interfaces.
 */
public class InterfacesStringifier implements Stringifier {

    private final StringifierRegistry mRegistry;
    private final Class<?>[] mInterfaces;

    public InterfacesStringifier(Class[] interfaces) {
        this(StringifierRegistryImpl.DEFAULT, interfaces);
    }


    public InterfacesStringifier(StringifierRegistry registry, Class[] interfaces) {
        mRegistry = registry;
        mInterfaces = interfaces;
    }


    private <T> String stringifyAs(Object o, Class<T> theClass) {
        String result = null;
        if (theClass.isAssignableFrom(o.getClass())) {
            final Stringifier stringifier = mRegistry.lookup(theClass);
            if (stringifier != null) {
                result = stringifier.stringify(o);
            }
        }
        return (result);
    }


    @Override
    public String stringify(Object o) {
        StringBuilder result = new StringBuilder();

        for (final Class<?> intf : mInterfaces) {
            final String s = stringifyAs(o, intf);
            if (s != null) {
                result.append(intf.getName()).append(": ").append(s).append("\n");
            }
        }

        if (result.length() == 0) {
            return o.toString();
        }

        return result.toString();
    }
}
