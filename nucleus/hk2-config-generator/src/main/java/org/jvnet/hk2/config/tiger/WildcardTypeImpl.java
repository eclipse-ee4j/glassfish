/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.jvnet.hk2.config.tiger;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

/**
 * @author Kohsuke Kawaguchi
 */
final class WildcardTypeImpl implements WildcardType {

    private final Type[] ub;
    private final Type[] lb;

    public WildcardTypeImpl(Type[] ub, Type[] lb) {
        this.ub = ub;
        this.lb = lb;
    }


    @Override
    public Type[] getUpperBounds() {
        return ub;
    }


    @Override
    public Type[] getLowerBounds() {
        return lb;
    }


    @Override
    public int hashCode() {
        return Arrays.hashCode(lb) ^ Arrays.hashCode(ub);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WildcardType) {
            WildcardType that = (WildcardType) obj;
            return Arrays.equals(that.getLowerBounds(), lb) && Arrays.equals(that.getUpperBounds(), ub);
        }
        return false;
    }
}
