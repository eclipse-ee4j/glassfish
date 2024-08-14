/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli.schemadoc;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.glassfish.api.admin.config.PropertyDesc;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Dom;

/**
 * Contains metadata information about a class
 */
public class ClassDef {
    private final String def;
    private List<String> interfaces;
    private Set<ClassDef> subclasses = new HashSet<ClassDef>();
    private Map<String, String> types = new HashMap<String, String>();
    private Map<String, Attribute> attributes = new TreeMap<String, Attribute>();
    private boolean deprecated;
    private Set<PropertyDesc> properties = new TreeSet<PropertyDesc>(new Comparator<PropertyDesc>() {
        @Override
        public int compare(PropertyDesc left, PropertyDesc right) {
            return left.name().compareTo(right.name());
        }
    });

    public ClassDef(String def, List<String> interfaces) {
        this.def = def;
        this.interfaces = interfaces;
    }

    public String getDef() {
        return def;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ClassDef classDef = (ClassDef) o;
        if (def != null ? !def.equals(classDef.def) : classDef.def != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return def != null ? def.hashCode() : 0;
    }

    public void addSubclass(ClassDef classDef) {
        subclasses.add(classDef);
    }

    public Set<ClassDef> getSubclasses() {
        return subclasses;
    }

    public void addAggregatedType(String name, String type) {
        types.put(name, type);
    }

    public Map<String, String> getAggregatedTypes() {
        return types;
    }

    @Override
    public String toString() {
        return def;
    }

    public Map<String, Attribute> getAttributes() {
        return attributes;
    }

    public void addAttribute(String name, Attribute annotation) {
        attributes.put(Dom.convertName(name), annotation);
    }

    public void removeAttribute(String name) {
        attributes.remove(Dom.convertName(name));
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public Set<PropertyDesc> getProperties() {
        return properties;
    }

    public void addProperty(PropertyDesc prop) {
        properties.add(prop);
    }

    public String getXmlName() {
        return Dom.convertName(def.substring(def.lastIndexOf(".") + 1));
    }
}
