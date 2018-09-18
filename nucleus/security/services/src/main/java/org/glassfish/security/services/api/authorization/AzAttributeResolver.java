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

package org.glassfish.security.services.api.authorization;


import org.glassfish.security.services.api.common.Attribute;


/**
 * <code>AzAttributeResolver</code> provides runtime resolution of attributes.
 * <p>
 * This is used to obtain attribute values which are not available in the
 * corresponding AzAttributes collection. Implementations may obtain
 * the attribute values from arbitrary sources, including
 * calculation or from remote servers. Since
 * an <code>AzAttributeResolver</code> may be called frequently during time-
 * sensitive operations, caching and other performance-enhancing techniques
 * should be used by the implementation.
 */

public interface AzAttributeResolver {

    /**
     * Resolves the specified attribute.
     *
     * @param attributeName The attribute to resolve.
     * @param collection The read-only collection within which the attribute resides.
     * @param environment The read-only operational context
     * @return The resolved attribute, null if unresolvable.
     */
    public Attribute resolve(
        String attributeName,
        AzAttributes collection,
        AzEnvironment environment );
}
