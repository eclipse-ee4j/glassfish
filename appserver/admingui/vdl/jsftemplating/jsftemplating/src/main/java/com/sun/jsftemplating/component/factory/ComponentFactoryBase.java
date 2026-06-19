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

package com.sun.jsftemplating.component.factory;

import com.sun.jsftemplating.component.ComponentUtil;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.layout.descriptors.handler.Handler;
import com.sun.jsftemplating.layout.event.CommandActionListener;
import com.sun.jsftemplating.layout.event.ValueChangeListener;
import com.sun.jsftemplating.util.LogUtil;

import jakarta.el.ValueExpression;
import jakarta.faces.component.ActionSource;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This abstract class provides common functionality for UIComponent factories.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public abstract class ComponentFactoryBase implements ComponentFactory {

    /**
     * <p>
     * This is the factory method responsible for creating the <code>UIComponent</code>.
     * </p>
     *
     * @param context The <code>FacesContext</code>
     * @param descriptor The {@link LayoutComponent} descriptor associated with the requested <code>UIComponent</code>.
     * @param parent The parent <code>UIComponent</code>
     *
     * @return The newly created <code>UIComponent</code>.
     */
    @Override
    public abstract UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent);

    /**
     * <p>
     * This method iterates through the Map of options. It looks at each one, if it contians an EL expression, it sets a
     * value binding. Otherwise, it calls setAttribute() on the component (which in turn will invoke the bean setter if
     * there is one).
     * </p>
     *
     * <p>
     * This method also interates through the child <code>LayoutElement</code>s of the given {@link LayoutComponent}
     * descriptor and adds Facets or children as appropriate.
     * </p>
     *
     * @param context The <code>FacesContext</code>
     * @param desc The {@link LayoutComponent} descriptor associated with the requested <code>UIComponent</code>.
     * @param comp The <code>UIComponent</code>
     */
    protected void setOptions(FacesContext context, LayoutComponent desc, UIComponent comp) {
        if (desc == null) {
            // Nothing to do
            return;
        }

        // First set the id if supplied, treated special b/c the component
        // used for ${} expressions is the parent and this must be set first
        // so other ${} expressions can use $this{id} and $this{clientId}.
        String compId = desc.getId(context, comp.getParent());
        if (compId != null && !compId.equals("")) {
            comp.setId(compId);
        }

        // Loop through all the options and set the values
// FIXME: Figure a way to skip options that should not be set on the Component
        Iterator<String> it = desc.getOptions().keySet().iterator();
        String key = null;
        while (it.hasNext()) {
            // Get next property
            key = it.next();

            setOption(context, comp, desc, key, desc.getOption(key));
        }

        // Check for "command" handlers...
        List<Handler> handlers = desc.getHandlers(LayoutComponent.COMMAND);
        if (handlers != null && comp instanceof ActionSource) {
            ((ActionSource) comp).addActionListener(CommandActionListener.getInstance());
        }

        // Check for "valueChange" handlers...
        handlers = desc.getHandlers(ValueChangeListener.VALUE_CHANGE);
        if (handlers != null && comp instanceof EditableValueHolder) {
            ((EditableValueHolder) comp).addValueChangeListener(ValueChangeListener.getInstance());
        }

        // Set the events on the new component
        storeInstanceHandlers(desc, comp);
    }

    /**
     * <p>
     * This method sets an individual option on the <code>UIComponent</code>. It will check to see if it is a
     * <code>ValueExpression</code>, if it is it will store it as such.
     * </p>
     */
    protected void setOption(FacesContext context, UIComponent comp, LayoutComponent desc, String key, Object value) {
        ComponentUtil.getInstance(context).setOption(context, key, value, desc, comp);
    }

    /**
     * <p>
     * This method is responsible for interating over the "instance" handlers and applying them to the UIComponent. An
     * "instance" handler is one that is defined <b>outside a renderer</b>, or <b>a nested component within a renderer</b>.
     * In other words, a handler that would not get fired by the TemplateRenderer. By passing this in via the UIComponent,
     * code that is aware of events (see {@link com.sun.jsftemplating.layout.descriptors.LayoutElementBase}) may find these
     * events and fire them. These may vary per "instance" of a particular component (i.e. <code>TreeNode</code>) unlike the
     * handlers defined in a TemplateRender's XML (which are shared and therefor should not change dynamically).
     * </p>
     *
     * <p>
     * This method is invoked from setOptions(), however, if setOptions is not used in by a factory, this method may be
     * invoked directly. Calling this method multiple times will not cause any harm, besides making an extra unnecessary
     * call.
     * </p>
     *
     * @param desc The descriptor potentially containing handlers to copy.
     * @param comp The UIComponent instance to store the handlers.
     */
    protected void storeInstanceHandlers(LayoutComponent desc, UIComponent comp) {
        if (!desc.isNested()) {
            UIComponent parent = comp.getParent();
            if (parent == null || parent instanceof UIViewRoot) {
                // This is not a nested LayoutComponent, it should not store
                // instance handlers
                // NOTE: could skip TemplateComponent children also. Although
                // this is harder to detect as dynamic children aren't
                // defined in the template and therefor must be stored in
                // the UIComponent tree.
                return;
            }
        }

        // Iterate over the instance handlers
        Iterator<String> it = desc.getHandlersByTypeMap().keySet().iterator();
        if (it.hasNext()) {
            String eventType = null;
            Map<String, Object> compAttrs = comp.getAttributes();
            while (it.hasNext()) {
                // Assign instance handlers to attribute for retrieval later
                // (NOTE: retrieval must be explicit, see LayoutElementBase)
                eventType = it.next();
                if (eventType.equals(LayoutComponent.BEFORE_CREATE) || eventType.equals(LayoutComponent.AFTER_CREATE)) {
                    // This is handled directly, no need for instance handler
                    continue;
                }
                compAttrs.put(eventType, desc.getHandlers(eventType));
            }
        }
    }

    /**
     * <p>
     * This method associates the given child with the given parent. By using this method we centralize the code so that if
     * we decide later to add it as a real child it can be done in one place.
     * </p>
     *
     * @param context The <code>FacesContext</code>
     * @param descriptor The {@link LayoutComponent} descriptor associated with the requested <code>UIComponent</code>.
     * @param parent The parent <code>UIComponent</code>
     * @param child The child <code>UIComponent</code>
     */
    protected void addChild(FacesContext context, LayoutComponent descriptor, UIComponent parent, UIComponent child) {
        // Check to see if we should add this as a facet. NOTE: We add
        // UIViewRoot children as facets b/c we render them via the
        // LayoutElement tree.
        String facetName = descriptor.getFacetName(parent);
        if (facetName != null) {
            // Add child as a facet...
            if (LogUtil.configEnabled() && facetName.equals("_noname")) {
                // Warn the developer that they may have a problem
                LogUtil.config("Warning: no id was supplied for " + "component '" + child + "'!");
            }
            // Resolve the id if its dynamic
            facetName = (String) ComponentUtil.getInstance(context).resolveValue(context, descriptor, child, facetName);
            parent.getFacets().put(facetName, child);
        } else {
            // Add this as an actual child
            parent.getChildren().add(child);
        }
    }

    /**
     * <p>
     * This method instantiates the <code>UIComponent</code> given its <code>ComponentType</code>. It will respect the
     * <code>binding</code> property so that a <code>UIComponent</code> can be created via the <code>binding</code>
     * property. While a custom {@link ComponentFactory} can do a better job, at times it may be desirable to use
     * <code>binding</code> instead.
     * </p>
     */
    protected UIComponent createComponent(FacesContext ctx, String componentType, LayoutComponent desc, UIComponent parent) {
        UIComponent comp = null;

        // Check for the "binding" property
        String binding = null;
        if (desc != null) {
            binding = (String) desc.getEvaluatedOption(ctx, "binding", parent);
        }
        if (binding != null && ComponentUtil.getInstance(ctx).isValueReference(binding)) {
            // Create a ValueExpression
            ValueExpression ve = ctx.getApplication().getExpressionFactory().createValueExpression(ctx.getELContext(), binding,
                    UIComponent.class);
            // Create / get the UIComponent
            comp = ctx.getApplication().createComponent(ve, ctx, componentType);
        } else {
            // No binding, do the normal way...
            comp = ctx.getApplication().createComponent(componentType);
        }

        // Parent the new component
        if (parent != null) {
            addChild(ctx, desc, parent, comp);
        }

        // Return it...
        return comp;
    }

    /**
     * <p>
     * This method returns the extraInfo that was set for this <code>ComponentFactory</code> from the
     * {@link com.sun.jsftemplating.layout.descriptors.ComponentType}.
     * </p>
     */
    @Override
    public Serializable getExtraInfo() {
        return _extraInfo;
    }

    /**
     * <p>
     * This method is invoked from the {@link com.sun.jsftemplating.layout.descriptors.ComponentType} to provide more
     * information to the factory. For example, if the JSF component type was passed in, a single factory class could
     * instatiate multiple components the extra info that is passed in.
     * </p>
     *
     * <p>
     * Some factory implementations may want to override this method to execute intialization code for the factory based in
     * the value passed in.
     * </p>
     */
    @Override
    public void setExtraInfo(Serializable extraInfo) {
        _extraInfo = extraInfo;
    }

    /**
     * <p>
     * Extra information associated with this ComponentFactory.
     * </p>
     */
    private Serializable _extraInfo = null;

}
