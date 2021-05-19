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
 *    This generated bean class RoleMap matches the DTD element role-map
 *
 *    Generated on Mon May 13 13:36:49 PDT 2002
 */

package com.sun.enterprise.deployment.runtime.connector;

import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;

/**
 * This class was based on the schema2beans generated one modified
 * to remove its dependencies on schema2beans libraries.

 * @author  Jerome Dochez
 * @version
 */
public class RoleMap extends RuntimeDescriptor {

    static public final String DESCRIPTION = "Description"; // NOI18N
    static public final String MAP_ELEMENT = "MapElement"; // NOI18N
    static public final String MAP_ID = "MapId";

    // This attribute is an array, possibly empty
    public void setMapElement(int index, MapElement value) {
        this.setValue(MAP_ELEMENT, index, value);
    }


    //
    public MapElement getMapElement(int index) {
        return (MapElement) this.getValue(MAP_ELEMENT, index);
    }


    // This attribute is an array, possibly empty
    public void setMapElement(MapElement[] value) {
        this.setValue(MAP_ELEMENT, value);
    }


    //
    public MapElement[] getMapElement() {
        return (MapElement[]) this.getValues(MAP_ELEMENT);
    }


    // Return the number of properties
    public int sizeMapElement() {
        return this.size(MAP_ELEMENT);
    }


    // Add a new element returning its index in the list
    public int addMapElement(MapElement value) {
        return this.addValue(MAP_ELEMENT, value);
    }


    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeMapElement(MapElement value) {
        return this.removeValue(MAP_ELEMENT, value);
    }


    // This method verifies that the mandatory properties are set
    public boolean verify() {
        return true;
    }
}
