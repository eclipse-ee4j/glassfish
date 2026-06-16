/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2011, 2022 Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright (c) 2011 Ken Paulsen
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

/**
 *  UtilCommands.java
 *
 *  Created  April 2, 2011
 *  @author  Ken Paulsen (kenapaulsen@gmail.com)
 */
package com.sun.jsft.commands;

import com.sun.jsft.event.Command;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This class contains methods that perform common utility-type functionality.
 * </p>
 *
 * @author Ken Paulsen (kenapaulsen@gmail.com)
 */
@ApplicationScoped
@Named("jsft")
public class JSFTCommands {

    /**
     * <p>
     * Default Constructor.
     * </p>
     */
    public JSFTCommands() {
    }

    /**
     * <p>
     * This command conditionally executes its child commands.
     * </p>
     */
    public void _if(boolean condition) {
        Command command = (Command) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get(Command.COMMAND_KEY);
        if (condition) {
            command.invokeChildCommands();
        } else {
            command = command.getElseCommand();
            if (command != null) {
                command.invoke();
            }
        }
    }

    /**
     * <p>
     * This command iterates over the given List and sets given
     */
    public void foreach(String var, List list) {
        // Get the Request Map
        Map<String, Object> reqMap = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();

        // Get the Current Command...
        Command command = (Command) reqMap.get(Command.COMMAND_KEY);

        // Iterate over each item in the List
        List<Command> childCommands = null;
        for (Object item : list) {
            // Set the item in the request scope under the given key
            reqMap.put(var, item);

            // Invoke all the child commands
            childCommands = command.getChildCommands();
            if (childCommands != null) {
                for (Command childCommand : childCommands) {
                    childCommand.invoke();
                }
            }
        }
    }

    /**
     * <p>
     * This command sets a requestScope attribute with the given <code>key</code> and <code>value</code>.
     * </p>
     */
    public void setAttribute(String key, Object value) {
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put(key, value);
    }

    /**
     * <p>
     * This command writes output using <code>System.out.println</code>. It requires <code>value</code> to be supplied.
     * </p>
     */
    public void println(String value) {
        System.out.println(value);
    }

    /**
     * <p>
     * This command writes using <code>FacesContext.getResponseWriter()</code>.
     * </p>
     *
     * @param context The HandlerContext.
     */
    public static void write(String text) {
        if (text == null) {
            text = "";
        }
        try {
            FacesContext.getCurrentInstance().getResponseWriter().write(text);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * <p>
     * This command marks the response complete. This means that no additional response will be sent. This is useful if
     * you've provided a response already and you don't want JSF to do it again (it may cause problems to do it 2x).
     * </p>
     *
     * @param context The HandlerContext.
     */
    public static void responseComplete() {
        FacesContext.getCurrentInstance().responseComplete();
    }

    /**
     * <p>
     * This command indicates to JSF that the request should proceed immediately to the render response phase. It will be
     * ignored if rendering has already begun. This is useful if you want to stop processing and jump to the response. This
     * is often the case when an error ocurrs or validation fails. Typically the page the user is on will be reshown
     * (although if navigation has already occurred, the new page will be shown.
     * </p>
     *
     * @param context The HandlerContext.
     */
    public void renderResponse() {
        FacesContext.getCurrentInstance().renderResponse();
    }

    /**
     * <p>
     * This command provides a way to see the call stack by printing a stack trace. The output will go to stderr and will
     * also be returned in the output value "stackTrace". An optional message may be provided to be included in the trace.
     * </p>
     */
    public void printStackTrace(String msg) {
        // See if we have a message to print w/ it
        if (msg == null) {
            msg = "";
        }

        // Get the StackTrace
        StringWriter strWriter = new StringWriter();
        new RuntimeException(msg).printStackTrace(new PrintWriter(strWriter));
        String trace = strWriter.toString();

        // Print it to stderr and return it
        System.err.println(trace);
    }

    /**
     * <p>
     * Returns the nano seconds since some point in time. This is only useful for relative measurments.
     * </p>
     */
    public long getNanoTime() {
        return nanoStartTime - System.nanoTime();
    }

    /**
     * <p>
     * This is application scoped, so it is not safe to change. Use caution.
     * </p>
     */
    private long nanoStartTime = System.nanoTime();
}
