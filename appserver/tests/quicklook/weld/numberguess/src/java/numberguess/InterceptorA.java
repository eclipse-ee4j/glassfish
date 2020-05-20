/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package numberguess;

import jakarta.interceptor.*;
import jakarta.annotation.*;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import java.io.Serializable;

@Interceptor
@SomeBindingType
public class InterceptorA implements Serializable {

    @EJB
    private StatelessLocal statelessLocal;

    @Inject Foo foo;

    @AroundInvoke
    public Object around(InvocationContext ctx) throws Exception {

        System.out.println("In InterceptorA::around");
        return ctx.proceed();

    }

    @PostConstruct
    public void init(InvocationContext ctx) {
        System.out.println("In InterceptorA::init()");

        System.out.println("statelessLocal = " + statelessLocal);
        System.out.println("foo = " + foo);

        try {
            ctx.proceed();
        } catch (Exception ignore ) {}
    }

    @PreDestroy
    public void destroy(InvocationContext ctx) {
        System.out.println("In InterceptorA::destroy()");
        try {
            ctx.proceed();
        } catch (Exception ignore ) {}
    }

}
