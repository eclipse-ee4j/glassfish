/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsft.event;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;

import java.util.List;

/**
 * <p>
 * This class handles the dispatching of events to commands. Currently Commands delegate execution to EL. In the future,
 * other Command types may be supported.
 * </p>
 *
 * Created March 29, 2011
 *
 * @author Ken Paulsen (kenapaulsen@gmail.com)
 */
public class CommandEventListener extends Command implements ComponentSystemEventListener {

    /**
     * <p>
     * Default constructor needed for serialization.
     * </p>
     */
    public CommandEventListener() {
    }

    /**
     * <p>
     * Primary constructor used. It is neeeded in order to supply a list of commands.
     * </p>
     */
    public CommandEventListener(List<Command> commands) {
        super(commands, null);
    }

    /**
     * <p>
     * This method is responsible for dispatching the event to the various EL expressions that are listening to this event.
     * It also stores the Event object in request scope under the key "theEvent" so that it can be accessed easiliy via EL.
     * For example: <code>util.println(theEvent);</code>
     * </p>
     */
    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {

        // Store the event under the key "theEvent" in case we want to access
        // it for some reason.
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("theEvent", event);

        // Execute the child commands
        invoke();
    }

    /**
     * <p>
     * This is the method responsible for performing the action. It is also responsible for invoking any of its child
     * commands.
     * </p>
     */
    @Override
    public Object invoke() throws AbortProcessingException {
        // Invoke the child commands...
        invokeChildCommands();

        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        CommandEventListener that = (CommandEventListener) obj;

        if (hashCode() != that.hashCode()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        if (hash == -1) {
            StringBuilder builder = new StringBuilder("");
            List<Command> commands = getChildCommands();
            if (commands != null) {
                for (Command command : commands) {
                    builder.append(command.toString());
                }
            }
            hash = builder.toString().hashCode();
        }
        return hash;
    }

    private transient int hash = -1;
    private static final long serialVersionUID = 6945415935164238909L;
}
