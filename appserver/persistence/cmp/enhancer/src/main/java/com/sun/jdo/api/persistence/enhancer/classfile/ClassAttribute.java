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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * An abstract base class for the attributes within a class file
 */
public abstract class ClassAttribute implements VMConstants {

    /* The name of the attribute */
    private ConstUtf8 attributeName;

    /**
     * Returns the name of the attribute
     */
    public ConstUtf8 attrName() {
        return attributeName;
    }

    /**
     * Constructor
     */
    ClassAttribute(ConstUtf8 theAttrName) {
        attributeName = theAttrName;
    }

    /**
     * General attribute reader
     */
    static ClassAttribute read(DataInputStream data, ConstantPool pool)
        throws IOException {

        ClassAttribute attr = null;
        int attrNameIndex = data.readUnsignedShort();
        ConstUtf8 attrName8 = (ConstUtf8) pool.constantAt(attrNameIndex);
        String attrName = attrName8.asString();
        int attrLength = data.readInt();

        if (attrName.equals(CodeAttribute.expectedAttrName)) {
            /* The old style code attribute reader uses more memory and
     cpu when the instructions don't need to be examined than the
     new deferred attribute reader.  We may at some point decide that
     we want to change the default based on the current situation
     but for now we will just use the deferred reader in all cases. */
            if (true) {
                attr = CodeAttribute.read(attrName8, attrLength, data, pool);
            } else {
                attr = CodeAttribute.read(attrName8, data, pool);
            }
        }
        else if (attrName.equals(SourceFileAttribute.expectedAttrName)) {
            attr = SourceFileAttribute.read(attrName8, data, pool);
        }
        else if (attrName.equals(ConstantValueAttribute.expectedAttrName)) {
            attr = ConstantValueAttribute.read(attrName8, data, pool);
        }
        else if (attrName.equals(ExceptionsAttribute.expectedAttrName)) {
            attr = ExceptionsAttribute.read(attrName8, data, pool);
        }
        else if (attrName.equals(AnnotatedClassAttribute.expectedAttrName)) {
            attr = AnnotatedClassAttribute.read(attrName8, data, pool);
        }
        else {
            /* Unrecognized method attribute */
            byte attrBytes[] = new byte[attrLength];
            data.readFully(attrBytes);
            attr = new GenericAttribute (attrName8, attrBytes);
        }

        return attr;
    }

    /*
     * CodeAttribute attribute reader
     */

    static ClassAttribute read(DataInputStream data, CodeEnv env)
        throws IOException {
        ClassAttribute attr = null;
        int attrNameIndex = data.readUnsignedShort();
        ConstUtf8 attrName8 = (ConstUtf8) env.pool().constantAt(attrNameIndex);
        String attrName = attrName8.asString();
        int attrLength = data.readInt();

        if (attrName.equals(LineNumberTableAttribute.expectedAttrName)) {
            attr = LineNumberTableAttribute.read(attrName8, data, env);
        }
        else if (attrName.equals(LocalVariableTableAttribute.expectedAttrName)) {
            attr = LocalVariableTableAttribute.read(attrName8, data, env);
        }
        else if (attrName.equals(AnnotatedMethodAttribute.expectedAttrName)) {
            attr = AnnotatedMethodAttribute.read(attrName8, data, env);
        }
        //@olsen: fix 4467428, added support for synthetic code attribute
        else if (attrName.equals(SyntheticAttribute.expectedAttrName)) {
            attr = SyntheticAttribute.read(attrName8, data, env.pool());
        }
        else {
            /* Unrecognized method attribute */
            byte attrBytes[] = new byte[attrLength];
            data.readFully(attrBytes);
            attr = new GenericAttribute (attrName8, attrBytes);
        }

        return attr;
    }

    /**
     * Write the attribute to the output stream
     */
    abstract void write(DataOutputStream out) throws IOException;

    /**
     * Print a description of the attribute to the print stream
     */
    abstract void print(PrintStream out, int indent);
}

