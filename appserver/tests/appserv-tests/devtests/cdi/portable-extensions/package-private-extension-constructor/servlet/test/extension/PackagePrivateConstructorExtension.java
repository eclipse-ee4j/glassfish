/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.extension;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessBean;

import test.beans.DuplicateTestBean;

public class PackagePrivateConstructorExtension implements Extension{
    public static boolean beforeBeanDiscoveryCalled = false;
    public static boolean afterBeanDiscoveryCalled = false;
    public static boolean processAnnotatedTypeCalled = false;
    public static boolean packagePrivateConstructorCalled = false;
    private Collection<Class<?>> enabledInterceptors;
    /* package private */ PackagePrivateConstructorExtension(){
        System.out.println("In MyExtension ctor");
        PackagePrivateConstructorExtension.packagePrivateConstructorCalled = true;
        this.enabledInterceptors = Collections.synchronizedSet(new
                HashSet<Class<?>>());
    }

   @SuppressWarnings("unused")
   void observeInterceptors(@Observes ProcessBean<?> pmb)
   {
      if (pmb.getBean() instanceof Interceptor<?>)
      {
         this.enabledInterceptors.add(pmb.getBean().getBeanClass());
      }
   }

   Collection<Class<?>> getEnabledInterceptors()
   {
      return enabledInterceptors;
   }

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bdd){
        System.out.println("MyExtension::beforeBeanDiscovery" + bdd);
        beforeBeanDiscoveryCalled = true;
    }

    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat){
        System.out.println("MyExtension:Process annotated type" + pat.getAnnotatedType().getBaseType());
        processAnnotatedTypeCalled = true;
        //Vetoing the processing of DuplicateTestBean
        //If this is not vetoed, at the InjectionPoint in Servlet, there would be
        //an ambiguous dependency due to TestBean and DuplicateTestBean
        if (pat.getAnnotatedType().getBaseType().equals(DuplicateTestBean.class)){
            pat.veto();
        }
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm){
        System.out.println("MyExtension: abd: " + abd + " BeanManager: " + bm);

        if (bm != null) {
            //ensure a valid BeanManager is injected
            afterBeanDiscoveryCalled = true;
        }
    }
}
