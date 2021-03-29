/*
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

package org.glassfish.config.support;

import org.glassfish.hk2.api.ServiceLocator;

/**
 * a extensible mechanism to define new configuration targets
 *
 * @author Jerome Dochez
 */
public interface TargetValidator {
    /**
     * returns true if the passed target parameter value is a valid identifier of a target instance.
     *
     * @param habitat the habitat where to lookup all the target instances
     * @param target the target identifier to check
     * @return true if target is a valid identifier of a target instance
     */
    public boolean isValid(ServiceLocator habitat, String target);

    /**
     * Returns a internalized aware string describing the target type
     *
     * @return a type description
     */
    public String getDescription();
}
