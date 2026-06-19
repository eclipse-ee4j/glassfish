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

package com.sun.jsftemplating.renderer;

import com.sun.jsftemplating.component.TemplateComponent;
import com.sun.jsftemplating.layout.descriptors.LayoutDefinition;
import com.sun.jsftemplating.layout.descriptors.Resource;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.Renderer;

import java.io.IOException;
import java.util.Iterator;

/**
 * <p>
 * This renderer is a generic "template-based" renderer. It uses a <code>LayoutElement</code> tree as its template and
 * walks this tree. This renderer will actually delegate the encode functionality to the {@link LayoutDefinition}
 * object, which is the top of the <code>LayoutElement</code> in the tree.
 * </p>
 *
 * <p>
 * This renderer also has the feature of registering {@link Resource} objects to the Request scope prior to rendering
 * its output. This allows {@link Resource} objects such as ResourceBundles to be added to the Request scope for easy
 * access.
 * </p>
 *
 * @see LayoutDefinition
 * @see com.sun.jsftemplating.layout.descriptors.LayoutElement
 * @see Resource
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class TemplateRenderer extends Renderer {

    /**
     * <p>
     * This method returns true. This method indicates that this <code>Renderer</code> will assume resposibilty for
     * rendering its own children.
     * </p>
     *
     * @return true
     *
     * @see #encodeChildren(FacesContext, UIComponent)
     */
    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * <p>
     * This method initializes the Resources so they will be available for children. It then calls encodeBegin on the
     * superclass.
     * </p>
     *
     * @param context The FacesContext
     * @param component The UIComponent, should be a {@link TemplateComponent}
     */
    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        // Make sure we have a TemplateComponent
        if (!(component instanceof TemplateComponent)) {
            throw new IllegalArgumentException("TemplateRenderer requires that its UIComponent be an " + "instance of TemplateComponent!");
        }
        TemplateComponent tempComp = (TemplateComponent) component;
        LayoutDefinition def = tempComp.getLayoutDefinition(context);

        // First ensure that our Resources are available
        Iterator<Resource> it = def.getResources().iterator();
        Resource resource = null;
        while (it.hasNext()) {
            resource = it.next();
            // Just calling getResource() puts it in the Request scope
            resource.getFactory().getResource(context, resource);
        }

        // Call the super class
        super.encodeBegin(context, component);
    }

    /**
     * <p>
     * This method prevents the super class's default functionality of rendering the child UIComponents. This
     * <code>Renderer</code> implementation requires that the children be explicitly rendered. This method does nothing.
     * </p>
     *
     * @param context The <code>FacesContext</code>
     * @param component The <code>UIComponent</code>
     */
    @Override
    public void encodeChildren(FacesContext context, UIComponent component) {
        // Do nothing...
    }

    /**
     * <p>
     * This method performs the rendering for the TemplateRenderer. It expects that component be an instanceof
     * {@link TemplateComponent}. It obtains the {@link LayoutDefinition} from the {@link TemplateComponent}, initializes
     * the {@link Resource} objects defined by the {@link LayoutDefinition} (if any), and finally delegates the encoding to
     * the {@link LayoutDefinition#encode(FacesContext, UIComponent)} method of the {@link LayoutDefinition}.
     * </p>
     *
     * @param context The FacesContext object.
     * @param component The {@link TemplateComponent}.
     */
    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        // Sanity Check...
        if (!component.isRendered()) {
            return;
        }

        // Get the LayoutDefinition and begin rendering
        TemplateComponent tempComp = (TemplateComponent) component;
        LayoutDefinition def = tempComp.getLayoutDefinition(context);

        // The following "encode" method does all the rendering
        def.encode(context, (UIComponent) tempComp);
    }

    /**
     * <p>
     * Decode any new state of the specified UIComponent from the request contained in the specified FacesContext, and store
     * that state on the UIComponent.
     * </p>
     *
     * <p>
     * During decoding, events may be queued for later processing (by event listeners that have registered an interest), by
     * calling <code>queueEvent()</code> on the associated UIComponent.
     * </p>
     *
     * <p>
     * This implementation of this method invokes the super class and then any handlers that have been registered to process
     * decode functionality. The execution of these handlers is delegated to the LayoutDefinition.
     * </p>
     *
     * @param context FacesContext for the request we are processing
     * @param component UIComponent to be decoded.
     *
     * @exception NullPointerException if <code>context</code> or <code>component</code> is <code>null</code>
     */
    @Override
    public void decode(FacesContext context, UIComponent component) {
        // Call the super first
        super.decode(context, component);

        // Call any decode handlers
        TemplateComponent tempComp = (TemplateComponent) component;
        LayoutDefinition def = tempComp.getLayoutDefinition(context);
        def.decode(context, component);
    }
}
