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

package com.sun.s1as.devtests.ejb.generics;

import jakarta.ejb.*;
import java.util.*;

/**
 * All business methods in this class are overridden by subclass to verify
 * these business methods are correctly processed.
 */
public abstract class AbstractBaseEJB<T> {
    //abstract method, use parameterized param type with T
    public abstract void doSomething(List<T> t);

    //regular business method, no use of generics param
    public String hello() {
        System.out.println("In AbstractBaseEJB.hello.");
        return "Hello from AbstractBaseEJB.";
    }

    //use parameterized param type with T
    public void doSomething2(List<T> t) {
        System.out.println("In AbstractBaseEJB.doSomething2.");
    }

    //use parameterized return type with T
    public List<T> doSomething3() {
        System.out.println("In AbstractBaseEJB.doSomething3.");
        return null;
    }

    //use TypeVariable generics T as param
    abstract public void doSomething4(T t);

    //superclass has param List<T>, and subclass has param List
    abstract public void doSomething5(List<T> t);

    abstract public void doSomething6(List<List<T>> t);
}
