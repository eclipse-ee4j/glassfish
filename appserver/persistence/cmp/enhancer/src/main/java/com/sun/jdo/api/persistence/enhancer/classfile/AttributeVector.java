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
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * A list of attributes within a class file.
 * These lists occur in several places within a class file
 *    - at class level
 *    - at method level
 *    - at field level
 *    - at attribute level
 */
public class AttributeVector {

    /* Vector of ClassAttribute */
    private ClassAttribute attributes[] = null;

    /**
     * Returns the i'th attribute in the array
     */
    private ClassAttribute attrAt(int i) {
        return attributes[i];
    }

    /**
     * Construct an empty AttributeVector
     */
    public AttributeVector() { }

    /**
     * Add an element to the vector
     */
    public void addElement(ClassAttribute attr) {
        if (attributes == null)
            attributes = new ClassAttribute[1];
        else {
            ClassAttribute newAttributes[] = new ClassAttribute[attributes.length+1];
            System.arraycopy(attributes, 0, newAttributes, 0, attributes.length);
            attributes = newAttributes;
        }
        attributes[attributes.length-1] = attr;
    }

    public Enumeration elements() {
        class AttributeVectorEnumeration implements Enumeration {
            private ClassAttribute[] attributes;
            private int current = 0;

            AttributeVectorEnumeration(ClassAttribute attrs[]) {
                attributes = attrs;
            }

            public boolean hasMoreElements() {
                return attributes != null && current < attributes.length;
            }
            public Object nextElement() {
                if (!hasMoreElements())
                    throw new NoSuchElementException();
                return attributes[current++];
            }
        }

        return new AttributeVectorEnumeration(attributes);
    }

    /**
     * Look for an attribute of a specific name
     */
    public ClassAttribute findAttribute(String attrName) {
        Enumeration e = elements();
        while (e.hasMoreElements()) {
            ClassAttribute attr = (ClassAttribute) e.nextElement();
            if (attr.attrName().asString().equals(attrName))
                return attr;
        }
        return null;
    }

    /**
     * General attribute reader
     */
    static AttributeVector readAttributes(
        DataInputStream data, ConstantPool constantPool)
            throws IOException {
        AttributeVector attribs = new AttributeVector();
        int n_attrs = data.readUnsignedShort();
        while (n_attrs-- > 0) {
            attribs.addElement(ClassAttribute.read(data, constantPool));
        }
        return attribs;
    }

    /**
     * ClassMethod attribute reader
     */
    static AttributeVector readAttributes(
        DataInputStream data, CodeEnv codeEnv)
            throws IOException {
        AttributeVector attribs = new AttributeVector();
        int n_attrs = data.readUnsignedShort();
        while (n_attrs-- > 0) {
            attribs.addElement(ClassAttribute.read(data, codeEnv));
        }
        return attribs;
    }

    /**
     * Write the attributes to the output stream
     */
    void write(DataOutputStream out) throws IOException {
        if (attributes == null) {
            out.writeShort(0);
        } else {
            out.writeShort(attributes.length);
            for (int i=0; i<attributes.length; i++)
                attributes[i].write(out);
        }
    }

    /**
     * Print a description of the attributes
     */
    void print(PrintStream out, int indent) {
        if (attributes != null) {
            for (int i=0; i<attributes.length; i++)
                attributes[i].print(out, indent);
        }
    }

    /**
     * Print a brief summary of the attributes
     */
    void summarize() {
        System.out.println((attributes == null ? 0 : attributes.length) +
            " attributes");//NOI18N
    }

}

