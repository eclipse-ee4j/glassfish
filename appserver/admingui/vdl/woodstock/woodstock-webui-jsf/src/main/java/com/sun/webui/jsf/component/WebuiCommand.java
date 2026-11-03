/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.webui.jsf.component;

import com.sun.faces.annotation.Property;

import jakarta.el.MethodExpression;
import jakarta.faces.component.UICommand;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.FacesEvent;

/**
 * WebUI command.
 */
public class WebuiCommand extends UICommand {

    /**
     * The component identifier for this component. This value must be unique
     * within the closest parent component that is a naming container.
     * This implementation invokes {@code set.setId}.
     * @param id id
     */
    @Property(name = "id")
    @Override
    public void setId(final String id) {
        super.setId(id);
    }

    /**
     * Flag indicating that event handling for this component should be handled
     * immediately (in Apply Request Values phase) rather than waiting until
     * Invoke Application phase.
     * This implementation invokes {@code set.setImmediate}.
     * @param immediate immediate
     */
    @Property(name = "immediate")
    @Override
    public void setImmediate(final boolean immediate) {
        super.setImmediate(immediate);
    }

    /**
     * Use the rendered attribute to indicate whether the HTML code for the
     * component should be included in the rendered HTML page. If set to false,
     * the rendered HTML page does not include the HTML for the component. If
     * the component is not rendered, it is also not processed on any subsequent
     * form submission.
     * This implementation invokes {@code set.setRendered}.
     * @param rendered rendered
     */
    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    /**
     * MethodExpression representing the application action to invoke when this
     * component is activated by the user. The expression must evaluate to a
     * either a String or a public method that takes no parameters, and returns
     * a String (the logical outcome) which is passed to the NavigationHandler
     * for this application.
     * This implementation invokes {@code super.getActionExpression}
     * @return MethodExpression
     */
    @Property(name = "actionExpression",
            isHidden = true,
            displayName = "Action Expression")
    @Property.Method(signature = "java.lang.String action()")
    @Override
    public MethodExpression getActionExpression() {
        return super.getActionExpression();
    }

    /**
     * Use the actionListenerExpression attribute to cause the component to fire
     * an event. The value must be an EL expression and it must evaluate to the
     * name of a public method that takes an ActionEvent parameter and returns
     * void.
     */
    @Property(name = "actionListenerExpression",
            displayName = "Action Listener Expression",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.MethodBindingPropertyEditor")
            //CHECKSTYLE:ON
    //CHECKSTYLE:OFF
    @Property.Method(signature = "void processAction(jakarta.faces.event.ActionEvent)")
    //CHECKSTYLE:ON
    private MethodExpression actionListenerExpression;

    /**
     * Simply return the stored {@code actionListenerExpression}.
     * The {@code broadcast} method is overridden in
     * {@code WebuiCommand} to invoke the
     * {@code actionListenerExpression}.
     * @return MethodExpression
     */
    public MethodExpression getActionListenerExpression() {
        return this.actionListenerExpression;
    }

    /**
     * Simply store the {@code actionListenerExpression}.
     * The {@code broadcast} method is overridden in
     * {@code WebuiCommand} to invoke the
     * {@code actionListenerExpression}.
     * @param newActionListenerExpression actionListenerExpression
     */
    public void setActionListenerExpression(
            final MethodExpression newActionListenerExpression) {

        this.actionListenerExpression = newActionListenerExpression;
    }

    /**
     * Before calling {@code super.broadcast},
     * pass the {@link ActionEvent} being broadcast to the
     * method referenced by {@code actionListenerExpression} (if any).
     *
     * @param event {@link FacesEvent} to be broadcast
     *
     * @throws AbortProcessingException Signal the JavaServer Faces
     *  implementation that no further processing on the current event
     *  should be performed
     * @throws IllegalArgumentException if the implementation class
     *  of this {@link FacesEvent} is not supported by this component
     * @throws NullPointerException if {@code event} is
     * {@code null}
     */
    @Override
    public void broadcast(final FacesEvent event)
            throws AbortProcessingException {

        if (event == null) {
            throw new NullPointerException();
        }
        if (event instanceof ActionEvent) {
            MethodExpression ale = getActionListenerExpression();
            if (ale != null) {
                ale.invoke(getFacesContext().getELContext(),
                        new Object[]{event});
            }
        }
        super.broadcast(event);
    }

    /**
     * This implementation restores the state of all properties.
     * @param context faces context
     * @param state state object
     */
    @Override
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        actionListenerExpression = (MethodExpression)
                restoreAttachedState(context, values[1]);
    }
    /**
     * This implementation saves the state of all properties.
     * @param context faces context
     * @return Object
     */
    @Override
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[2];
        values[0] = super.saveState(context);
        values[1] = saveAttachedState(context, actionListenerExpression);
        return values;
    }
}
