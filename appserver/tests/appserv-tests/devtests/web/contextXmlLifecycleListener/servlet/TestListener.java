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

package test;

import java.io.*;
import java.util.*;
import jakarta.servlet.*;
import org.apache.catalina.*;

public class TestListener implements LifecycleListener {

    private int debug;
    private String name;
    private Context context;

    /**
     * Acknowledge the occurrence of the specified event.
     *
     * @param event LifecycleEvent that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event)
        throws LifecycleException {

        // Identify the context we are associated with
        try {
            context = (Context) event.getLifecycle();
        } catch (ClassCastException e) {
            System.out.println(e);
        }

        System.out.println(event+" "+event.getType()+" debug="+debug+" name="+name);

        context.getServletContext().setInitParameter("initParamName", "initParamValue");

        // Process the event that has occurred
        if (event.getType().equals(Lifecycle.START_EVENT)) {

        } else if (event.getType().equals(Lifecycle.STOP_EVENT)) {

        } else if (event.getType().equals(Lifecycle.INIT_EVENT)) {
            context.getServletContext().setInitParameter("initParamName", "initParamValue");
        }
    }

    /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {
        return (this.debug);
    }

    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {
        this.debug = debug;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return (this.name);
    }

}
