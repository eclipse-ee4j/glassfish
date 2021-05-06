/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.acme;

import jakarta.ejb.*;
import jakarta.annotation.*;

import javax.naming.InitialContext;

@Singleton
@Startup
public class SimpleSingleton {

    @Resource
    private SessionContext sessionCtx;

    @PostConstruct
    private void init() {
         System.out.println("In SimpleSingleton:init()");

         try {
             SimpleStateless ss = (SimpleStateless)
                 sessionCtx.lookup("java:app/env/slref");
             ss.hello();
             System.out.println("Successfully looked up web-component defined environment dependency from @Startup Singleton @PostConstruct");
         } catch(Exception e) {
             throw new EJBException(e);
         }

    }

    public void hello() {
         System.out.println("In SimpleSingleton:hello()");
         try {
             SimpleStateless ss = (SimpleStateless)
                 sessionCtx.lookup("java:app/env/slref");
             ss.hello();

             // assumes 299 enabled new InitialContext().lookup("java:comp/BeanManager");
         } catch(Exception e) {
             throw new EJBException(e);
         }
    }

     @PreDestroy
     private void destroy() {
         System.out.println("In SimpleSingleton:destroy()");
     }



}
