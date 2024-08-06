/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
    public AttributesImpl(AttributesImpl other) {
        if (other == null) {
            throw new NullPointerException("Given illegal null AttributesImpl.");
        }

        attributes = new TreeMap<>(other.attributes);
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
        Attribute attribute = attributes.get(name);
        if (attribute != null) {
            return attribute.getValue();
        }

        return null;
    }

    @Override
    public Set<String> getAttributeValues(String name) {
        Attribute attribute = attributes.get(name);
        if (attribute != null) {
            return attribute.getValues();
        }

        return null;
    }

    @Override
    public String[] getAttributeValuesAsArray(String name) {
        Attribute attribute = attributes.get(name);
        if (attribute != null) {
            return attribute.getValuesAsArray();
        }

        return null;
    }

    @Override
    public void addAttribute(String name, String value, boolean replace) {
        Attribute attribute = attributes.get(name);
        if (attribute != null && !replace) {
            attribute.addValue(value);
        } else {
            attributes.put(name, new AttributeImpl(name, value));
        }
    }

    @Override
    public void addAttribute(String name, Set<String> values, boolean replace) {
        Attribute attribute = attributes.get(name);
        if (attribute != null && !replace) {
            attribute.addValues(values);
        } else {
            attributes.put(name, new AttributeImpl(name, values));
        }
    }

    @Override
    public void addAttribute(String name, String[] values, boolean replace) {
        Attribute attribute = attributes.get(name);
        if (attribute != null && !replace) {
            attribute.addValues(values);
        } else {
            attributes.put(name, new AttributeImpl(name, values));
        }
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public void removeAttributeValue(String name, String value) {
        Attribute attribute = attributes.get(name);
        if (attribute != null) {
            attribute.removeValue(value);
        }
    }

    @Override
    public void removeAttributeValues(String name, Set<String> values) {
        Attribute attribute = attributes.get(name);
        if (attribute != null) {
            attribute.removeValues(values);
        }
    }

    @Override
    public void removeAttributeValues(String name, String[] values) {
        Attribute attribute = attributes.get(name);
        if (attribute != null) {
            attribute.removeValues(values);
        }
    }

    @Override
    public void removeAllAttributeValues(String name) {
        Attribute attribute = attributes.get(name);
        if (attribute != null) {
            attribute.clear();
        }
    }

    @Override
    public void clear() {
        attributes.clear();

    }

}
