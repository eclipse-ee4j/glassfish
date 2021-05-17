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
import java.util.TreeSet;
import java.util.Set;

import org.glassfish.security.services.api.common.Attribute;


public class AttributeImpl implements Attribute {

    private String name = null;
    private Set<String> values = new TreeSet<String>();

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

    public int getValueCount() { return values.size(); }

    public String getName() { return name; }

    public String getValue() {
        if(getValueCount() == 0) {
            return null;
        }
        Iterator<String> i = values.iterator();
        return i.next();
    }

    public Set<String> getValues() { return values; }

    public String[] getValuesAsArray() { return values.toArray(new String[0]); }

    public void addValue(String value) {
        if (value != null && !value.trim().equals("")) {
            values.add(value);
        }
    }

    public void addValues(Set<String> values) {
        addValues(values.toArray(new String[0]));
    }

    public void addValues(String[] values) {
        for (int i = 0; i < values.length; i++) {
            addValue(values[i]);
        }
    }

    public void removeValue(String value) {
        values.remove(value);
    }

    public void removeValues(Set<String> values) {
        this.values.removeAll(values);
    }

    public void removeValues(String[] values) {
        this.values.removeAll(Arrays.asList(values));
    }

    public void clear() {
        values.clear();
    }

}
