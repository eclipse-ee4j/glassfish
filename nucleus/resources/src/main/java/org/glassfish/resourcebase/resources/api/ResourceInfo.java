/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.resourcebase.resources.api;

import java.util.Objects;

import org.glassfish.api.naming.SimpleJndiName;

/**
 * Represents resource information (typically, bindable-resource)
 *
 * @author Jagadish Ramu
 */
public class ResourceInfo implements GenericResourceInfo {

    private static final long serialVersionUID = 8908989934557767621L;

    private final SimpleJndiName name;
    private final String applicationName;
    private final String moduleName;

    public ResourceInfo(SimpleJndiName name) {
        this(name, null, null);
    }


    public ResourceInfo(SimpleJndiName name, String applicationName) {
        this(name, applicationName, null);
    }


    public ResourceInfo(SimpleJndiName name, String applicationName, String moduleName) {
        this.name = name;
        this.applicationName = applicationName;
        this.moduleName = moduleName;
    }


    @Override
    public SimpleJndiName getName() {
        return name;
    }


    @Override
    public String getApplicationName() {
        return applicationName;
    }


    @Override
    public String getModuleName() {
        return moduleName;
    }


    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof ResourceInfo) {
            ResourceInfo ri = (ResourceInfo) o;
            return Objects.equals(this.name, ri.name) && Objects.equals(this.applicationName, ri.applicationName)
                && Objects.equals(this.moduleName, ri.moduleName);
        }
        return false;
    }


    @Override
    public int hashCode() {
        return Objects.hash(name, applicationName, moduleName);
    }


    @Override
    public String toString() {
        return super.toString() + "[jndiName=" + name + ", applicationName=" + applicationName + ", moduleName="
            + moduleName + "]";
    }
}
