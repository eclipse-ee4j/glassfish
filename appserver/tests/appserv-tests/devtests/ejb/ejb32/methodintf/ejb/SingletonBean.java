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
import javax.naming.InitialContext;

@Singleton
@Startup
public class SingletonBean {

    private boolean _pc = false;
    private boolean _intf = false;
    private String caller = null;

    @PostConstruct
    public void test() {
        if (caller == null) {
            System.out.println("In SingletonBean: test LC");
            _pc = Verifier.verify_tx(false);
        } else if (caller.equals("intf")) {
            System.out.println("In SingletonBean: test local");
            _intf = Verifier.verify_tx(true);
        }
        caller = null;
    }

    public boolean verifyResult() {
        return _pc && _intf;
    }

    @AroundInvoke
    private Object around_invoke(InvocationContext ctx) throws Exception {
        caller = "intf";
        return ctx.proceed();
    }
}
