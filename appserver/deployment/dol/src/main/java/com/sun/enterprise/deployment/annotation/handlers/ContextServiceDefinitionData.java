/*
 * Copyright (c) 2022, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
 * Copyright (c) 2024 Payara Foundation and/or its affiliates
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

package com.sun.enterprise.deployment.annotation.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import static com.sun.enterprise.universal.JavaLangUtils.nonNull;

/**
 * @author David Matejcek
 */
public class ContextServiceDefinitionData implements ConcurrencyResourceDefinition {

    private static final long serialVersionUID = -6964391431010485710L;

    private String name;
    private Set<String> cleared = new HashSet<>();
    private Set<String> propagated = new HashSet<>();
    private Set<String> unchanged = new HashSet<>();
    private final List<String> qualifiers = new ArrayList<>();
    private final Properties properties = new Properties();

    @Override
    public String getName() {
        return name;
    }


    @Override
    public void setName(String name) {
        this.name = name;
    }


    @Override
    public List<String> getQualifiers() {
        return qualifiers;
    }


    public void setQualifiers(List<String> qualifiers) {
        this.qualifiers.clear();
        this.qualifiers.addAll(qualifiers);
    }


    @Override
    public void addQualifier(String qualifier) {
        this.qualifiers.add(qualifier);
    }


    public Set<String> getCleared() {
        return cleared;
    }


    public void setCleared(Set<String> cleared) {
        this.cleared = nonNull(cleared, HashSet::new);
    }


    public void addCleared(String clearedItem) {
        this.cleared.add(clearedItem);
    }


    public Set<String> getPropagated() {
        return propagated;
    }


    public void setPropagated(Set<String> propagated) {
        this.propagated = nonNull(propagated, HashSet::new);
    }


    public void addPropagated(String propagatedItem) {
        this.propagated.add(propagatedItem);
    }


    public Set<String> getUnchanged() {
        return unchanged;
    }


    public void setUnchanged(Set<String> unchanged) {
        this.unchanged = nonNull(unchanged, HashSet::new);
    }


    public void addUnchanged(String unchangedItem) {
        this.unchanged.add(unchangedItem);
    }


    public Properties getProperties() {
        return properties;
    }


    public void setProperties(Properties properties) {
        this.properties.clear();
        this.properties.putAll(properties);
    }


    public void addContextServiceExecutorDescriptor(String name, String value) {
        properties.put(name, value);
    }


    @Override
    public boolean equals(Object object) {
        if (object instanceof ContextServiceDefinitionData) {
            ContextServiceDefinitionData another = (ContextServiceDefinitionData) object;
            return getName().equals(another.getName());
        }
        return false;
    }


    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }


    @Override
    public String toString() {
        return super.toString() + "[name=" + name + ", cleared=" + cleared + ", propagated=" + propagated
            + ", unchanged=" + unchanged + ", qualifiers=" + qualifiers + ", properties=" + properties + ']';
    }
}
