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

package org.glassfish.admin.rest.composite.metadata;

/**
 * An implementation of this interface will allow for the generation/calculation of default values for those situations
 * where such values are dependent on other values in the running system. For example, a default account name can not
 * conflict with an existing account, so such an implementation would apply logic to calculate a reasonable default that
 * is not currently in use in the system.
 *
 * @author jdlee
 */
public interface DefaultsGenerator {
    /**
     * Generate/calculate the default value for the property of the given name.
     *
     * @param propertyName
     * @return
     */
    Object getDefaultValue(String propertyName);
}
