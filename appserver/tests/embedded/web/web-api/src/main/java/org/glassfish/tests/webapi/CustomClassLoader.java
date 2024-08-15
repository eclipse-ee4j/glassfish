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

package org.glassfish.tests.webapi;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;

public class CustomClassLoader extends ClassLoader {

    public CustomClassLoader(){
        super(CustomClassLoader.class.getClassLoader());
    }

    public Class loadClass(String className) throws ClassNotFoundException {
         return findClass(className);
    }

    public Class findClass(String className){
        byte classByte[];
        Class result=null;
        result = (Class)classes.get(className);
        if(result != null){
            return result;
        }

        try{
            return findSystemClass(className);
        }catch(Exception e){
        }

        try{
            String classPath =
                   ((String)ClassLoader.getSystemResource(
                           className.replace('.',File.separatorChar)+".class").getFile()).substring(1);
            classByte = loadClassData(classPath);
            result = defineClass(className,classByte,0,classByte.length,null);
            classes.put(className,result);
            return result;
        }catch(Exception e){
            return null;
        }
    }

    private byte[] loadClassData(String className) throws IOException{

        File f ;
        f = new File(className);
        int size = (int)f.length();
        byte buff[] = new byte[size];
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        dis.readFully(buff);
        dis.close();
        return buff;

    }

    private Hashtable classes = new Hashtable();

}
