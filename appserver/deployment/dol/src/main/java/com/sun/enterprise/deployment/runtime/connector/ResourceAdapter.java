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
 *    This generated bean class ResourceAdapter matches the DTD element resource-adapter
 *
 *    Generated on Mon May 13 13:36:49 PDT 2002
 */

package com.sun.enterprise.deployment.runtime.connector;

import com.sun.enterprise.deployment.NameValuePairDescriptor;
import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;

/**
 * This class was based on the schema2beans generated one modified
 * to remove its dependencies on schema2beans libraries.
 *
 * @author Jerome Dochez
 * @version
 */
public class ResourceAdapter extends RuntimeDescriptor {

    static public final String DESCRIPTION = "Description"; // NOI18N
    static public final String PROPERTY = "PropertyElement"; // NOI18N

    static public final String JNDI_NAME = "JndiName";
    static public final String MAX_POOL_SIZE = "MaxPoolSize";
    static public final String STEADY_POOL_SIZE = "SteadyPoolSize";
    static public final String MAX_WAIT_TIME_IN_MILLIS = "MaxWaitTimeInMillis";
    static public final String IDLE_TIMEOUT_IN_SECONDS = "IdleTimeoutInSeconds";

    // This attribute is an array, possibly empty
    public void setPropertyElement(int index, NameValuePairDescriptor value) {
        this.setValue(PROPERTY, index, value);
    }


    //
    public boolean isPropertyElement(int index) {
        NameValuePairDescriptor ret = (NameValuePairDescriptor) this.getValue(PROPERTY, index);
        return ret != null;
    }


    // This attribute is an array, possibly empty
    public void setPropertyElement(NameValuePairDescriptor[] values) {
        this.setValues(PROPERTY, values);
    }


    //
    public NameValuePairDescriptor[] getPropertyElement() {
        return (NameValuePairDescriptor[]) this.getValues(PROPERTY);
    }


    // Return the number of properties
    public int sizePropertyElement() {
        return this.size(PROPERTY);
    }


    // Add a new element returning its index in the list
    public int addPropertyElement(NameValuePairDescriptor value) {
        return this.addValue(PROPERTY, value);
    }


    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removePropertyElement(NameValuePairDescriptor value) {
        return this.removeValue(PROPERTY, value);
    }


    //
    // Remove an element using its index
    //
    public void removePropertyElement(int index) {
        this.removeValue(PROPERTY, index);
    }


    // This method verifies that the mandatory properties are set
    public boolean verify() {
        return true;
    }
}
