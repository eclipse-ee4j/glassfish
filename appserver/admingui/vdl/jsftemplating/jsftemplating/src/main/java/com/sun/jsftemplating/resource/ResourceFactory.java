/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.resource;

import com.sun.jsftemplating.layout.descriptors.Resource;

import jakarta.faces.context.FacesContext;

/**
 * <p>
 * This file defines the ResourceFactory interface. Resources are added to the Request scope so that they may be
 * accessed easily using JSF EL value-binding, or by other convient means.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public interface ResourceFactory {

    /**
     * <p>
     * This is the method responsible for getting the resource using the given {@link Resource} descriptor.
     * </p>
     *
     * @param context The FacesContext
     * @param descriptor The Resource descriptor that is associated with the requested Resource.
     *
     * @return The newly created (or found) resource.
     */
    Object getResource(FacesContext context, Resource descriptor);
}
