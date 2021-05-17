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

package org.glassfish.ejb.deployment.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.InterceptorBindingDescriptor;
import org.glassfish.ejb.deployment.descriptor.InterceptorBindingDescriptor.BindingType;

public class InterceptorBindingTranslator {

    private List<InterceptorBindingDescriptor> interceptorBindings;
    private EjbBundleDescriptorImpl ejbBundle;

    private List<String> defaultInterceptorChain =
        new LinkedList<String>();

    private List<String> classInterceptorChain =
        new LinkedList<String>();

    private boolean hasTotalClassLevelOrdering = false;
    private List<String> totalClassLevelOrdering =
        new LinkedList<String>();

    private Map<MethodDescriptor, LinkedList<String>> methodInterceptorsMap =
        new HashMap<MethodDescriptor, LinkedList<String>>();

    // true if there are 0 bindings.
    private boolean isEmpty;

    public InterceptorBindingTranslator(EjbBundleDescriptorImpl bundle) {

        ejbBundle = bundle;
        interceptorBindings = ejbBundle.getInterceptorBindings();

        if( interceptorBindings.isEmpty() ) {

            isEmpty = true;

        } else {

            validateInterceptors();

        }


    }

    public TranslationResults apply(String ejbName) {

        if( isEmpty ) {
            return new TranslationResults();
        }

        defaultInterceptorChain.clear();
        classInterceptorChain.clear();

        hasTotalClassLevelOrdering = false;
        totalClassLevelOrdering.clear();

        methodInterceptorsMap.clear();

        // Do a pass through default interceptor bindings.
        for(InterceptorBindingDescriptor binding : interceptorBindings) {

            if( binding.getBindingType() == BindingType.DEFAULT ) {
                defaultInterceptorChain.addAll
                    (binding.getInterceptorClasses());
            }

        }

        // Do a pass through Class level bindings.
        for(InterceptorBindingDescriptor binding : interceptorBindings) {

            if( binding.getBindingType() == BindingType.CLASS ) {

                if( binding.getEjbName().equals(ejbName) ) {
                    processClassLevelBinding(binding);
                }
            }

        }

        // Now do method-level bindings.

        Map<MethodDescriptor, List<InterceptorBindingDescriptor>>
            methodBindings = new HashMap<MethodDescriptor,
                                         List<InterceptorBindingDescriptor>>();

        // First build a map of all business methods for the current
        // ejb that have binding information, and their associated
        // bindings.
        for(InterceptorBindingDescriptor binding : interceptorBindings) {

            if( (binding.getEjbName().equals(ejbName)) &&
                (binding.getBindingType() == BindingType.METHOD) ) {

                MethodDescriptor method = binding.getBusinessMethod();

                List<InterceptorBindingDescriptor> methodBindingDescs =
                    methodBindings.get(method);
                if( methodBindingDescs == null ) {
                    methodBindingDescs =
                        new LinkedList<InterceptorBindingDescriptor>();
                }

                methodBindingDescs.add(binding);

                methodBindings.put(method, methodBindingDescs);
            }

        }

        for(Map.Entry<MethodDescriptor, List<InterceptorBindingDescriptor>> next
                : methodBindings.entrySet()) {
            processMethod(next.getKey(), next.getValue());
        }

        TranslationResults results = buildResults();

        return results;

    }

    private void processClassLevelBinding(InterceptorBindingDescriptor
                                          binding) {

        if( binding.getExcludeDefaultInterceptors() ) {
            defaultInterceptorChain.clear();
        }

        if( binding.getIsTotalOrdering() ) {

            hasTotalClassLevelOrdering = true;
            totalClassLevelOrdering.clear();
            totalClassLevelOrdering.addAll(binding.getInterceptorClasses());

            // totalClassLevelOrdering will take precedence, but keep
            // classInterceptorChain updated to contain class-level, but not
            // default-level, interceptors.  These might be needed during
            // method-level exclude-class-interceptors processing.
            for(String next : binding.getInterceptorClasses()) {
                if( !defaultInterceptorChain.contains(next) ) {
                    if( !classInterceptorChain.contains(next) ) {
                        classInterceptorChain.add(next);
                    }
                }
            }
        } else {
            classInterceptorChain.addAll(binding.getInterceptorClasses());
        }

    }

    private void processMethod(MethodDescriptor businessMethod,
                               List<InterceptorBindingDescriptor> bindings) {

        LinkedList<String> tempDefaultInterceptorChain =
            new LinkedList<String>();

        LinkedList<String> tempClassInterceptorChain =
            new LinkedList<String>();

        LinkedList<String> tempMethodInterceptorChain =
            new LinkedList<String>();

        if( hasTotalClassLevelOrdering ) {
            tempClassInterceptorChain.addAll(totalClassLevelOrdering);
        } else {
            tempDefaultInterceptorChain.addAll(defaultInterceptorChain);
            tempClassInterceptorChain.addAll(classInterceptorChain);
        }

        for(InterceptorBindingDescriptor nextBinding : bindings) {

            if( nextBinding.getExcludeDefaultInterceptors() ) {
                if( hasTotalClassLevelOrdering ) {
                    tempClassInterceptorChain.removeAll
                        (defaultInterceptorChain);
                } else {
                    tempDefaultInterceptorChain.clear();
                }
            }

            if( nextBinding.getExcludeClassInterceptors() ) {
                if( hasTotalClassLevelOrdering ) {
                    tempClassInterceptorChain.removeAll
                        (classInterceptorChain);
                } else {
                    tempClassInterceptorChain.clear();
                }
            }

            if( nextBinding.getIsTotalOrdering() ) {
                tempDefaultInterceptorChain.clear();
                tempClassInterceptorChain.clear();
                tempMethodInterceptorChain.clear();
            }

            tempMethodInterceptorChain.addAll
                (nextBinding.getInterceptorClasses());

        }

        LinkedList<String> methodInterceptors = new LinkedList<String>();
        methodInterceptors.addAll(tempDefaultInterceptorChain);
        methodInterceptors.addAll(tempClassInterceptorChain);
        methodInterceptors.addAll(tempMethodInterceptorChain);

        methodInterceptorsMap.put(businessMethod, methodInterceptors);

    }

    private TranslationResults buildResults() {

        TranslationResults results = new TranslationResults();

        if( hasTotalClassLevelOrdering ) {

            for(String next : totalClassLevelOrdering ) {
                EjbInterceptor interceptor =
                    ejbBundle.getInterceptorByClassName(next);
                results.allInterceptorClasses.add(interceptor);
                results.classInterceptorChain.add(interceptor);
            }

        } else {

            for(String next : defaultInterceptorChain) {
                EjbInterceptor interceptor =
                    ejbBundle.getInterceptorByClassName(next);

                results.allInterceptorClasses.add(interceptor);
                results.classInterceptorChain.add(interceptor);
            }

            for(String next : classInterceptorChain) {
                EjbInterceptor interceptor =
                    ejbBundle.getInterceptorByClassName(next);

                results.allInterceptorClasses.add(interceptor);
                results.classInterceptorChain.add(interceptor);
            }
        }
        Iterator<Map.Entry<MethodDescriptor, LinkedList<String>>> entryIterator =
                methodInterceptorsMap.entrySet().iterator();
        while(entryIterator.hasNext()) {
            Map.Entry<MethodDescriptor, LinkedList<String>> entry = entryIterator.next();
            List<String> interceptorClassChain = entry.getValue();

            List<EjbInterceptor> interceptorChain =
                    new LinkedList<EjbInterceptor>();

            for(String nextClass : interceptorClassChain) {
                EjbInterceptor interceptor =
                        ejbBundle.getInterceptorByClassName(nextClass);

                results.allInterceptorClasses.add(interceptor);
                interceptorChain.add(interceptor);

            }

            results.methodInterceptorsMap.put(entry.getKey(), interceptorChain);
        }

        return results;
    }

    private void validateInterceptors() {

        // Make sure there's an interceptor defined for every interceptor
        // class name listed in the bindings.
        for(InterceptorBindingDescriptor binding : interceptorBindings) {

            for(String interceptor : binding.getInterceptorClasses()) {

                if(ejbBundle.getInterceptorByClassName(interceptor) == null) {
                    throw new IllegalStateException
                        ("Interceptor binding contains an interceptor class " +
                         " name = " + interceptor +
                         " that is not defined as an interceptor");
                }
            }
        }

    }

    public static class TranslationResults {

        public Set<EjbInterceptor> allInterceptorClasses;

        public List<EjbInterceptor> classInterceptorChain;

        public Map<MethodDescriptor, List<EjbInterceptor>>
            methodInterceptorsMap;

        public TranslationResults() {
            allInterceptorClasses = new HashSet<EjbInterceptor>();
            classInterceptorChain = new LinkedList<EjbInterceptor>();
            methodInterceptorsMap =
                new HashMap<MethodDescriptor, List<EjbInterceptor>>();
        }

    }

}

