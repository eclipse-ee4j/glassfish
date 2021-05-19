/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.admin.amx.util.stringifier;

import java.util.Iterator;

/**
 * Stringifies an Iterator, using an optional element Stringifier.
 * Must be subclassed to provide Stringification of an element.
 */
public abstract class IteratorStringifierBase implements Stringifier {

    public final String mDelim;
    public final Stringifier mElementStringifier;

    public IteratorStringifierBase() {
        this(ObjectStringifier.DEFAULT);
    }


    public IteratorStringifierBase(String delim) {
        this(delim, new SmartStringifier(delim));
    }


    public IteratorStringifierBase(Stringifier elementStringifier) {
        this(",", elementStringifier);
    }


    public IteratorStringifierBase(String delim, Stringifier elementStringifier) {
        mDelim = delim;
        mElementStringifier = elementStringifier;
    }


    @Override
    public String stringify(Object o) {
        assert (o != null);
        Iterator iter = (Iterator) o;

        return (this.stringify(iter, mDelim, mElementStringifier));
    }


    /*
     * Subclass may choose to override this.
     */
    protected abstract void stringifyElement(Object elem, String delim, StringBuffer buf);


    public String stringify(Iterator iter, String delim, Stringifier stringifier) {
        assert (iter != null);

        StringBuffer buf = new StringBuffer();

        while (iter.hasNext()) {
            final Object elem = iter.next();

            stringifyElement(elem, delim, buf);
            buf.append(delim);
        }

        // strip trailing delimiter
        final int length = buf.length();
        if (length != 0) {
            buf.setLength(length - delim.length());
        }

        return (buf.toString());
    }

    public final static IteratorStringifier DEFAULT = new IteratorStringifier(",");
}
