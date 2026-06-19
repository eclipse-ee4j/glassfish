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
import com.sun.jsftemplating.util.Util;

import jakarta.faces.context.FacesContext;

import java.util.Map;

/**
 * <p>
 * This factory class provides a means to instantiate a java.util.ResouceBundle. It implements the
 * {@link ResourceFactory} which the {@link com.sun.jsftemplating.renderer.TemplateRenderer} knows how to use to create
 * arbitrary {@link Resource} objects. This factory utilizes the ResourceBundleManager for efficiency.
 * </p>
 *
 * @see ResourceFactory
 * @see Resource
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class ResourceBundleFactory implements ResourceFactory {

    /**
     * <p>
     * This is the factory method responsible for obtaining a ResourceBundle. This method uses the ResourceBundleManager to
     * manage instances of ResourceBundles per key/locale.
     * </p>
     *
     * <p>
     * It should be noted that this method does not do anything if there is already a request attribute with the given id.
     * </p>
     *
     * @param context The FacesContext
     * @param descriptor The Resource descriptor that is associated with the requested Resource.
     *
     * @return The newly created Resource
     */
    @Override
    public Object getResource(FacesContext context, Resource descriptor) {
        // Get the id from the descriptor, this is the id that should be used
        // to store it in the RequestScope
        String id = descriptor.getId();
        Map<String, Object> map = context.getExternalContext().getRequestMap();
        if (map.containsKey(id)) {
            // It is already set
            return map.get(id);
        }

        // Obtain the ResourceBundle
        Object resource = ResourceBundleManager.getInstance(context).getBundle(descriptor.getExtraInfo(), Util.getLocale(context));

        // The id does not exist in the request scope yet.
        map.put(id, resource);

        return resource;
    }
}
