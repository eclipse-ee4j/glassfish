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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.WildcardType;
import java.lang.reflect.TypeVariable;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class TypeVisitor<T,P> {

    public final T visit(Type t, P param) {
        assert t != null;

        if (t instanceof Class) {
            return onClass((Class) t, param);
        }
        if (t instanceof ParameterizedType) {
            return onParameterizdType((ParameterizedType) t, param);
        }
        if (t instanceof GenericArrayType) {
            return onGenericArray((GenericArrayType) t, param);
        }
        if (t instanceof WildcardType) {
            return onWildcard((WildcardType) t, param);
        }
        if (t instanceof TypeVariable) {
            return onVariable((TypeVariable) t, param);
        }

        // covered all the cases
        assert false;
        throw new IllegalArgumentException();
    }


    protected abstract T onClass(Class c, P param);

    protected abstract T onParameterizdType(ParameterizedType p, P param);

    protected abstract T onGenericArray(GenericArrayType g, P param);

    protected abstract T onVariable(TypeVariable v, P param);

    protected abstract T onWildcard(WildcardType w, P param);
}
