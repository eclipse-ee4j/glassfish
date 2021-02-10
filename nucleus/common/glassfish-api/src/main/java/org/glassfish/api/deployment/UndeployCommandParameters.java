/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.deployment;

import java.util.Properties;

import org.glassfish.api.Param;

/**
 * Parameters passed to a deployment command.
 *
 * @Author Jerome Dochez
 */
public class UndeployCommandParameters extends OpsParams {

    @Param(primary = true)
    public String name = null;

    @Param(optional = true)
    public String target;

    @Param(optional = true, defaultValue = "false")
    public Boolean keepreposdir;

    @Param(optional = true, defaultValue = "false")
    public Boolean isredeploy = false;

    public Boolean isRedeploy() {
        return isredeploy;
    }

    @Param(optional = true)
    public Boolean droptables;

    @Param(optional = true, defaultValue = "false")
    public Boolean cascade;

    // used for internal purposes only, not to expose to user
    @Param(optional = true, defaultValue = "false", name = "_ignoreCascade")
    public Boolean _ignoreCascade = false;

    // used for internal purpose to carry the archive type information
    public String _type = null;

    @Param(optional = true, separator = ':')
    public Properties properties = null;

    public Properties getProperties() {
        return properties;
    }

    @Param(optional = true)
    public Boolean keepstate;

    @Override
    public String name() {
        return name;
    }

    public UndeployCommandParameters() {
    }

    public UndeployCommandParameters(String name) {
        this.name = name;
    }

    @Override
    public String libraries() {
        throw new IllegalStateException("We need to be able to get access to libraries when undeploying");
    }
}
