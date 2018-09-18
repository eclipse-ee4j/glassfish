/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.tools.apt;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.HashSet;
import java.util.Set;

/**
 * Recursively decent all the super classes and super interfaces.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class TypeHierarchyVisitor<P> {
    /**
     * {@link DeclaredType}s whose contracts are already checked.
     */
    protected final Set<TypeElement> visited = new HashSet<TypeElement>();

    protected void check(TypeElement d, P param) {
        if (ElementKind.CLASS.equals(d.getKind()))
            checkClass(d, param);
        else
            checkInterface(d, param);
    }

    protected void checkInterface(TypeElement id, P param) {
        checkSuperInterfaces(id,param);
    }

    protected void checkClass(TypeElement cd, P param) {
        TypeMirror sc = cd.getSuperclass();
        if (sc.getKind().equals(TypeKind.NONE))
            check((TypeElement) ((DeclaredType) sc).asElement(), param);

        checkSuperInterfaces(cd,param);
    }

    protected void checkSuperInterfaces(TypeElement d, P param) {
        for (TypeMirror intf : d.getInterfaces()) {
            TypeElement i = (TypeElement) ((DeclaredType) intf).asElement();
            if(visited.add(i)) {
                check(i,param);
            }
        }
    }
}
