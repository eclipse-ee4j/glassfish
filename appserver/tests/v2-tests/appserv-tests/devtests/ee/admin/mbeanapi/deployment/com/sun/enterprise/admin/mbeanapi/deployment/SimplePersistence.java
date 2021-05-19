/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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
 * SimplePersistence.java
 *
 * Created on May 19, 2003, 10:37 AM
 */

package com.sun.enterprise.admin.mbeanapi.deployment;

import java.io.*;
import java.util.*;
import java.net.URL;

/**
 *
 * @author  bnevins
 */

public class SimplePersistence
{
    public SimplePersistence(Object o)
    {
        this(o, null, true);
    }

    //////////////////////////////////////////////////////////////////////////

    public SimplePersistence(Object o, boolean autoFlush)
    {
        this(o, null, autoFlush);
    }

    //////////////////////////////////////////////////////////////////////////

    public SimplePersistence(Object o, String fname)
    {
        this(o, fname, true);
    }

    //////////////////////////////////////////////////////////////////////////

    public SimplePersistence(Object o, String fname, boolean autoFlush)
    {
        persistenceFileRootName = fname;
        this.autoFlush = autoFlush;
        init(o);
        load();
    }

    //////////////////////////////////////////////////////////////////////////

    public SimplePersistence(File f)
    {
        this(f, true);
    }

    //////////////////////////////////////////////////////////////////////////

    public SimplePersistence(File f, boolean autoFlush)
    {
        persistenceFile =f;
        this.autoFlush = autoFlush;

        if(!persistenceFile.exists())
            store();

        load();
    }

    //////////////////////////////////////////////////////////////////////////

    public String getProperty(String key)
    {
        return props.getProperty(key);
    }

    //////////////////////////////////////////////////////////////////////////

    public Properties getProperties()
    {
        return props;
    }

    //////////////////////////////////////////////////////////////////////////

    public void setProperty(String key, String value)
    {
        props.setProperty(key, value);

        if(autoFlush)
            store();
    }

    //////////////////////////////////////////////////////////////////////////

    public void store()
    {
        try
        {
            OutputStream os = new FileOutputStream(persistenceFile);
            props.store(os, "Simple Persistence Properties");
            os.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////////////////////////////

    public final void clear()
    {
        props.clear();
    }

    //////////////////////////////////////////////////////////////////////////

    private void init(Object o)
    {
        Class    clazz    = o.getClass();
        Package pkg        = clazz.getPackage();

        // we need the name of a file in the caller's package.  ANY file will do!
        // so we use the caller's .class file

        final String classFileName = clazz.getName().substring(pkg.getName().length() + 1) + ".class";

        URL url = clazz.getResource(classFileName);
        String filename = url.getPath();

        if(canWrite(filename))
        {
            // make the properties filename simple
            if(persistenceFileRootName == null)
                persistenceFileRootName = "persist.properties";

            filename = filename.substring(0, filename.indexOf(classFileName));
            filename += persistenceFileRootName;
            persistenceFile = new File(filename);
        }
        else
        {
            // make the properties filename equal to the package name
            // to avoid conflicts with other callers.
            if(persistenceFileRootName == null)
                persistenceFileRootName = pkg.getName() + ".properties";

            // most likely -- we are running from a jar!
            persistenceFile = new File(System.getProperty("java.io.tmpdir"));
            persistenceFile = new File(persistenceFile, persistenceFileRootName);
        }

        if(!persistenceFile.exists())
        {
            // write an empty file...
            store();
        }

        System.out.println("Persistence File: " + persistenceFile.getAbsolutePath());
    }

    //////////////////////////////////////////////////////////////////////////

    private void load()
    {
        try
        {
            InputStream in = new FileInputStream(persistenceFile);
            props.load(in);
            in.close();
            System.out.println(props);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////////////////////////////

    private boolean canWrite(String fname)
    {
        File f = new File(fname);

        if(f.exists())
        {
            return f.canWrite();
        }

        try
        {
            return f.createNewFile();
        }
        catch(IOException e)
        {
            return false;
        }
    }

    //////////////////////////////////////////////////////////////////////////

    private                    Properties    props                    = new Properties();
    private                    File        persistenceFile;
    private                    Class        caller;
    private                    String        persistenceFileRootName;
    private                    boolean        autoFlush;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        class foo {}
        SimplePersistence sp = new SimplePersistence(new foo(), true);
        sp.setProperty("foo", "goo");
        //m.getProps();
    }

}

