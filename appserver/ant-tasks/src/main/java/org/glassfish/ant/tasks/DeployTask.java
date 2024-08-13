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

package org.glassfish.ant.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

public class DeployTask extends AdminTask {

    private String action;
    private String file;
    private Component component;
    private List<Component> components = new ArrayList<Component>();

    public DeployTask() {
        setAction("deploy");
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setName(String name) {
        addCommandParameter("name", name);
    }

    public void setContextroot(String contextroot) {
        addCommandParameter("contextroot", contextroot);
    }


    public void setPrecompilejsp(boolean precompilejsp) {
        addCommandParameter("precompilejsp", Boolean.toString(precompilejsp));
    }

    public void setVerify(boolean verify) {
        addCommandParameter("verify", Boolean.toString(verify));
    }

    public void setCreatetables(boolean createtables) {
        addCommandParameter("createtables", Boolean.toString(createtables));
    }

    public void setDropandcreatetables(boolean dropandcreatetables) {
        addCommandParameter("dropandcreatetables", Boolean.toString(dropandcreatetables));
    }

    public void setUniquetablenames(boolean uniquetablenames) {
        addCommandParameter("uniquetablenames", Boolean.toString(uniquetablenames));
    }

    public void setEnabled(boolean enabled) {
        addCommandParameter("enabled", Boolean.toString(enabled));
    }

    public void setAvailabilityenabled(boolean availabilityenabled) {
        addCommandParameter("availabilityenabled", Boolean.toString(availabilityenabled));
    }

    public void setVirtualservers(String virtualservers) {
        addCommandParameter("virtualservers", virtualservers);
    }

    public void setRetrievestubs(String retrieve) {
        addCommandParameter("retrieve", retrieve);
    }

    public void setdbvendorname(String dbvendorname) {
        addCommandParameter("dbvendorname", dbvendorname);
    }

    public void setLibraries(String libraries) {
        addCommandParameter("libraries", libraries);
    }

    public void setDeploymentPlan(String deploymentplan) {
        addCommandParameter("deploymentplan", deploymentplan);
    }

    public void setForce(boolean force) {
        addCommandParameter("force", Boolean.toString(force));
    }

    public void setUpload(boolean force) {
        addCommandParameter("upload", Boolean.toString(force));
    }

    public void setProperties(String properties) {
        addCommandParameter("properties", properties);
    }

    public void setType(String type) {
        addCommandParameter("type", type);
    }

    public Component createComponent() {
        component = new Component();
        components.add(component);
        return component;
    }

    public void execute() throws BuildException {
        if (components.size() == 0 && file == null ) {
            log("File attributes or component must be specified", Project.MSG_WARN);
            return;
        }
        processComponents();
        if (file != null) {
            addCommandOperand(file);
            super.execute(action + " " + getCommand());
        }
    }

    private void processComponents() throws BuildException {
        for (Component comp : components) {
            if (comp.name != null)
                comp.addCommandParameter("name", comp.name);
            if (comp.file == null) {
                log("File attribute must be specified in component to deploy", Project.MSG_WARN);
                continue;
            }
            comp.addCommandOperand(comp.file);
            super.execute(action + " " + comp.getCommand());
        }
    }
}
