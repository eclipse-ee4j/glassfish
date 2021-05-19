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

package com.sun.enterprise.deployment;

import org.glassfish.deployment.common.Descriptor;

/**
 * This descriptor describes a name value pair association
 *
 * @author Jerome Dochez
 */
public class NameValuePairDescriptor extends Descriptor {

    private String value=null;

    /**
     * copy constructor.
     */
    public NameValuePairDescriptor(NameValuePairDescriptor other) {
        super(other);
        value = other.value;
    }

    /**
     * standard constructor.
     */
    public NameValuePairDescriptor() {
        super();
    }

    /**
     * set the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Appends a string describing the values I hold
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("\nProp : ").append(getName()).append("->").append(value);
    }

}
