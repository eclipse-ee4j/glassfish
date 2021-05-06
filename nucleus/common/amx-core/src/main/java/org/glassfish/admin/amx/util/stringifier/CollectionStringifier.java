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

import java.util.Collection;
import java.util.Iterator;

public class CollectionStringifier implements Stringifier {

    public final static CollectionStringifier DEFAULT = new CollectionStringifier(",");

    public final String mDelim;
    public final Stringifier mElementStringifier;

    public CollectionStringifier(String delim) {
        this(delim, SmartStringifier.DEFAULT);
    }


    public CollectionStringifier(Stringifier elementStringifier) {
        this(",", elementStringifier);
    }


    public CollectionStringifier(String delim, Stringifier elementStringifier) {
        mDelim = delim;
        mElementStringifier = elementStringifier;
    }


    @Override
    public String stringify(Object o) {
        final Collection c = (Collection) o;
        final Iterator iter = c.iterator();

        String result = IteratorStringifier.DEFAULT.stringify(iter, mDelim, mElementStringifier);

        return (result);
    }


    public static String toString(final Object o, final String delim) {
        final Collection c = (Collection) o;
        final Iterator iter = c.iterator();

        String result = IteratorStringifier.stringify(iter, delim);

        return (result);
    }
}
