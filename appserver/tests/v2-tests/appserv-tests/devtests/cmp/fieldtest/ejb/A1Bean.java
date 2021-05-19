/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package fieldtest;

import jakarta.ejb.*;
import javax.naming.*;

/**
 * 1.1 bean.
 * @author mvatkina
 */


public class A1Bean implements jakarta.ejb.EntityBean {

    private jakarta.ejb.EntityContext context;

    /**
     * @see jakarta.ejb.EntityBean#setEntityContext(jakarta.ejb.EntityContext)
     */
    public void setEntityContext(jakarta.ejb.EntityContext aContext) {
        context=aContext;
    }

    /**
     * @see jakarta.ejb.EntityBean#ejbActivate()
     */
    public void ejbActivate() {

    }

    /**
     * @see jakarta.ejb.EntityBean#ejbPassivate()
     */
    public void ejbPassivate() {

    }

    /**
     * @see jakarta.ejb.EntityBean#ejbRemove()
     */
    public void ejbRemove() {
        System.out.println("Debug: A1Bean ejbRemove");
    }

    /**
     * @see jakarta.ejb.EntityBean#unsetEntityContext()
     */
    public void unsetEntityContext() {
        context=null;
    }

    /**
     * @see jakarta.ejb.EntityBean#ejbLoad()
     */
    public void ejbLoad() {

    }

    /**
     * @see jakarta.ejb.EntityBean#ejbStore()
     */
    public void ejbStore() {
    }

    public void update() {
        sqldate = new java.sql.Date(mydate.getTime());
        mydate.setTime(0);
        list.add(name);
    }

    public java.lang.String getName() {
        return name;
    }

    public java.util.ArrayList getList() {
        return list;
    }

    public java.util.Date getMyDate() {
        return mydate;
    }

    public java.sql.Date getSqlDate() {
        return sqldate;
    }

    public byte[] getBlb() {
        return blb;
    }

    public java.lang.String id1;
    public java.util.Date iddate;

    public java.lang.String name;
    public java.util.Date mydate;
    public java.sql.Date sqldate;
    public byte[] blb;
    public java.util.ArrayList list;

    public A1PK ejbCreate(java.lang.String name) throws jakarta.ejb.CreateException {

        this.name = name;
        id1 = name;
        long now = System.currentTimeMillis();
        iddate = new java.util.Date(0);
        mydate = new java.util.Date(now);

        return null;
    }

    public void ejbPostCreate(java.lang.String name) throws jakarta.ejb.CreateException {
        blb = new byte[]{1,2};
        list = new java.util.ArrayList();
    }

}
