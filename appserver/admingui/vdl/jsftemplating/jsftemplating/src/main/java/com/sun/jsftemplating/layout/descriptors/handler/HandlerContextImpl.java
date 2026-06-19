/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.layout.descriptors.handler;

import com.sun.jsftemplating.layout.descriptors.LayoutElement;

import jakarta.faces.context.FacesContext;

import java.util.EventObject;

/**
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class HandlerContextImpl implements HandlerContext {

    /**
     * <p>
     * Constructor.
     * </p>
     */
    public HandlerContextImpl(FacesContext context, LayoutElement layoutDesc, EventObject event, String eventType) {
        _facesContext = context;
        _layoutDesc = layoutDesc;
        _event = event;
        _eventType = eventType;
    }

    /**
     * <p>
     * Constructor that gets all its values from the given HandlerContext.
     * </p>
     *
     * @param context The HandlerContext to clone.
     */
    public HandlerContextImpl(HandlerContext context) {
        _facesContext = context.getFacesContext();
        _layoutDesc = context.getLayoutElement();
        _event = context.getEventObject();
        _eventType = context.getEventType();
        _handler = context.getHandler();
    }

    /**
     * <p>
     * Accessor for the FacesContext.
     * </p>
     *
     * @return FacesContext
     */
    @Override
    public FacesContext getFacesContext() {
        return _facesContext;
    }

    /**
     * <p>
     * Accessor for the LayoutElement associated with this Handler.
     * </p>
     */
    @Override
    public LayoutElement getLayoutElement() {
        return _layoutDesc;
    }

    /**
     * <p>
     * Accessor for the EventObject associated with this Handler. This may be null if an EventObject was not created for
     * this handler. An EventObject, if it does exist, may provide additional details describing the context in which this
     * Event is invoked.
     * </p>
     */
    @Override
    public EventObject getEventObject() {
        return _event;
    }

    /**
     * <p>
     * This method provides access to the EventType. This is mostly helpful for diagnostics, but may be used in a handler to
     * determine more information about the context in which the code is executing.
     * </p>
     */
    @Override
    public String getEventType() {
        return _eventType;
    }

    /**
     * <p>
     * Accessor for the Handler descriptor for this Handler. The Handler descriptor object contains specific meta
     * information describing the invocation of this handler. This includes details such as input values, and where output
     * values are to be set.
     * </p>
     */
    @Override
    public Handler getHandler() {
        return _handler;
    }

    /**
     * <p>
     * Setter for the Handler descriptor for this Handler.
     * </p>
     */
    @Override
    public void setHandler(Handler handler) {
        _handler = handler;
    }

    /**
     * <p>
     * Accessor for the Handler descriptor for this Handler. The HandlerDefinition descriptor contains meta information
     * about the actual Java handler that will handle the processing. This includes the inputs required, outputs produces,
     * and the types for both.
     * </p>
     */
    @Override
    public HandlerDefinition getHandlerDefinition() {
        return _handler.getHandlerDefinition();
    }

    /**
     * <p>
     * This method returns the value for the named input. Input values are not stored in this HandlerContext itself, but in
     * the Handler. If you are trying to set input values for a handler, you must create a new Handler object and set its
     * input values.
     * </p>
     *
     * <p>
     * This method attempts to resolve $...{...} expressions. It also will return the default value if the value is null. If
     * you don't want these things to happen, look at Handler.getInputValue(String).
     * </p>
     *
     * @param name The input name
     *
     * @return The value of the input (null if not found)
     */
    @Override
    public Object getInputValue(String name) {
        return getHandler().getInputValue(this, name);
    }

    /**
     * <p>
     * This method retrieves an Output value. Output values must not be stored in this Context itself (remember
     * HandlerContext objects are shared). Output values are stored according to what is specified in the HandlerDefintion.
     * </p>
     *
     * @param name The output name
     *
     * @return The value of the output (null if not found)
     */
    @Override
    public Object getOutputValue(String name) {
        return getHandler().getOutputValue(this, name);
    }

    /**
     * <p>
     * This method sets an Output value. Output values must not be stored in this Context itself (remember HandlerContext
     * objects are shared). Output values are stored according to what is specified in the HandlerDefintion.
     * </p>
     */
    @Override
    public void setOutputValue(String name, Object value) {
        getHandler().setOutputValue(this, name, value);
    }

    private String _eventType = null;
    private FacesContext _facesContext = null;
    private LayoutElement _layoutDesc = null;
    private EventObject _event = null;
    private Handler _handler = null;
}
