/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * FullHASession.java
 *
 * Created on October 3, 2002, 3:15 PM
 */

package org.glassfish.web.ha.session.management;

import org.apache.catalina.Manager;

/**
 *
 * @author  lwhite
 * @author  Rajiv Mordani
 */
public class FullHASession extends BaseHASession {

    /**
     * Creates a new instance of FullHASession
     * @param manager
     */
    public FullHASession(Manager manager) {
        super(manager);
    }

    /**
     * always return true for isDirty()
     * this type of session is always dirty
     */
    public boolean isDirty() {
        return true;
    }

    /**
     * this is deliberately a no-op
     * store framework calls this method
     * so it must be there but must not have
     * any effect
     * @param isDirty
     */
    public void setDirty(boolean isDirty) {
    }

    public void removeAttribute(String name) {
        super.removeAttribute(name);
        setDirty(true);
    }

    public void setAttribute(String name, Object value) {
        super.setAttribute(name, value);
        setDirty(true);
    }

    public Object getAttribute(String name) {
        setDirty(true);
        return super.getAttribute(name);
    }
}
