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

package org.glassfish.admin.amx.util.stringifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.glassfish.admin.amx.util.ArrayConversion;
import org.glassfish.admin.amx.util.ClassUtil;

/**
 * Stringifies an Object in the "best" possible way, using the
 * StringifierRegistry.DEFAULT registry and/or internal logic.
 */
public final class SmartStringifier implements Stringifier {

    public static final SmartStringifier DEFAULT = new SmartStringifier(",");
    private final String mMultiDelim;
    private final boolean mEncloseArrays;
    private StringifierRegistry mRegistry;

    public SmartStringifier() {
        this(",");
    }

    public SmartStringifier(String multiDelim) {
        this(multiDelim, true);
    }

    public SmartStringifier(String multiDelim, boolean encloseArrays) {
        this(StringifierRegistryImpl.DEFAULT, multiDelim, encloseArrays);
    }

    public SmartStringifier(StringifierRegistry registry, String multiDelim, boolean encloseArrays) {
        mMultiDelim = multiDelim;
        mEncloseArrays = encloseArrays;
        mRegistry = registry;
    }

    public void setRegistry(StringifierRegistry registry) {
        mRegistry = registry;
    }
    private final static Class[] STRINGIFIER_REGISTRY_LOOKUPS = {
        Iterator.class,
        Collection.class,
        HashMap.class
    };

    private Stringifier getStringifier(final Object target) {
        if (target == null) {
            return (null);
        }

        final Class<?> targetClass = target.getClass();

        Stringifier stringifier = mRegistry.lookup(targetClass);

        if (target instanceof javax.management.ObjectName) {
            assert (stringifier != null);
        }

        if (stringifier == null) {
            // exact match failed...look for match in defined order
            final int numLookups = STRINGIFIER_REGISTRY_LOOKUPS.length;
            for (int i = 0; i < numLookups; ++i) {
                final Class<?> theClass = STRINGIFIER_REGISTRY_LOOKUPS[ i];

                stringifier = mRegistry.lookup(theClass);
                if (stringifier != null && theClass.isAssignableFrom(target.getClass())) {
                    break;
                }
            }
        }

        if (stringifier == null) {
            // see if there is a Stringifier for any superclass;
            Class tempClass = targetClass;
            while (tempClass != Object.class) {
                stringifier = mRegistry.lookup(tempClass);
                if (stringifier != null) {
                    break;
                }

                tempClass = tempClass.getSuperclass();
            }

        }

        if (stringifier == null) {
            final Class[] interfaces = targetClass.getInterfaces();
            if (interfaces.length != 0) {
                stringifier = new InterfacesStringifier(interfaces);
            }
        }

        return (stringifier);
    }

    private String smartStringify(Object target) {
        String result = null;

        if (ClassUtil.objectIsArray(target)) {


            final Class elementClass =
                    ClassUtil.getArrayElementClass(target.getClass());

            Object[] theArray = ClassUtil.isPrimitiveClass(elementClass) ?
                ArrayConversion.toAppropriateType(target) :
                (Object[]) target;

            result = ArrayStringifier.stringify(theArray, mMultiDelim, this);
            if (mEncloseArrays) {
                result = "{" + result + "}";
            }
        } else {
            Stringifier stringifier = getStringifier(target);

            if (stringifier != null && stringifier.getClass() == this.getClass()) {
                // avoid recursive call to self
                stringifier = null;
            }

            if (stringifier != null) {
                result = stringifier.stringify(target);
            }
        }

        if (result == null) {
            result = target.toString();
        }

        return (result);
    }

    public static String toString(Object target) {
        return (DEFAULT.stringify(target));
    }

    @Override
    public String stringify(Object target) {
        if (target == null) {
            return ("<null>");
        }

        return (smartStringify(target));
    }
}
