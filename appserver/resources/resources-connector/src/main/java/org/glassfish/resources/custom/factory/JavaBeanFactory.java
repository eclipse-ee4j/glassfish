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


import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;


public class JavaBeanFactory implements Serializable, ObjectFactory {

    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        Reference reference = (Reference) obj;

        try {
            Class beanClass;
            try {
                beanClass = Thread.currentThread().getContextClassLoader().loadClass(reference.getClassName());
            } catch (ClassNotFoundException e) {
                throw new NamingException("Unable to load class : " + reference.getClassName());
            }

            Object bean = beanClass.newInstance();

            BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
            PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();

            Enumeration enumeration = reference.getAll();

            while (enumeration.hasMoreElements()) {

                RefAddr ra = (RefAddr) enumeration.nextElement();
                String propertyName = ra.getType();
                String value = (String) ra.getContent();

                for (PropertyDescriptor desc : properties) {
                    if (desc.getName().equals(propertyName)) {
                        String type = desc.getPropertyType().getName();
                        Object result = null;

                        if(type != null){
                            type = type.toUpperCase(Locale.getDefault());
                            if(type.endsWith("INT") || type.endsWith("INTEGER")){
                                result =  Integer.valueOf(value);
                            } else if (type.endsWith("LONG")){
                                result = Long.valueOf(value);
                            } else if(type.endsWith("DOUBLE")){
                                result = Double.valueOf(value);
                            } else if(type.endsWith("FLOAT") ){
                                result = Float.valueOf(value);
                            } else if(type.endsWith("CHAR") || type.endsWith("CHARACTER")){
                                result = value.charAt(0);
                            } else if(type.endsWith("SHORT")){
                                result = Short.valueOf(value);
                            } else if(type.endsWith("BYTE")){
                                result = Byte.valueOf(value);
                            } else if(type.endsWith("BOOLEAN")){
                                result = Boolean.valueOf(value);
                            } else if(type.endsWith("STRING")){
                                result = value;
                            }
                        } else {
                            throw new NamingException("Unable to find the type of property : " + propertyName);
                        }

                        Method setter = desc.getWriteMethod();
                        if (setter != null) {
                            setter.invoke(bean, result);
                        } else {
                            throw new NamingException
                                    ("Unable to find the setter method for property : "+ propertyName);
                        }
                        break;
                    }
                }
            }
            return bean;
        } catch(Exception e){
            NamingException ne = new NamingException("Unable to instantiate JavaBean");
            ne.setRootCause(e);
            throw ne;
        }
    }
}
