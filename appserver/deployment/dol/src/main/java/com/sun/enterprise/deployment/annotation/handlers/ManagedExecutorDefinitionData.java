/*
 * Copyright (c) 2022, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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
import java.util.List;
import java.util.Properties;

/**
 * @author David Matejcek
 */
public class ManagedExecutorDefinitionData implements ContextualResourceDefinition {

    private static final long serialVersionUID = -6027151040382351476L;

    private String name;
    private String context;
    private int maximumPoolSize = Integer.MAX_VALUE;
    private long hungAfterSeconds;
    private boolean useVirtualThreads;
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
    public String getContext() {
        return context;
    }


    @Override
    public void setContext(String context) {
        this.context = context;
    }


    @Override
    public List<String> getQualifiers() {
        return qualifiers;
    }


    @Override
    public void addQualifier(String qualifier) {
        this.qualifiers.add(qualifier);
    }


    public void setQualifiers(List<String> qualifiers) {
        this.qualifiers.clear();
        this.qualifiers.addAll(qualifiers);
    }


    @Override
    public boolean getUseVirtualThreads() {
        return useVirtualThreads;
    }


    @Override
    public void setUseVirtualThreads(boolean useVirtualThreads) {
        this.useVirtualThreads = useVirtualThreads;
    }


    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }


    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }


    public long getHungAfterSeconds() {
        return hungAfterSeconds;
    }


    public void setHungAfterSeconds(long hungAfterSeconds) {
        this.hungAfterSeconds = hungAfterSeconds;
    }


    public Properties getProperties() {
        return properties;
    }


    public String getProperty(String key) {
        return properties.getProperty(key);
    }


    public void addProperty(String key, String value) {
        properties.setProperty(key, value);
    }


    public void addManagedExecutorPropertyDescriptor(String name, String value) {
        properties.put(name, value);
    }


    @Override
    public String toString() {
        return super.toString() + "[name=" + getName() + ", context=" + context + ", useVirtualThreads=" + useVirtualThreads
                + ", maximumPoolSize=" + maximumPoolSize + ", hungAfterSeconds=" + hungAfterSeconds
            + ", qualifiers=" + qualifiers + ", properties=" + properties + "]";
    }
}
