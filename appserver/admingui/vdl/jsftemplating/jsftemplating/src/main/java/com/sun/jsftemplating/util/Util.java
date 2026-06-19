/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.util;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * <p>
 * This class is for general purpose utility methods.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class Util {

    /**
     * <p>
     * This method returns the ContextClassLoader unless it is null, in which case it returns the ClassLoader that loaded
     * "obj". Unless it is null, in which it will return the system ClassLoader.
     * </p>
     *
     * @param obj May be null, if non-null when the Context ClassLoader is null, then the Classloader used to load this
     * Object will be returned.
     */
    public static ClassLoader getClassLoader(Object obj) {
        // Get the ClassLoader
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            if (obj != null) {
                loader = obj.getClass().getClassLoader();
            }
        }

        // Wrap with custom ClassLoader if specified
        loader = getCustomClassLoader(loader);

        return loader;
    }
// NOTE: Maybe in addition to getClassLoader, we should have Iterator<ClassLoader> getClassLoaders() for cases where we want to attempt multiple ClassLoaders

    /**
     * <p>
     * Method to get the custom <code>ClassLoader</code> if one exists. If one does not exist, it will return the
     * <code>ClassLoader</code> that is passed in. If (null) is passed in for the parent <code>ClassLoader</code>, it will
     * get the <b>System</b> <code>ClassLoader</code>, not the context <code>ClassLoader</code> or any other one.
     * </p>
     */
    private static ClassLoader getCustomClassLoader(ClassLoader parent) {
        // Figure out the parent ClassLoader
        parent = parent == null ? ClassLoader.getSystemClassLoader() : parent;

        // Check to see if we've calculated the ClassLoader for this parent
        FacesContext ctx = FacesContext.getCurrentInstance();
        Map<ClassLoader, ClassLoader> classLoaderCache = getClassLoaderCache(ctx);
        ClassLoader loader = classLoaderCache.get(parent);
        if (loader != null) {
            return loader;
        }
        loader = parent;

        // Look to see if a custom ClassLoader was specified via an initParam
        String clsName = null;
        if (ctx != null) {
            clsName = ctx.getExternalContext().getInitParameterMap().get(CUSTOM_CLASS_LOADER);
        }
        if (clsName != null) {
            if (clsName.equals(loader.getClass().getName())) {
                // It has already been wrapped
                return loader;
            }
            try {
                // Intantiate the custom classloader w/ "loader" as its parent
                Class cls = Class.forName(clsName, true, parent);
                loader = (ClassLoader) cls.getConstructor(ClassLoader.class).newInstance(parent);

                // Set custom classloader as the context-classloader... This
                // didn't work, JSF blew up... revisit this if necessary
//        Thread.currentThread().setContextClassLoader(loader);
            } catch (ClassNotFoundException ex) {
                throw new IllegalArgumentException("Unable to load class (" + clsName + ").  Make sure your context-param is "
                        + "specified correctly and that your custom ClassLoader " + "is included in your application.", ex);
            } catch (NoSuchMethodException ex) {
                throw new IllegalArgumentException("Unable to load class (" + clsName + ").  You must have a constructor that "
                        + "allows the parent ClassLoader to be provided on your " + "custom ClassLoader.", ex);
            } catch (InstantiationException ex) {
                throw new RuntimeException("Unable to instantiate class (" + clsName + ")!", ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Unable to access class (" + clsName + ")!", ex);
            } catch (java.lang.reflect.InvocationTargetException ex) {
                throw new RuntimeException("Unable to instantiate class (" + clsName + ")!", ex);
            }
        }

        // Cache for next time
        classLoaderCache.put(parent, loader);

        // Return the ClassLoader (may be the same one passed in)
        return loader;
    }

    /**
     * <p>
     * Provides access to the application-scoped Map which stores custom ClassLoaders which may wrap a parent ClassLoader in
     * this application.
     * </p>
     */
    private static Map<ClassLoader, ClassLoader> getClassLoaderCache(FacesContext ctx) {
        if (ctx == null) {
            ctx = FacesContext.getCurrentInstance();
        }
        Map<ClassLoader, ClassLoader> map = null;
        if (ctx != null) {
            map = (Map<ClassLoader, ClassLoader>) ctx.getExternalContext().getApplicationMap().get(CLASSLOADER_CACHE);
        }
        if (map == null) {
            // 1st time... initialize it
            map = new HashMap<>(4);
            if (ctx != null) {
                ctx.getExternalContext().getApplicationMap().put(CLASSLOADER_CACHE, map);
            }
        }

        // Return the map...
        return map;
    }

    /**
     * <p>
     * This method will attempt to load a Class from the context ClassLoader. If it fails, it will try from the ClassLoader
     * used to load the given object. If that's null, or fails, it will try using the System ClassLoader.
     * </p>
     *
     * @param className The full name of the class to load.
     * @param obj An optional Object used to help find the ClassLoader to use.
     */
    public static Class loadClass(String className, Object obj) throws ClassNotFoundException {
        // Get the context ClassLoader
        ClassLoader loader = getClassLoader(obj);
        Class cls = null;
        if (loader != null) {
            try {
                cls = Class.forName(className, false, loader);
            } catch (ClassNotFoundException ex) {
                // Ignore
                if (LogUtil.finestEnabled()) {
                    LogUtil.finest("Unable to find class (" + className + ") using the context ClassLoader: '" + loader
                            + "'.  I will keep looking.", ex);
                }
            }
        }
        if (cls == null) {
            // Still haven't found it... look for it somewhere else.
            if (obj != null) {
                loader = obj.getClass().getClassLoader();
                if (loader != null) {
                    try {
                        cls = Class.forName(className, false, loader);
                    } catch (ClassNotFoundException ex) {
                        // Ignore
                        if (LogUtil.finestEnabled()) {
                            LogUtil.finest("Unable to find class (" + className + ") using ClassLoader: '" + loader
                                    + "'.  I will try the System ClassLoader.", ex);
                        }
                    }
                }
            }
            if (cls == null) {
                // Still haven't found it, use System ClassLoader
                loader = ClassLoader.getSystemClassLoader();

                // Allow this one to throw the Exception if not found
                cls = Class.forName(className, false, loader);
            }
        }

        // Return the Class
        return cls;
    }

    /**
     * <p>
     * Method which returns the Class for the given class name, or null if any exception occurs. No exceptions are thrown.
     * </p>
     */
    public static Class noExceptionLoadClass(String name) {
        Class cls = null;
        try {
            cls = Util.loadClass(name, null);
        } catch (Exception ex) {
            // Ignore...
        }
        return cls;
    }

    /**
     * <p>
     * This method attempts load the requested Class. If obj is a String, it will use this value as the fully qualified
     * class name. If it is a Class, it will return it. If it is anything else, it will return the Class for the given
     * Object.
     * </p>
     *
     * @param obj The Object describing the requested Class
     */
    public static Class getClass(Object obj) throws ClassNotFoundException {
        if (obj == null || obj instanceof Class) {
            return (Class) obj;
        }
        Class cls = null;
        if (obj instanceof String) {
            cls = loadClass((String) obj, obj);
        } else {
            cls = obj.getClass();
        }
        return cls;
    }

    /**
     * <p>
     * This method locates the requested <code>Method</code> on the given <code>Class</code>, with the given
     * <code>params</code>. This method does not throw any exceptions. Instead it will return <code>null</code> if unable to
     * locate the method.
     * </p>
     */
    public static Method getMethod(Class cls, String name, Class... prms) {
        Method method = null;
        try {
            method = cls.getMethod(name, prms);
        } catch (NoSuchMethodException ex) {
            // Do nothing, we're eating the exception
        } catch (SecurityException ex) {
            // Do nothing, we're eating the exception
        }
        return method;
    }

    /**
     * <p>
     * This method converts the given Map into a Properties Map (if it is already one, then it simply returns the given
     * Map).
     * </p>
     */
    public static Properties mapToProperties(Map map) {
        if (map == null || map instanceof Properties) {
            return (Properties) map;
        }

        // Create Properties and add all the values
        Properties props = new Properties();
        props.putAll(map);

        // Return the result
        return props;
    }

    /**
     * <p>
     * Help obtain the current <code>Locale</code>.
     * </p>
     */
    public static Locale getLocale(FacesContext context) {
        Locale locale = null;
        if (context != null) {
            // Attempt to obtain the locale from the UIViewRoot
            UIViewRoot root = context.getViewRoot();
            if (root != null) {
                locale = root.getLocale();
            }
        }

        // Return the locale; if not found, return the system default Locale
        return locale == null ? Locale.getDefault() : locale;
    }

    /**
     * <p>
     * This method escapes text so that HTML tags and escape characters can be shown in an HTML page without seeming to be
     * parsed.
     * </p>
     */
    public static String htmlEscape(String str) {
        if (str == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer("");
        for (char ch : str.toCharArray()) {
            switch (ch) {
            case '&':
                buf.append("&amp;");
                break;
            case '<':
                buf.append("&lt;");
                break;
            case '>':
                buf.append("&gt;");
                break;
            default:
                buf.append(ch);
                break;
            }
        }
        return buf.toString();
    }

    /**
     * <p>
     * This method strips leading delimeter.
     * </p>
     *
     */
    protected static String stripLeadingDelimeter(String str, char ch) {
        if (str == null || str.equals("")) {
            return str;
        }
        int j = 0;
        char[] strArr = str.toCharArray();
        for (int i = 0; i < strArr.length; i++) {
            j = i;
            if (strArr[i] != ch) {
                break;
            }
        }
        return str.substring(j);

    }

    /**
     * Closes an InputStream if it is non-null, throwing away any Exception that may occur
     *
     * @param is
     */
    public static void closeStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * <p>
     * Application scope attribute name for storing custom <code>ClassLoaders</code>.
     * </p>
     */
    private static final String CLASSLOADER_CACHE = "__jsft_ClassLoaders";

    /**
     * <p>
     * This is the context-param that specifies the JSFTemplating custom <code>ClassLoader</code> to use.
     * </p>
     */
    public static final String CUSTOM_CLASS_LOADER = "com.sun.jsftemplating.CLASSLOADER";
}
