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

package org.glassfish.api.admin;

import java.util.List;

/**
 * A parameter mapper acts as a bridge between supplied parameters (by the user on the command line for instance) and
 * expected parameters by a action.
 * <p>
 * For example, a command execution requires parameters from the command line while a supplemented command may require a
 * different set of parameter names which can be either discovered or translated from the originally supplied list.
 *
 * @author Jerome Dochez
 */
public interface ParameterBridge {

    /**
     * Returns the parameter value as expected by the injection code when a dependency injection annotated field or method
     * (for instance, annotated with @Param or @Inject) needs to be resolved.
     *
     * @param map is the original set of parameters as used to inject the supplemented command.
     * @param resourceName the name of the resource as defined by the action
     * @return the value used to inject the parameter identified by its resourceName
     */
    public String getOne(ParameterMap map, String resourceName);

    /**
     * Returns the parameter values as expected by the injection code when a dependency injection annotated field or method
     * (for instance, annotated with @Param or @Inject) needs to be resolved.
     *
     * @param map is the original set of parameters as used to inject the supplemented command.
     * @param resourceName the name of the resource as defined by the action
     * @return a list of values used to inject the parameter identified by its resourceName
     */
    public List<String> get(ParameterMap map, String resourceName);

    /**
     * Provided mapper that does not change parameters names or values from the input set.
     */
    final class NoMapper implements ParameterBridge {
        @Override
        public String getOne(ParameterMap map, String resourceName) {
            return map.getOne(resourceName);
        }

        @Override
        public List<String> get(ParameterMap map, String resourceName) {
            return map.get(resourceName);
        }
    }
}
