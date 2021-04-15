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

package org.glassfish.admin.rest.testing;

class Indenter {

    public static final int INDENT = 2;
    private String indentPerLevel;
    private int level = 0;
    private String indent;

    Indenter() {
        computeIndentPerLevel();
        computeIndent();
    }

    String getIndent() {
        return indent;
    }

    void indent() {
        level++;
        computeIndent();
    }

    void undent() {
        level--;
        computeIndent();
    }

    private void computeIndentPerLevel() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < INDENT; i++) {
            sb.append(" ");
        }
        indentPerLevel = sb.toString();
    }

    private void computeIndent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append(indentPerLevel);
        }
        indent = sb.toString();
    }
}
