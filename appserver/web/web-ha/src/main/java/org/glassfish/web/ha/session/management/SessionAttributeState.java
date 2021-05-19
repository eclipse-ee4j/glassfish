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
 * SessionAttributeState.java
 *
 * Created on October 3, 2002, 3:42 PM
 */

package org.glassfish.web.ha.session.management;

/**
 *
 * @author  lwhite
 * @author Rajiv Mordani
 *
 * SessionAttributeState represents the state (with regards to persistence
 * activity) of each attribute in a session.
 *
 * Invariants:
 * 1. newly added attribute (not yet persistent)
 *      not persistent
 *      not dirty
 *      not deleted
 * 2. modified already existing session
 *      persistent
 *      dirty
 *      not deleted
 * 3. already existing session to be deleted
 *      persistent
 *      dirty or not dirty
 *      deleted
 */
public class SessionAttributeState {

    /** Creates a new instance of SessionAttributeState */
    public SessionAttributeState() {
    }

    /**
     * create an instance of SessionAttributeState
     * representing a persistent attribute
     */
    public static SessionAttributeState createPersistentAttribute() {
        SessionAttributeState result = new SessionAttributeState();
        result.setPersistent(true);
        return result;
    }

    /**
     * return isDirty
     */
    public boolean isDirty() {
        return _dirtyFlag;
    }

    /**
     * set isDirty
     * @param value
     */
    public void setDirty(boolean value) {
        _dirtyFlag = value;
    }

    /**
     * return isPersistent
     */
    public boolean isPersistent() {
        return _persistentFlag;
    }

    /**
     * set persistentFlag
     * @param value
     */
    public void setPersistent(boolean value) {
        _persistentFlag = value;
    }

    /**
     * return isDeleted
     */
    public boolean isDeleted() {
        return _deletedFlag;
    }

    /**
     * set deletedFlag
     * @param value
     */
    public void setDeleted(boolean value) {
        _deletedFlag = value;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("SessionAttributeState");
        sb.append("\n__dirtyFlag = " + _dirtyFlag);
        sb.append("\n__persistentFlag = " + _persistentFlag);
        sb.append("\n_deletedFlag = " + _deletedFlag);
        return sb.toString();
    }

    boolean _dirtyFlag = false;
    boolean _persistentFlag = false;
    boolean _deletedFlag = false;

}
