/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.stubs.proxy;

import jakarta.ejb.*;

public class BmpEJB implements EntityBean
{
    private EntityContext ec_;

    public BmpEJB(){}

    public String ejbCreate(String s) {
        return s;
    }

    public void ejbPostCreate(String s) {}

    public void notSupported() {}
    public void required() {}
    public void requiresNew() {}
    public void mandatory() {}
    public void never() {}
    public void supports() {}

    public void setEntityContext(EntityContext c)
    {
        ec_ = c;
    }

    public void unsetEntityContext()
    {}

    public void ejbRemove()
    {}

    public void ejbActivate()
    {}

    public void ejbPassivate()
    {}

    public void ejbLoad()
    {}

    public void ejbStore()
    {}

    public String ejbFindByPrimaryKey(String s) {
        return s;
    }

    public void testException1() throws Exception {
        throw new Exception("testException1");
    }

    // will throw ejb exception
    public void testException2() {
        throw new EJBException("testException2");
    }

    // throws some checked exception which is a subclass of the declared
    // checked exception
    public void testException3() throws jakarta.ejb.FinderException {
        throw new ObjectNotFoundException("testException3");
    }

    // throws some checked exception
    public void testException4() throws jakarta.ejb.FinderException {
        throw new FinderException("testException4");
    }

     public void testPassByRef1(int a) {

    }

    public void testPassByRef2(Helper1 helper1) {
        helper1.a++;
        helper1.b = helper1.b + "BmpEJB::testPassByRef2";
    }

    public void testPassByRef3(Helper2 helper2) {
        helper2.a++;
        helper2.b = helper2.b + "BmpEJB::testPassByRef3";
    }

    public void testPassByRef4(CommonRemote cr) {

    }

    public Helper1 testPassByRef5() {
        Helper1 h1 = new Helper1();
        h1.a = 1;
        h1.b = "testPassByRef5";
        return h1;
    }

    public Helper2 testPassByRef6() {
        Helper2 h2 = new Helper2();
        h2.a = 1;
        h2.b = "testPassByRef6";
        return h2;
    }

    public CommonRemote testPassByRef7() {
        return (CommonRemote) ec_.getEJBObject();
    }

    public int testPassByRef8() {
        return 8;
    }

}
