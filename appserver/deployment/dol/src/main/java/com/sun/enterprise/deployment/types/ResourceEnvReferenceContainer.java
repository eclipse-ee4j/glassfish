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

package com.sun.enterprise.deployment.types;

import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;

import java.util.Set;

/**
 * This type defines the behaviour of a J2EE Component containing
 * resources env references.
 *
 * @author  Jeome Dochez
 * @version
 */
public interface ResourceEnvReferenceContainer {

    /**
     * Add a resource environment reference to myself
     *
     * @param the new resource environment ref
     */
    public void addResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvReference);

    /**
     * Return a resource environment reference by the same name or throw an IllegalArgumentException.
     *
     * @param the resource environment reference name
     */
    public ResourceEnvReferenceDescriptor getResourceEnvReferenceByName(String name);

   /**
    * Return the set of resource environment references
    */
    public Set getResourceEnvReferenceDescriptors();

}

