/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.jvnet.hk2.config.tiger;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import org.glassfish.hk2.utilities.reflection.GenericArrayTypeImpl;

/**
 * Type arithmetic functions.
 *
 * @author Kohsuke Kawaguchi
 */
public class Types {

    private static final TypeVisitor<Type, Class> baseClassFinder = new TypeVisitor<>() {

        @Override
        public Type onClass(Class c, Class sup) {
            // t is a raw type
            if (sup == c) {
                return sup;
            }

            Type r;

            Type sc = c.getGenericSuperclass();
            if (sc != null) {
                r = visit(sc, sup);
                if (r != null) {
                    return r;
                }
            }

            for (Type i : c.getGenericInterfaces()) {
                r = visit(i, sup);
                if (r != null) {
                    return r;
                }
            }

            return null;
        }


        @Override
        public Type onParameterizdType(ParameterizedType p, Class sup) {
            Class raw = (Class) p.getRawType();
            if (raw == sup) {
                // p is of the form sup<...>
                return p;
            }
            // recursively visit super class/interfaces
            Type r = raw.getGenericSuperclass();
            if (r != null) {
                r = visit(bind(r, raw, p), sup);
            }
            if (r != null) {
                return r;
            }
            for (Type i : raw.getGenericInterfaces()) {
                r = visit(bind(i, raw, p), sup);
                if (r != null) {
                    return r;
                }
            }
            return null;
        }


        @Override
        public Type onGenericArray(GenericArrayType g, Class sup) {
            // not clear what I should do here
            return null;
        }


        @Override
        public Type onVariable(TypeVariable v, Class sup) {
            return visit(v.getBounds()[0], sup);
        }


        @Override
        public Type onWildcard(WildcardType w, Class sup) {
            // not clear what I should do here
            return null;
        }


        /**
         * Replaces the type variables in {@code t} by its actual arguments.
         *
         * @param decl
         *            provides a list of type variables. See
         *            {@link GenericDeclaration#getTypeParameters()}
         * @param args
         *            actual arguments. See {@link ParameterizedType#getActualTypeArguments()}
         */
        private Type bind(Type t, GenericDeclaration decl, ParameterizedType args) {
            return binder.visit(t, new BinderArg(decl, args.getActualTypeArguments()));
        }
    };

    private static class BinderArg {

        final TypeVariable[] params;
        final Type[] args;

        BinderArg(TypeVariable[] params, Type[] args) {
            this.params = params;
            this.args = args;
            assert params.length == args.length;
        }


        public BinderArg(GenericDeclaration decl, Type[] args) {
            this(decl.getTypeParameters(), args);
        }


        Type replace(TypeVariable v) {
            for (int i = 0; i < params.length; i++) {
                if (params[i].equals(v)) {
                    return args[i];
                }
            }
            return v; // this is a free variable
        }
    }

    private static final TypeVisitor<Type, BinderArg> binder = new TypeVisitor<>() {

        @Override
        public Type onClass(Class c, BinderArg args) {
            return c;
        }


        @Override
        public Type onParameterizdType(ParameterizedType p, BinderArg args) {
            Type[] params = p.getActualTypeArguments();

            boolean different = false;
            for (int i = 0; i < params.length; i++) {
                Type t = params[i];
                params[i] = visit(t, args);
                different |= t != params[i];
            }

            Type newOwner = p.getOwnerType();
            if (newOwner != null) {
                newOwner = visit(newOwner, args);
            }
            different |= p.getOwnerType() != newOwner;

            if (!different) {
                return p;
            }

            return new ParameterizedTypeImpl((Class<?>) p.getRawType(), params, newOwner);
        }


        @Override
        public Type onGenericArray(GenericArrayType g, BinderArg types) {
            Type c = visit(g.getGenericComponentType(), types);
            if (c == g.getGenericComponentType()) {
                return g;
            }

            return new GenericArrayTypeImpl(c);
        }


        @Override
        public Type onVariable(TypeVariable v, BinderArg types) {
            return types.replace(v);
        }


        @Override
        public Type onWildcard(WildcardType w, BinderArg types) {
            // TODO: this is probably still incorrect
            // bind( "? extends T" ) with T= "? extends Foo" should be "? extends Foo",
            // not "? extends (? extends Foo)"
            Type[] lb = w.getLowerBounds();
            Type[] ub = w.getUpperBounds();
            boolean diff = false;

            for (int i = 0; i < lb.length; i++) {
                Type t = lb[i];
                lb[i] = visit(t, types);
                diff |= (t != lb[i]);
            }

            for (int i = 0; i < ub.length; i++) {
                Type t = ub[i];
                ub[i] = visit(t, types);
                diff |= (t != ub[i]);
            }

            if (!diff) {
                return w;
            }

            return new WildcardTypeImpl(lb, ub);
        }
    };

    /**
     * Gets the parameterization of the given base type.
     * <p>
     * For example, given the following
     *
     * <pre>
     * <xmp>
     * interface Foo<T> extends List<List<T>> {}
     * interface Bar extends Foo<String> {}
     * </xmp>
     * </pre>
     *
     * This method works like this:
     *
     * <pre>
     * <xmp>
     * getBaseClass( Bar, List ) = List<List<String>
     * getBaseClass( Bar, Foo  ) = Foo<String>
     * getBaseClass( Foo<? extends Number>, Collection ) = Collection<List<? extends Number>>
     * getBaseClass( ArrayList<? extends BigInteger>, List ) = List<? extends BigInteger>
     * </xmp>
     * </pre>
     *
     * @param type
     *            The type that derives from {@code baseType}
     * @param baseType
     *            The class whose parameterization we are interested in.
     * @return
     *         The use of {@code baseType} in {@code type}.
     *         or null if the type is not assignable to the base type.
     */
    public static Type getBaseClass(Type type, Class baseType) {
        return baseClassFinder.visit(type, baseType);
    }


    /**
     * Gets the display name of the type object
     *
     * @return
     *         a human-readable name that the type represents.
     */
    public static String getTypeName(Type type) {
        if (type instanceof Class) {
            Class c = (Class) type;
            if (c.isArray()) {
                return getTypeName(c.getComponentType()) + "[]";
            }
            return c.getName();
        }
        return type.toString();
    }


    /**
     * Checks if {@code sub} is a sub-type of {@code sup}.
     */
    public static boolean isSubClassOf(Type sub, Type sup) {
        return erasure(sup).isAssignableFrom(erasure(sub));
    }

    /**
     * Implements the logic for {@link #erasure(Type)}.
     */
    private static final TypeVisitor<Class, Void> eraser = new TypeVisitor<>() {

        @Override
        public Class onClass(Class c, Void v) {
            return c;
        }


        @Override
        public Class onParameterizdType(ParameterizedType p, Void v) {
            // TODO: why getRawType returns Type? not Class?
            return visit(p.getRawType(), null);
        }


        @Override
        public Class onGenericArray(GenericArrayType g, Void v) {
            return Array.newInstance(visit(g.getGenericComponentType(), null), 0).getClass();
        }


        @Override
        public Class onVariable(TypeVariable t, Void v) {
            return visit(t.getBounds()[0], null);
        }


        @Override
        public Class onWildcard(WildcardType w, Void v) {
            return visit(w.getUpperBounds()[0], null);
        }
    };

    /**
     * Returns the {@link Class} representation of the given type.
     * This corresponds to the notion of the erasure in JSR-14.
     * <p>
     * It made me realize how difficult it is to define the common navigation
     * layer for two different underlying reflection library. The other way
     * is to throw away the entire parameterization and go to the wrapper approach.
     */
    public static <T> Class<T> erasure(Type t) {
        return eraser.visit(t, null);
    }


    /**
     * Returns the {@link Type} object that represents {@code clazz&lt;T1,T2,T3>}.
     */
    public static ParameterizedType createParameterizedType(Class rawType, Type... arguments) {
        return new ParameterizedTypeImpl(rawType, arguments, null);
    }


    /**
     * Checks if the type is an array type.
     */
    public static boolean isArray(Type t) {
        if (t instanceof Class) {
            Class c = (Class) t;
            return c.isArray();
        }
        if (t instanceof GenericArrayType) {
            return true;
        }
        return false;
    }


    /**
     * Checks if the type is an array type but not byte[].
     */
    public static boolean isArrayButNotByteArray(Type t) {
        if (t instanceof Class) {
            Class c = (Class) t;
            return c.isArray() && c != byte[].class;
        }
        if (t instanceof GenericArrayType) {
            t = ((GenericArrayType) t).getGenericComponentType();
            return t != Byte.TYPE;
        }
        return false;
    }


    /**
     * Gets the component type of the array.
     *
     * @param t
     *            must be an array.
     */
    public static Type getComponentType(Type t) {
        if (t instanceof Class) {
            Class c = (Class) t;
            return c.getComponentType();
        }
        if (t instanceof GenericArrayType) {
            return ((GenericArrayType) t).getGenericComponentType();
        }

        throw new IllegalArgumentException();
    }


    /**
     * Gets the i-th type argument from a parameterized type.
     * <p>
     * Unlike {@link #getTypeArgument(Type, int, Type)}, this method
     * throws {@link IllegalArgumentException} if the given type is
     * not parameterized.
     */
    public static Type getTypeArgument(Type type, int i) {
        Type r = getTypeArgument(type, i, null);
        if (r == null) {
            throw new IllegalArgumentException();
        }
        return r;
    }


    /**
     * Gets the i-th type argument from a parameterized type.
     * <p>
     * For example, {@code getTypeArgument([Map<Integer,String>],0)=Integer}
     * If the given type is not a parameterized type, returns the specified
     * default value.
     * <p>
     * This is convenient for handling raw types and parameterized types uniformly.
     *
     * @throws IndexOutOfBoundsException
     *             If i is out of range.
     */
    public static Type getTypeArgument(Type type, int i, Type defaultValue) {
        if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            return fix(p.getActualTypeArguments()[i]);
        } else {
            return defaultValue;
        }
    }


    /**
     * Checks if the given type is a primitive type.
     */
    public static boolean isPrimitive(Type type) {
        if (type instanceof Class) {
            Class c = (Class) type;
            return c.isPrimitive();
        }
        return false;
    }


    public static boolean isOverriding(Method method, Class base) {
        // this isn't actually correct,
        // as the JLS considers
        // class Derived extends Base<Integer> {
        // Integer getX() { ... }
        // }
        // class Base<T> {
        // T getX() { ... }
        // }
        // to be overrided. Handling this correctly needs a careful implementation

        String name = method.getName();
        Class[] params = method.getParameterTypes();

        while (base != null) {
            try {
                if (null != base.getDeclaredMethod(name, params)) {
                    return true;
                }
            } catch (NoSuchMethodException e) {
                // recursively go into the base class
            }

            base = base.getSuperclass();
        }

        return false;
    }


    /**
     * JDK 5.0 has a bug of createing {@link GenericArrayType} where it shouldn't.
     * fix that manually to work around the problem.
     * See bug 6202725.
     */
    private static Type fix(Type t) {
        if (!(t instanceof GenericArrayType)) {
            return t;
        }

        GenericArrayType gat = (GenericArrayType) t;
        if (gat.getGenericComponentType() instanceof Class) {
            Class c = (Class) gat.getGenericComponentType();
            return Array.newInstance(c, 0).getClass();
        }

        return t;
    }

}
