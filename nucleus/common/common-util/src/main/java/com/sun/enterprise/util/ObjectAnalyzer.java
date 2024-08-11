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

/**
 * WBN -- generic mechanism for simulating toString() and equals()
 * for any class
 */
package com.sun.enterprise.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjectAnalyzer {
    /**
     * @param className
     * @return  */
    public static String getMethods(String className) {
        try {
            Class clazz = Class.forName(className);
            return getMethods(clazz, false);
        }
        catch (Exception e) {
            return "Error loading class: " + e.getMessage();//NOI18N
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * @param obj
     * @return  */
    public static String getMethods(Object obj) {
        return getMethods(obj, false);
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * @param obj
     * @param settersOnly
     * @return  */
    public static String getMethods(Object obj, boolean settersOnly) {
        try {
            Class clazz = safeGetClass(obj);
            return getMethods(clazz, settersOnly);
        }
        catch (Exception e) {
            return e.getMessage();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * @param clazz
     * @param settersOnly
     * @return  */
    public static String getMethods(Class clazz, boolean settersOnly) {
        StringBuilder sb = new StringBuilder();

        Method[] methods = clazz.getMethods();

        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            boolean isSetter = m.getName().startsWith("set");//NOI18N

            if (settersOnly && isSetter == false)
                continue;

            sb.append(m.toString()).append('\n');
        }
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * @param obj
     * @return  */
    public static String getSetters(Object obj) {
        return getMethods(obj, true);
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * @param clazz
     * @return  */
    public static String getSetters(Class clazz) {
        return getMethods(clazz, true);
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * @param obj
     * @return  */
    public static String toString(Object obj) {
        return toString(obj, false);
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * @param obj
     * @return  */
    public static String toStringWithSuper(Object obj) {
        return toString(obj, true);
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * @param a
     * @param b
     * @return  */
    public static boolean equals(Object a, Object b) {
        Class cl = a.getClass();

        if (!cl.equals(b.getClass()))
            return false;

        Field[] fields = cl.getDeclaredFields();
        setAccessible(fields);

        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            try {
                if (!f.get(a).equals(f.get(b)))
                    return false;
            }
            catch (IllegalAccessException e) {
                return false;
            }
        }

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////
    public static void useShortClassNames() {
        useShortClassNames_ = true;
    }

    ////////////////////////////////////////////////////////////////////////////
    public static void useLongClassNames() {
        useShortClassNames_ = false;
    }

    ////////////////////////////////////////////////////////////////////////////
    private static String toString(Object obj, boolean doSuperClasses) {
        try {
            return getFieldInfo(obj, doSuperClasses).toString();
        }
        catch (ObjectAnalyzerException e) {
            return e.getMessage();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    private static Class safeGetClass(Object obj) throws ObjectAnalyzerException {
        if (obj == null)
            throw new ObjectAnalyzerException(fatal + "null Object parameter");//NOI18N

        Class cl = obj.getClass();

        if (cl == null)
            throw new ObjectAnalyzerException(fatal + "getClass() on parameter Object returned null");//NOI18N

        return cl;
    }

    ////////////////////////////////////////////////////////////////////////////
    private static Class safeGetSuperclass(Class cl) throws ObjectAnalyzerException {
        Class sc = cl.getSuperclass();

        if (sc == null)
            throw new ObjectAnalyzerException("getSuperclass() on parameter Object returned null");//NOI18N

        return sc;
    }

    ////////////////////////////////////////////////////////////////////////////
    private static FieldInfoVector getFieldInfo(Object obj, boolean doSuperClasses) throws ObjectAnalyzerException {
        FieldInfoVector fiv = new FieldInfoVector();
        Class cl = safeGetClass(obj);

        if (doSuperClasses == false) {
            getFieldInfo(cl, obj, fiv);
            return fiv;
        }

        for (Class theClass = cl; !theClass.equals(Object.class); theClass = safeGetSuperclass(theClass))
            getFieldInfo(theClass, obj, fiv);

        return fiv;
    }

    ////////////////////////////////////////////////////////////////////////////
    private static void getFieldInfo(Class cl, Object obj, FieldInfoVector fiv) throws ObjectAnalyzerException {
        Field[] fields = null;

        try {
            fields = cl.getDeclaredFields();
        }
        catch (SecurityException e) {
            throw new ObjectAnalyzerException("got a SecurityException when calling getDeclaredFields() on " + cl.getName());//NOI18N
        }

        // FindBugs says this is a redundant null check.  We are now depending
        // on (assuming) that getDeclaredFields() will never return null;
        //if (fields == null)
            //throw new ObjectAnalyzerException("calling getDeclaredFields() on " + cl.getName() + " returned null");//NOI18N

        setAccessible(fields);

        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            String sval;
            String modifiers = Modifier.toString(f.getModifiers());
            String className = cl.getName();

            if (useShortClassNames_)
                className = StringUtils.toShortClassName(className);

            if (modifiers.length() <= 0)
                modifiers = "(package)";//NOI18N

            try {
                Object val = f.get(obj);

                if (val == null)
                    sval = "<null>";//NOI18N
                else
                    sval = val.toString();
            }
            catch (IllegalAccessException e) {
                sval = "<IllegalAccessException>";//NOI18N
            }

            fiv.addElement(new FieldInfo(className, f, sval, modifiers));
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    private static void setAccessible(Field[] fields) {
        if (setAccessibleMethod == null)
            return;        // Must be pre JDK 1.2.x

        try {
            Boolean b = Boolean.valueOf(true);
            setAccessibleMethod.invoke(null, new Object[]{fields, b});
        }
        catch (Exception e) {
            Logger.getAnonymousLogger().warning("Got an exception invoking setAccessible: " + e);//NOI18N
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    private static void setupSetAccessibleMethod() {
        // whoa!  what's this reflection stuff doing here?
        // AccessibleObject is a JDK 1.2 class that lets you peek at
        // private variable values.  Since we need to support JDK 1.1
        // for the VCafe plug-in -- it is now called via 100% reflection
        // techniques...

        setAccessibleMethod = null;

        Class AO;

        try {
            AO = Class.forName("java.lang.reflect.AccessibleObject");//NOI18N
        }
        catch (ClassNotFoundException e) {
            Logger.getAnonymousLogger().info("Can't find java.lang.reflect.AccessibleObject -- thus I can't show any private or protected variable values.  This must be pre JDK 1.2.x");//NOI18N
            return;
        }

        Method[] methods = AO.getDeclaredMethods();

        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];

            if (m.getName().equals("setAccessible") && m.getParameterTypes().length == 2)//NOI18N
            {
                if(Logger.getAnonymousLogger().isLoggable(Level.FINER))
                    Logger.getAnonymousLogger().finer("Found setAccessible: " + m);//NOI18N
                setAccessibleMethod = m;
                break;
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    private static final String fatal = "Fatal Error in ObjectAnalyzer.toString():  ";//NOI18N
    private static boolean useShortClassNames_ = true;
    private static Method setAccessibleMethod;

    static {
        setupSetAccessibleMethod();
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * @param args  */
    public static void main(String[] args) {
        String s = "Hello!";//NOI18N

        System.out.println("Regular: \n" + toString(s) + "\n\n");//NOI18N
        System.out.println("Super: \n" + toStringWithSuper(s) + "\n\n");//NOI18N
    }
}

////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
class ObjectAnalyzerException extends Exception {
    ObjectAnalyzerException(String s) {
        super(s);
    }
}

////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
class FieldInfo {
    Field field;
    String value;
    //Class        clazz;
    String className;
    String modifiers;

    FieldInfo(String c, Field f, String v, String m) {
        className = c;
        field = f;
        value = v;
        modifiers = m;
    }
}

////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
class FieldInfoVector // { extends Vector
{
    Vector v = null;

    FieldInfoVector() {
        v = new Vector();
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * @param o  */
    public void addElement(Object o) {
        v.addElement(o);
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return  */
    public String toString() {
        int veclen = v.size();
        StringBuffer s = new StringBuffer();

        setLongestNames();

        //s.append("classNameLength: " + classNameLength + "fieldNameLength: " + fieldNameLength + "modifiersLength: " + modifiersLength + "\n\n");//NOI18N

        s.append(StringUtils.padRight("Class", classNameLength));//NOI18N
        s.append(StringUtils.padRight("Modifiers", modifiersLength));//NOI18N
        s.append(StringUtils.padRight("Field", fieldNameLength));//NOI18N
        s.append("Value");//NOI18N
        s.append("\n\n");//NOI18N

        for (int i = 0; i < veclen; i++) {
            FieldInfo fi = fetch(i);

            s.append(StringUtils.padRight(fi.className, classNameLength));
            s.append(StringUtils.padRight(fi.modifiers, modifiersLength));
            s.append(StringUtils.padRight(fi.field.getName(), fieldNameLength));
            s.append(fi.value);
            s.append('\n');
        }

        return s.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    private FieldInfo fetch(int i) {
        return (FieldInfo) v.elementAt(i);
    }

    ////////////////////////////////////////////////////////////////////////////
    private void setLongestNames() {
        int veclen = v.size();

        classNameLength = 5;
        fieldNameLength = 5;
        modifiersLength = 5;

        for (int i = 0; i < veclen; i++) {
            FieldInfo fi = fetch(i);

            int clen = fi.className.length();
            int flen = fi.field.getName().length();
            int mlen = fi.modifiers.length();
            if (clen > classNameLength)
                classNameLength = clen;
            if (flen > fieldNameLength)
                fieldNameLength = flen;
            if (mlen > modifiersLength)
                modifiersLength = mlen;
        }

        classNameLength += 2;
        fieldNameLength += 2;
        modifiersLength += 2;
    }
    ////////////////////////////////////////////////////////////////////////////
    private int classNameLength = 0;
    private int fieldNameLength = 0;
    private int modifiersLength = 0;
}
