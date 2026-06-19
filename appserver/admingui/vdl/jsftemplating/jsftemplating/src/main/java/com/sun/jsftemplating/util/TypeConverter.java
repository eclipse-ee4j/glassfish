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

import jakarta.faces.context.FacesContext;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides an efficient and robust mechanism for converting an object to a different type. For example, one can convert
 * a <code>String</code> to an <code>Integer</code> using the <code>TypeConverter</code> like this:
 *
 * <pre>
 * Integer i = (Integer) TypeConverter.asType(Integer.class, "123");
 * </pre>
 *
 * or using the shortcut method:
 *
 * <pre>
 * int i = TypeConverter.asInt("123");
 * </pre>
 *
 * The <code>TypeConverter</code> comes ready to convert all the primitive types, plus a few more like
 * <code>java.sql.Date</code> and <code>
 * java.math.BigDecimal</code>.
 * <p>
 *
 * The conversion process has been optimized so that it is now a constant time operation (aside from the conversion
 * itself, which may vary). Because of this optimization, it is possible to register classes that implement the new
 * <code>TypeConversion</code> interface for conversion to a custom type. For example, this means that you can define a
 * class to convert arbitrary objects to type <code>Foo</code>, and register it for use throughout the VM:
 *
 * <pre>
 *    TypeConversion fooConversion = new FooTypeConversion();
 *    TypeConverter.registerTypeConversion(Foo.class, fooConversion);
 *    ...
 *    Bar bar = new Bar();
 *    Foo foo = (Foo) TypeConverter.asType(Foo.class, bar);
 *    ...
 *    String s = "bar";
 *    Foo foo = (Foo) TypeConverter.asType(Foo.class, s);
 * </pre>
 *
 * The TypeConverter allows specification of an arbitrary <i>type key</i> in the <code>registerTypeConversion()</code>
 * and <code>asType()</code> methods, so one can simultaneously register a conversion object under a <code>
 * Class</code> object, a class name, and a logical type name. For example, the following are valid ways of converting a
 * string to an <code>int</code> using <code>TypeConverter</code>:
 *
 * <pre>
 * Integer i = (Integer) TypeConverter.asType(Integer.class, "123");
 * Integer i = (Integer) TypeConverter.asType("java.lang.Integer", "123");
 * Integer i = (Integer) TypeConverter.asType(TypeConverter.TYPE_INT, "123");
 * Integer i = (Integer) TypeConverter.asType(TypeConverter.TYPE_INTEGER, "123");
 * Integer i = (Integer) TypeConverter.asType("int", "123");
 * Integer i = (Integer) TypeConverter.asType("integer", "123");
 * int i = TypeConverter.asInt("123");
 * </pre>
 *
 * Default type conversions have been registered under the following keys:
 *
 * <pre>
 *    Classes:
 *        java.lang.Object
 *        java.lang.String
 *        java.lang.Integer
 *        java.lang.Integer.TYPE (int)
 *        java.lang.Double
 *        java.lang.Double.TYPE (double)
 *        java.lang.Boolean
 *        java.lang.Boolean.TYPE (boolean)
 *        java.lang.Long
 *        java.lang.Long.TYPE (long)
 *        java.lang.Float
 *        java.lang.Float.TYPE (float)
 *        java.lang.Short
 *        java.lang.Short.TYPE (short)
 *        java.lang.Byte
 *        java.lang.Byte.TYPE (byte)
 *        java.lang.Character
 *        java.lang.Character.TYPE (char)
 *        java.math.BigDecimal
 *        java.sql.Date
 *        java.sql.Time
 *        java.sql.Timestamp
 *              java.util.Locale
 *
 *    Class name strings:
 *        "java.lang.Object"
 *        "java.lang.String"
 *        "java.lang.Integer"
 *        "java.lang.Double"
 *        "java.lang.Boolean"
 *        "java.lang.Long"
 *        "java.lang.Float"
 *        "java.lang.Short"
 *        "java.lang.Byte"
 *        "java.lang.Character"
 *        "java.math.BigDecimal"
 *        "java.sql.Date"
 *        "java.sql.Time"
 *        "java.sql.Timestamp"
 *              "java.util.Locale"
 *
 *    Logical type name string constants:
 *        TypeConverter.TYPE_UNKNOWN ("null")
 *        TypeConverter.TYPE_OBJECT ("object")
 *        TypeConverter.TYPE_STRING ("string")
 *        TypeConverter.TYPE_INT ("int")
 *        TypeConverter.TYPE_INTEGER ("integer")
 *        TypeConverter.TYPE_DOUBLE ("double")
 *        TypeConverter.TYPE_BOOLEAN ("boolean")
 *        TypeConverter.TYPE_LONG ("long")
 *        TypeConverter.TYPE_FLOAT ("float")
 *        TypeConverter.TYPE_SHORT ("short")
 *        TypeConverter.TYPE_BYTE ("byte")
 *        TypeConverter.TYPE_CHAR ("char")
 *        TypeConverter.TYPE_CHARACTER ("character")
 *        TypeConverter.TYPE_BIG_DECIMAL ("bigdecimal")
 *        TypeConverter.TYPE_SQL_DATE ("sqldate")
 *        TypeConverter.TYPE_DATE ("date")
 *        TypeConverter.TYPE_SQL_TIME ("sqltime")
 *        TypeConverter.TYPE_SQL_TIMESTAMP ("sqltimestamp")
 *              TypeConverter.TYPE_LOCALE ("locale")
 * </pre>
 *
 * The <code>TypeConverter</code> treats type keys of type <code>Class</code> slightly differently than other keys. If
 * the provided value is already of the type specified by the type key class, it is returned without a conversion taking
 * place. For example, a value of type <code>MySub</code> that extends the class <code>MySuper</code> would not be
 * converted in the following situation because it is already of the necessary type:
 *
 * <pre>
 * MySub o = (MySub) TypeConverter.asType(MySuper.class, mySub);
 * </pre>
 *
 * Be warned that although the type conversion infrastructure in this class is desgned to add only minimal overhead to
 * the conversion process, conversion of an object to another type is a potentially expensive operation and should be
 * used with discretion.
 *
 * @see TypeConversion
 * @author Todd Fast, todd.fast@sun.com
 * @author Mike Frisino, michael.frisino@sun.com
 * @author Ken Paulsen, ken.paulsen@sun.com (stripped down)
 * @author Jason Lee, jason@steeplesoft.com
 */
public class TypeConverter extends Object {

    /**
     * <p>
     * Cannot instantiate.
     * </p>
     */
    private TypeConverter() {
        super();
    }

    /**
     * <p>
     * Returns the <code>Map</code> of {@link TypeConversion} objects registered for this application. The keys for the
     * values in this <code>Map</code> may be arbitrary objects, the values are of type {@link TypeConversion}. The
     * <code>Map</code> will be a <code>ConcurrentHashMap</code>.
     * </p>
     */
    public static Map<Object, TypeConversion> getTypeConversions(FacesContext ctx) {
        if (ctx == null) {
            ctx = FacesContext.getCurrentInstance();
        }
        Map<Object, TypeConversion> conversions = null;
        if (ctx != null) {
            conversions = (Map<Object, TypeConversion>) ctx.getExternalContext().getApplicationMap().get(CONVERSIONS);
        }
        if (conversions == null) {
            // 1st time... initialize it
            conversions = new ConcurrentHashMap<>(80, 0.75f, 1);

            // Add type conversions by class
            conversions.put(Object.class, OBJECT_TYPE_CONVERSION);
            conversions.put(String.class, STRING_TYPE_CONVERSION);
            conversions.put(Integer.class, INTEGER_TYPE_CONVERSION);
            conversions.put(Integer.TYPE, INTEGER_TYPE_CONVERSION);
            conversions.put(Double.class, DOUBLE_TYPE_CONVERSION);
            conversions.put(Double.TYPE, DOUBLE_TYPE_CONVERSION);
            conversions.put(Boolean.class, BOOLEAN_TYPE_CONVERSION);
            conversions.put(Boolean.TYPE, BOOLEAN_TYPE_CONVERSION);
            conversions.put(Long.class, LONG_TYPE_CONVERSION);
            conversions.put(Long.TYPE, LONG_TYPE_CONVERSION);
            conversions.put(Float.class, FLOAT_TYPE_CONVERSION);
            conversions.put(Float.TYPE, FLOAT_TYPE_CONVERSION);
            conversions.put(Short.class, SHORT_TYPE_CONVERSION);
            conversions.put(Short.TYPE, SHORT_TYPE_CONVERSION);
            conversions.put(BigDecimal.class, BIG_DECIMAL_TYPE_CONVERSION);
            conversions.put(Byte.class, BYTE_TYPE_CONVERSION);
            conversions.put(Byte.TYPE, BYTE_TYPE_CONVERSION);
            conversions.put(Character.class, CHARACTER_TYPE_CONVERSION);
            conversions.put(Character.TYPE, CHARACTER_TYPE_CONVERSION);
            conversions.put(java.util.Date.class, DATE_TYPE_CONVERSION);
            conversions.put(java.sql.Date.class, SQL_DATE_TYPE_CONVERSION);
            conversions.put(java.sql.Time.class, SQL_TIME_TYPE_CONVERSION);
            conversions.put(java.sql.Timestamp.class, SQL_TIMESTAMP_TYPE_CONVERSION);
            conversions.put(java.util.Locale.class, LOCALE_TYPE_CONVERSION);

            // Add type conversions by class name
            conversions.put(Object.class.getName(), OBJECT_TYPE_CONVERSION);
            conversions.put(String.class.getName(), STRING_TYPE_CONVERSION);
            conversions.put(Integer.class.getName(), INTEGER_TYPE_CONVERSION);
            conversions.put(Double.class.getName(), DOUBLE_TYPE_CONVERSION);
            conversions.put(Boolean.class.getName(), BOOLEAN_TYPE_CONVERSION);
            conversions.put(Long.class.getName(), LONG_TYPE_CONVERSION);
            conversions.put(Float.class.getName(), FLOAT_TYPE_CONVERSION);
            conversions.put(Short.class.getName(), SHORT_TYPE_CONVERSION);
            conversions.put(BigDecimal.class.getName(), BIG_DECIMAL_TYPE_CONVERSION);
            conversions.put(Byte.class.getName(), BYTE_TYPE_CONVERSION);
            conversions.put(Character.class.getName(), CHARACTER_TYPE_CONVERSION);
            conversions.put(java.util.Date.class.getName(), DATE_TYPE_CONVERSION);
            conversions.put(java.sql.Date.class.getName(), SQL_DATE_TYPE_CONVERSION);
            conversions.put(java.sql.Time.class.getName(), SQL_TIME_TYPE_CONVERSION);
            conversions.put(java.sql.Timestamp.class.getName(), SQL_TIMESTAMP_TYPE_CONVERSION);
            conversions.put(java.util.Locale.class.getName(), LOCALE_TYPE_CONVERSION);

            // Add type conversions by name
            conversions.put(TYPE_UNKNOWN, UNKNOWN_TYPE_CONVERSION);
            conversions.put(TYPE_OBJECT, OBJECT_TYPE_CONVERSION);
            conversions.put(TYPE_STRING, STRING_TYPE_CONVERSION);
            conversions.put(TYPE_INT, INTEGER_TYPE_CONVERSION);
            conversions.put(TYPE_INTEGER, INTEGER_TYPE_CONVERSION);
            conversions.put(TYPE_DOUBLE, DOUBLE_TYPE_CONVERSION);
            conversions.put(TYPE_BOOLEAN, BOOLEAN_TYPE_CONVERSION);
            conversions.put(TYPE_LONG, LONG_TYPE_CONVERSION);
            conversions.put(TYPE_FLOAT, FLOAT_TYPE_CONVERSION);
            conversions.put(TYPE_SHORT, SHORT_TYPE_CONVERSION);
            conversions.put(TYPE_BIG_DECIMAL, BIG_DECIMAL_TYPE_CONVERSION);
            conversions.put(TYPE_BYTE, BYTE_TYPE_CONVERSION);
            conversions.put(TYPE_CHAR, CHARACTER_TYPE_CONVERSION);
            conversions.put(TYPE_CHARACTER, CHARACTER_TYPE_CONVERSION);
            conversions.put(TYPE_DATE, DATE_TYPE_CONVERSION);
            conversions.put(TYPE_SQL_DATE, SQL_DATE_TYPE_CONVERSION);
            conversions.put(TYPE_SQL_TIME, SQL_TIME_TYPE_CONVERSION);
            conversions.put(TYPE_SQL_TIMESTAMP, SQL_TIMESTAMP_TYPE_CONVERSION);
            conversions.put(TYPE_LOCALE, LOCALE_TYPE_CONVERSION);

            if (ctx != null) {
                // Save it for next time...
                ctx.getExternalContext().getApplicationMap().put(CONVERSIONS, conversions);
            }
        }

        // Return all the type conversions...
        return conversions;
    }

    /**
     * Register a type conversion object under the specified key. This method can be used by developers to register custom
     * type conversion objects.
     */
    public static void registerTypeConversion(FacesContext ctx, Object key, TypeConversion conversion) {
        getTypeConversions(ctx).put(key, conversion);
    }

    /**
     * <p>
     * Convert an object to the type specified by the provided type key. A type conversion object must have been previously
     * registered under the provided key in order for the conversion to succeed (with one exception, see below).
     * </p>
     *
     * <p>
     * Note, this method treats type keys of type <code>Class</code> differently than other type keys. That is, this method
     * will check if the provided value is the same as or a subclass of the specified class. If it is, this method returns
     * the value object immediately without attempting to convert its type. One exception to this rule is if the provided
     * type key is <code>Object.class</code>, in which case the conversion is attempted anyway. The reason for this
     * deviation is that this key may have special meaning based on the type of the provided value. For example, if the
     * provided value is a byte array, the <code>ObjectTypeConversion</code> class assumes it is a serialized object and
     * attempts to deserialize it. Because all objects, including arrays, are of type <code>Object</code>, this conversion
     * would never be attempted without this special handling. (Note that the default conversion for type key <code>
     *        Object.class</code> is to simply return the original object.)
     * </p>
     *
     * @param typeKey The key under which the desired type conversion object has been previously registered. Most commonly,
     * this key should be a <code>Class</code> object, a class name string, or a logical type string represented by the
     * various <code>TYPE_*</code> constants defined in this class.
     *
     * @param value The value to convert to the specified target type
     *
     * @return The converted value object, or <code>null</code> if the original value is <code>null</code>
     */
    public static Object asType(Object typeKey, Object value) {
        if (value == null) {
            return null;
        }

        /*
         * System.out.println("COERCE_TYPE: Coercing ("+value+ ")\n\tfrom "+value.getClass().getName()+
         * "\n\tto   "+typeKey+"\n");
         */

        if (typeKey == null) {
            return value;
        }

        // Check if the provided value is already of the target type
        if (typeKey instanceof Class && (Class) typeKey != Object.class) {
            if (((Class) typeKey).isInstance(value)) {
                return value;
            }
        }

        // Find the type conversion object
        TypeConversion conversion = getTypeConversions(null).get(typeKey);

        // Convert the value
        if (conversion != null) {
            return conversion.convertValue(value);
        } else {
            throw new IllegalArgumentException(
                    "Could not find type conversion for " + "type \"" + typeKey + "\" (value = \"" + value + "\")");
        }
    }

    /**
     *
     */
    public static byte asByte(Object value) {
        return asByte(value, (byte) 0);
    }

    /**
     *
     */
    public static byte asByte(Object value, byte defaultValue) {
        value = asType(Byte.class, value);
        if (value != null) {
            return ((Byte) value).byteValue();
        }
        return defaultValue;
    }

    /**
     *
     */
    public static short asShort(Object value) {
        return asShort(value, (short) 0);
    }

    /**
     *
     */
    public static short asShort(Object value, short defaultValue) {
        value = asType(Short.class, value);
        if (value != null) {
            return ((Short) value).shortValue();
        }
        return defaultValue;
    }

    /**
     *
     */
    public static int asInt(Object value) {
        return asInt(value, 0);
    }

    /**
     *
     */
    public static int asInt(Object value, int defaultValue) {
        value = asType(Integer.class, value);
        if (value != null) {
            return ((Integer) value).intValue();
        }
        return defaultValue;
    }

    /**
     *
     */
    public static long asLong(Object value) {
        return asLong(value, 0L);
    }

    /**
     *
     */
    public static long asLong(Object value, long defaultValue) {
        value = asType(Long.class, value);
        if (value != null) {
            return ((Long) value).longValue();
        }
        return defaultValue;
    }

    /**
     *
     */
    public static float asFloat(Object value) {
        return asFloat(value, 0F);
    }

    /**
     *
     */
    public static float asFloat(Object value, float defaultValue) {
        value = asType(Float.class, value);
        if (value != null) {
            return ((Float) value).floatValue();
        }
        return defaultValue;
    }

    /**
     *
     */
    public static double asDouble(Object value) {
        return asDouble(value, 0D);
    }

    /**
     *
     */
    public static double asDouble(Object value, double defaultValue) {
        value = asType(Double.class, value);
        if (value != null) {
            return ((Double) value).doubleValue();
        }
        return defaultValue;
    }

    /**
     *
     */
    public static char asChar(Object value) {
        return asChar(value, (char) 0);
    }

    /**
     *
     */
    public static char asChar(Object value, char defaultValue) {
        value = asType(Character.class, value);
        if (value != null) {
            return ((Character) value).charValue();
        }
        return defaultValue;
    }

    /**
     *
     */
    public static boolean asBoolean(Object value) {
        return asBoolean(value, false);
    }

    /**
     *
     */
    public static boolean asBoolean(Object value, boolean defaultValue) {
        value = asType(Boolean.class, value);
        if (value != null) {
            return ((Boolean) value).booleanValue();
        }
        return defaultValue;
    }

    /**
     *
     */
    public static String asString(Object value) {
        return (String) asType(String.class, value);
    }

    /**
     *
     */
    public static String asString(Object value, String defaultValue) {
        value = asType(String.class, value);
        if (value != null) {
            return (String) value;
        }
        return defaultValue;
    }

    /////////////////////////////////////////////////////////////////////////
    // Inner classes
    /////////////////////////////////////////////////////////////////////////

    /**
     *
     */
    public static class UnknownTypeConversion implements TypeConversion {

        /**
         *
         */
        @Override
        public Object convertValue(Object value) {
            return value;
        }
    }

    /**
     *
     */
    public static class StringTypeConversion implements TypeConversion {

        /**
         *
         */
        @Override
        public Object convertValue(Object value) {
            if (value == null) {
                return null;
            }

            if (value.getClass().isArray()) {
                // This is a byte array; we can convert it to a string
                if (value.getClass().getComponentType() == Byte.TYPE) {
                    value = new String((byte[]) value, UTF8);
                } else if (value.getClass().getComponentType() == Character.TYPE) {
                    value = new String((char[]) value);
                }
            } else if (!(value instanceof String)) {
                value = value.toString();
            }
            return value;
        }
    }

    /**
     *
     */
    public static class IntegerTypeConversion implements TypeConversion {

        /**
         *
         */
        @Override
        public Object convertValue(Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof Integer)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    value = null;
                } else {
                    value = new Integer(v);
                }
            }

            return value;
        }
    }

    /**
     *
     */
    public static class DoubleTypeConversion implements TypeConversion {

        /**
         *
         */
        @Override
        public Object convertValue(Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof Double)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    value = null;
                } else {
                    value = new Double(v);
                }
            }

            return value;
        }
    }

    /**
     *
     */
    public static class BooleanTypeConversion implements TypeConversion {

        /**
         *
         */
        @Override
        public Object convertValue(Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof Boolean)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    value = null;
                } else {
                    value = Boolean.valueOf(v);
                }
            }

            return value;
        }
    }

    /**
     *
     */
    public static class LongTypeConversion implements TypeConversion {

        /**
         *
         */
        @Override
        public Object convertValue(Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof Long)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    value = null;
                } else {
                    value = new Long(v);
                }
            }

            return value;
        }
    }

    /**
     *
     */
    public static class FloatTypeConversion implements TypeConversion {

        /**
         *
         */
        @Override
        public Object convertValue(Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof Float)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    value = null;
                } else {
                    value = new Float(v);
                }
            }

            return value;
        }
    }

    /**
     *
     */
    public static class ShortTypeConversion implements TypeConversion {

        /**
         *
         */
        @Override
        public Object convertValue(Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof Short)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    value = null;
                } else {
                    value = new Short(v);
                }
            }

            return value;
        }
    }

    /**
     *
     */
    public static class BigDecimalTypeConversion implements TypeConversion {

        /**
         *
         */
        @Override
        public Object convertValue(Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof BigDecimal)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    value = null;
                } else {
                    value = new BigDecimal(v);
                }
            }

            return value;
        }
    }

    /**
     *
     */
    public static class ByteTypeConversion implements TypeConversion {

        /**
         *
         */
        @Override
        public Object convertValue(Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof Byte)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    value = null;
                } else {
                    value = new Byte(v);
                }
            }

            return value;
        }
    }

    /**
     *
     */
    public static class CharacterTypeConversion implements TypeConversion {

        /**
         *
         */
        @Override
        public Object convertValue(Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof Character)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    value = null;
                } else {
                    value = new Character(v.charAt(0));
                }
            }

            return value;
        }
    }

    /**
     *
     */
    public static class SqlDateTypeConversion implements TypeConversion {
        /**
         *
         */
        @Override
        public Object convertValue(Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof java.sql.Date)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    value = null;
                } else {
                    // Value must be in the "yyyy-mm-dd" format
                    value = java.sql.Date.valueOf(v);
                }
            }

            return value;
        }
    }

    /**
     *
     */
    public static class DateTypeConversion implements TypeConversion {
        /**
         *
         */
        @Override
        public Object convertValue(Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof java.util.Date)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    value = null;
                } else {
                    value = new java.util.Date(v);
                }
            }

            return value;
        }
    }

    /**
     *
     */
    public static class SqlTimeTypeConversion implements TypeConversion {

        /**
         *
         */
        @Override
        public Object convertValue(Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof java.sql.Time)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    value = null;
                } else {
                    // Value must be in the "hh:mm:ss" format
                    value = java.sql.Time.valueOf(v);
                }
            }

            return value;
        }
    }

    /**
     *
     */
    public static class SqlTimestampTypeConversion implements TypeConversion {

        /**
         *
         */
        @Override
        public Object convertValue(Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof java.sql.Timestamp)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    value = null;
                } else {
                    // Value must be in the "yyyy-mm-dd hh:mm:ss.fffffffff"
                    // format
                    value = java.sql.Timestamp.valueOf(v);
                }
            }

            return value;
        }
    }

    /**
     *
     */
    public static class ObjectTypeConversion implements TypeConversion {
        @Override
        public Object convertValue(Object value) {
            /*
             * if (value==null) { return null; }
             *
             * // TODO: Decide if this is important functionality. For now just return the Object that is passed in. if
             * (value.getClass().isArray()) { // This is a byte array; we can convert it to an object if
             * (value.getClass().getComponentType() == Byte.TYPE) { ByteArrayInputStream bis = new
             * ByteArrayInputStream((byte[])value); ApplicationObjectInputStream ois = null; try { ois = new
             * ApplicationObjectInputStream(bis); value = ois.readObject(); } catch (Exception e) { throw new
             * WrapperRuntimeException( "Could not deserialize object",e); } finally { try { if (ois != null) { ois.close(); } if
             * (bis != null) { bis.close(); } } catch (IOException e) { // Ignore } } } else { // value is OK as is } }
             */

            return value;
        }
    }

    /**
     *
     */
    public static class LocaleTypeConversion implements TypeConversion {
        @Override
        public Object convertValue(Object value) {
            if (value == null) {
                return null;
            }
            if (!(value instanceof java.util.Locale)) {
                String language = value.toString();
                String country = null;
                String locale = value.toString();
                int index = locale.indexOf("_");
                if (index > -1) {
                    language = locale.substring(0, index);
                    country = locale.substring(index + 1);
                    value = new java.util.Locale(language, country);
                } else {
                    value = new java.util.Locale(language);
                }
            }

            return value;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    // Test classes
    /////////////////////////////////////////////////////////////////////////

    /*
     * private static class TestSuperclass extends Object { }
     *
     * private static class Test extends TestSuperclass { public static void main(String[] args) { if
     * (!(TypeConverter.asString(new Integer(12)) instanceof String)) { throw new Error(); }
     *
     * if (!(TypeConverter.asType(Integer.class, "12") instanceof Integer)) { throw new Error(); }
     *
     * if (!(TypeConverter.asType(Long.class, "12") instanceof Long)) { throw new Error(); }
     *
     * if (!(TypeConverter.asType(Float.class, "12.0") instanceof Float)) { throw new Error(); }
     *
     * if (!(TypeConverter.asType(Double.class, "12.0") instanceof Double)) { throw new Error(); }
     *
     * if (!(TypeConverter.asType(Short.class, "12") instanceof Short)) { throw new Error(); }
     *
     * if (!(TypeConverter.asType(BigDecimal.class, "12") instanceof BigDecimal)) { throw new Error(); }
     *
     * if (!(TypeConverter.asType(Boolean.class, "true") instanceof Boolean)) { throw new Error(); }
     *
     * if (!(TypeConverter.asType(Byte.class, "12") instanceof Byte)) { throw new Error(); }
     *
     * if (!(TypeConverter.asType(Character.class, "1") instanceof Character)) { throw new Error(); }
     *
     * System.out.println("Test passed."); } }
     */

    /////////////////////////////////////////////////////////////////////////
    // Class variables
    /////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Application scope key for storing {@link TypeConversion}s for this application.
     * </p>
     */
    private static final String CONVERSIONS = "__jsft_TypeConversions";
    private static final Charset UTF8 = Charset.forName("UTF-8");

    /** Logical type name "null" */
    public static final String TYPE_UNKNOWN = "null";

    /** Logical type name "object" */
    public static final String TYPE_OBJECT = "object";

    /** Logical type name "string" */
    public static final String TYPE_STRING = "string";

    /** Logical type name "int" */
    public static final String TYPE_INT = "int";

    /** Logical type name "integer" */
    public static final String TYPE_INTEGER = "integer";

    /** Logical type name "long" */
    public static final String TYPE_LONG = "long";

    /** Logical type name "float" */
    public static final String TYPE_FLOAT = "float";

    /** Logical type name "double" */
    public static final String TYPE_DOUBLE = "double";

    /** Logical type name "short" */
    public static final String TYPE_SHORT = "short";

    /** Logical type name "boolean" */
    public static final String TYPE_BOOLEAN = "boolean";

    /** Logical type name "byte" */
    public static final String TYPE_BYTE = "byte";

    /** Logical type name "char" */
    public static final String TYPE_CHAR = "char";

    /** Logical type name "character" */
    public static final String TYPE_CHARACTER = "character";

    /** Logical type name "bigdecimal" */
    public static final String TYPE_BIG_DECIMAL = "bigdecimal";

    /** Logical type name "sqldate" */
    public static final String TYPE_SQL_DATE = "sqldate";

    /** Logical type name "date java.util.Date" */
    public static final String TYPE_DATE = "date";

    /** Logical type name "sqltime" */
    public static final String TYPE_SQL_TIME = "sqltime";

    /** Logial type name = "locale" */
    public static final String TYPE_LOCALE = "locale";

    /** Logical type name "sqltimestamp" */
    public static final String TYPE_SQL_TIMESTAMP = "sqltimestamp";

    public static final TypeConversion UNKNOWN_TYPE_CONVERSION = new UnknownTypeConversion();
    public static final TypeConversion OBJECT_TYPE_CONVERSION = new ObjectTypeConversion();
    public static final TypeConversion STRING_TYPE_CONVERSION = new StringTypeConversion();
    public static final TypeConversion INTEGER_TYPE_CONVERSION = new IntegerTypeConversion();
    public static final TypeConversion DOUBLE_TYPE_CONVERSION = new DoubleTypeConversion();
    public static final TypeConversion BOOLEAN_TYPE_CONVERSION = new BooleanTypeConversion();
    public static final TypeConversion LONG_TYPE_CONVERSION = new LongTypeConversion();
    public static final TypeConversion FLOAT_TYPE_CONVERSION = new FloatTypeConversion();
    public static final TypeConversion SHORT_TYPE_CONVERSION = new ShortTypeConversion();
    public static final TypeConversion BIG_DECIMAL_TYPE_CONVERSION = new BigDecimalTypeConversion();
    public static final TypeConversion BYTE_TYPE_CONVERSION = new ByteTypeConversion();
    public static final TypeConversion CHARACTER_TYPE_CONVERSION = new CharacterTypeConversion();
    public static final TypeConversion DATE_TYPE_CONVERSION = new DateTypeConversion();
    public static final TypeConversion SQL_DATE_TYPE_CONVERSION = new SqlDateTypeConversion();
    public static final TypeConversion SQL_TIME_TYPE_CONVERSION = new SqlTimeTypeConversion();
    public static final TypeConversion SQL_TIMESTAMP_TYPE_CONVERSION = new SqlTimestampTypeConversion();
    public static final TypeConversion LOCALE_TYPE_CONVERSION = new LocaleTypeConversion();
}
