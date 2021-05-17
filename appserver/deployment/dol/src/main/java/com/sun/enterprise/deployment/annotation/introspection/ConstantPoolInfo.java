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

/*
 * ConstantPoolInfo.java
 *
 * Created on May 24, 2005, 4:43 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.enterprise.deployment.annotation.introspection;

import com.sun.enterprise.deployment.util.DOLUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
/**
 *
 * @author dochez
 */
public class ConstantPoolInfo {

    byte[] bytes = new byte[Short.MAX_VALUE];
    private AnnotationScanner scanner = null;


    public ConstantPoolInfo(AnnotationScanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Read the input channel and initialize instance data
     * structure.
     */
    public boolean containsAnnotation(int constantPoolSize, final ByteBuffer buffer) throws IOException {

        for (int i=1;i<constantPoolSize;i++) {
            final byte type = buffer.get();
            switch(type) {
                case ASCIZ:
                case UNICODE:
                    final short length = buffer.getShort();
                    if (length<0) {
                        return true;
                    }
                    buffer.get(bytes, 0, length);
                    /* to speed up the process, I am comparing the first few
                     * bytes to Ljava since all annotations are in the java
                     * package, the reduces dramatically the number or String
                     * construction
                     */
                    if (bytes[0]=='L' && bytes[1]=='j' && bytes[2]=='a') {
                        String stringValue;
                        if (type==ASCIZ) {
                            stringValue = new String(bytes, 0, length,"US-ASCII");
                        } else {
                            stringValue = new String(bytes, 0, length);
                        }
                        if (scanner.isAnnotation(stringValue)) {
                            return true;
                        }
                    }
                    break;
                case CLASS:
                case STRING:
                    buffer.getShort();
                    break;
                case FIELDREF:
                case METHODREF:
                case INTERFACEMETHODREF:
                case INTEGER:
                case FLOAT:
                    buffer.position(buffer.position()+4);
                    break;
                case LONG:
                case DOUBLE:
                    buffer.position(buffer.position()+8);
                    // for long, and double, they use 2 constantPool
                    i++;
                    break;
                case NAMEANDTYPE:
                    buffer.getShort();
                    buffer.getShort();
                    break;
                default:
                    DOLUtils.getDefaultLogger().severe("Unknow type constant pool " + type + " at position" + i);
                    break;
            }
        }
        return false;
    }


    public static final byte CLASS = 7;
    public static final int FIELDREF = 9;
    public static final int METHODREF = 10;
    public static final int STRING = 8;
    public static final int INTEGER = 3;
    public static final int FLOAT = 4;
    public static final int LONG = 5;
    public static final int DOUBLE = 6;
    public static final int INTERFACEMETHODREF = 11;
    public static final int NAMEANDTYPE = 12;
    public static final int ASCIZ = 1;
    public static final int UNICODE = 2;
}
