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

package org.glassfish.web.deployment.runtime;

import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;

/**
* Interface for all web property containers
*
* @author Jerome Dochez
*/
public class WebPropertyContainer extends RuntimeDescriptor {

    static public final String NAME = "Name"; // NOI18N
    static public final String VALUE = "Value"; // NOI18N
    static public final String PROPERTY = "WebProperty"; // NOI18N

    // copy constructor
    public WebPropertyContainer(WebPropertyContainer other) {
        super(other);
    }


    // constructor
    public WebPropertyContainer() {
        super();
    }


    // This attribute is an array, possibly empty
    public void setWebProperty(int index, WebProperty value) {
        this.setValue(PROPERTY, index, value);
    }


    //
    public WebProperty getWebProperty(int index) {
        return (WebProperty) this.getValue(PROPERTY, index);
    }


    // This attribute is an array, possibly empty
    public void setWebProperty(WebProperty[] value) {
        this.setValue(PROPERTY, value);
    }


    //
    public WebProperty[] getWebProperty() {
        WebProperty[] props = (WebProperty[]) this.getValues(PROPERTY);
        if (props == null) {
            return new WebProperty[0];
        } else {
            return props;
        }
    }


    // Return the number of properties
    public int sizeWebProperty() {
        return this.size(PROPERTY);
    }


    // Add a new element returning its index in the list
    public int addWebProperty(WebProperty value) {
        return this.addValue(PROPERTY, value);
    }


    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeWebProperty(WebProperty value) {
        return this.removeValue(PROPERTY, value);
    }


    // This method verifies that the mandatory properties are set
    public boolean verify() {
        return true;
    }
}
