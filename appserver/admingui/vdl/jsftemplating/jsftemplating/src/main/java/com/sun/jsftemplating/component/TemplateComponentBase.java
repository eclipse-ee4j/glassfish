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

import com.sun.jsftemplating.layout.LayoutDefinitionManager;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.layout.descriptors.LayoutDefinition;
import com.sun.jsftemplating.layout.descriptors.LayoutElement;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * This abstract class provides base functionality for components that work in conjunction with the
 * {@link com.sun.jsftemplating.renderer.TemplateRenderer} that do not need any "special" component features (such as
 * provided by <code>UIInput</code>, or an <code>ActionSource</code> implementation). This class is a suitable class for
 * "container" types of components, or components that simply agregate other components together. It provides a default
 * implementation of the {@link com.sun.jsftemplating.component.TemplateComponent} interface.
 * </p>
 *
 * @see com.sun.jsftemplating.renderer.TemplateRenderer
 * @see com.sun.jsftemplating.component.TemplateComponent
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public abstract class TemplateComponentBase extends UIComponentBase implements TemplateComponent {

    /**
     * <p>
     * Our <code>TemplateComponentHelper</code>. We initialize it on access b/c we want to ensure it exists, if it is
     * serialized it won't exist if we init it here or in the constructor.
     * </p>
     */
    private transient TemplateComponentHelper _helper;

    /**
     * <p>
     * This method will find the request child <code>UIComponent</code> by id. If it is not found, it will attempt to create
     * it if it can find a {@link LayoutElement} describing it.
     * </p>
     *
     * @param context The <code>FacesContext</code>.
     * @param id The <code>UIComponent</code> id to find.
     *
     * @return The requested <code>UIComponent</code>.
     */
    @Override
    public UIComponent getChild(FacesContext context, String id) {
        return getHelper().getChild(this, context, id);
    }

    /**
     * <p>
     * This method will find the request child <code>UIComponent</code> by id. If it is not found, it will use the given
     * {@link LayoutComponent} to create it.
     * </p>
     *
     * @param context The <code>FacesContext</code>.
     * @param descriptor The <code>UIComponent</code> id to find.
     *
     * @return The requested <code>UIComponent</code>.
     */
    @Override
    public UIComponent getChild(FacesContext context, LayoutComponent descriptor) {
        return getHelper().getChild(this, context, descriptor);
    }

    /**
     * <p>
     * This method returns the {@link LayoutDefinition} associated with this component.
     * </p>
     *
     * @param context The <code>FacesContext</code>.
     *
     * @return {@link LayoutDefinition} associated with this component.
     */
    @Override
    public LayoutDefinition getLayoutDefinition(FacesContext context) {
        return getHelper().getLayoutDefinition(context);
    }

    /**
     * <p>
     * This method saves the state for this component. It relies on the superclass to save its own sate, this method will
     * invoke super.saveState().
     * </p>
     *
     * @param context The <code>FacesContext</code>.
     *
     * @return The serialized state.
     */
    @Override
    public Object saveState(FacesContext context) {
        return getHelper().saveState(context, super.saveState(context));
    }

    /**
     * <p>
     * This method restores the state for this component. It will invoke the superclass to restore its state.
     * </p>
     *
     * @param context The <code>FacesContext</code>.
     * @param state The serialized state.
     */
    @Override
    public void restoreState(FacesContext context, Object state) {
        super.restoreState(context, getHelper().restoreState(context, state));
    }

    /**
     * <p>
     * This method returns the {@link LayoutDefinition} key for this component.
     * </p>
     *
     * @return The key to use in the {@link LayoutDefinitionManager}.
     */
    @Override
    public String getLayoutDefinitionKey() {
        return getHelper().getLayoutDefinitionKey();
    }

    /**
     * <p>
     * This method sets the {@link LayoutDefinition} key for this component.
     * </p>
     *
     * @param key The key to use in the {@link LayoutDefinitionManager}.
     */
    @Override
    public void setLayoutDefinitionKey(String key) {
        getHelper().setLayoutDefinitionKey(key);
    }

    @Override
    public <V> V getPropertyValue(V field, String attributeName, V defaultValue) {
        return getHelper().getAttributeValue(this, field, attributeName, defaultValue);
    }

    /**
     * <p>
     * This method retrieves the {@link TemplateComponentHelper} used by this class to help implement the
     * {@link TemplateComponent} interface.
     * </p>
     *
     * @return The {@link TemplateComponentHelper} for this component.
     */
    protected TemplateComponentHelper getHelper() {
        if (_helper == null) {
            _helper = new TemplateComponentHelper();
        }
        return _helper;
    }

}
