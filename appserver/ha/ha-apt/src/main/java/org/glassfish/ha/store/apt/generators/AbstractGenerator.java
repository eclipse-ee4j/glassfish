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

package org.glassfish.ha.store.apt.generators;

import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.TypeMirror;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * @author Mahesh Kannan
 */
public class AbstractGenerator {

    private int indent;

    private String space = "";

    protected Map<String, TypeMirror> types =
            new HashMap<String, TypeMirror>();

    protected Set<String> attrNames = new HashSet<String>();

    protected void increaseIndent() {
        indent++;
        space += "\t";
    }

    protected void decreaseIndent() {
        indent--;
        space = space.substring(1);
    }

    protected void println(String msg) {
        System.out.println(space + msg);
    }

    protected void print(String msg) {
        System.out.print(space + msg);
    }

    protected void println() {
        System.out.println(space);
    }

    protected void addAttribute(String attrName, TypeMirror decl) {
        attrNames.add(attrName);
        types.put(attrName, decl);
    }

    protected static String getWrapperType(TypeMirror type) {
        String result = type.toString();
        if (type instanceof PrimitiveType) {
            result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        }
        /*
        int index = result.lastIndexOf(' ');
        result = result.substring(0, index);

        if (type instanceof PrimitiveType) {
            result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        }

        int ltIndex = result.indexOf('<');

        return (ltIndex == -1) ? result : result.substring(0, ltIndex);
        */
        return result;
    }

}
