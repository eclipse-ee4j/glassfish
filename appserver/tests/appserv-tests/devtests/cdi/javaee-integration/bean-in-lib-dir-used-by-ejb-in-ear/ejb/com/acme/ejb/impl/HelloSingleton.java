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

package com.acme.ejb.impl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.DependsOn;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

import com.acme.ejb.api.Hello;
import com.acme.util.TestDatabase;
import com.acme.util.TestDependentBeanInLib;
import com.acme.util.TestSessionScopedBeanInLib;
import com.acme.util.UtilInLibDir;

@Singleton
@Startup
@DependsOn("Singleton4")
public class HelloSingleton implements Hello {

    @Resource
    SessionContext sessionCtx;

    @PersistenceUnit(unitName = "pu1")
    @TestDatabase
    private EntityManagerFactory emf;

    @Inject
    TestDependentBeanInLib tdbil;

    @Inject
    TestSessionScopedBeanInLib tssil;

    @PostConstruct
    private void init() {
        System.out.println("HelloSingleton::init()");

        String appName;
        String moduleName;
        appName = (String) sessionCtx.lookup("java:app/AppName");
        moduleName = (String) sessionCtx.lookup("java:module/ModuleName");
        System.out.println("AppName = " + appName);
        System.out.println("ModuleName = " + moduleName);
    }

    public String hello() {
        System.out.println("HelloSingleton::hello()");
        String res = testEMF();
        if (!res.equals(""))
            return res;
        
        res = testInjectionOfBeansInLibDir();
        if (!res.equals(""))
            return res;
        
        UtilInLibDir uilb = new UtilInLibDir();
        if (!(uilb.add(1, 2) == 3)) {
            return "Can't use utility class in library directory";
        }
        return ALL_OK_STRING;
    }

    private String testInjectionOfBeansInLibDir() {
        if (tdbil == null)
            return "Injection of Dependent Bean in lib into an EJB in that ear failed";
        
        if (tssil == null)
            return "Injection of SessionScoped Bean in lib into an EJB in that ear failed";
        
        return "";
    }

    private String testEMF() {
        if (emf == null)
            return "EMF injection failed, is null in Singleton EJB";
        
        if (emf.createEntityManager() == null)
            return "Usage of EMF failed in Singleton EJB";
        
        return "";
    }

    @PreDestroy
    private void destroy() {
        System.out.println("HelloSingleton::destroy()");
    }

}
