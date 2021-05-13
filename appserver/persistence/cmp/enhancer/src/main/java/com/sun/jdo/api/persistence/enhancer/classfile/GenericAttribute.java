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

package com.sun.jdo.api.persistence.enhancer.classfile;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * GenericAttribute represents a class attribute in a class file which
 * is not recognized as any supported attribute type.  These attributes
 * are maintained, and are not modified in any way.
 */
public class GenericAttribute extends ClassAttribute {

    /* The bytes of the attribute following the name */
    byte attributeBytes[];

    /* public accessors */

    /**
     * constructor
     */
    public GenericAttribute(ConstUtf8 attrName, byte attrBytes[]) {
        super(attrName);
        attributeBytes = attrBytes;
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(attrName().getIndex());
        out.writeInt(attributeBytes.length);
        out.write(attributeBytes, 0, attributeBytes.length);
    }

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println("Generic Attribute(" + attrName().asString() + "): " +//NOI18N
            Integer.toString(attributeBytes.length) +
            " in length");//NOI18N
        for (int i=0; i<attributeBytes.length; i++) {
            if ((i % 16) == 0) {
                if (i != 0)
                    out.println();
                out.print(i + " :");//NOI18N
            }
            out.print(" " + Integer.toString((attributeBytes[i] & 0xff), 16));//NOI18N
        }
        out.println();
    }
}
