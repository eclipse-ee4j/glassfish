/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.EJBException;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@Singleton
@Startup
@ApplicationScoped
public class SingletonBean {

    @Resource
    private SessionContext sessionCtx;

    @Inject
    private StatelessLocal statelessLocal;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
        System.out.println("sessionCtx = " + sessionCtx);
        if (sessionCtx == null) {
            throw new EJBException("EE injection error");
        }
    }

    public void hello() {
        System.out.println("In SingletonBean::hello()");
        statelessLocal.hello();
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }

}
