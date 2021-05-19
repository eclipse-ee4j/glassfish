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
 * 1.1 Bean that is deployed as a read-only bean
 * @author mvatkina
 */


public class A1Bean implements EntityBean {

    public String id;
    public String shortName;
    public String description;

    private EntityContext context;

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

    /** Method is used to test read-only functionality */
    public String getShortName() {
        return shortName;
    }

    /** Method is used to test non-DFG field in read-only beans */
    public String getDescription() {
        return description;
    }
}
