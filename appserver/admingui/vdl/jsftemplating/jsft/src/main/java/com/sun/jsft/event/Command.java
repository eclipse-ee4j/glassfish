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

import jakarta.faces.event.AbortProcessingException;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * This class represents a Command.
 * </p>
 *
 * Created March 31, 2011
 *
 * @author Ken Paulsen kenapaulsen@gmail.com
 */
public abstract class Command implements Serializable {

    /**
     * <p>
     * Default constructor needed for serialization.
     * </p>
     */
    public Command() {
    }

    /**
     * <p>
     * Constructor which sets the child commands.
     * </p>
     */
    public Command(List<Command> children, Command elseCommand) {
        setChildCommands(children);
        setElseCommand(elseCommand);
    }

    /**
     * <p>
     * This is the method responsible for performing the action. It is also responsible for invoking any of its child
     * commands.
     * </p>
     */
    public abstract Object invoke() throws AbortProcessingException;

    /**
     * <p>
     * This getter method retrieves the command to be invoked if this command has an "else" clause. In most cases this will
     * return <code>null</code>.
     * </p>
     */
    public Command getElseCommand() {
        return this.elseCommand;
    }

    /**
     * <p>
     * Returns a reference to list of child commands. Note, there is nothing to stop you from modifying this list...
     * however, it is strongly discouraged and may lead to problems.
     * </p>
     */
    public List<Command> getChildCommands() {
        return childCommands;
    }

    /**
     * <p>
     * This method is a useful helper utility for invoking the child {@link Command}s.
     * </p>
     */
    public void invokeChildCommands() {
        if (this.childCommands != null) {
            for (Command childCommand : this.childCommands) {
                childCommand.invoke();
            }
        }
    }

    /**
     * <p>
     * Print out the <code>ELCommand</code>.
     * </p>
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("");
        if (childCommands != null) {
            buf.append("{\n");
            Iterator<Command> it = childCommands.iterator();
            while (it.hasNext()) {
                buf.append(it.next().toString());
            }
            buf.append("}\n");
        } else {
            buf.append(";\n");
        }
        return buf.toString();
    }

    /**
     *
     */
    private void setChildCommands(List<Command> commands) {
        this.childCommands = commands;
    }

    /**
     * <p>
     * This setter method stores the command to be invoked if this command has an "else" clause.
     * </p>
     */
    private void setElseCommand(Command command) {
        this.elseCommand = command;
    }

    /**
     * <p>
     * This is the request scoped key which will store the child {@link Command} for the currently executing
     * {@link Command}.
     * </p>
     */
    public static final String COMMAND_KEY = "jsftCommand";

    private static final long serialVersionUID = 6945415932011238909L;

    private List<Command> childCommands = null;
    private Command elseCommand = null;
}
