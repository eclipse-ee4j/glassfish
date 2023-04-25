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

package org.jvnet.hk2.config;

import org.jvnet.hk2.annotations.Contract;

/**
 * @author jwells
 *
 */
@Contract
public interface ConfigBeanProxyCustomizer {

    /**
     * Returns the parent element of this configuration element.
     *
     * <p>It is possible to return a not {@code null} parent while the parent knows
     * nothing of this child element. This could happen when the child element was removed
     * from the configuration tree, yet it's parent would not have been reset.
     *
     * @return the parent configuration node.
     */
    ConfigBeanProxy getParent(ConfigBeanProxy proxy);

    /**
     * Returns the typed parent element of this configuration element.
     *
     * <p>It is possible to return a not {@code null} parent while the parent knows
     * nothing of this child element. This could happen when the child element was removed
     * from the configuration tree, yet it's parent would not have been reset.
     *
     * @param type parent's type
     * @return the parent configuration node.
     */
    ConfigBeanProxy getParent(ConfigBeanProxy proxy, Class<?> type);

    /**
     * Creates a child element of this configuration element
     *
     * @param type the child element type
     * @return the newly created child instance
     */
    ConfigBeanProxy createChild(ConfigBeanProxy proxy, Class<?> type);


    /**
     * Performs a deep copy of this configuration element and returns it.
     * The parent of this configuration must be locked in a transaction
     * and the newly created child will be automatically enrolled in the
     * parent's transaction.
     *
     * @param proxy a configuration element to copy
     * @param parent the writable copy of the parent
     * @return a deep copy of itself.
     */
    ConfigBeanProxy deepCopy(ConfigBeanProxy proxy, ConfigBeanProxy parent);
}
