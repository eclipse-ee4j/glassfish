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
public interface HandlerContext {

    /**
     * <p>
     * Accessor for the FacesContext.
     * </p>
     *
     * @return FacesContext
     */
    FacesContext getFacesContext();

    /**
     * <p>
     * Accessor for the LayoutElement associated with this Handler. The LayoutElement associated with this Handler is the
     * LayoutElement which declared the handler. This provides a way for the handler to obtain access to the LayoutElement
     * which is responsible for it being invoked.
     * </p>
     */
    LayoutElement getLayoutElement();

    /**
     * <p>
     * Accessor for the EventObject associated with this Handler. This may be null if an EventObject was not created for
     * this handler. An EventObject, if it does exist, may provide additional details describing the context in which this
     * Event is invoked.
     * </p>
     */
    EventObject getEventObject();

    /**
     * <p>
     * This method provides access to the EventType. This is mostly helpful for diagnostics, but may be used in a handler to
     * determine more information about the context in which the code is executing.
     * </p>
     */
    String getEventType();

    /**
     * <p>
     * Accessor for the Handler descriptor for this Handler. The Handler descriptor object contains specific meta
     * information describing the invocation of this handler. This includes details such as input values, and where output
     * values are to be set.
     * </p>
     */
    Handler getHandler();

    /**
     * <p>
     * Setter for the Handler descriptor for this Handler.
     * </p>
     *
     * @param handler The Handler
     */
    void setHandler(Handler handler);

    /**
     * <p>
     * Accessor for the Handler descriptor for this Handler. The HandlerDefinition descriptor contains meta information
     * about the actual Java handler that will handle the processing. This includes the inputs required, outputs produces,
     * and the types for both.
     * </p>
     */
    HandlerDefinition getHandlerDefinition();

    /**
     * <p>
     * This method returns the value for the named input. Input values are not stored in this Context itself, but in the
     * Handler. If you are trying to set input values for a handler, you must create a new Handler object and set its input
     * values.
     * </p>
     *
     * @param name The input name
     *
     * @return The value of the input (null if not found)
     */
    Object getInputValue(String name);

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
    Object getOutputValue(String name);

    /**
     * <p>
     * This method sets an Output value. Output values must not be stored in this Context itself (remember HandlerContext
     * objects are shared). Output values are stored according to what is specified in the HandlerDefintion.
     * </p>
     */
    void setOutputValue(String name, Object value);
}
