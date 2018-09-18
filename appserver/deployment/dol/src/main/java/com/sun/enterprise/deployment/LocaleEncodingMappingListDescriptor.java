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

import java.util.*;
/**
 */
public class LocaleEncodingMappingListDescriptor extends Descriptor {
    
    private Set<LocaleEncodingMappingDescriptor> list = null;
        
    /*
     * standard constructor 
     */
    public LocaleEncodingMappingListDescriptor() {
    }

    /*
     * copy constructor 
     */
    public LocaleEncodingMappingListDescriptor(LocaleEncodingMappingListDescriptor other) {
	super(other);
	if (other.list != null) {
	    list = new HashSet();
	    for (Iterator i = other.list.iterator(); i.hasNext();) {
		LocaleEncodingMappingDescriptor lemd = (LocaleEncodingMappingDescriptor)i.next();
		list.add(new LocaleEncodingMappingDescriptor(lemd));
	    }
	} else {
	    list = null;
	}
    }

    public Set<LocaleEncodingMappingDescriptor> getLocaleEncodingMappingSet() {
	if(list == null) {
	    list = new HashSet<LocaleEncodingMappingDescriptor>();
	}
	return list;
    }

    public Enumeration getLocaleEncodingMappings() {
	return (new Vector(this.getLocaleEncodingMappingSet())).elements();
    }

    public void addLocaleEncodingMapping(LocaleEncodingMappingDescriptor desc) {
	getLocaleEncodingMappingSet().add(desc);
    }

    public void removeLocaleEncodingMapping(LocaleEncodingMappingDescriptor desc) {
	getLocaleEncodingMappingSet().remove(desc);
    }

    /**
     * @return a string describing the values I hold
     */
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("\nProp : ").append(list);
    }
}
