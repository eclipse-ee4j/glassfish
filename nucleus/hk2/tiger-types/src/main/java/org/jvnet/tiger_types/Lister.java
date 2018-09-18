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

package org.jvnet.tiger_types;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.EnumSet;

/**
 * Abstracts away the process of creating a collection (array, {@link List}, etc)
 * of items.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Lister<T> {
    /**
     * Type of the individual item
     */
    public final Class itemType;
    public final Type itemGenericType;

    protected final Collection r;

    protected Lister(Class itemType, Type itemGenericType) {
        this(itemType,itemGenericType,new ArrayList());
    }

    protected Lister(Class itemType, Type itemGenericType, Collection r) {
        this.itemType = itemType;
        this.itemGenericType = itemGenericType;
        this.r = r;
    }

    public void add(Object o) {
        r.add(o);
    }

    public abstract T toCollection();

    /**
     * Creates a {@link Lister} instance that produces the given type.
     */
    public static Lister create(Type t) {
        return create(Types.erasure(t),t);
    }

    /**
     * Creates a {@link Lister} instance that produces the given type.
     *
     * @param c
     *      The erasure version of 't'. This is taken
     *      as a parameter as a performance optimizaiton.
     *
     * @return
     *      null if the given type doesn't look like a collection.
     * @throws IllegalArgumentException
     *      if the given type does look like a collection yet this implementation
     *      is not capable of how to handle it.
     */
    public static <T> Lister<T> create(Class<T> c, Type t) {
        if(c.isArray()) {
            // array
            Class<?> ct = c.getComponentType();
            return new Lister(ct,ct) {
                public Object toCollection() {
                    return r.toArray((Object[])Array.newInstance(itemType,r.size()));
                }
            };
        }
        if(Collection.class.isAssignableFrom(c)) {
            final Type col = Types.getBaseClass(t, Collection.class);

            final Type itemType;
            if (col instanceof ParameterizedType)
                itemType = Types.getTypeArgument(col, 0);
            else
                itemType = Object.class;

            Collection items=null;
            try {
                items = (Collection)c.newInstance();
            } catch (InstantiationException e) {
                // this is not instanciable. Try known instanciable versions.
                for (Class ct : CONCRETE_TYPES) {
                    if(c.isAssignableFrom(ct)) {
                        try {
                            items = (Collection)ct.newInstance();
                            break;
                        } catch (InstantiationException x) {
                            throw toError(x);
                        } catch (IllegalAccessException x) {
                            throw toError(x);
                        }
                    }
                }
                // EnumSet
                if(items==null && c==EnumSet.class) {
                    items = EnumSet.noneOf(Types.erasure(itemType).asSubclass(Enum.class));
                }
                if(items==null)
                    throw new IllegalArgumentException("Don't know how to instanciate "+c);
            } catch (IllegalAccessException e) {
                throw toError(e);
            }

            return new Lister(Types.erasure(itemType),itemType,items) {
                public Object toCollection() {
                    return r;
                }
            };
        }

        return null;
    }

    private static IllegalAccessError toError(IllegalAccessException e) {
        IllegalAccessError x = new IllegalAccessError();
        x.initCause(e);
        return x;
    }

    private static InstantiationError toError(InstantiationException e) {
        InstantiationError x = new InstantiationError();
        x.initCause(e);
        return x;
    }

    private static final Class[] CONCRETE_TYPES = new Class[] {
        ArrayList.class,
        HashSet.class
    };
    public Type getItemGenericType() {
      return this.itemGenericType;
    }

}
