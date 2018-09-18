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

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.util.LocalStringManagerImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;

public class FileClassLoader extends ClassLoader {
    String codebase;
    Hashtable cache = new Hashtable();

    private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(FileClassLoader.class);

    public FileClassLoader(String codebase)
    {
	this.codebase = codebase;
    }

    private byte[] loadClassData(String name)
	throws IOException
    {
        // load the class data from the codebase
	String sep = System.getProperty("file.separator");
	String c = name.replace('.', sep.charAt(0)) + ".class";
	File file = new File(codebase + sep + c);
	if (!file.exists()) {
	    File wf = new File(codebase + sep + "WEB-INF" + sep + "classes" + sep + c);
	    if (wf.exists()) {
		file = wf;
	    }
	}
	FileInputStream fis = null;
        byte[] buf = null;
        try {
          fis = new FileInputStream(file);
          int avail = fis.available();
          buf = new byte[avail];
          fis.read(buf);
        } finally {
          if (fis != null)
            fis.close();
        }
	return buf;
    }
    
    String getClassName(File f) throws IOException, ClassFormatError {
	FileInputStream fis = null;
        byte[] buf = null;
        int avail = 0;
        try {
          fis = new FileInputStream(f);
          avail = fis.available();
          buf = new byte[avail];
          fis.read(buf);
        } finally {
          if (fis != null)
            fis.close();
        }
	Class c = super.defineClass(null, buf, 0, avail);
	return c.getName();
    }

    /**
     * @exception ClassNotFoundException if class load fails
     */
    public synchronized Class loadClass(String name, boolean resolve) 
	throws ClassNotFoundException
    {
        Class c = (Class)cache.get(name);
        if (c == null) {
	    try { 
                byte data[] = loadClassData(name);
                c = defineClass(null,data, 0, data.length);
                if( !name.equals(c.getName()) ) {
                    throw new ClassNotFoundException(localStrings.getLocalString("classloader.wrongpackage", "", new Object[] { name, c.getName() }));
                }
	    }
	    catch ( Exception ex ) {
		// Try to use default classloader. If this fails, 
		// then a ClassNotFoundException is thrown.
		c = Class.forName(name);
	    }
            cache.put(name, c);
        }
        if (resolve)
            resolveClass(c);
        return c;
    }

    public String toString()
    {
	return "FileClassLoader: Codebase = "+codebase+"\n";
    }
}
