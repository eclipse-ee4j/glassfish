/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.composite;

import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LRETURN;

/**
 * This enum encapsulates the metadata for primitives needed for generating fields, getters and setters
 *
 * @author jdlee
 */
enum Primitive {

    DOUBLE("D", DRETURN, DLOAD), FLOAT("F", FRETURN, FLOAD), LONG("J", LRETURN, LLOAD), SHORT("S", IRETURN, ILOAD),
    INT("I", IRETURN, ILOAD),
    //        CHAR   ("C", IRETURN, ILOAD),
    BYTE("B", IRETURN, ILOAD), BOOLEAN("Z", IRETURN, ILOAD);

    private final int returnOpcode;
    private final int setOpcode;
    private final String internalType;

    Primitive(String type, int returnOpcode, int setOpcode) {
        this.internalType = type;
        this.returnOpcode = returnOpcode;
        this.setOpcode = setOpcode;
    }

    public int getReturnOpcode() {
        return returnOpcode;
    }

    public int getSetOpCode() {
        return setOpcode;
    }

    public String getInternalType() {
        return internalType;
    }

    static Primitive getPrimitive(String type) {
        if ("S".equals(type) || "short".equals(type)) {
            return SHORT;
        } else if ("J".equals(type) || "long".equals(type)) {
            return LONG;
        } else if ("I".equals(type) || "int".equals(type)) {
            return INT;
        } else if ("F".equals(type) || "float".equals(type)) {
            return FLOAT;
        } else if ("D".equals(type) || "double".equals(type)) {
            return DOUBLE;
            //            } else if ("C".equals(type) || "char".equals(type)) {
            //                return CHAR;
        } else if ("B".equals(type) || "byte".equals(type)) {
            return BYTE;
        } else if ("Z".equals(type) || "boolean".equals(type)) {
            return BOOLEAN;
        } else {
            throw new RuntimeException("Unknown primitive type: " + type);
        }
    }
}
