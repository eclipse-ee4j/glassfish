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

package org.glassfish.apf.impl;

import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;

/**
 * This class represent a stack element as the AnnotationContext
 * will use to store the visited AnnotatedElements.
 *
 * @author Jerome Dochez
 */
class StackElement {

    private final ElementType type;
    private final AnnotatedElement element;

    /** Creates a new instance of StackElement */
    public StackElement(ElementType type, AnnotatedElement element) {
        this.type = type;
        this.element = element;
    }

    ElementType getElementType() {
        return type;
    }

    AnnotatedElement getAnnotatedElement() {
        return element;
    }

}
