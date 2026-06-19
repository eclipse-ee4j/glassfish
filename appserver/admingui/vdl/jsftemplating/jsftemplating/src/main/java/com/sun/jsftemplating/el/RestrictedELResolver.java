/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.jsftemplating.el;

import jakarta.el.ELClass;
import jakarta.el.ELContext;
import jakarta.el.ELResolver;

import java.util.Objects;

/**
 * This {@link ELResolver} exists to restrict access to the next EL expressions:
 *
 * <ul>
 * <li>Static method calls</li>
 * <li>Constructor calls</li>
 * <li>The {@code getClass() instance method calls}</li>
 * <li>The {@code class} instance property</li>
 * <li>Static fields</li>
 * </ul>
 *
 * @author avpinchuk
 */
public class RestrictedELResolver extends ELResolver {

    @Override
    public Object getValue(ELContext elContext, Object base, Object property) {
        // Go to the next resolver in chain
        if (base == null) {
            return null;
        }
        // Disable static fields and 'class' instance property
        if (base instanceof ELClass || Objects.equals(property, "class")) {
            elContext.setPropertyResolved(true);
            throw new PropertyNotAllowedException();
        }
        return null;
    }

    @Override
    public Object invoke(ELContext elContext, Object base, Object method, Class<?>[] paramTypes, Object[] params) {
        // Go to the next resolver in chain
        if (base == null) {
            return null;
        }
        // Disable static methods, constructors and 'getClass()' method
        if (base instanceof ELClass || Objects.equals(method, "getClass")) {
            elContext.setPropertyResolved(true);
            throw new MethodNotAllowedException();
        }
        return null;
    }

    @Override
    public Class<?> getType(ELContext elContext, Object base, Object property) {
        return null;
    }

    @Override
    public void setValue(ELContext elContext, Object base, Object property, Object value) {
        // Do nothing
    }

    @Override
    public boolean isReadOnly(ELContext elContext, Object base, Object property) {
        return false;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext elContext, Object base) {
        return null;
    }
}
