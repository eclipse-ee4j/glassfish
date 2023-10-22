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

import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;

import java.util.Collection;
import java.util.Locale;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor6;

/**
 * Defines several type arithemetic operations.
 * @author Kohsuke Kawaguchi
 */
class TypeMath {

    protected final ProcessingEnvironment env;

    /**
     * Given a declaration X and mirror Y, finds the parametrization of Z=X&lt;...> such that
     * Y is assignable to Z.
     */
    final SimpleTypeVisitor6<TypeMirror, TypeElement> baseClassFinder = new SimpleTypeVisitor6<TypeMirror, TypeElement>() {

        @Override
        public TypeMirror visitDeclared(DeclaredType t, TypeElement sup) {

            TypeMirror r = onDeclaredType(t, sup);
            if (r != null) {
                return r;
            }

            Element e = t.asElement();
            switch (e.getKind()) {
                case CLASS: {
                    // otherwise recursively apply super class and base types
                    TypeMirror sc = ((TypeElement) e).getSuperclass();
                    if (!TypeKind.NONE.equals(sc.getKind())) {
                        r = visitDeclared((DeclaredType) sc, sup);
                    }
                    if (r != null) {
                        return r;
                    }
                }
            }
            return null;
        }

        @Override
        protected TypeMirror defaultAction(TypeMirror e, TypeElement typeElement) {
            return null;
        }

        private TypeMirror onDeclaredType(DeclaredType t, TypeElement sup) {
            // t = sup<...>
            if (t.asElement().equals(sup)) {
                return t;
            }

            for (TypeMirror i : env.getTypeUtils().directSupertypes(t)) {
                TypeMirror r = visitDeclared((DeclaredType) i, sup);
                if (r != null) {
                    return r;
                }
            }

            return null;
        }

        @Override
        public TypeMirror visitTypeVariable(TypeVariable t, TypeElement sup) {
            // we are checking if T (declared as T extends A&B&C) is assignable to sup.
            // so apply bounds recursively.
            for (TypeMirror r : ((TypeParameterElement) t.asElement()).getBounds()) {
                TypeMirror m = visit(r, sup);
                if (m != null) {
                    return m;
                }
            }
            return null;
        }

        @Override
        public TypeMirror visitWildcard(WildcardType type, TypeElement sup) {
            // we are checking if T (= ? extends A&B&C) is assignable to sup.
            // so apply bounds recursively.
            return visit(type.getExtendsBound(), sup);
        }
    };
    /**
     * Adapts the string expression into the expression of the given type.
     */
    final SimpleTypeVisitor6<JExpression, JExpression> simpleValueConverter = new SimpleTypeVisitor6<JExpression, JExpression>() {

        @Override
        public JExpression visitPrimitive(PrimitiveType type, JExpression param) {
            String kind = type.getKind().toString();
            return JExpr.invoke("as" + kind.charAt(0) + kind.substring(1).toLowerCase(Locale.ENGLISH)).arg(param);
        }

        @Override
        public JExpression visitDeclared(DeclaredType type, JExpression param) {
            String qn = ((TypeElement) type.asElement()).getQualifiedName().toString();
            if (qn.equals("java.lang.String"))
             {
                return param;   // no conversion needed for string
            }
            // return JExpr.invoke("as"+type.getDeclaration().getSimpleName()).arg(param);
            throw new UnsupportedOperationException();
        }

        @Override
        protected JExpression defaultAction(TypeMirror e, JExpression jExpression) {
            throw new UnsupportedOperationException();
        }
    };

    public TypeMath(ProcessingEnvironment env) {
        this.env = env;
    }

    TypeMirror isCollection(TypeMirror t) {
        TypeMirror collectionType = baseClassFinder.visit(t, env.getElementUtils().getTypeElement(Collection.class.getName()));
        if (collectionType != null) {
            DeclaredType d = (DeclaredType) collectionType;
            return d.getTypeArguments().iterator().next();
        } else {
            return null;
        }
    }
}
