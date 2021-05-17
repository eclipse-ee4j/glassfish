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
 * 2.0 bean.
 * @author mvatkina
 */


public abstract class A2Bean implements jakarta.ejb.EntityBean {

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
        System.out.println("Debug: A2Bean ejbRemove");
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

        setSqlDate(new java.sql.Date(getMyDate().getTime()));
        java.util.Date d = getMyDate();
        d.setTime(0);
        setMyDate(d);
        java.util.ArrayList c = getList();
        c.add(getName());
        setList(c);
    }

    public abstract java.lang.String getId1() ;
    public abstract void setId1(java.lang.String s) ;

    public abstract java.util.Date getIddate();
    public abstract void setIddate(java.util.Date d);

    public abstract java.lang.String getName() ;
    public abstract void setName(java.lang.String s) ;

    public abstract java.util.ArrayList getList();
    public abstract void setList(java.util.ArrayList l);

    public abstract java.util.Date getMyDate();
    public abstract void setMyDate(java.util.Date d);

    public abstract java.sql.Date getSqlDate() ;
    public abstract void setSqlDate(java.sql.Date d) ;

    public abstract byte[] getBlb() ;
    public abstract void setBlb(byte[] b) ;

    public A2PK ejbCreate(java.lang.String name) throws jakarta.ejb.CreateException {

        long now = System.currentTimeMillis();
        setId1(name);
        setName(name);
        setIddate(new java.util.Date(0));
        setMyDate(new java.util.Date(now));

        return null;
    }

    public void ejbPostCreate(java.lang.String name) throws jakarta.ejb.CreateException {
        setBlb(new byte[]{1,2});
        setList(new java.util.ArrayList());
    }

}
