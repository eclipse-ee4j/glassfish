/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.util;

import java.util.Iterator;

/**
 *
 * @author tjquinn
 */
/**
 * Iterates through the initially specified class and its ancestors.
 */
public class ClassLineageIterator implements Iterator<Class> {
    private Class c;

    ClassLineageIterator(final Class c) {
        this.c = c;
    }

    @Override
    public boolean hasNext() {
        return c != null;
    }

    @Override
    public Class<?> next() {
        final Class result = c;
        c = c.getSuperclass();
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
