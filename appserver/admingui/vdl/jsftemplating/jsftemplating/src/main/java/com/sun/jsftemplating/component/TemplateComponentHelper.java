/*
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

package com.sun.jsftemplating.component;

import com.sun.jsftemplating.layout.LayoutDefinitionException;
import com.sun.jsftemplating.layout.LayoutDefinitionManager;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.layout.descriptors.LayoutDefinition;
import com.sun.jsftemplating.layout.descriptors.LayoutElement;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * This class provides base functionality for components that work in conjunction with the
 * {@link com.sun.jsftemplating.renderer.TemplateRenderer}. It provides the bulk of the default implementation of the
 * {@link TemplateComponent} interface.
 * </p>
 *
 * <p>
 * This class is meant to be used inside a <code>UIComponent</code> class that implements <code>TemplateComponent</code>
 * to help provide the behavior of a <code>TemplateComponent</code>. It is <em>NOT</em> an implementation by itself. A
 * <code>TemplateComonent</code> implementation class may use this to help define its functionality and must also be an
 * instance of <code>UIComponent</code>.
 * </p>
 *
 * @see com.sun.jsftemplating.renderer.TemplateRenderer
 * @see TemplateComponent
 * @see TemplateComponentBase
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class TemplateComponentHelper {

    /**
     * <p>
     * This is the LayoutDefinition key for the <code>UIComponent</code>. This is typically set by the Tag. The Component
     * may also provide a default by setting it in its constructor.
     * </p>
     */
    private String _ldmKey;

    /**
     * <p>
     * This is a cached reference to the {@link LayoutDefinition} used by the <code>UIComponent</code>.
     * </p>
     */
    private transient LayoutDefinition _layoutDefinition;

    /**
     * <p>
     * This class should only be used by <code>TemplateComponent</code> implementations to help them provide their
     * <code>TemplateComponent</code> funtionality.
     * </p>
     */
    public TemplateComponentHelper() {
    }

    /**
     * <p>
     * This method will find the request child UIComponent by id. If it is not found, it will attempt to create it if it can
     * find a {@link LayoutElement} describing it.
     * </p>
     *
     * @param context The <code>FacesContext</code>.
     * @param id The <code>UIComponent</code> id to find.
     *
     * @return The requested UIComponent
     */
    public UIComponent getChild(UIComponent comp, FacesContext context, String id) {
        if (id == null || id.trim().equals("")) {
            // No id, no LayoutComponent, nothing we can do.
            return null;
        }

        // We have an id, use it to search for an already-created child
        // FIXME: I am doing this 2x if it falls through to create the child...
        // FIXME: think about optimizing this
        UIComponent childComponent = ComponentUtil.getInstance(context).findChild(comp, id, id);
        if (childComponent != null) {
            return childComponent;
        }

        // If we're still here, then we need to create it... hopefully we have
        // a LayoutComponent to tell us how to do this!
        LayoutDefinition ld = getLayoutDefinition(context);
        if (ld == null) {
            // No LayoutDefinition to tell us how to create it... return null
            return null;
        }

        // Attempt to find a LayoutComponent matching the id
        LayoutElement elt = LayoutDefinition.getChildLayoutElementById(context, id, ld, comp);

        // Create the child from the LayoutComponent
        return getChild(comp, context, (LayoutComponent) elt);
    }

    /**
     * <p>
     * This method will find the request child <code>UIComponent</code> by id (the id is obtained from the given
     * {@link LayoutComponent}). If it is not found, it will attempt to create it from the supplied {@link LayoutElement}.
     * </p>
     *
     * @param descriptor The {@link LayoutElement} describing the <code>UIComponent</code>.
     *
     * @return The requested UIComponent
     */
    public UIComponent getChild(UIComponent comp, FacesContext context, LayoutComponent descriptor) {
        UIComponent childComponent = null;

        // Sanity check
        if (descriptor == null) {
            throw new IllegalArgumentException("The LayoutComponent is null!");
        }

        // First pull off the id from the descriptor
        String id = descriptor.getId(context, comp);
        ComponentUtil compUtil = ComponentUtil.getInstance(context);
        if (id != null && !id.trim().equals("")) {
            // We have an id, use it to search for an already-created child
            childComponent = compUtil.findChild(comp, id, id);
            if (childComponent != null) {
                return childComponent;
            }
        }

        // No id, or the component hasn't been created. In either case, we
        // create a new component (moral: always have an id)

        // Invoke "beforeCreate" handlers
        descriptor.beforeCreate(context, comp);

        // Create UIComponent
        childComponent = compUtil.createChildComponent(context, descriptor, comp);

        // Invoke "afterCreate" handlers
        descriptor.afterCreate(context, childComponent);

        // Return the newly created UIComponent
        return childComponent;
    }

    /**
     * <p>
     * This method returns the {@link LayoutDefinition} associated with the <code>UIComponent</code>.
     * </p>
     *
     * @param context The <code>FacesContext</code>.
     *
     * @return {@link LayoutDefinition} associated with the <code>UIComponent</code>.
     */
    public LayoutDefinition getLayoutDefinition(FacesContext context) {
        // Make sure we don't already have it...
        if (_layoutDefinition != null) {
            return _layoutDefinition;
        }

        // Get the LayoutDefinitionManager key
        String key = getLayoutDefinitionKey();
        if (key == null) {
            throw new NullPointerException("LayoutDefinition key is null!");
        }

        // Save the LayoutDefinition for future calls to this method
        try {
            _layoutDefinition = LayoutDefinitionManager.getLayoutDefinition(context, key);
        } catch (LayoutDefinitionException ex) {
            throw new IllegalArgumentException("A LayoutDefinition was not provided for '" + key + "'!  This is required.", ex);
        }

        // Return the LayoutDefinition (if found)
        return _layoutDefinition;
    }

    /**
     * <p>
     * This method saves the state for the <code>UIComponent</code>. It relies on the <code>UIComponent</code>'s superclass
     * to save its own sate, and to pass in that <code>Object</code> to this method.
     * </p>
     *
     * @param context The <code>FacesContext</code>.
     * @param superState The <code>UIComponent</code>'s superclass state.
     *
     * @return The serialized State.
     */
    public Object saveState(FacesContext context, Object superState) {
        Object[] values = new Object[2];
        values[0] = superState;
        values[1] = _ldmKey;
        return values;
    }

    /**
     * <p>
     * This method restores the state for the <code>UIComponent</code>. It will return an <code>Object</code> that must be
     * passed to the superclass's <code>restoreState</code> method.
     * </p>
     *
     * @param context The <code>FacesContext</code>.
     * @param state The serialized state.
     *
     * @return The State for the superclass to deserialize.
     */
    public Object restoreState(FacesContext context, Object state) {
        Object[] values = (Object[]) state;
        _ldmKey = (java.lang.String) values[1];
        return values[0];
    }

    /**
     * <p>
     * This method returns the {@link LayoutDefinition} key for the <code>UIComponent</code>.
     * </p>
     *
     * @return key The key to use in the {@link LayoutDefinitionManager}.
     */
    public String getLayoutDefinitionKey() {
        return _ldmKey;
    }

    /**
     * <p>
     * This method sets the LayoutDefinition key for the <code>UIComponent</code>.
     * </p>
     *
     * @param key The key to use in the {@link LayoutDefinitionManager}.
     */
    public void setLayoutDefinitionKey(String key) {
        _ldmKey = key;
    }

    public <V> V getAttributeValue(UIComponent comp, V field, String attributeName, V defaultValue) {
        if (field != null) {
            return field;
        }
        ValueExpression ve = comp.getValueExpression(attributeName);
        return ve != null ? (V) ve.getValue(FacesContext.getCurrentInstance().getELContext()) : defaultValue;
    }

}
