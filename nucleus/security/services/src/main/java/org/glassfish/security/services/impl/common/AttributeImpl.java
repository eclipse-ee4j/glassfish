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


import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.glassfish.security.services.api.common.Attribute;


public class AttributeImpl implements Attribute {

    private String name = null;
    private final Set<String> values = new TreeSet<>();

    protected AttributeImpl() {}

    public AttributeImpl(String name) {
        this.name = name;
    }

    public AttributeImpl(String name, String value) {
        this(name);
        addValue(value);
    }

    public AttributeImpl(String name, Set<String> values) {
        this(name);
        addValues(values);
    }

    public AttributeImpl(String name, String[] values) {
        this(name);
        addValues(values);
    }

    @Override
    public int getValueCount() { return values.size(); }

    @Override
    public String getName() { return name; }

    @Override
    public String getValue() {
        if(getValueCount() == 0) {
            return null;
        }
        Iterator<String> i = values.iterator();
        return i.next();
    }

    @Override
    public Set<String> getValues() { return values; }

    @Override
    public String[] getValuesAsArray() { return values.toArray(new String[0]); }

    @Override
    public void addValue(String value) {
        if (value != null && !value.trim().equals("")) {
            values.add(value);
        }
    }

    @Override
    public void addValues(Set<String> values) {
        addValues(values.toArray(new String[0]));
    }

    @Override
    public void addValues(String[] values) {
        for (String value : values) {
            addValue(value);
        }
    }

    @Override
    public void removeValue(String value) {
        values.remove(value);
    }

    @Override
    public void removeValues(Set<String> values) {
        this.values.removeAll(values);
    }

    @Override
    public void removeValues(String[] values) {
        this.values.removeAll(Arrays.asList(values));
    }

    @Override
    public void clear() {
        values.clear();
    }

}
