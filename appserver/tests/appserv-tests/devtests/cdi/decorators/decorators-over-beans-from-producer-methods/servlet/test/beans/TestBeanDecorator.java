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

package test.beans;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import jakarta.inject.Inject;

@Decorator
public abstract class TestBeanDecorator implements TestBeanInterface{
    
    public static boolean decoratorCalled = false;
    
    @Inject
    @Delegate
    @ProducedAtRuntime
    TestBeanInterface tb;

    @Override
    public String m1() {
        System.out.println("Decorator called");
        decoratorCalled = true;
        return tb.m1();
    }
    
    

}
