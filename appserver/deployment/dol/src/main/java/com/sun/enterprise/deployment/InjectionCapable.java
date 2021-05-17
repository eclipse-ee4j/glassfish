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

import java.util.Set;

/**
 * InjectionCapable describes a type of J2EE component environment resource
 * that is capable of being injected at runtime.
 *
 * @author Kenneth Saks
 */
public interface InjectionCapable {

    /**
     * True if a particular resource instance is injectable.  All injectable
     * resources are also exposed via their corresponding java:comp/env
     * namespaces, but not all resources in java:comp/env are injectable.
     * A resource is either field injectable or method injectable but never
     * both.
     */
    public boolean isInjectable();

    /**
     * @return the set of injection targets for this resource dependency
     */
    public Set<InjectionTarget> getInjectionTargets();

    /**
     * Add a new injection target for this dependency
     */
    public void addInjectionTarget(InjectionTarget target);

    /**
     * ComponentEnvName is the name of the corresponding java:comp/env
     * entry for the object that is to be injected.
     */
    public String getComponentEnvName();

    /**
     * This is the class name of the type of resource that is to be injected.
     */
    public String getInjectResourceType();
    public void setInjectResourceType(String resourceType);

}
