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

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;

import java.util.List;

/**
 * <p>
 * This class represents a Command that is processed via Unified EL.
 * </p>
 *
 * Created March 31, 2011
 *
 * @author Ken Paulsen (kenapaulsen@gmail.com)
 */
public class ELCommand extends Command {

    /**
     * <p>
     * Default constructor needed for serialization.
     * </p>
     */
    public ELCommand() {
    }

    /**
     * <p>
     * This constructor should be used to create a new <code>ELCommand</code> instance. It expects to be passed an
     * expression, and optionally a List&lt;Command&gt; that represent <i>child</i> commands.
     * </p>
     *
     * FIXME: Add more documentation on how this works...
     */
    public ELCommand(String resultVar, String el, List<Command> childCommands, Command elseCommand) {
        super(childCommands, elseCommand);
        this.resultVar = resultVar;
        this.el = el;
    }

    /**
     * <p>
     * This method is responsible for dispatching the event to the various EL expressions that are listening to this event.
     * It also stores the Event object in request scope under the key "theEvent" so that it can be accessed easiliy via EL.
     * For example: <code>util.println(theEvent);</code>
     * </p>
     */
    @Override
    public Object invoke() throws AbortProcessingException {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ELContext elCtx = ctx.getELContext();

        // Store the Command for access inside the expression.
        // This is useful for loops or other commands which need access to
        // their child Commands.
        ctx.getExternalContext().getRequestMap().put(COMMAND_KEY, this);

        // Create expression
        ExpressionFactory fact = ctx.getApplication().getExpressionFactory();
        ValueExpression ve = null;
        Object result = null;
        if (this.el.length() > 0) {
            ve = fact.createValueExpression(elCtx, "#{" + this.el + "}", Object.class);
            // Execute expression
            result = ve.getValue(elCtx);

            // If we should store the result... do it.
            if (this.resultVar != null) {
                ve = fact.createValueExpression(elCtx, "#{" + this.resultVar + "}", Object.class);
                ve.setValue(elCtx, result);
            }
        } else {
            // Do this since we have no command to execute (which is normally
            // responsible for doing this)
            invokeChildCommands();
        }
        return result;
    }

    /**
     * <p>
     * Print out the <code>ELCommand</code>.
     * </p>
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(el);
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        ELCommand that = (ELCommand) obj;

        if (hashCode() == that.hashCode()) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (hash == -1) {
            StringBuilder builder = new StringBuilder(el);
            List<Command> children = getChildCommands();
            if (children != null) {
                for (Command command : children) {
                    builder.append(command.toString());
                }
            }
            hash = builder.toString().hashCode();
        }
        return hash;
    }

    private String resultVar = null;
    private String el = null;
    private transient int hash = -1;
    private static final long serialVersionUID = 6201115935174238909L;
}
