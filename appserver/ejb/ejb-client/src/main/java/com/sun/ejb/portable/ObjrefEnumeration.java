/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.ejb.portable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class is necessary because neither Vector nor Hashtable return a Serializable Enumeration, which can be sent
 * from the EJB server back to the client. This class must be available at the client too, and it could be instantiated
 * in another vendor's container.
 *
 */
public final class ObjrefEnumeration implements Enumeration, Serializable {

    private int count;
    private List<Object> objrefs;

    // This is called only by the EJB container in the RI.
    public void add(Object obj) {
        if (objrefs == null) {
            objrefs = new ArrayList<>();
        }

        objrefs.add(obj);
    }

    @Override
    public boolean hasMoreElements() {
        if (objrefs == null) {
            return false;
        }

        return count < objrefs.size();
    }

    @Override
    public Object nextElement() {
        if (objrefs != null) {
            synchronized (this) {
                if (count < objrefs.size()) {
                    return objrefs.get(count++);
                }
            }
        }

        throw new NoSuchElementException("ObjrefEnumeration");
    }
}
