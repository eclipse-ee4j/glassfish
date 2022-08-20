/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.deployment.common.Descriptor;


public class LocaleEncodingMappingListDescriptor extends Descriptor {

    private static final long serialVersionUID = 1L;
    private Set<LocaleEncodingMappingDescriptor> list;

    /**
     * standard constructor
     */
    public LocaleEncodingMappingListDescriptor() {
    }


    /**
     * copy constructor
     */
    public LocaleEncodingMappingListDescriptor(LocaleEncodingMappingListDescriptor other) {
        super(other);
        if (other.list == null) {
            list = null;
            return;
        }
        list = new HashSet<>();
        for (LocaleEncodingMappingDescriptor lemd : other.list) {
            list.add(new LocaleEncodingMappingDescriptor(lemd));
        }
    }


    public Set<LocaleEncodingMappingDescriptor> getLocaleEncodingMappingSet() {
        if (list == null) {
            list = new HashSet<>();
        }
        return list;
    }


    public Enumeration<LocaleEncodingMappingDescriptor> getLocaleEncodingMappings() {
        return Collections.enumeration(this.getLocaleEncodingMappingSet());
    }


    public void addLocaleEncodingMapping(LocaleEncodingMappingDescriptor desc) {
        getLocaleEncodingMappingSet().add(desc);
    }


    public void removeLocaleEncodingMapping(LocaleEncodingMappingDescriptor desc) {
        getLocaleEncodingMappingSet().remove(desc);
    }


    /**
     * Adds a string describing the values I hold
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("\nProp : ").append(list);
    }
}
