/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.rmi.Remote;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

/**
 * Handy class full of static functions.
 */
public final class Utility {

    private static final Logger LOG = CULoggerInfo.getLogger();
    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(Utility.class);

    public static void checkJVMVersion() {
        // do not perform any JVM version checking
    }

    /**
     * Returns true if the given string is null or is empty.
     *
     * @param string The string to be checked on emptiness.
     * @return True if the given string is null or is empty.
     */
    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    /**
     * Returns <code>true</code> if the given array is null or is empty.
     *
     * @param array The array to be checked on emptiness.
     * @return <code>true</code> if the given array is null or is empty.
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Returns <code>true</code> if the given collection is null or is empty.
     *
     * @param collection The collection to be checked on emptiness.
     * @return <code>true</code> if the given collection is null or is empty.
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Returns <code>true</code> if the given value is null or is empty. Types of String, Collection, Map, Optional and
     * Array are recognized. If none is recognized, then examine the emptiness of the toString() representation instead.
     *
     * @param value The value to be checked on emptiness.
     * @return <code>true</code> if the given value is null or is empty.
     */
    public static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        } else if (value instanceof String) {
            return ((String) value).isEmpty();
        } else if (value instanceof Collection<?>) {
            return ((Collection<?>) value).isEmpty();
        } else if (value instanceof Map<?, ?>) {
            return ((Map<?, ?>) value).isEmpty();
        } else if (value instanceof Optional<?>) {
            return !((Optional<?>) value).isPresent();
        } else if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        } else {
            return value.toString() == null || value.toString().isEmpty();
        }
    }

    /**
     * Returns true if all values are empty, false if at least one value is not empty.
     *
     * @param values the values to be checked on emptiness
     * @return True if all values are empty, false otherwise
     */
    public static boolean isAllEmpty(Object... values) {
        for (Object value : values) {
            if (!isEmpty(value)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns <code>true</code> if at least one value is empty.
     *
     * @param values the values to be checked on emptiness
     * @return <code>true</code> if any value is empty and <code>false</code> if no values are empty
     */
    public static boolean isAnyEmpty(Object... values) {
        for (Object value : values) {
            if (isEmpty(value)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isAllNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return false;
            }
        }

        return true;
    }

    public static boolean isAnyNull(Object... values) {
        for (Object value : values) {
            if (value == null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns <code>true</code> if the given object equals one of the given objects.
     *
     * @param <T> The generic object type.
     * @param object The object to be checked if it equals one of the given objects.
     * @param objects The argument list of objects to be tested for equality.
     * @return <code>true</code> if the given object equals one of the given objects.
     */
    @SafeVarargs
    public static <T> boolean isOneOf(T object, T... objects) {
        for (Object other : objects) {
            if (object == null ? other == null : object.equals(other)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the first non-<code>null</code> object of the argument list, or <code>null</code> if there is no such
     * element.
     *
     * @param <T> The generic object type.
     * @param objects The argument list of objects to be tested for non-<code>null</code>.
     * @return The first non-<code>null</code> object of the argument list, or <code>null</code> if there is no such
     * element.
     */
    @SafeVarargs
    public static <T> T coalesce(T... objects) {
        for (T object : objects) {
            if (object != null) {
                return object;
            }
        }

        return null;
    }

    public static Properties getPropertiesFromFile(String file) throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream(file);
        InputStream is2 = null;
        try {
            if (is != null) {
                Properties config = new Properties();
                config.load(is);
                return config;
            } else {
                String remoteclient = "/" + file;
                is2 = Utility.class.getResourceAsStream(remoteclient);
                Properties config = new Properties();
                config.load(is2);
                return config;
            }
        } finally {
            try {
                if (is2 != null) {
                    is2.close();
                }
            } catch (Exception e) {
                // nothing can be done about it.
            }
        }
    }

    /**
     * Return the hostname of the local machine.
     */
    public static String getLocalHost() {
        String hostname = null;
        try {
            InetAddress ia = InetAddress.getLocalHost();
            hostname = ia.getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
        return hostname;
    }

    /**
     * Return the hostname of the local machine.
     */
    public static String getLocalAddress() {
        String address = null;
        try {
            InetAddress ia = InetAddress.getLocalHost();
            address = ia.getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
        return address;
    }

    /**
     * This is a convenience method to lookup a remote object by name within the naming context.
     *
     * @exception javax.naming.NamingException if the object with that name could not be found.
     */
    public static Remote lookupObject(String publishedName, Class anInterface) throws javax.naming.NamingException {

        Context ic = new InitialContext();
        Object objRef = ic.lookup(publishedName);
        return (Remote) PortableRemoteObject.narrow(objRef, anInterface);
    }

    /**
     * Returns a character array for the valid characters in a CharBuffer.
     *
     * @param cb
     * @return
     */
    public static char[] toCharArray(final CharBuffer cb) {
        return cb.toString().toCharArray();
    }

    /**
     * Returns a byte array for the valid bytes in a ByteBuffer.
     *
     * @param bb
     * @return
     */
    public static byte[] toByteArray(final ByteBuffer bb) {
        final byte[] result = new byte[bb.limit() - bb.position()];
        bb.get(result);
        return result;
    }

    /**
     * Unmarshal a byte array to an integer. Assume the bytes are in BIGENDIAN order. i.e. array[offset] is the
     * most-significant-byte and array[offset+3] is the least-significant-byte.
     *
     * @param array The array of bytes.
     * @param offset The offset from which to start unmarshalling.
     */
    public static int bytesToInt(byte[] array, int offset) {
        int b1, b2, b3, b4;

        b1 = (array[offset++] << 24) & 0xFF000000;
        b2 = (array[offset++] << 16) & 0x00FF0000;
        b3 = (array[offset++] << 8) & 0x0000FF00;
        b4 = (array[offset++] << 0) & 0x000000FF;

        return (b1 | b2 | b3 | b4);
    }

    /**
     * Marshal an integer to a byte array. The bytes are in BIGENDIAN order. i.e. array[offset] is the most-significant-byte
     * and array[offset+3] is the least-significant-byte.
     *
     * @param array The array of bytes.
     * @param offset The offset from which to start marshalling.
     */
    public static void intToBytes(int value, byte[] array, int offset) {
        array[offset++] = (byte) ((value >>> 24) & 0xFF);
        array[offset++] = (byte) ((value >>> 16) & 0xFF);
        array[offset++] = (byte) ((value >>> 8) & 0xFF);
        array[offset++] = (byte) ((value >>> 0) & 0xFF);
    }

    /**
     * Unmarshal a byte array to an long. Assume the bytes are in BIGENDIAN order. i.e. array[offset] is the
     * most-significant-byte and array[offset+7] is the least-significant-byte.
     *
     * @param array The array of bytes.
     * @param offset The offset from which to start unmarshalling.
     */
    public static long bytesToLong(byte[] array, int offset) {
        long l1, l2;

        l1 = (long) bytesToInt(array, offset) << 32;
        l2 = bytesToInt(array, offset + 4) & 0xFFFFFFFFL;

        return (l1 | l2);
    }

    /**
     * Marshal an long to a byte array. The bytes are in BIGENDIAN order. i.e. array[offset] is the most-significant-byte
     * and array[offset+7] is the least-significant-byte.
     *
     * @param array The array of bytes.
     * @param offset The offset from which to start marshalling.
     */
    public static void longToBytes(long value, byte[] array, int offset) {
        array[offset++] = (byte) ((value >>> 56) & 0xFF);
        array[offset++] = (byte) ((value >>> 48) & 0xFF);
        array[offset++] = (byte) ((value >>> 40) & 0xFF);
        array[offset++] = (byte) ((value >>> 32) & 0xFF);
        array[offset++] = (byte) ((value >>> 24) & 0xFF);
        array[offset++] = (byte) ((value >>> 16) & 0xFF);
        array[offset++] = (byte) ((value >>> 8) & 0xFF);
        array[offset++] = (byte) ((value >>> 0) & 0xFF);
    }

    /**
     * Verify and invoke main if present in the specified class.
     */
    public static void invokeApplicationMain(Class mainClass, String[] args)
            throws InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        String err = I18N.getLocalString("utility.no.main", "", new Object[] { mainClass });

        // determine the main method using reflection
        // verify that it is public static void and takes
        // String[] as the only argument
        Method mainMethod = null;
        try {
            mainMethod = mainClass.getMethod("main", new Class[] { String[].class });
        } catch (NoSuchMethodException msme) {
            LOG.log(Level.SEVERE, CULoggerInfo.exceptionInUtility, msme);
            throw new ClassNotFoundException(err);
        }

        // check modifiers: public static
        // check return type and exceptions
        int modifiers = mainMethod.getModifiers();
        if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers) || !mainMethod.getReturnType().equals(Void.TYPE)) {
            err = I18N.getLocalString("utility.main.invalid", "The main method signature is invalid");
            LOG.log(Level.SEVERE, CULoggerInfo.mainNotValid);
            throw new ClassNotFoundException(err);
        }

        // build args to the main and call it
        Object params[] = new Object[1];
        params[0] = args;
        mainMethod.invoke(null, params);

    }

    public static void invokeSetMethod(Object obj, String prop, String value)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class cl = obj.getClass();
        // change first letter to uppercase
        String setMeth = "set" + prop.substring(0, 1).toUpperCase(Locale.US) + prop.substring(1);

        // try string method
        try {
            Class[] cldef = { String.class };
            Method meth = cl.getMethod(setMeth, cldef);
            Object[] params = { value };
            meth.invoke(obj, params);
            return;
        } catch (NoSuchMethodException ex) {
            try {
                // try int method
                Class[] cldef = { Integer.TYPE };
                Method meth = cl.getMethod(setMeth, cldef);
                Object[] params = { Integer.valueOf(value) };
                meth.invoke(obj, params);
                return;
            } catch (NoSuchMethodException nsmex) {
                // try boolean method
                Class[] cldef = { Boolean.TYPE };
                Method meth = cl.getMethod(setMeth, cldef);
                Object[] params = { Boolean.valueOf(value) };
                meth.invoke(obj, params);
                return;
            }
        }
    }

    public static void invokeSetMethodCaseInsensitive(Object obj, String prop, String value)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String alternateMethodName = null;
        Class cl = obj.getClass();

        String setMeth = "set" + prop;

        Method[] methodsList = cl.getMethods();
        boolean methodFound = false;
        int i = 0;
        for (i = 0; i < methodsList.length; ++i) {
            if (methodsList[i].getName().equalsIgnoreCase(setMeth) == true) {
                Class[] parameterTypes = methodsList[i].getParameterTypes();
                if (parameterTypes.length == 1) {
                    if (parameterTypes[0].getName().equals("java.lang.String")) {
                        methodFound = true;
                        break;
                    } else {
                        alternateMethodName = methodsList[i].getName();
                    }
                }

            }
        }
        if (methodFound == true) {
            Object[] params = { value };
            methodsList[i].invoke(obj, params);
            return;
        }
        if (alternateMethodName != null) {
            try {
                // try int method
                Class[] cldef = { Integer.TYPE };
                Method meth = cl.getMethod(alternateMethodName, cldef);
                Object[] params = { Integer.valueOf(value) };
                meth.invoke(obj, params);
                return;
            } catch (NoSuchMethodException nsmex) {
                // try boolean method
                Class[] cldef = { Boolean.TYPE };
                Method meth = cl.getMethod(alternateMethodName, cldef);
                Object[] params = { Boolean.valueOf(value) };
                meth.invoke(obj, params);
                return;
            }

        } else {
            throw new NoSuchMethodException(setMeth);
        }
    }

    // Ports are marshalled as shorts on the wire. The IDL
    // type is unsigned short, which lacks a convenient representation
    // in Java in the 32768-65536 range. So, we treat ports as
    // ints throught this code, except that marshalling requires a
    // scaling conversion. intToShort and shortToInt are provided
    // for this purpose.

    public static short intToShort(int value) {
        if (value > 32767) {
            return (short) (value - 65536);
        }
        return (short) value;
    }

    public static int shortToInt(short value) {
        if (value < 0) {
            return value + 65536;
        }
        return value;
    }

    /**
     * Get the current thread's context class loader which is set to the CommonClassLoader by ApplicationServer
     *
     * @return the thread's context classloader if it exists; else the system class loader.
     */
    public static ClassLoader getClassLoader() {
        if (Thread.currentThread().getContextClassLoader() != null) {
            return Thread.currentThread().getContextClassLoader();
        }

        return ClassLoader.getSystemClassLoader();
    }

    /**
     * Loads the class with the common class loader.
     *
     * @param className the class name
     * @return the loaded class
     * @exception if the class is not found.
     */
    public static Class loadClass(String className) throws ClassNotFoundException {
        return getClassLoader().loadClass(className);
    }

    /**
     * Utility routine for setting the context class loader.
     *
     * @return previous class loader; can be the same instance.
     */
    public static ClassLoader setContextClassLoader(final ClassLoader classLoader) {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader original = currentThread.getContextClassLoader();
        if (classLoader == original) {
            return classLoader;
        }
        LOG.log(Level.FINER, "setContextClassLoader(classLoader={0}; original: {1})",
            new Object[] {classLoader, original});
        if (System.getSecurityManager() == null) {
            currentThread.setContextClassLoader(classLoader);
        } else {
            PrivilegedAction<Void> action = () -> {
                currentThread.setContextClassLoader(classLoader);
                return null;
            };
            AccessController.doPrivileged(action);
        }
        return original;
    }

    /**
     * Run an action with a specific classloader as the context classloader. Sets the context classloader, calls the method, resets the context classloader to the previous classloader.
     * @param Type of a value returned from the supplied action and from this method
     * @param contextClassLoader Classloader to be used as the context classloader during thi method call
     * @param action A mathod to call with the classloader as the context classloader.
     * @return Value returned by the action method
     */
    public static <T> T runWithContextClassLoader(ClassLoader contextClassLoader, Supplier<T> action) {
        ClassLoader originalClassLoader = null;
        try {
            originalClassLoader = setContextClassLoader(contextClassLoader, originalClassLoader);
            return action.get();
        } finally {
            resetContextClassLoder(originalClassLoader);
        }
    }

    public interface RunnableWithException<E extends Exception> {
        void run() throws E;
    }

    /**
     * Same as {@link Utility#runWithContextClassLoader(java.lang.ClassLoader, java.util.function.Supplier)} but with an action that doesn't return anything
     */
    public static <E extends Exception> void runWithContextClassLoader(ClassLoader contextClassLoader,
            RunnableWithException<E> action) throws E {
        ClassLoader originalClassLoader = null;
        try {
            originalClassLoader = setContextClassLoader(contextClassLoader, originalClassLoader);
            action.run();
        } finally {
            resetContextClassLoder(originalClassLoader);
        }
    }

    private static ClassLoader setContextClassLoader(ClassLoader contextClassLoader, ClassLoader originalClassLoader) {
        if (contextClassLoader != null) {
            originalClassLoader = setContextClassLoader(contextClassLoader);
        }
        return originalClassLoader;
    }

    private static void resetContextClassLoder(ClassLoader originalClassLoader) {
        if (originalClassLoader != null) {
            setContextClassLoader(originalClassLoader);
        }
    }

    public static void setEnvironment() {
        Environment.obtain().activateEnvironment();
    }

    /**
     * Return the value for a given name from the System Properties or the Environmental Variables. The former overrides the
     * latter.
     *
     * @param name - the name of the System Property or Environmental Variable
     * @return the value of the variable or null if it was not found
     */
    public static String getEnvOrProp(String name) {
        // System properties override env. variables
        String envVal = System.getenv(name);
        String sysPropVal = System.getProperty(name);

        if (sysPropVal != null) {
            return sysPropVal;
        }

        return envVal;
    }

    /**
     * Convert the byte array to char array with respect to given charset.
     *
     * @param byteArray
     * @param charset null or "" means default charset
     * @throws CharacterCodingException
     */
    public static char[] convertByteArrayToCharArray(byte[] byteArray, Charset charset) throws CharacterCodingException {
        if (byteArray == null) {
            return null;
        }

        byte[] bArray = byteArray.clone();
        ByteBuffer byteBuffer = ByteBuffer.wrap(bArray);
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer charBuffer = null;
        try {
            charBuffer = decoder.decode(byteBuffer);
        } catch (CharacterCodingException cce) {
            throw cce;
        } catch (Throwable t) {
            CharacterCodingException e = new CharacterCodingException();
            e.initCause(t);
            throw e;
        }
        char[] result = toCharArray(charBuffer);
        clear(byteBuffer);
        clear(charBuffer);

        return result;
    }

    /**
     * Convert the char array to byte array with respect to given charset.
     *
     * @param charArray
     * @param charset null or "" means default charset
     * @throws CharacterCodingException
     */
    public static byte[] convertCharArrayToByteArray(char[] charArray, Charset charset) throws CharacterCodingException {
        if (charArray == null) {
            return null;
        }

        char[] cArray = charArray.clone();
        CharBuffer charBuffer = CharBuffer.wrap(cArray);
        CharsetEncoder encoder = charset.newEncoder();
        ByteBuffer byteBuffer = null;
        try {
            byteBuffer = encoder.encode(charBuffer);
        } catch (CharacterCodingException cce) {
            throw cce;
        } catch (Exception e) {
            CharacterCodingException cce = new CharacterCodingException();
            cce.initCause(e);
            throw cce;
        }

        byte[] result = new byte[byteBuffer.remaining()];
        byteBuffer.get(result);
        clear(byteBuffer);
        clear(charBuffer);

        return result.clone();
    }

    private static void clear(ByteBuffer byteBuffer) {
        byte[] bytes = byteBuffer.array();
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 0;
        }
    }

    private static void clear(CharBuffer charBuffer) {
        char[] chars = charBuffer.array();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = '0';
        }
    }
}
