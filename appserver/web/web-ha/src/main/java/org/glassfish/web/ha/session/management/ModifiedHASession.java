/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.web.ha.session.management;

import org.apache.catalina.Manager;


/**
 *
 * @author  lwhite
 * @author Rajiv Mordani
 */
public class ModifiedHASession extends BaseHASession {

    private transient boolean dirtyFlag = false;


    /** Creates a new instance of ModifiedHASession */
    public ModifiedHASession(Manager manager) {
        super(manager);
    }

    /**
     * set the attribute name to the value value
     * and update the dirty flag to true
     * @param name
     * @param value
     */
    public void setAttribute(String name, Object value) {
        super.setAttribute(name, value);
        this.setDirty(true);
    }

    /**
     * remove the attribute name
     * and update the dirty flag to true
     * @param name
     */
    public void removeAttribute(String name) {
        super.removeAttribute(name);
        this.setDirty(true);
    }

    /**
     * return isDirty
     */
    public boolean isDirty() {
        return dirtyFlag;
    }

    /**
     * set isDirty
     * @param isDirty
     */
    public void setDirty(boolean isDirty) {
        dirtyFlag = isDirty;
    }

}
