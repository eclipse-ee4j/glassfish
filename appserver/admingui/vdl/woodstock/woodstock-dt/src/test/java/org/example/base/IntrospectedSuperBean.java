/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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
package org.example.base;

import jakarta.el.MethodExpression;

/**
 * This bean provides two events, introspectedAction01 and introspectedAction02,
 * the second of which is associated with the property
 * introspectedListener2Expression.
 */
public class IntrospectedSuperBean {

    private MethodExpression introspectedListener2Expression;

    public MethodExpression getIntrospectedListener2Expression() {
        return this.introspectedListener2Expression;
    }

    public void setIntrospectedListener2Expression(MethodExpression expr) {
        this.introspectedListener2Expression = expr;
    }

    public void addIntrospectedActionListener01(IntrospectedActionListener01 actionListener) {
        // ...
    }

    public void removeIntrospectedActionListener01(IntrospectedActionListener01 actionListener) {
        // ...
    }

    public void addIntrospectedActionListener02(IntrospectedActionListener02 actionListener) {
        // ...
    }

    public void removeIntrospectedActionListener02(IntrospectedActionListener02 actionListener) {
        // ...
    }
}
