/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ant.embedded.tasks;

import org.apache.tools.ant.BuildException;

public class StartServerTask extends TaskBase {

    String serverID = Constants.DEFAULT_SERVER_ID;
    int port = Constants.DEFAULT_HTTP_PORT;
    String installRoot = null, instanceRoot = null, configFile = null;
    Boolean configFileReadOnly = true;

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    /**
     * Set the default http port
     *
     * The default http port will be used only when a custom domain.xml isn't provided
     * via instanceRoot or configFileURI
     *
     * @param port default http port
     */
    public void setHttpPort(int port) {
        this.port = port;
    }

    public void setInstallRoot(String installRoot) {
        this.installRoot = installRoot;
    }

    public void setInstanceRoot(String instanceRoot) {
        this.instanceRoot = instanceRoot;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public void setConfigFileReadOnly(Boolean configFileReadOnly) {
        this.configFileReadOnly = configFileReadOnly;
    }

    public void execute() throws BuildException {
        try {
            Util.startGlassFish(serverID, installRoot, instanceRoot,
                    configFile, configFileReadOnly, port);
        } catch (Exception ex) {
            error(ex);
        }
    }

}
