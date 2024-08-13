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

package org.glassfish.resources.custom.factory;


import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

public class PrimitivesAndStringFactory implements Serializable, ObjectFactory {

    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        Reference ref = (Reference)obj;

        Enumeration<RefAddr> refAddrs = ref.getAll();
        String type = null;
        String value = null;
        while(refAddrs.hasMoreElements()){
            RefAddr addr = refAddrs.nextElement();
            String propName = addr.getType();

            type = ref.getClassName();

            if(propName.equalsIgnoreCase("value")){
                value = (String)addr.getContent();
            }
        }

        if(type != null && value != null){
            type = type.toUpperCase(Locale.getDefault());
            if(type.endsWith("INT") || type.endsWith("INTEGER")){
                return Integer.valueOf(value);
            } else if (type.endsWith("LONG")){
                return Long.valueOf(value);
            } else if(type.endsWith("DOUBLE")){
                return Double.valueOf(value);
            } else if(type.endsWith("FLOAT") ){
                return Float.valueOf(value);
            } else if(type.endsWith("CHAR") || type.endsWith("CHARACTER")){
                return value.charAt(0);
            } else if(type.endsWith("SHORT")){
                return Short.valueOf(value);
            } else if(type.endsWith("BYTE")){
                return Byte.valueOf(value);
            } else if(type.endsWith("BOOLEAN")){
                return Boolean.valueOf(value);
            } else if(type.endsWith("STRING")){
                return value;
            }else{
                throw new IllegalArgumentException("unknown type ["+type+"] ");
            }
        }else if (type == null){
            throw new IllegalArgumentException("type cannot be null");
        }else{
            throw new IllegalAccessException("value cannot be null");
        }
    }
}
