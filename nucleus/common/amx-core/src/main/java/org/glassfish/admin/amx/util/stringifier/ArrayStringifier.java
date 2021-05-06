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

import java.util.Arrays;
import java.util.Iterator;

/**
 * Stringifies an array, using an optional array element Stringifier
 */

public final class ArrayStringifier implements Stringifier {

    final String mDelim;
    final Stringifier mElementStringifier;
    boolean mAddBraces;

    static final char LEFT_BRACE = '{';
    static final char RIGHT_BRACE = '}';
    static final String DEFAULT_DELIM = ", ";

    public ArrayStringifier() {
        this(SmartStringifier.DEFAULT);
        mAddBraces = false;
    }


    public ArrayStringifier(boolean addBraces) {
        this(DEFAULT_DELIM, SmartStringifier.DEFAULT, addBraces);
    }


    public ArrayStringifier(String delim) {
        this(delim, false);
    }


    public ArrayStringifier(String delim, boolean addBraces) {
        this(delim, SmartStringifier.DEFAULT, addBraces);
    }


    public ArrayStringifier(Stringifier elementStringifier) {
        this(DEFAULT_DELIM, elementStringifier);
    }


    public ArrayStringifier(String delim, Stringifier elementStringifier) {
        this(delim, elementStringifier, false);
    }


    public ArrayStringifier(String delim, Stringifier elementStringifier, boolean addBraces) {
        mDelim = delim;
        mElementStringifier = elementStringifier;
        mAddBraces = addBraces;
    }


    static String addBraces(boolean add, String s) {
        String out = s;
        if (add) {
            out = LEFT_BRACE + s + RIGHT_BRACE;
        }
        return (out);
    }


    @Override
    public String stringify(Object o) {
        final String s = ArrayStringifier.stringify((Object[]) o, mDelim, mElementStringifier);

        return (addBraces(mAddBraces, s));
    }


    /**
     * Static variant when direct call will suffice.
     */
    public static String stringify(Object[] o, String delim, Stringifier stringifier) {
        final Iterator iter = Arrays.asList(o).iterator();
        final IteratorStringifier iterStringifier = new IteratorStringifier(delim, stringifier);

        final String s = iterStringifier.stringify(iter);
        return (s);
    }


    /**
     * Static variant when direct call will suffice.
     */
    public static String stringify(Object[] o, String delim) {
        return (stringify(o, delim, SmartStringifier.DEFAULT));
    }

    public final static ArrayStringifier DEFAULT = new ArrayStringifier(",");
}

