/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.webui.jsf.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO remove Provides an efficient and robust mechanism for converting an
 * object to a different type. For example, one can convert a {@code String} to
 * an {@code Integer} using the {@code TypeConverter} like this:
 *
 * <pre>
 *  Integer i = (Integer) TypeConverter.asType(Integer.class, "123");
 * </pre>
 *
 * or using the shortcut method:
 *
 * <pre>
 *  int i = TypeConverter.asInt("123");
 * </pre>
 *
 * The {@code TypeConverter} comes ready to convert all the primitive types,
 * plus a few more like {@code java.sql.Date} and {@code java.math.BigDecimal}.
 *
 * The conversion process has been optimized so that it is now a constant time
 * operation (aside from the conversion itself, which may vary). Because of this
 * optimization, it is possible to register classes that implement the new
 * {@code TypeConversion} interface for conversion to a custom type. For
 * example, this means that you can define a class to convert arbitrary objects
 * to type {@code Foo}, and register it for use throughout the VM:
 *
 * <pre>
 *  TypeConversion fooConversion = new FooTypeConversion();
 *  TypeConverter.registerTypeConversion(Foo.class, fooConversion);
 *  ...
 *  Bar bar = new Bar();
 *  Foo foo = (Foo) TypeConverter.asType(Foo.class, bar);
 *  ...
 *  String s = "bar";
 *  Foo foo = (Foo) TypeConverter.asType(Foo.class, s);
 * </pre>
 *
 * The TypeConverter allows specification of an arbitrary <i>type key</i> in the
 * {@code registerTypeConversion()} and {@code asType()} methods, so one can
 * simultaneously register a conversion object under a {@code Class} object, a
 * class name, and a logical type name. For example, the following are valid
 * ways of converting a string to an {@code int} using {@code TypeConverter}:
 *
 * <pre>
 *  Integer i = (Integer) TypeConverter.asType(Integer.class, "123");
 *  Integer i = (Integer) TypeConverter.asType("java.lang.Integer", "123");
 *  Integer i = (Integer) TypeConverter.asType("int", "123");
 *  Integer i = (Integer) TypeConverter.asType("integer", "123");
 *  int i = TypeConverter.asInt("123");
 * </pre>
 *
 * Default type conversions have been registered under the following keys:
 *
 * <pre>
 *  Classes:
 *      java.lang.Object
 *      java.lang.String
 *      java.lang.Integer
 *      java.lang.Integer.TYPE (int)
 *      java.lang.Double
 *      java.lang.Double.TYPE (double)
 *      java.lang.Boolean
 *      java.lang.Boolean.TYPE (boolean)
 *      java.lang.Long
 *      java.lang.Long.TYPE (long)
 *      java.lang.Float
 *      java.lang.Float.TYPE (float)
 *      java.lang.Short
 *      java.lang.Short.TYPE (short)
 *      java.lang.Byte
 *      java.lang.Byte.TYPE (byte)
 *      java.lang.Character
 *      java.lang.Character.TYPE (char)
 *      java.math.BigDecimal
 *      java.sql.Date
 *      java.sql.Time
 *      java.sql.Timestamp
 *
 *  Class name strings:
 *      "java.lang.Object"
 *      "java.lang.String"
 *      "java.lang.Integer"
 *      "java.lang.Double"
 *      "java.lang.Boolean"
 *      "java.lang.Long"
 *      "java.lang.Float"
 *      "java.lang.Short"
 *      "java.lang.Byte"
 *      "java.lang.Character"
 *      "java.math.BigDecimal"
 *      "java.sql.Date"
 *      "java.sql.Time"
 *      "java.sql.Timestamp"
 *
 *  Logical type name string constants:
 *      TypeConverter.TYPE_UNKNOWN ("null")
 *      TypeConverter.TYPE_OBJECT ("object")
 *      TypeConverter.TYPE_STRING ("string")
 *      TypeConverter.TYPE_INT ("int")
 *      TypeConverter.TYPE_INTEGER ("integer")
 *      TypeConverter.TYPE_DOUBLE ("double")
 *      TypeConverter.TYPE_BOOLEAN ("boolean")
 *      TypeConverter.TYPE_LONG ("long")
 *      TypeConverter.TYPE_FLOAT ("float")
 *      TypeConverter.TYPE_SHORT ("short")
 *      TypeConverter.TYPE_BYTE ("byte")
 *      TypeConverter.TYPE_CHAR ("char")
 *      TypeConverter.TYPE_CHARACTER ("character")
 *      TypeConverter.TYPE_BIG_DECIMAL ("bigdecimal")
 *      TypeConverter.TYPE_SQL_DATE ("sqldate")
 *      TypeConverter.TYPE_SQL_TIME ("sqltime")
 *      TypeConverter.TYPE_SQL_TIMESTAMP ("sqltimestamp")
 * </pre>
 *
 * The {@code TypeConverter} treats type keys of type {@code Class} slightly
 * differently than other keys. If the provided value is already of the type
 * specified by the type key class, it is returned without a conversion taking
 * place. For example, a value of type {@code MySub} that extends the class
 * {@code MySuper} would not be converted in the following situation because it
 * is already of the necessary type:
 *
 * <pre>
 *  MySub o = (MySub) TypeConverter.asType(MySuper.class, mySub);
 * </pre>
 *
 * Be warned that although the type conversion infrastructure in this class is
 * designed to add only minimal overhead to the conversion process, conversion
 * of an object to another type is a potentially expensive operation and should
 * be used with discretion.
 *
 * @see TypeConversion
 */
public final class TypeConverter {

    /**
     * Type conversions map.
     */
    private static final Map<Object, TypeConversion> TYPE_CONVERSIONS
            = new HashMap<Object, TypeConversion>();

    /**
     * Logical type name "null".
     */
    public static final String TYPE_UNKNOWN = "null";

    /**
     * Logical type name "object".
     */
    public static final String TYPE_OBJECT = "object";

    /**
     * Logical type name "string".
     */
    public static final String TYPE_STRING = "string";

    /**
     * Logical type name "int".
     */
    public static final String TYPE_INT = "int";

    /**
     * Logical type name "integer".
     */
    public static final String TYPE_INTEGER = "integer";

    /**
     * Logical type name "long".
     */
    public static final String TYPE_LONG = "long";

    /**
     * Logical type name "float".
     */
    public static final String TYPE_FLOAT = "float";

    /**
     * Logical type name "double".
     */
    public static final String TYPE_DOUBLE = "double";

    /**
     * Logical type name "short".
     */
    public static final String TYPE_SHORT = "short";

    /**
     * Logical type name "boolean".
     */
    public static final String TYPE_BOOLEAN = "boolean";

    /**
     * Logical type name "byte".
     */
    public static final String TYPE_BYTE = "byte";

    /**
     * Logical type name "char".
     */
    public static final String TYPE_CHAR = "char";

    /**
     * Logical type name "character".
     */
    public static final String TYPE_CHARACTER = "character";

    /**
     * Logical type name "bigdecimal".
     */
    public static final String TYPE_BIG_DECIMAL = "bigdecimal";

    /**
     * Logical type name "sqldate".
     */
    public static final String TYPE_SQL_DATE = "sqldate";

    /**
     * Logical type name "sqltime".
     */
    public static final String TYPE_SQL_TIME = "sqltime";

    /**
     * Logical type name "sqltimestamp".
     */
    public static final String TYPE_SQL_TIMESTAMP = "sqltimestamp";

    /**
     * Type conversion instance for "unknown" type.
     */
    private static final TypeConversion UNKNOWN_TYPE_CONVERSION
            = new UnknownTypeConversion();

    /**
     * Type conversion instance for "object" type.
     */
    private static final TypeConversion OBJECT_TYPE_CONVERSION
            = new ObjectTypeConversion();

    /**
     * Type conversion instance for "string" type.
     */
    private static final TypeConversion STRING_TYPE_CONVERSION
            = new StringTypeConversion();

    /**
     * Type conversion instance for "int" type.
     */
    private static final TypeConversion INTEGER_TYPE_CONVERSION
            = new IntegerTypeConversion();

    /**
     * Type conversion instance for "double" type.
     */
    private static final TypeConversion DOUBLE_TYPE_CONVERSION
            = new DoubleTypeConversion();

    /**
     * Type conversion instance for "boolean" type.
     */
    private static final TypeConversion BOOLEAN_TYPE_CONVERSION
            = new BooleanTypeConversion();

    /**
     * Type conversion instance for "long" type.
     */
    private static final TypeConversion LONG_TYPE_CONVERSION
            = new LongTypeConversion();

    /**
     * Type conversion instance for "float" type.
     */
    private static final TypeConversion FLOAT_TYPE_CONVERSION
            = new FloatTypeConversion();

    /**
     * Type conversion instance for "short" type.
     */
    private static final TypeConversion SHORT_TYPE_CONVERSION
            = new ShortTypeConversion();

    /**
     * Type conversion instance for "bigdecimal" type.
     */
    private static final TypeConversion BIG_DECIMAL_TYPE_CONVERSION
            = new BigDecimalTypeConversion();

    /**
     * Type conversion instance for "byte" type.
     */
    private static final TypeConversion BYTE_TYPE_CONVERSION
            = new ByteTypeConversion();

    /**
     * Type conversion instance for "char" type.
     */
    private static final TypeConversion CHARACTER_TYPE_CONVERSION
            = new CharacterTypeConversion();

    /**
     * Type conversion instance for "sqldate" type.
     */
    private static final TypeConversion SQL_DATE_TYPE_CONVERSION
            = new SqlDateTypeConversion();

    /**
     * Type conversion instance for "sqltime" type.
     */
    private static final TypeConversion SQL_TIME_TYPE_CONVERSION
            = new SqlTimeTypeConversion();

    /**
     * Type conversion instance for "sqltimestamp" type.
     */
    private static final TypeConversion SQL_TIMESTAMP_TYPE_CONVERSION
            = new SqlTimestampTypeConversion();

    static {
        // Add type conversions by class
        registerTypeConversion(Object.class, OBJECT_TYPE_CONVERSION);
        registerTypeConversion(String.class, STRING_TYPE_CONVERSION);
        registerTypeConversion(Integer.class, INTEGER_TYPE_CONVERSION);
        registerTypeConversion(Integer.TYPE, INTEGER_TYPE_CONVERSION);
        registerTypeConversion(Double.class, DOUBLE_TYPE_CONVERSION);
        registerTypeConversion(Double.TYPE, DOUBLE_TYPE_CONVERSION);
        registerTypeConversion(Boolean.class, BOOLEAN_TYPE_CONVERSION);
        registerTypeConversion(Boolean.TYPE, BOOLEAN_TYPE_CONVERSION);
        registerTypeConversion(Long.class, LONG_TYPE_CONVERSION);
        registerTypeConversion(Long.TYPE, LONG_TYPE_CONVERSION);
        registerTypeConversion(Float.class, FLOAT_TYPE_CONVERSION);
        registerTypeConversion(Float.TYPE, FLOAT_TYPE_CONVERSION);
        registerTypeConversion(Short.class, SHORT_TYPE_CONVERSION);
        registerTypeConversion(Short.TYPE, SHORT_TYPE_CONVERSION);
        registerTypeConversion(BigDecimal.class, BIG_DECIMAL_TYPE_CONVERSION);
        registerTypeConversion(Byte.class, BYTE_TYPE_CONVERSION);
        registerTypeConversion(Byte.TYPE, BYTE_TYPE_CONVERSION);
        registerTypeConversion(Character.class, CHARACTER_TYPE_CONVERSION);
        registerTypeConversion(Character.TYPE, CHARACTER_TYPE_CONVERSION);
        registerTypeConversion(java.sql.Date.class, SQL_DATE_TYPE_CONVERSION);
        registerTypeConversion(java.sql.Time.class, SQL_TIME_TYPE_CONVERSION);
        registerTypeConversion(java.sql.Timestamp.class,
                SQL_TIMESTAMP_TYPE_CONVERSION);

        // Add type conversions by class name
        registerTypeConversion(Object.class.getName(), OBJECT_TYPE_CONVERSION);
        registerTypeConversion(String.class.getName(), STRING_TYPE_CONVERSION);
        registerTypeConversion(Integer.class.getName(),
                INTEGER_TYPE_CONVERSION);
        registerTypeConversion(Double.class.getName(), DOUBLE_TYPE_CONVERSION);
        registerTypeConversion(Boolean.class.getName(),
                BOOLEAN_TYPE_CONVERSION);
        registerTypeConversion(Long.class.getName(), LONG_TYPE_CONVERSION);
        registerTypeConversion(Float.class.getName(), FLOAT_TYPE_CONVERSION);
        registerTypeConversion(Short.class.getName(), SHORT_TYPE_CONVERSION);
        registerTypeConversion(BigDecimal.class.getName(),
                BIG_DECIMAL_TYPE_CONVERSION);
        registerTypeConversion(Byte.class.getName(), BYTE_TYPE_CONVERSION);
        registerTypeConversion(Character.class.getName(),
                CHARACTER_TYPE_CONVERSION);
        registerTypeConversion(java.sql.Date.class.getName(),
                SQL_DATE_TYPE_CONVERSION);
        registerTypeConversion(java.sql.Time.class.getName(),
                SQL_TIME_TYPE_CONVERSION);
        registerTypeConversion(java.sql.Timestamp.class.getName(),
                SQL_TIMESTAMP_TYPE_CONVERSION);

        // Add type conversions by name
        registerTypeConversion(TYPE_UNKNOWN, UNKNOWN_TYPE_CONVERSION);
        registerTypeConversion(TYPE_OBJECT, OBJECT_TYPE_CONVERSION);
        registerTypeConversion(TYPE_STRING, STRING_TYPE_CONVERSION);
        registerTypeConversion(TYPE_INT, INTEGER_TYPE_CONVERSION);
        registerTypeConversion(TYPE_INTEGER, INTEGER_TYPE_CONVERSION);
        registerTypeConversion(TYPE_DOUBLE, DOUBLE_TYPE_CONVERSION);
        registerTypeConversion(TYPE_BOOLEAN, BOOLEAN_TYPE_CONVERSION);
        registerTypeConversion(TYPE_LONG, LONG_TYPE_CONVERSION);
        registerTypeConversion(TYPE_FLOAT, FLOAT_TYPE_CONVERSION);
        registerTypeConversion(TYPE_SHORT, SHORT_TYPE_CONVERSION);
        registerTypeConversion(TYPE_BIG_DECIMAL, BIG_DECIMAL_TYPE_CONVERSION);
        registerTypeConversion(TYPE_BYTE, BYTE_TYPE_CONVERSION);
        registerTypeConversion(TYPE_CHAR, CHARACTER_TYPE_CONVERSION);
        registerTypeConversion(TYPE_CHARACTER, CHARACTER_TYPE_CONVERSION);
        registerTypeConversion(TYPE_SQL_DATE, SQL_DATE_TYPE_CONVERSION);
        registerTypeConversion(TYPE_SQL_TIME, SQL_TIME_TYPE_CONVERSION);
        registerTypeConversion(TYPE_SQL_TIMESTAMP,
                SQL_TIMESTAMP_TYPE_CONVERSION);
    }

    /**
     * Cannot instantiated.
     */
    private TypeConverter() {
        super();
    }

    /**
     * Return the map of type conversion objects.The keys for the values in this
     * map may be arbitrary objects, but the values are of type
     * {@code TypeConversion}.
     *
     * @return Map
     */
    public static Map<Object, TypeConversion> getTypeConversions() {
        return TYPE_CONVERSIONS;
    }

    /**
     * Register a type conversion object under the specified key.This method can
     * be used by developers to register custom type conversion objects.
     *
     * @param key type key
     * @param conversion type conversion
     */
    private static void registerTypeConversion(final Object key,
            final TypeConversion conversion) {

        TYPE_CONVERSIONS.put(key, conversion);
    }

    /**
     * Convert an object to the type specified by the provided type key. A type
     * conversion object must have been previously registered under the provided
     * key in order for the conversion to succeed (with one exception, see
     * below).
     *
     * Note, this method treats type keys of type {@code Class} differently than
     * other type keys. That is, this method will check if the provided value is
     * the same as or a subclass of the specified class. If it is, this method
     * returns the value object immediately without attempting to convert its
     * type. One exception to this rule is if the provided type key is
     * {@code Object.class}, in which case the conversion is attempted anyway.
     * The reason for this deviation is that this key may have special meaning
     * based on the type of the provided value. For example, if the provided
     * value is a byte array, the {@code ObjectTypeConversion} class assumes it
     * is a serialized object and attempts to de-serialize it. Because all
     * objects, including arrays, are of type {@code Object}, this conversion
     * would never be attempted without this special handling. (Note that the
     * default conversion for type key {@code
     *      Object.class} is to simply return the original object.)
     *
     * @param typeKey The key under which the desired type conversion object has
     * been previously registered. Most commonly, this key should be a
     * {@code Class} object, a class name string, or a logical type string
     * represented by the various {@code TYPE_*} constants defined in this
     * class.
     *
     * @param value The value to convert to the specified target type
     *
     * @return The converted value object, or {@code null} if the original value
     * is {@code null}
     */
    private static Object asType(final Object typeKey, final Object value) {
        if (value == null) {
            return null;
        }

        if (typeKey == null) {
            return value;
        }

        // Check if the provided value is already of the target type
        if (typeKey instanceof Class && ((Class) typeKey) != Object.class) {
            if (((Class) typeKey).isInstance(value)) {
                return value;
            }
        }

        // Find the type conversion object
        TypeConversion conversion = TYPE_CONVERSIONS.get(typeKey);

        // Convert the value
        if (conversion != null) {
            return conversion.convertValue(value);
        } else {
            throw new IllegalArgumentException(
                    "Could not find type conversion for "
                    + "type \"" + typeKey + "\" (value = \""
                    + value + "\"");
        }
    }

    /**
     * Convert an object to byte.
     *
     * @param value value to convert
     * @return byte
     */
    public static byte asByte(final Object value) {
        return asByte(value, (byte) 0);
    }

    /**
     * Convert an object to byte with a fallback default value.
     *
     * @param value value to convert
     * @param defaultValue fallback default value
     * @return byte
     */
    public static byte asByte(final Object value, final byte defaultValue) {
        Object res = asType(Byte.class, value);
        if (res != null) {
            return ((Byte) res);
        }
        return defaultValue;
    }

    /**
     * Convert an object to short.
     *
     * @param value value to convert
     * @return short
     */
    public static short asShort(final Object value) {
        return asShort(value, (short) 0);
    }

    /**
     * Convert an object to short with a fallback default value.
     *
     * @param value value to convert
     * @param defaultValue fallback default value
     * @return short
     */
    public static short asShort(final Object value, final short defaultValue) {
        Object res = asType(Short.class, value);
        if (res != null) {
            return ((Short) res);
        }
        return defaultValue;
    }

    /**
     * Convert an object to int.
     *
     * @param value value to convert
     * @return int
     */
    public static int asInt(final Object value) {
        return asInt(value, 0);
    }

    /**
     * Convert an object to int with a fallback default value.
     *
     * @param value value to convert
     * @param defaultValue fallback default value
     * @return int
     */
    public static int asInt(final Object value, final int defaultValue) {
        Object res = asType(Integer.class, value);
        if (res != null) {
            return ((Integer) res);
        }
        return defaultValue;
    }

    /**
     * Convert an object to long.
     *
     * @param value value to convert
     * @return long
     */
    public static long asLong(final Object value) {
        return asLong(value, 0L);
    }

    /**
     * Convert an object to long with a fallback default value.
     *
     * @param value value to convert
     * @param defaultValue fallback default value
     * @return long
     */
    public static long asLong(final Object value, final long defaultValue) {
        Object res = asType(Long.class, value);
        if (res != null) {
            return ((Long) res);
        }
        return defaultValue;
    }

    /**
     * Convert an object to float.
     *
     * @param value value to convert
     * @return float
     */
    public static float asFloat(final Object value) {
        return asFloat(value, 0F);
    }

    /**
     * Convert an object to float with a fallback default value.
     *
     * @param value value to convert
     * @param defaultValue fallback default value
     * @return float
     */
    public static float asFloat(final Object value, final float defaultValue) {
        Object res = asType(Float.class, value);
        if (res != null) {
            return ((Float) res);
        }
        return defaultValue;
    }

    /**
     * Convert an object to double.
     *
     * @param value value to convert
     * @return double
     */
    public static double asDouble(final Object value) {
        return asDouble(value, 0D);
    }

    /**
     * Convert an object to double with a fallback default value.
     *
     * @param value value to convert
     * @param defaultValue fallback default value
     * @return double
     */
    public static double asDouble(final Object value,
            final double defaultValue) {

        Object res = asType(Double.class, value);
        if (res != null) {
            return ((Double) res);
        }
        return defaultValue;
    }

    /**
     * Convert an object to char.
     *
     * @param value value to convert
     * @return char
     */
    public static char asChar(final Object value) {
        return asChar(value, (char) 0);
    }

    /**
     * Convert an object to char with a fallback default value.
     *
     * @param value value to convert
     * @param defaultValue fallback default value
     * @return char
     */
    public static char asChar(final Object value, final char defaultValue) {
        Object res = asType(Character.class, value);
        if (res != null) {
            return ((Character) res);
        }
        return defaultValue;
    }

    /**
     * Convert an object to boolean.
     *
     * @param value value to convert
     * @return boolean
     */
    public static boolean asBoolean(final Object value) {
        return asBoolean(value, false);
    }

    /**
     * Convert an object to boolean with a fallback default value.
     *
     * @param value value to convert
     * @param defaultValue fallback default value
     * @return boolean
     */
    public static boolean asBoolean(final Object value,
            final boolean defaultValue) {

        Object res = asType(Boolean.class, value);
        if (res != null) {
            return ((Boolean) res);
        }
        return defaultValue;
    }

    /**
     * Convert an object to String.
     *
     * @param value value to convert
     * @return String
     */
    public static String asString(final Object value) {
        return (String) asType(String.class, value);
    }

    /**
     * Convert an object to String with a fallback default value.
     *
     * @param value value to convert
     * @param defaultValue fallback default value
     * @return String
     */
    public static String asString(final Object value,
            final String defaultValue) {

        Object res = asType(String.class, value);
        if (res != null) {
            return (String) res;
        }
        return defaultValue;
    }

    /**
     * Unknown type conversion.
     */
    private static final class UnknownTypeConversion implements TypeConversion {

        @Override
        public Object convertValue(final Object value) {
            return value;
        }
    }

    /**
     * String conversion.
     */
    private static final class StringTypeConversion implements TypeConversion {

        @Override
        public Object convertValue(final Object value) {
            if (value == null) {
                return null;
            }

            if (value.getClass().isArray()) {
                // This is a byte array; we can convert it to a string
                if (value.getClass().getComponentType() == Byte.TYPE) {
                    return new String((byte[]) value);
                } else if (value.getClass()
                        .getComponentType() == Character.TYPE) {
                    return new String((char[]) value);
                }
            } else if (!(value instanceof String)) {
                return value.toString();
            }
            return value;
        }
    }

    /**
     * Int conversion.
     */
    private static final class IntegerTypeConversion implements TypeConversion {

        @Override
        public Object convertValue(final Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof Integer)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    return null;
                } else {
                    return new Integer(v);
                }
            }

            return value;
        }
    }

    /**
     * Double conversion.
     */
    private static final class DoubleTypeConversion implements TypeConversion {

        @Override
        public Object convertValue(final Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof Double)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    return null;
                } else {
                    return new Double(v);
                }
            }

            return value;
        }
    }

    /**
     * Boolean conversion.
     */
    private static final class BooleanTypeConversion implements TypeConversion {

        @Override
        public Object convertValue(final Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof Boolean)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    return null;
                } else {
                    return Boolean.valueOf(v);
                }
            }

            return value;
        }
    }

    /**
     * Long conversion.
     */
    private static final class LongTypeConversion implements TypeConversion {

        @Override
        public Object convertValue(final Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof Long)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    return null;
                } else {
                    return new Long(v);
                }
            }

            return value;
        }
    }

    /**
     * Float conversion.
     */
    private static final class FloatTypeConversion implements TypeConversion {

        @Override
        public Object convertValue(final Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof Float)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    return null;
                } else {
                    return new Float(v);
                }
            }

            return value;
        }
    }

    /**
     * Short conversion.
     */
    private static final class ShortTypeConversion implements TypeConversion {

        @Override
        public Object convertValue(final Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof Short)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    return null;
                } else {
                    return new Short(v);
                }
            }

            return value;
        }
    }

    /**
     * BigDecimal conversion.
     */
    private static final class BigDecimalTypeConversion
            implements TypeConversion {

        @Override
        public Object convertValue(final Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof BigDecimal)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    return null;
                } else {
                    return new BigDecimal(v);
                }
            }

            return value;
        }
    }

    /**
     * Byte conversion.
     */
    private static final class ByteTypeConversion implements TypeConversion {

        @Override
        public Object convertValue(final Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof Byte)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    return null;
                } else {
                    return new Byte(v);
                }
            }

            return value;
        }
    }

    /**
     * Char conversion.
     */
    private static final class CharacterTypeConversion
            implements TypeConversion {

        @Override
        public Object convertValue(final Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof Character)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    return null;
                } else {
                    return v.charAt(0);
                }
            }

            return value;
        }
    }

    /**
     * SqlDate conversion.
     */
    private static final class SqlDateTypeConversion
            implements TypeConversion {

        @Override
        public Object convertValue(final Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof java.sql.Date)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    return null;
                } else {
                    // Value must be in the "yyyy-mm-dd" format
                    return java.sql.Date.valueOf(v);
                }
            }

            return value;
        }
    }

    /**
     * SqlTime conversion.
     */
    private static final class SqlTimeTypeConversion
            implements TypeConversion {

        @Override
        public Object convertValue(final Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof java.sql.Time)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    return null;
                } else {
                    // Value must be in the "hh:mm:ss" format
                    return java.sql.Time.valueOf(v);
                }
            }

            return value;
        }
    }

    /**
     * SqlTimestamp conversion.
     */
    private static final class SqlTimestampTypeConversion
            implements TypeConversion {

        @Override
        public Object convertValue(final Object value) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof java.sql.Timestamp)) {
                String v = value.toString();
                if (v.trim().length() == 0) {
                    return null;
                } else {
                    // Value must be in the "yyyy-mm-dd hh:mm:ss.fffffffff"
                    // format
                    return java.sql.Timestamp.valueOf(v);
                }
            }

            return value;
        }
    }

    /**
     * Object conversion.
     */
    private static final class ObjectTypeConversion implements TypeConversion {

        @Override
        public Object convertValue(final Object value) {
            return value;
        }
    }
}
