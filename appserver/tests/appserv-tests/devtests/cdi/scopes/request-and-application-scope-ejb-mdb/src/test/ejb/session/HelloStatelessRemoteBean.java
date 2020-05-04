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

package test.ejb.session;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import javax.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import test.beans.BeanToTestTimerUse;
import test.beans.TestApplicationScopedBean;
import test.beans.TestRequestScopedBean;

@Stateless
@Remote( { HelloSless.class })
public class HelloStatelessRemoteBean implements HelloSless {

    @Inject
    TestApplicationScopedBean tasb;
    @Inject
    TestRequestScopedBean trsb;

    @Inject
    BeanManager bm;
    
    @Inject BeanToTestTimerUse timerBeanTest;

    @PostConstruct
    public void afterCreate() {
        System.out
                .println("In StatelessRemoteBean::afterCreate() marked as PostConstruct");
    }

    public String hello() {
        System.out.println("In HelloStatelessRemoteBean:hello()");
        String msg = "hello";
        System.out.println("tasb=" + tasb);
        System.out.println("trsb=" + trsb);
        if (tasb == null)
            msg += "Injection of Application scoped bean in EJB remote method failed";
        if (trsb == null)
            msg += "Injection of Request scoped bean in EJB remote method failed";
        
        if (!timerBeanTest.getResult()) 
            msg += "Injection of session scoped bean into EJB timer beans failed";
                
        return msg;
    }
}
