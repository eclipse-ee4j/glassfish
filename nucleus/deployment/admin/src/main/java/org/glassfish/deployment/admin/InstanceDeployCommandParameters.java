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

package org.glassfish.deployment.admin;

import java.io.File;
import java.util.Properties;

import org.glassfish.api.Param;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.deployment.common.DeploymentProperties;

/**
 * Parameters for the remote instance deploy command, beyond the ones already
 * defined for the DAS DeployCommand.
 *
 * @author Tim Quinn
 */
public class InstanceDeployCommandParameters extends DeployCommandParameters {

    @Param(name=ParameterNames.GENERATED_CONTENT, optional=true)
    public File generatedcontent = null;

    @Param(name=ParameterNames.APP_PROPS, separator=':')
    public Properties appprops = null;

    @Param(name=ParameterNames.PRESERVED_CONTEXTROOT, optional=true)
    public String preservedcontextroot = null;

    @Param(name=DeploymentProperties.PREVIOUS_VIRTUAL_SERVERS, separator=':', optional=true)
    public Properties previousVirtualServers = null;

    @Param(name=DeploymentProperties.PREVIOUS_ENABLED_ATTRIBUTES, separator=':', optional=true)
    public Properties previousEnabledAttributes = null;

    public static class ParameterNames {
        public static final String GENERATED_CONTENT = "generatedcontent";
        public static final String APP_PROPS = "appprops";
        public static final String PRESERVED_CONTEXTROOT = "preservedcontextroot";
    }


}
