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

public class UndeployTask extends AdminTask {

    private String action = "undeploy";
    private String name;
    private Component component;
    private List<Component> components = new ArrayList<Component>();

    public UndeployTask() {
        setCommand(action);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setForce(boolean force) {
        addCommandParameter("force", Boolean.toString(force));
    }

    public void setDroptables(boolean droptables) {
        addCommandParameter("droptables", Boolean.toString(droptables));
    }

    public void setCascade(boolean cascade) {
        addCommandParameter("cascade", Boolean.toString(cascade));
    }

    public void setKeepreposdir(boolean keepreposdir) {
        addCommandParameter("keepreposdir", Boolean.toString(keepreposdir));
    }

    public Component createComponent() {
        component = new Component();
        components.add(component);
        return component;
    }

    public void execute() throws BuildException {
        if (components.size() == 0 && name == null) {
            log("name attribute or component must be specified", Project.MSG_WARN);
            return;
        }
        processComponents();
        if (name != null) {
            addCommandOperand(name);
            super.execute();
        }
    }

    private void processComponents() throws BuildException {
        for (Component comp : components) {
            if (comp.name == null) {
                log("name attribute must be specified in component to deploy", Project.MSG_WARN);
                return;
            }

            comp.addCommandOperand(comp.name);
            super.execute(action + " " + comp.getCommand());
        }
    }
}
