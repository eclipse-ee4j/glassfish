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

package com.sun.ejb.containers.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.lang.reflect.Method;

/**
 * This is an optimized map for resolving java.lang.reflect.Method objects.
 * Doing a method lookup, even on an unsynchronized Map, can be an
 * expensive operation, in many cases taking multiple microseconds.
 * In most situations this overhead is negligible, but it can be noticeable
 * when performed in the common path of a local ejb invocation, where our
 * goal is to be as fast as a raw java method call.
 *
 * A MethodMap must be created with an existing Map and is immutable after
 * construction(except for clear()).
 * It does not support the optional Map operations
 * put, putAll, and remove.  NOTE that these operations could
 * be implemented but are not necessary at this point since the main use
 * is for the container's method info, which is invariant after initialization.
 *
 * As this is a map for Method objects, null keys are not supported.
 * This map is unsynchronized.
 */
public final class MethodMap extends HashMap {

    // If bucket size is not specified by caller, this is the number
    // of buckets per method that will be created.
    private static final int DEFAULT_BUCKET_MULTIPLIER = 20;

    private int numBuckets_;

    // Sparse array of method info.  Each element represents one method
    // or is null.  Array is hashed by a combination of the
    // method name's hashcode and its parameter length.  See
    // getBucket() below for more details.
    //
    // Note that reference equality is not very useful on Method since
    // it defines the equals() method and each call to Class.getMethods()
    // returns new Method instances.
    private MethodInfo[] methodInfo_;

    public MethodMap(Map methodMap) {
        super(methodMap);

        numBuckets_ = methodMap.size() * DEFAULT_BUCKET_MULTIPLIER;

        buildLookupTable(methodMap);
    }

    public MethodMap(Map methodMap, int numBuckets) {
        super(methodMap);

        if( numBuckets <= 0 ) {
            throw new IllegalArgumentException
                ("Invalid value of numBuckets = " + numBuckets);
        }

        numBuckets_ = numBuckets;
        buildLookupTable(methodMap);
    }

    public Object put(Object key, Object value) {
        throw new UnsupportedOperationException();
    }
    public void putAll(Map t) {
        throw new UnsupportedOperationException();
    }
    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    public Object get(Object key) {

        if( key instanceof Method ) {
            Method m = (Method) key;
            Class[] paramTypes = m.getParameterTypes();
            return get(m, paramTypes.length);
        }

        return null;
    }

    public Object get(Method m, int numParams) {

        if( methodInfo_ == null ) {
            return null;
        } else if( numParams < 0 ) {
            throw new IllegalStateException
                ("invalid numParams = " + numParams);
        }

        Object value = null;

        MethodInfo methodInfo = methodInfo_[getBucket(m, numParams)];

        if( methodInfo != null) {
            // Declaring classes must be the same for methods to be equal.
            if(methodInfo.declaringClass == m.getDeclaringClass()) {
                value = methodInfo.value;
            }
        }

        return (value != null) ? value : super.get(m);

    }

    public void clear() {

        if( methodInfo_ != null ) {
            methodInfo_ = null;
            super.clear();
        }

    }

    private void buildLookupTable(Map methodMap) {

        methodInfo_ = new MethodInfo[numBuckets_];

        Set occupied = new HashSet();

        for(Iterator iter = methodMap.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            Object nextObj = entry.getKey();
            Method next = null;

            if( nextObj == null ) {
                throw new IllegalStateException("null keys not supported");
            } else if( nextObj instanceof Method ) {
                next = (Method) nextObj;
            } else {
                throw new IllegalStateException
                    ("invalid key type = " + nextObj.getClass() +
                     " key must be of type java.lang.reflect.Method");
            }

            int bucket = getBucket(next);

            if( !occupied.contains(bucket) ) {

                MethodInfo methodInfo = new MethodInfo();
                methodInfo.value = entry.getValue();

                // cache declaring class so we can avoid the method call
                // during lookup operation.
                methodInfo.declaringClass = next.getDeclaringClass();

                methodInfo_[bucket] = methodInfo;

                occupied.add(bucket);

            } else {
                // there's a clash for this bucket, so null it out and
                // defer to backing HashMap for results.
                methodInfo_[bucket] = null;
            }
        }
    }

    private final int getBucket(Method m) {

        // note : getParameterTypes is guaranteed to be 0-length array
        // (as opposed to null) for a method with no arguments.
        Class[] paramTypes = m.getParameterTypes();

        return getBucket(m, paramTypes.length);
    }

    private final int getBucket(Method m, int numParams) {

        String methodName = m.getName();

        // The normal Method.hashCode() method makes 5 method calls
        // and does not cache the result.  Here, we use the method name's
        // hashCode since String.hashCode() makes 0 method calls *and* caches
        // the result.   The tradeoff is that using only method name will
        // not account for overloaded methods, so we also add the number of
        // parameters to the calculation.  In many cases, the caller
        // already knows the number of parameters, so it can be passed in
        // to the lookup.  This gives up some encapsulation for
        // speed.   It will result in better performance because
        // we can skip the call to m.getClass().getParameterTypes(),
        // which results in multiple method calls and can involve some
        // expensive copying depending of the types themselves.
        // Of course, this still won't account for the case where methods
        // are overloaded with the same number of parameters but different
        // types.  However, the cache miss penalty should be small enough
        // in this case that it's a fair tradeoff.  Adding anything else
        // to the hashcode calculation will have too large an impact on the
        // common case.

        int hashCode = methodName.hashCode();

        // account for negative hashcodes
        hashCode = (hashCode >= 0) ? hashCode : (hashCode * -1);
        hashCode = (hashCode > numParams) ?
            (hashCode - numParams) : (hashCode + numParams);
        return (hashCode % numBuckets_);
    }


    private static class MethodInfo {
        public Class declaringClass;
        public Object value;
    }
}
