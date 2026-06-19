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

package com.sun.jsftemplating.layout.descriptors;

import com.sun.jsftemplating.layout.descriptors.handler.Handler;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.io.IOException;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This interface is declares the methods required to be a LayoutElement. A LayoutElement is the building block of the
 * tree structure which defines a layout for a particular component. There are different implementations of
 * LayoutElement that provide various different types of functionality and data. Some examples are:
 * </p>
 *
 * <ul>
 * <li>Conditional ({@link LayoutIf}), this allows portions of the layout tree to be conditionally rendered.</li>
 * <li>Iterative ({@link LayoutWhile}), this allows portions of the layout tree to be iteratively rendered.</li>
 * <li>UIComponent ({@link LayoutComponent}), this allows concrete UIComponents to be used. If the component doesn't
 * already exist, it will be created automatically.</li>
 * <li>Facet place holders ({@link LayoutFacet}), this provides a means to specify where a facet should be rendered. It
 * is not a facet itself but where a facet should be drawn. However, in addition, it may specify a default value if no
 * facet was provided.</li>
 * </ul>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public interface LayoutElement extends java.io.Serializable {

    /**
     * This method is used to add a LayoutElement. LayoutElements should be added sequentially in the order in which they
     * are to be rendered.
     */
    void addChildLayoutElement(LayoutElement element);

    /**
     * This method returns the child LayoutElements as a List of LayoutElement.
     *
     * @return List of LayoutElements
     */
    List<LayoutElement> getChildLayoutElements();

    /**
     * <p>
     * This method returns the requested child {@link LayoutElement} by <code>id</code>.
     * </p>
     *
     * @param id The <code>id</code> of the child to find and return.
     *
     * @return The requested {@link LayoutElement}; <code>null</code> if not found.
     */
    LayoutElement getChildLayoutElement(String id);

    /**
     * <p>
     * This method searches the <code>LayoutElement</code> tree breadth-first for a <code>LayoutElement</code> with the
     * given id.
     * </p>
     */
    LayoutElement findLayoutElement(String id);

    /**
     * This method returns the parent LayoutElement.
     *
     * @return parent LayoutElement
     */
    LayoutElement getParent();

    /**
     * This method returns the LayoutDefinition. If unable to, it will throw an Exception.
     *
     * @return The LayoutDefinition
     */
    LayoutDefinition getLayoutDefinition();

    /**
     * <p>
     * This method retrieves the {@link Handler}s for the requested type.
     * </p>
     *
     * @param type The event type of {@link Handler}s to retrieve.
     *
     * @return A List of {@link Handler}s.
     */
    List<Handler> getHandlers(String type);

    /**
     * <p>
     * This method retrieves the {@link Handler}s for the requested type. This method is unique in that it looks at the
     * <code>UIComponent</code> passed in to see if there are {@link Handler}s defined on it (instance handlers vs. those
     * defined on the <code>LayoutElement</code>.
     * </p>
     *
     * @param type The event type of {@link Handler}s to retrieve.
     * @param comp The associated <code>UIComponent</code> (or null).
     *
     * @return A List of {@link Handler}s.
     */
    List<Handler> getHandlers(String type, UIComponent comp);

    /**
     * <p>
     * This method provides access to the "handlersByType" <code>Map</code>.
     * </p>
     */
    Map<String, List<Handler>> getHandlersByTypeMap();

    /**
     * <p>
     * This method associates 'type' with the given list of Handlers.
     * </p>
     *
     * @param type The String type for the List of Handlers
     * @param handlers The List of Handlers
     */
    void setHandlers(String type, List<Handler> handlers);

    /**
     * Accessor method for id. This should always return a non-null value, it may return "" if id does not apply.
     *
     * @return a non-null id
     */
    String getId(FacesContext context, UIComponent parent);

    /**
     * <p>
     * This method generally should not be used. It does not resolve expressions. Instead use
     * {@link #getId(FacesContext, UIComponent)}.
     * </p>
     *
     * @return The unevaluated id.
     */
    String getUnevaluatedId();

    /**
     * This method performs any encode action for this particular LayoutElement.
     *
     * @param context The FacesContext
     * @param component The UIComponent
     */
    void encode(FacesContext context, UIComponent component) throws IOException;

    /**
     *
     */
    Object dispatchHandlers(HandlerContext handlerCtx, List<Handler> handlers);

    /**
     * <p>
     * This method iterates over the handlers and executes each one. A HandlerContext will be created to pass to each
     * Handler. The HandlerContext object is reused across all Handlers that are invoked; the setHandler(Handler) method is
     * invoked with the correct Handler descriptor before the handler is executed.
     * </p>
     *
     * @param context The FacesContext
     * @param eventType The event type which is being fired
     * @param event An optional EventObject providing more detail
     *
     * @return By default, (null) is returned. However, if any of the handlers produce a non-null return value, then the
     * value from the last handler to produces a non-null return value is returned.
     */
    Object dispatchHandlers(FacesContext context, String eventType, EventObject event);
}
