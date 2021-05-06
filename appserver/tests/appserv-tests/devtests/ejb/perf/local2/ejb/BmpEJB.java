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

package com.sun.s1asdev.ejb.perf.local2;

import jakarta.ejb.*;

public class BmpEJB implements EntityBean
{
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
    {}

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
}
