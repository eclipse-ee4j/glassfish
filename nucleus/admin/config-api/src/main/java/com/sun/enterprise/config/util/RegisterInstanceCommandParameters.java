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

package com.sun.enterprise.config.util;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandParameters;

/**
 * Parameters for the _register-instance command
 *
 * @author Jennifer Chou
 */
public class RegisterInstanceCommandParameters implements CommandParameters {

    /* instance params */
    @Param(name = ParameterNames.OPERAND_NAME, primary = true)
    public String instanceName;
    @Param(name = ParameterNames.PARAM_CONFIG, optional = true)
    public String config;
    @Param(name = ParameterNames.PARAM_NODE, optional = true)
    public String node;
    @Param(name = ParameterNames.PARAM_CLUSTER, optional = true)
    public String clusterName;
    @Param(name = ParameterNames.PARAM_LBENABLED, optional = true)
    public String lbEnabled = null;

    public static class ParameterNames {
        public static final String OPERAND_NAME = "name";
        public static final String PARAM_LBENABLED = "lbenabled";
        public static final String PARAM_PORTBASE = "portbase";
        public static final String PARAM_NODE = "node";
        public static final String PARAM_CLUSTER = "cluster";
        public static final String PARAM_CONFIG = "config";
        public static final String PARAM_CHECKPORTS = "checkports";
    }

}
