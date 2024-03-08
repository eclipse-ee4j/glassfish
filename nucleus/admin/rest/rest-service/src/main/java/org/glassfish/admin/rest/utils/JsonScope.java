/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.utils;

import java.util.Stack;

/**
 * @author tmoreau
 */
public class JsonScope {

    private String scope;
    private Stack<ScopeElement> scopeStack = new Stack<ScopeElement>();

    public JsonScope() {
    }

    public void beginObjectAttr(String name) {
        if (!scopeStack.isEmpty() && scopeStack.peek() instanceof ArrayAttr) {
            if (!((ArrayAttr) (scopeStack.peek())).inElement()) {
                throw new IllegalStateException("Not currently in an array element");
            }
        }
        scopeStack.push(new ObjectAttr(name));
        computeScope();
    }

    public void endObjectAttr() {
        if (!(scopeStack.peek() instanceof ObjectAttr)) {
            throw new IllegalStateException("Not currently in an object attribute");
        }
        scopeStack.pop();
        computeScope();
    }

    public void beginArrayAttr(String name) {
        if (!scopeStack.isEmpty() && scopeStack.peek() instanceof ArrayAttr) {
            if (!((ArrayAttr) (scopeStack.peek())).inElement()) {
                throw new IllegalStateException("Not currently in an array element");
            }
        }
        scopeStack.push(new ArrayAttr(name));
        computeScope();
    }

    public void endArrayAttr() {
        if (!(scopeStack.peek() instanceof ArrayAttr)) {
            throw new IllegalStateException("Not currently in an array attribute");
        }
        scopeStack.pop();
        computeScope();
    }

    public void beginArrayElement() {
        if (!(scopeStack.peek() instanceof ArrayAttr)) {
            throw new IllegalStateException("Not currently in an array attribute");
        }
        ((ArrayAttr) (scopeStack.peek())).beginElement();
        computeScope();
    }

    public void endArrayElement() {
        if (!(scopeStack.peek() instanceof ArrayAttr)) {
            throw new IllegalStateException("Not currently in an array attribute");
        }
        ((ArrayAttr) (scopeStack.peek())).endElement();
        computeScope();
    }

    private void computeScope() {
        if (scopeStack.isEmpty()) {
            this.scope = null;
            return;
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (ScopeElement e : scopeStack) {
            if (!first) {
                sb.append(".");
            } else {
                first = false;
            }
            sb.append(e.toString());
        }
        this.scope = sb.toString();
    }

    @Override
    public String toString() {
        return this.scope;
    }

    private interface ScopeElement {
    }

    private static class ObjectAttr implements ScopeElement {
        private String name;

        private ObjectAttr(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    private static class ArrayAttr implements ScopeElement {
        private boolean inElement;
        private String name;
        int index;

        private ArrayAttr(String name) {
            this.name = name;
            this.inElement = false;
            this.index = -1;
        }

        private boolean inElement() {
            return this.inElement;
        }

        private void beginElement() {
            if (this.inElement) {
                throw new IllegalStateException("Already in an array element");
            }
            this.inElement = true;
            this.index++;
        }

        private void endElement() {
            if (!this.inElement) {
                throw new IllegalStateException("Not in an array element");
            }
            this.inElement = false;
        }

        @Override
        public String toString() {
            if (inElement) {
                return this.name + "[" + index + "]";
            } else {
                return this.name;
            }
        }
    }
}
