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

package ejb32.methodintf;

import jakarta.ejb.*;
import jakarta.interceptor.*;
import jakarta.annotation.*;

@Stateless
public class StlesEJB implements St {

    private static boolean tm = false;
    private static boolean intf = false;

    private String caller = null;

    @EJB SingletonBean singleton;

    @Schedule(second="*/2",minute="*",hour="*")
    public void test() {
        if (caller != null) {
            if (caller.equals("timeout")) {
                System.out.println("In StlesEJB: test timeout");
                tm = Verifier.verify_tx(false);
            } else {
                System.out.println("In StlesEJB: test remote");
                intf = Verifier.verify_tx(true);
                singleton.test();
            }
        }
        caller = null;
    }

    @AroundInvoke
    private Object around_invoke(InvocationContext ctx) throws Exception {
        caller = "intf";
        return ctx.proceed();
    }

    @AroundTimeout
    private Object around_timeout(InvocationContext ctx) throws Exception {
        caller = "timeout";
        return ctx.proceed();
    }

    public boolean verify() {
        boolean rc = singleton.verifyResult();
        return tm && intf && rc;
    }

}
