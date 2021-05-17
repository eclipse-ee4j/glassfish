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

package com.sun.s1asdev.jdbc.cmpsimple.ejb;

import jakarta.ejb.*;
import javax.naming.*;

/**
 * @author mvatkina
 */


public abstract class BlobTestBean implements jakarta.ejb.EntityBean {

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
        System.out.println("Debug: BlobTest ejbRemove");
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

    public abstract Integer getId();
    public abstract void setId(Integer id);

    public abstract java.lang.String getName();
    public abstract void setName(java.lang.String name);

    /*
    public abstract byte[] getBlb();
    public abstract void setBlb(byte[] b);


    public java.lang.Integer ejbCreate(Integer id, java.lang.String name, byte[] b) throws jakarta.ejb.CreateException {
        setId(id);
        setName(name);
        setBlb(b);

        return null;
    }
    */
    public java.lang.Integer ejbCreate(Integer id, java.lang.String name)
        throws jakarta.ejb.CreateException
    {
        setId(id);
        setName(name);

        return null;
    }

    /*
    public void ejbPostCreate(Integer id, java.lang.String name, byte[] b) throws jakarta.ejb.CreateException {
    }
    */

    public void ejbPostCreate(Integer id, java.lang.String name )
        throws jakarta.ejb.CreateException {}
}
