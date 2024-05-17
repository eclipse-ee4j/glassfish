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

import java.util.Properties;

/**
 * @author David Matejcek
 */
public class ManagedExecutorDefinitionData implements ContextualResourceDefinition {

    private static final long serialVersionUID = -6027151040382351476L;

    private String name;
    private String context;
    private Class<?>[] qualifiers;
    private int maximumPoolSize = Integer.MAX_VALUE;
    private long hungAfterSeconds;
    private boolean virtual;
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
    public Class<?>[] getQualifiers() {
        return qualifiers;
    }


    @Override
    public void setQualifiers(Class<?>[] qualifiers) {
        this.qualifiers = qualifiers;
    }


    @Override
    public boolean isVirtual() {
        return virtual;
    }


    @Override
    public void setVirtual(boolean virtual) {
        this.virtual = virtual;
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
        return super.toString() + "[" + getName() + "]";
    }
}
