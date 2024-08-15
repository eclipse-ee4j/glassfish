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

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;


public class DeployTask extends TaskBase {

    String serverID = Constants.DEFAULT_SERVER_ID;
    String app = null; // a default value?
    List<String> deployParams = new ArrayList();

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public void setName(String name) {
        deployParams.add("--name=" + name);
    }

    public void setContextroot(String contextroot) {
        deployParams.add("--contextroot=" + contextroot);
    }

    public void setForce(boolean force) {
        deployParams.add("--force=" + force);
    }


    public void setPrecompilejsp(boolean precompilejsp) {
        deployParams.add("--precompilejsp=" + precompilejsp);
    }

    public void setVerify(boolean verify) {
        deployParams.add("--verify=" + verify);
    }

    public void setCreatetables(boolean createtables) {
        deployParams.add("--createtables=" + createtables);
    }

    public void setDropandcreatetables(boolean dropandcreatetables) {
        deployParams.add("--dropandcreatetables=" + dropandcreatetables);
    }

    public void setUniquetablenames(boolean uniquetablenames) {
        deployParams.add("--uniquetablenames=" + uniquetablenames);
    }

    public void setEnabled(boolean enabled) {
        deployParams.add("--enabled=" + enabled);
    }

    public void setAvailabilityenabled(boolean availabilityenabled) {
        deployParams.add("--availabilityenabled=" + availabilityenabled);
    }

    public void setDescription(String description) {
        deployParams.add("--description=" + description);
    }

    public void setVirtualservers(String virtualservers) {
        deployParams.add("--virtualservers=" + virtualservers);
    }

    public void setRetrievestubs(String retrieve) {
        deployParams.add("--retrieve=" + retrieve);
    }

    public void setdbvendorname(String dbvendorname) {
        deployParams.add("--dbvendorname=" + dbvendorname);
    }

    public void setLibraries(String libraries) {
        deployParams.add("--libraries=" + libraries);
    }

    public void setDeploymentPlan(String deploymentplan) {
        deployParams.add("--deploymentplan=" + deploymentplan);
    }

    public void execute() throws BuildException {
        try {
            Util.deploy(app, serverID, deployParams);
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

}
