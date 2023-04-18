/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config.types;

import org.jvnet.hk2.annotations.Contract;

/**
 * @author jwells
 */
@Contract
public interface PropertyBagCustomizer {

    String DEFAULT_IMPLEMENTATION = "system default";

    Property getProperty(PropertyBag propertyBag, String name);

    /**
     * Returns a property value if the bean has properties and one of its
     * properties name is equal to the one passed.
     *
     * @param name the property name requested
     * @return the property value or null if not found
     */
    String getPropertyValue(PropertyBag propertyBag, String name);

    /**
     * Returns a property value if the bean has properties and one of its
     * properties name is equal to the one passed. Otherwise, return
     * the default value.
     *
     * @param name the property name requested
     * @param defaultValue is the default value to return in case the property
     * of that name does not exist in this bag
     * @return the property value
     */
    String getPropertyValue(PropertyBag propertyBag, String name, String defaultValue);
}
