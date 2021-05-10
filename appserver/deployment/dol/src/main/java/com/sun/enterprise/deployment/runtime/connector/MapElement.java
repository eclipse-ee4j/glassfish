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

/**
*    This generated bean class MapElement matches the DTD element map-element
*
*    Generated on Mon May 13 13:36:49 PDT 2002
*/

package com.sun.enterprise.deployment.runtime.connector;

import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;

/**
 * This class was based on the schema2beans generated one modified
 * to remove its dependencies on schema2beans libraries.
 *
 * @author Jerome Dochez
 * @version
 */
public class MapElement extends RuntimeDescriptor {

    static public final String PRINCIPAL = "Principal"; // NOI18N
    static public final String BACKEND_PRINCIPAL = "BackendPrincipal"; // NOI18N

    Principal backendPrincipal = null;

    // copy constructor
    public MapElement(MapElement other) {
        super(other);
    }


    // constructor
    public MapElement() {
        super();
    }


    // This attribute is an array containing at least one element
    public void setPrincipal(int index, Principal value) {
        this.setValue(PRINCIPAL, index, value);
    }


    //
    public Principal getPrincipal(int index) {
        return (Principal) this.getValue(PRINCIPAL, index);
    }


    // This attribute is an array containing at least one element
    public void setPrincipal(Principal[] value) {
        this.setValue(PRINCIPAL, value);
    }


    //
    public Principal[] getPrincipal() {
        return (Principal[]) this.getValues(PRINCIPAL);
    }


    // Return the number of properties
    public int sizePrincipal() {
        return this.size(PRINCIPAL);
    }


    // Add a new element returning its index in the list
    public int addPrincipal(Principal value) {
        return this.addValue(PRINCIPAL, value);
    }


    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removePrincipal(Principal value) {
        return this.removeValue(PRINCIPAL, value);
    }


    // This attribute is mandatory
    public void setBackendPrincipal(boolean value) {
        this.setValue(BACKEND_PRINCIPAL, Boolean.valueOf(value));
    }


    //
    public boolean isBackendPrincipal() {
        Boolean ret = (Boolean) this.getValue(BACKEND_PRINCIPAL);
        if (ret == null) {
            return false;
        }
        return ret.booleanValue();
    }


    // This method verifies that the mandatory properties are set
    public boolean verify() {
        return true;
    }
}
