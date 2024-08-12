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


public class ComponentTask extends DeployTask {

    String action;
    private Component component;
    private List<Component> components = new ArrayList<Component>();

    public ComponentTask() {
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Component createComponent() {
        component = new Component();
        components.add(component);
        return component;
    }

    public void execute() throws BuildException {
        if (components.size() == 0) {
            log("component must be specified", Project.MSG_WARN);
            return;
        }
        if (!valid(action)) {
            log("action must be specified. Valid values are 'enable' and 'disable'", Project.MSG_WARN);
            return;
        }
        processComponents();
    }

    private void processComponents() throws BuildException {
        for (Component comp : components) {
            super.execute(action + " " + comp.name);
        }
    }

    private boolean valid(String action) {
        return (action != null &&
                (action.equals("enable") || action.equals("disable")));
    }
}
