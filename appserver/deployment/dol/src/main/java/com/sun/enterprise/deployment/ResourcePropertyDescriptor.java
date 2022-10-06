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
 * Created with IntelliJ IDEA.
 * User: naman
 * Date: 7/9/12
 * Time: 11:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class ResourcePropertyDescriptor extends Descriptor {

    private String name;
    private String value;

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ResourcePropertyDescriptor) {
            ResourcePropertyDescriptor that = (ResourcePropertyDescriptor) o;
            String thatName = that.getName();
            if (thatName != null && this.name != null && this.name.equals(thatName)) {
                String thatValue = that.getValue();
                if (thatValue != null && this.value != null && this.value.equals(thatValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + getName().hashCode();
        return result;
    }
}
