/*
 * Copyright (c) 2011-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.glassfish.web.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.glassfish.embeddable.GlassFishVariable.JAVA_HOME;

// Depends: JDK1.1

/**
 * Utils for introspection and reflection
 */
public final class IntrospectionUtils {


    private static final Logger log = org.glassfish.web.loader.LogFacade.getLogger();

    /**
     * Call execute() - any ant-like task should work
     */
    public static void execute(Object proxy, String method) throws Exception {
        Method executeM = null;
        Class<?> c = proxy.getClass();
        Class<?> params[] = new Class[0];
        // params[0]=args.getClass();
        executeM = findMethod(c, method, params);
        if (executeM == null) {
            throw new RuntimeException("No execute in " + proxy.getClass());
        }
        executeM.invoke(proxy, (Object[]) null);//new Object[] { args });
    }

    /**
     * Call void setAttribute( String ,Object )
     */
    public static void setAttribute(Object proxy, String n, Object v)
            throws Exception {
        if (proxy instanceof AttributeHolder) {
            ((AttributeHolder) proxy).setAttribute(n, v);
            return;
        }

        Method executeM = null;
        Class<?> c = proxy.getClass();
        Class<?> params[] = new Class[2];
        params[0] = String.class;
        params[1] = Object.class;
        executeM = findMethod(c, "setAttribute", params);
        if (executeM == null) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("No setAttribute in " + proxy.getClass());
            }
            return;
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("Setting " + n + "=" + v + "  in " + proxy);
        }
        executeM.invoke(proxy, new Object[] { n, v });
        return;
    }

    /**
     * Call void getAttribute( String )
     */
    public static Object getAttribute(Object proxy, String n) throws Exception {
        Method executeM = null;
        Class<?> c = proxy.getClass();
        Class<?> params[] = new Class[1];
        params[0] = String.class;
        executeM = findMethod(c, "getAttribute", params);
        if (executeM == null) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("No getAttribute in " + proxy.getClass());
            }
            return null;
        }
        return executeM.invoke(proxy, new Object[] { n });
    }

    /**
     * Construct a URLClassLoader. Will compile and work in JDK1.1 too.
     */
    public static ClassLoader getURLClassLoader(URL urls[], ClassLoader parent) {
        try {
            Class<?> urlCL = Class.forName("java.net.URLClassLoader");
            Class<?> paramT[] = new Class[2];
            paramT[0] = urls.getClass();
            paramT[1] = ClassLoader.class;
            Method m = findMethod(urlCL, "newInstance", paramT);
            if (m == null) {
                return null;
            }

            ClassLoader cl = (ClassLoader) m.invoke(urlCL, new Object[] { urls,
                    parent });
            return cl;
        } catch (ClassNotFoundException ex) {
            // jdk1.1
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String guessInstall(String installSysProp,
            String homeSysProp, String jarName) {
        return guessInstall(installSysProp, homeSysProp, jarName, null);
    }

    /**
     * Guess a product install/home by analyzing the class path. It works for
     * product using the pattern: lib/executable.jar or if executable.jar is
     * included in classpath by a shell script. ( java -jar also works )
     *
     * Insures both "install" and "home" System properties are set. If either or
     * both System properties are unset, "install" and "home" will be set to the
     * same value. This value will be the other System property that is set, or
     * the guessed value if neither is set.
     */
    public static String guessInstall(String installSysProp,
            String homeSysProp, String jarName, String classFile) {
        String install = null;
        String home = null;

        if (installSysProp != null) {
            install = System.getProperty(installSysProp);
        }

        if (homeSysProp != null) {
            home = System.getProperty(homeSysProp);
        }

        if (install != null) {
            if (home == null) {
                System.getProperties().put(homeSysProp, install);
            }
            return install;
        }

        // Find the directory where jarName.jar is located

        String cpath = System.getProperty("java.class.path");
        String pathSep = System.getProperty("path.separator");
        StringTokenizer st = new StringTokenizer(cpath, pathSep);
        while (st.hasMoreTokens()) {
            String path = st.nextToken();
            // log( "path " + path );
            if (path.endsWith(jarName)) {
                home = path.substring(0, path.length() - jarName.length());
                try {
                    if ("".equals(home)) {
                        home = new File("./").getCanonicalPath();
                    } else if (home.endsWith(File.separator)) {
                        home = home.substring(0, home.length() - 1);
                    }
                    File f = new File(home);
                    String parentDir = f.getParent();
                    if (parentDir == null)
                     {
                        parentDir = home; // unix style
                    }
                    File f1 = new File(parentDir);
                    install = f1.getCanonicalPath();
                    if (installSysProp != null) {
                        System.getProperties().put(installSysProp, install);
                    }
                    if (homeSysProp != null) {
                        System.getProperties().put(homeSysProp, install);
                    }
                    return install;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                String fname = path + (path.endsWith("/") ? "" : "/")
                        + classFile;
                if (new File(fname).exists()) {
                    try {
                        File f = new File(path);
                        String parentDir = f.getParent();
                        if (parentDir == null)
                         {
                            parentDir = path; // unix style
                        }
                        File f1 = new File(parentDir);
                        install = f1.getCanonicalPath();
                        if (installSysProp != null) {
                            System.getProperties().put(installSysProp, install);
                        }
                        if (home == null && homeSysProp != null) {
                            System.getProperties().put(homeSysProp, install);
                        }
                        return install;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        // if install directory can't be found, use home as the default
        if (home != null) {
            System.getProperties().put(installSysProp, home);
            return home;
        }

        return null;
    }

    /**
     * Debug method, display the classpath
     */
    public static void displayClassPath(String msg, URL[] cp) {
        if (log.isLoggable(Level.FINE)) {
            log.fine(msg);
            for (URL element : cp) {
                log.fine(element.getFile());
            }
        }
    }

    public static final String PATH_SEPARATOR = System.getProperty("path.separator");

    /**
     * Adds classpath entries from a vector of URL's to the "tc_path_add" System
     * property. This System property lists the classpath entries common to web
     * applications. This System property is currently used by WaSP when its
     * Jakarta Pages servlet compiles the Java file for a Jakarta Page.
     */
    public static String classPathAdd(URL urls[], String cp) {
        if (urls == null) {
            return cp;
        }

        for (URL url : urls) {
            if (cp != null) {
                cp += PATH_SEPARATOR + url.getFile();
            } else {
                cp = url.getFile();
            }
        }
        return cp;
    }

    /**
     * Find a method with the right name If found, call the method ( if param is
     * int or boolean we'll convert value to the right type before) - that means
     * you can have setDebug(1).
     */
    public static boolean setProperty(Object o, String name, String value) {
        return setProperty(o,name,value,true);
    }
    public static boolean setProperty(Object o, String name, String value,
            boolean invokeSetProperty) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("IntrospectionUtils: setProperty(" +
                    o.getClass() + " " + name + "=" + value + ")");
        }

        String setter = "set" + capitalize(name);

        try {
            Method methods[] = findMethods(o.getClass());
            Method setPropertyMethodVoid = null;
            Method setPropertyMethodBool = null;

            // First, the ideal case - a setFoo( String ) method
            for (Method method : methods) {
                Class<?> paramT[] = method.getParameterTypes();
                if (setter.equals(method.getName()) && paramT.length == 1
                        && "java.lang.String".equals(paramT[0].getName())) {

                    method.invoke(o, new Object[] { value });
                    return true;
                }
            }

            // Try a setFoo ( int ) or ( boolean )
            for (Method method : methods) {
                boolean ok = true;
                if (setter.equals(method.getName())
                        && method.getParameterTypes().length == 1) {

                    // match - find the type and invoke it
                    Class<?> paramType = method.getParameterTypes()[0];
                    Object params[] = new Object[1];

                    // Try a setFoo ( int )
                    if ("java.lang.Integer".equals(paramType.getName())
                            || "int".equals(paramType.getName())) {
                        try {
                            params[0] = Integer.valueOf(value);
                        } catch (NumberFormatException ex) {
                            ok = false;
                        }
                    // Try a setFoo ( long )
                    }else if ("java.lang.Long".equals(paramType.getName())
                                || "long".equals(paramType.getName())) {
                            try {
                                params[0] = Long.valueOf(value);
                            } catch (NumberFormatException ex) {
                                ok = false;
                            }

                        // Try a setFoo ( boolean )
                    } else if ("java.lang.Boolean".equals(paramType.getName())
                            || "boolean".equals(paramType.getName())) {
                        params[0] = Boolean.valueOf(value);

                        // Try a setFoo ( InetAddress )
                    } else if ("java.net.InetAddress".equals(paramType
                            .getName())) {
                        try {
                            params[0] = InetAddress.getByName(value);
                        } catch (UnknownHostException exc) {
                            if (log.isLoggable(Level.FINE)) {
                                log.fine("IntrospectionUtils: Unable to resolve host name:" + value);
                            }
                            ok = false;
                        }

                        // Unknown type
                    } else {
                        if (log.isLoggable(Level.FINE)) {
                            log.fine("IntrospectionUtils: Unknown type " +
                                    paramType.getName());
                        }
                    }

                    if (ok) {
                        method.invoke(o, params);
                        return true;
                    }
                }

                // save "setProperty" for later
                if ("setProperty".equals(method.getName())) {
                    if (method.getReturnType()==Boolean.TYPE){
                        setPropertyMethodBool = method;
                    }else {
                        setPropertyMethodVoid = method;
                    }

                }
            }

            // Ok, no setXXX found, try a setProperty("name", "value")
            if (invokeSetProperty && (setPropertyMethodBool != null ||
                    setPropertyMethodVoid != null)) {
                Object params[] = new Object[2];
                params[0] = name;
                params[1] = value;
                if (setPropertyMethodBool != null) {
                    try {
                        return (Boolean) setPropertyMethodBool.invoke(o, params);
                    }catch (IllegalArgumentException biae) {
                        //the boolean method had the wrong
                        //parameter types. lets try the other
                        if (setPropertyMethodVoid!=null) {
                            setPropertyMethodVoid.invoke(o, params);
                            return true;
                        }else {
                            throw biae;
                        }
                    }
                } else {
                    setPropertyMethodVoid.invoke(o, params);
                    return true;
                }
            }

        } catch (IllegalArgumentException ex2) {
            log.log(Level.WARNING, "IAE " + o + " " + name + " " + value, ex2);
        } catch (SecurityException ex1) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "IntrospectionUtils: SecurityException for " +
                        o.getClass() + " " + name + "=" + value + ")", ex1);
            }
        } catch (IllegalAccessException iae) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "IntrospectionUtils: IllegalAccessException for " +
                        o.getClass() + " " + name + "=" + value + ")", iae);
            }
        } catch (InvocationTargetException ie) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "IntrospectionUtils: InvocationTargetException for " +
                        o.getClass() + " " + name + "=" + value + ")", ie);
            }
        }
        return false;
    }

    public static Object getProperty(Object o, String name) {
        String getter = "get" + capitalize(name);
        String isGetter = "is" + capitalize(name);

        try {
            Method methods[] = findMethods(o.getClass());
            Method getPropertyMethod = null;

            // First, the ideal case - a getFoo() method
            for (Method method : methods) {
                Class<?> paramT[] = method.getParameterTypes();
                if (getter.equals(method.getName()) && paramT.length == 0) {
                    return method.invoke(o, (Object[]) null);
                }
                if (isGetter.equals(method.getName()) && paramT.length == 0) {
                    return method.invoke(o, (Object[]) null);
                }

                if ("getProperty".equals(method.getName())) {
                    getPropertyMethod = method;
                }
            }

            // Ok, no setXXX found, try a getProperty("name")
            if (getPropertyMethod != null) {
                Object params[] = new Object[1];
                params[0] = name;
                return getPropertyMethod.invoke(o, params);
            }

        } catch (IllegalArgumentException ex2) {
            log.log(Level.WARNING, "IAE " + o + " " + name, ex2);
        } catch (SecurityException ex1) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "IntrospectionUtils: SecurityException for " +
                        o.getClass() + " " + name + ")", ex1);
            }
        } catch (IllegalAccessException iae) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "IntrospectionUtils: IllegalAccessException for " +
                        o.getClass() + " " + name + ")", iae);
            }
        } catch (InvocationTargetException ie) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "IntrospectionUtils: InvocationTargetException for " +
                        o.getClass() + " " + name + ")");
            }
        }
        return null;
    }

    /**
     */
    public static void setProperty(Object o, String name) {
        String setter = "set" + capitalize(name);
        try {
            Method methods[] = findMethods(o.getClass());
            // find setFoo() method
            for (Method method : methods) {
                Class<?> paramT[] = method.getParameterTypes();
                if (setter.equals(method.getName()) && paramT.length == 0) {
                    method.invoke(o, new Object[] {});
                    return;
                }
            }
        } catch (Exception ex1) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "IntrospectionUtils: Exception for " +
                        o.getClass() + " " + name, ex1);
            }
        }
    }

    /**
     * Replace ${NAME} with the property value
     */
    public static String replaceProperties(String value,
            Hashtable<Object,Object> staticProp, PropertySource dynamicProp[]) {
        if (value.indexOf("$") < 0) {
            return value;
        }
        StringBuilder sb = new StringBuilder();
        int prev = 0;
        // assert value!=nil
        int pos;
        while ((pos = value.indexOf("$", prev)) >= 0) {
            if (pos > 0) {
                sb.append(value.substring(prev, pos));
            }
            if (pos == (value.length() - 1)) {
                sb.append('$');
                prev = pos + 1;
            } else if (value.charAt(pos + 1) != '{') {
                sb.append('$');
                prev = pos + 1; // XXX
            } else {
                int endName = value.indexOf('}', pos);
                if (endName < 0) {
                    sb.append(value.substring(pos));
                    prev = value.length();
                    continue;
                }
                String n = value.substring(pos + 2, endName);
                String v = null;
                if (staticProp != null) {
                    v = (String) staticProp.get(n);
                }
                if (v == null && dynamicProp != null) {
                    for (PropertySource element : dynamicProp) {
                        v = element.getProperty(n);
                        if (v != null) {
                            break;
                        }
                    }
                }
                if (v == null) {
                    v = "${" + n + "}";
                }

                sb.append(v);
                prev = endName + 1;
            }
        }
        if (prev < value.length()) {
            sb.append(value.substring(prev));
        }
        return sb.toString();
    }

    /**
     * Reverse of Introspector.decapitalize
     */
    public static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    public static String unCapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    // -------------------- Class path tools --------------------

    /**
     * Add all the jar files in a dir to the classpath, represented as a Vector
     * of URLs.
     */
    public static void addToClassPath(Vector<URL> cpV, String dir) {
        try {
            String cpComp[] = getFilesByExt(dir, ".jar");
            if (cpComp != null) {
                int jarCount = cpComp.length;
                for (int i = 0; i < jarCount; i++) {
                    URL url = getURL(dir, cpComp[i]);
                    if (url != null) {
                        cpV.addElement(url);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void addToolsJar(Vector<URL> v) {
        try {
            // Add tools.jar in any case
            String javaHome = System.getProperty(JAVA_HOME.getSystemPropertyName());
            File f = new File(javaHome + "/lib/tools.jar");
            if (!f.exists()) {
                // On some environments is still the jre directory.
                f = new File(javaHome + "/../lib/tools.jar");
            }
            v.addElement(new URL("file", "", f.getAbsolutePath()));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Return all files with a given extension in a dir
     */
    public static String[] getFilesByExt(String ld, String ext) {
        File dir = new File(ld);
        String[] names = null;
        final String lext = ext;
        if (dir.isDirectory()) {
            names = dir.list((d, name) -> name.endsWith(lext));
        }
        return names;
    }

    /**
     * Construct a file url from a file, using a base dir
     */
    public static URL getURL(String base, String file) {
        try {
            File baseF = new File(base);
            File f = new File(baseF, file);
            String path = f.getCanonicalPath();
            if (f.isDirectory()) {
                path += "/";
            }
            if (!f.exists()) {
                return null;
            }
            return new URL("file", "", path);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Add elements from the classpath <i>cp </i> to a Vector <i>jars </i> as
     * file URLs (We use Vector for JDK 1.1 compat).
     * <p>
     *
     * @param jars The jar list
     * @param cp a String classpath of directory or jar file elements
     *   separated by path.separator delimiters.
     * @throws IOException If an I/O error occurs
     * @throws MalformedURLException Doh ;)
     */
    public static void addJarsFromClassPath(Vector<URL> jars, String cp)
            throws IOException, MalformedURLException {
        String sep = System.getProperty("path.separator");
        StringTokenizer st;
        if (cp != null) {
            st = new StringTokenizer(cp, sep);
            while (st.hasMoreTokens()) {
                File f = new File(st.nextToken());
                String path = f.getCanonicalPath();
                if (f.isDirectory()) {
                    path += "/";
                }
                URL url = new URL("file", "", path);
                if (!jars.contains(url)) {
                    jars.addElement(url);
                }
            }
        }
    }

    /**
     * Return a URL[] that can be used to construct a class loader
     */
    public static URL[] getClassPath(Vector<URL> v) {
        URL[] urls = new URL[v.size()];
        for (int i = 0; i < v.size(); i++) {
            urls[i] = v.elementAt(i);
        }
        return urls;
    }

    /**
     * Construct a URL classpath from files in a directory, a cpath property,
     * and tools.jar.
     */
    public static URL[] getClassPath(String dir, String cpath,
            String cpathProp, boolean addTools) throws IOException,
            MalformedURLException {
        Vector<URL> jarsV = new Vector<>();
        if (dir != null) {
            // Add dir/classes first, if it exists
            URL url = getURL(dir, "classes");
            if (url != null) {
                jarsV.addElement(url);
            }
            addToClassPath(jarsV, dir);
        }

        if (cpath != null) {
            addJarsFromClassPath(jarsV, cpath);
        }

        if (cpathProp != null) {
            String cpath1 = System.getProperty(cpathProp);
            addJarsFromClassPath(jarsV, cpath1);
        }

        if (addTools) {
            addToolsJar(jarsV);
        }

        return getClassPath(jarsV);
    }

    // -------------------- other utils --------------------
    public static void clear() {
        objectMethods.clear();
    }

    static Hashtable<Class<?>,Method[]> objectMethods =
        new Hashtable<>();

    /**
     * This method may return an array of length, but not null.
     */
    public static Method[] findMethods(Class<?> c) {
        Method methods[] = objectMethods.get(c);
        if (methods != null) {
            return methods;
        }

        methods = c.getMethods();
        objectMethods.put(c, methods);
        return methods;
    }

    public static Method findMethod(Class<?> c, String name,
            Class<?> params[]) {
        Method methods[] = findMethods(c);
        for (Method method : methods) {
            if (method.getName().equals(name)) {
                Class<?> methodParams[] = method.getParameterTypes();
                if (params == null) {
                    if (methodParams.length == 0) {
                        return method;
                    } else {
                        continue;
                    }
                }
                if (params.length != methodParams.length) {
                    continue;
                }
                boolean found = true;
                for (int j = 0; j < params.length; j++) {
                    if (params[j] != methodParams[j]) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    return method;
                }
            }
        }
        return null;
    }

    /** Test if the object implements a particular
     *  method
     */
    public static boolean hasHook(Object obj, String methodN) {
        try {
            Method myMethods[] = findMethods(obj.getClass());
            for (Method myMethod : myMethods) {
                if (methodN.equals(myMethod.getName())) {
                    // check if it's overridden
                    Class<?> declaring = myMethod.getDeclaringClass();
                    Class<?> parentOfDeclaring = declaring.getSuperclass();
                    // this works only if the base class doesn't extend
                    // another class.

                    // if the method is declared in a top level class
                    // like BaseInterceptor parent is Object, otherwise
                    // parent is BaseInterceptor or an intermediate class
                    if (!"java.lang.Object".equals(parentOfDeclaring.getName())) {
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static void callMain(Class<?> c, String args[]) throws Exception {
        Class<?> p[] = new Class[1];
        p[0] = args.getClass();
        Method m = c.getMethod("main", p);
        m.invoke(c, new Object[] { args });
    }

    public static Object callMethod1(Object target, String methodN,
            Object param1, String typeParam1, ClassLoader cl) throws Exception {
        if (target == null) {
            throw new IllegalArgumentException(
                    "IntrospectionUtils: Assert: Illegal params null target");
        }
        if (param1 == null) {
            throw new IllegalArgumentException(
                    "IntrospectionUtils: Assert: Illegal params null param1");
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("IntrospectionUtils: callMethod1 " +
                    target.getClass().getName() + " " +
                    param1.getClass().getName() + " " + typeParam1);
        }

        Class<?> params[] = new Class[1];
        if (typeParam1 == null) {
            params[0] = param1.getClass();
        } else {
            params[0] = cl.loadClass(typeParam1);
        }
        Method m = findMethod(target.getClass(), methodN, params);
        if (m == null) {
            throw new NoSuchMethodException(target.getClass().getName() + " "
                    + methodN);
        }
        return m.invoke(target, new Object[] { param1 });
    }

    public static Object callMethod0(Object target, String methodN)
            throws Exception {
        if (target == null) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("IntrospectionUtils: Assert: Illegal params null");
            }
            return null;
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("IntrospectionUtils: callMethod0 " +
                    target.getClass().getName() + "." + methodN);
        }

        Class<?> params[] = new Class[0];
        Method m = findMethod(target.getClass(), methodN, params);
        if (m == null) {
            throw new NoSuchMethodException(target.getClass().getName() + " "
                    + methodN);
        }
        return m.invoke(target, emptyArray);
    }

    static Object[] emptyArray = new Object[] {};

    public static Object callMethodN(Object target, String methodN,
            Object params[], Class<?> typeParams[]) throws Exception {
        Method m = null;
        m = findMethod(target.getClass(), methodN, typeParams);
        if (m == null) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("IntrospectionUtils: Can't find method " + methodN +
                        " in " + target + " CLASS " + target.getClass());
            }
            return null;
        }
        Object o = m.invoke(target, params);

        if (log.isLoggable(Level.FINE)) {
            // debug
            StringBuilder sb = new StringBuilder();
            sb.append("" + target.getClass().getName() + "." + methodN + "( ");
            for (int i = 0; i < params.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(params[i]);
            }
            sb.append(")");
            log.fine("IntrospectionUtils:" + sb.toString());
        }
        return o;
    }

    public static Object convert(String object, Class<?> paramType) {
        Object result = null;
        if ("java.lang.String".equals(paramType.getName())) {
            result = object;
        } else if ("java.lang.Integer".equals(paramType.getName())
                || "int".equals(paramType.getName())) {
            try {
                result = Integer.valueOf(object);
            } catch (NumberFormatException ex) {
            }
            // Try a setFoo ( boolean )
        } else if ("java.lang.Boolean".equals(paramType.getName())
                || "boolean".equals(paramType.getName())) {
            result = Boolean.valueOf(object);

            // Try a setFoo ( InetAddress )
        } else if ("java.net.InetAddress".equals(paramType
                .getName())) {
            try {
                result = InetAddress.getByName(object);
            } catch (UnknownHostException exc) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("IntrospectionUtils: Unable to resolve host name:" +
                            object);
                }
            }

            // Unknown type
        } else {
            if (log.isLoggable(Level.FINE)) {
                log.fine("IntrospectionUtils: Unknown type " +
                        paramType.getName());
            }
        }
        if (result == null) {
            throw new IllegalArgumentException("Can't convert argument: " + object);
        }
        return result;
    }

    // -------------------- Get property --------------------
    // This provides a layer of abstraction

    public interface PropertySource {

        String getProperty(String key);

    }

    public interface AttributeHolder {

        void setAttribute(String key, Object o);

    }

}
