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

package test;

import jakarta.ejb.*;

/**
 * 2.0 Bean that is deployed as a read-only bean
 * @author mvatkina
 */


public abstract class A2Bean implements EntityBean {

    private EntityContext context;

    public abstract String getId();
    public abstract void setId(String s);

    public abstract String getShortName();
    public abstract void setShortName(String s);

    public abstract String getDescription();
    public abstract void setDescription(String s);

    /**
     * @see EntityBean#setEntityContext(EntityContext)
     */
    public void setEntityContext(EntityContext aContext) {
        context=aContext;
    }

    /**
     * @see EntityBean#ejbActivate()
     */
    public void ejbActivate() {

    }

    /**
     * @see EntityBean#ejbPassivate()
     */
    public void ejbPassivate() {

    }

    /**
     * @see EntityBean#ejbRemove()
     */
    public void ejbRemove() {

    }

    /**
     * @see EntityBean#unsetEntityContext()
     */
    public void unsetEntityContext() {
        context=null;
    }

    /**
     * @see EntityBean#ejbLoad()
     */
    public void ejbLoad() {

    }

    /**
     * @see EntityBean#ejbStore()
     */
    public void ejbStore() {
    }

}
