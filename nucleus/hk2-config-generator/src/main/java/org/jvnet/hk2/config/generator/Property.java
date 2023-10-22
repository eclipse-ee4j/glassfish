/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.jvnet.hk2.config.generator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;

import java.lang.annotation.Annotation;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.Element;

/**
 * Represents configurable property of the component.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class Property {

    private Boolean isKey = null;

    /**
     * Field/method declaration of this property.
     * Used to read annotations on this property.
     */
    abstract javax.lang.model.element.Element decl();

    /**
     * Name used as a seed for the XML name of this property.
     * This is the property name / field name.
     */
    String seedName() {
        return decl().getSimpleName().toString();
    }

    /**
     * The type of the property.
     */
    abstract TypeMirror type();

    abstract void assign(JVar $target, JBlock block, JExpression rhs);

    <A extends Annotation> A getAnnotation(Class<A> a) {
        return decl().getAnnotation(a);
    }


    /**
     * Does this property have {@link Attribute#key()} or {@link Element#key()}.
     */
    final boolean isKey() {
        if (isKey == null) {
            isKey = _isKey();
        }
        return isKey;
    }


    private boolean _isKey() {
        Element e = getAnnotation(Element.class);
        if (e != null && e.key()) {
            return true;
        }

        Attribute a = getAnnotation(Attribute.class);
        return a != null && a.key();
    }


    String inferName(String name) {
        if (name.isEmpty()) {
            name = Dom.convertName(seedName());
        }
        return name;
    }

    /**
     * Property that consists of a set/add/get method.
     */
    static final class Method extends Property {
        final ExecutableElement method;
        /**
         * True if this property is based on the getter method. False if the setter/adder.
         */
        final boolean getter;

        public Method(ExecutableElement method) {
            this.method = method;
            this.getter = !method.getReturnType().toString().equals("void");
        }

        @Override
        javax.lang.model.element.Element decl() {
            return method;
        }

        @Override
        TypeMirror type() {
            if (getter) {
                return method.getReturnType();
            }
            return method.getParameters().iterator().next().asType();
        }

        @Override
        void assign(JVar $target, JBlock block, JExpression rhs) {
            block.invoke($target, method.getSimpleName().toString()).arg(rhs);
        }
    }

    /**
     * Property that consists of a field.
     */
    static final class Field extends Property {
        final VariableElement field;

        public Field(VariableElement field) {
            this.field = field;
        }

        @Override
        javax.lang.model.element.Element decl() {
            return field;
        }

        @Override
        TypeMirror type() {
            return field.asType();
        }

        @Override
        void assign(JVar $target, JBlock block, JExpression rhs) {
            block.assign($target.ref(field.getSimpleName().toString()), rhs);
        }
    }
}
