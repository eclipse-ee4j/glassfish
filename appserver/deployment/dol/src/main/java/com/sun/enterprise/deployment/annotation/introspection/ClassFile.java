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

package com.sun.enterprise.deployment.annotation.introspection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * This class is encapsulating binary .class file information as
 * defined at
 * http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html
 *
 * This is used by the annotation frameworks to quickly scan .class files
 * for the presence of annotations. This avoid the annotation framework having
 * to load each .class file in the class loader.
 *
 * The bytecode to be scanned could come from either a File (which lends itself
 * easily to a channel) or from a JAR (which works better with a byte array).  So
 * we provide two variants of the containsAnnotation method, one for each.
 *
 * @author Jerome Dochez
 */
public class ClassFile {

    ByteBuffer header;
    ConstantPoolInfo constantPoolInfo;

    /** Creates a new instance of ClassFile */
    public ClassFile(ConstantPoolInfo poolInfo) {
        header = ByteBuffer.allocate(12000);
        constantPoolInfo = poolInfo;
    }

    /**
     * Read the input channel and initialize instance data
     * structure.
     */public boolean containsAnnotation(ReadableByteChannel in, long size) throws IOException {
        header.clear();
        if (size!=-1 && size>header.capacity()) {
            // time to expand...
            header = ByteBuffer.allocate((int) size);
        }
        long read = (long) in.read(header);
        if (size!=-1 && read!=size) {
            return false;
        }
        header.rewind();
        return containsAnnotation(header);
    }

    /**
     * Read the input channel and initialize instance data
     * structure.
     */
    public boolean containsAnnotation(final byte[] bytecode) throws IOException {
        final ByteBuffer wrappedHeader = ByteBuffer.wrap(bytecode);
        return containsAnnotation(wrappedHeader);
    }

    private boolean containsAnnotation(final ByteBuffer headerBuffer) throws IOException {


        /**
         * this is the .class file layout
         *
        ClassFile {
            u4 magic;
            u2 minor_version;
            u2 major_version;
            u2 constant_pool_count;
            cp_info constant_pool[constant_pool_count-1];
            u2 access_flags;
            u2 this_class;
            u2 super_class;
            u2 interfaces_count;
            u2 interfaces[interfaces_count];
            u2 fields_count;
            field_info fields[fields_count];
            u2 methods_count;
            method_info methods[methods_count];
            u2 attributes_count;
            attribute_info attributes[attributes_count];
        }
         **/

        if (headerBuffer.getInt()!=magic) {
            return false;
        }

        headerBuffer.getShort(); // major version
        headerBuffer.getShort(); // minor version
        int constantPoolSize = headerBuffer.getShort();

        return constantPoolInfo.containsAnnotation(constantPoolSize, headerBuffer);

    }

    public ConstantPoolInfo    constantPool[];
    public short            accessFlags;
    public ConstantPoolInfo    thisClass;
    public ConstantPoolInfo    superClass;
    public ConstantPoolInfo    interfaces[];
    /**
     * bunch of stuff I really don't care too much for now.
     *
    FieldInfo            fields[];
    MethodInfo            methods[];
    AttributeInfo        attributes[];
     */

    private static final int magic = 0xCAFEBABE;

    public static final int ACC_PUBLIC         = 0x1;
    public static final int ACC_PRIVATE     = 0x2;
    public static final int ACC_PROTECTED     = 0x4;
    public static final int ACC_STATIC         = 0x8;
    public static final int ACC_FINAL         = 0x10;
    public static final int ACC_SYNCHRONIZED     = 0x20;
    public static final int ACC_THREADSAFE     = 0x40;
    public static final int ACC_TRANSIENT     = 0x80;
    public static final int ACC_NATIVE         = 0x100;
    public static final int ACC_INTERFACE     = 0x200;
    public static final int ACC_ABSTRACT     = 0x400;

}
