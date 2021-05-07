/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.impl.common;


import java.util.Set;
import java.util.TreeMap;

import org.glassfish.security.services.api.common.Attribute;
import org.glassfish.security.services.api.common.Attributes;


public class AttributesImpl implements Attributes {

    private final TreeMap<String, Attribute> attributes;

    public AttributesImpl() {
        attributes = new TreeMap<>();
    }

    /**
     * Copy constructor
     */
    public AttributesImpl( AttributesImpl other ) {
        if ( null == other ) {
            throw new NullPointerException( "Given illegal null AttributesImpl." );
        }
        attributes = new TreeMap<>( other.attributes );
    }

    @Override
    public int getAttributeCount() {
        return attributes.size();
    }

    @Override
    public Set<String> getAttributeNames() {
        return attributes.keySet();
    }

    @Override
    public Attribute getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public String getAttributeValue(String name) {
        Attribute a = attributes.get(name);
        if(a != null) {
            return a.getValue();
        }
        return null;
    }

    @Override
    public Set<String> getAttributeValues(String name) {
        Attribute a = attributes.get(name);
        if(a != null) {
            return a.getValues();
        }
        return null;
    }

    @Override
    public String[] getAttributeValuesAsArray(String name) {
        Attribute a = attributes.get(name);
        if(a != null) {
            return a.getValuesAsArray();
        }
        return null;
    }

    @Override
    public void addAttribute(String name, String value, boolean replace) {
        Attribute a = attributes.get(name);
        if(a != null && !replace) {
            a.addValue(value);
        }
        else {
            attributes.put(name, new AttributeImpl(name, value));
        }
    }

    @Override
    public void addAttribute(String name, Set<String> values, boolean replace) {
        Attribute a = attributes.get(name);
        if(a != null && !replace) {
            a.addValues(values);
        }
        else {
            attributes.put(name, new AttributeImpl(name, values));
        }
    }

    @Override
    public void addAttribute(String name, String[] values, boolean replace) {
        Attribute a = attributes.get(name);
        if(a != null && !replace) {
            a.addValues(values);
        }
        else {
            attributes.put(name, new AttributeImpl(name, values));
        }
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public void removeAttributeValue(String name, String value) {
        Attribute a = attributes.get(name);
        if (a != null) {
            a.removeValue(value);
        }
    }

    @Override
    public void removeAttributeValues(String name, Set<String> values) {
        Attribute a = attributes.get(name);
        if (a != null) {
            a.removeValues(values);
        }
    }

    @Override
    public void removeAttributeValues(String name, String[] values) {
        Attribute a = attributes.get(name);
        if (a != null) {
            a.removeValues(values);
        }
    }

    @Override
    public void removeAllAttributeValues(String name) {
        Attribute a = attributes.get(name);
        if (a != null) {
            a.clear();
        }
    }

    @Override
    public void clear() {
        attributes.clear();

    }

}
