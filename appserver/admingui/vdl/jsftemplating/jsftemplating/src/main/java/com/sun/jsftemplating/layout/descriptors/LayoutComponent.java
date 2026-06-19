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

import com.sun.jsftemplating.component.ChildManager;
import com.sun.jsftemplating.component.ComponentUtil;
import com.sun.jsftemplating.component.TemplateComponent;
import com.sun.jsftemplating.el.VariableResolver;
import com.sun.jsftemplating.layout.LayoutViewHandler;
import com.sun.jsftemplating.layout.ViewRootUtil;
import com.sun.jsftemplating.layout.descriptors.handler.Handler;
import com.sun.jsftemplating.layout.event.AfterCreateEvent;
import com.sun.jsftemplating.layout.event.AfterEncodeEvent;
import com.sun.jsftemplating.layout.event.BeforeCreateEvent;
import com.sun.jsftemplating.layout.event.BeforeEncodeEvent;
import com.sun.jsftemplating.util.LayoutElementUtil;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This class defines a <code>LayoutComponent</code>. A <code>LayoutComponent</code> describes a
 * <code>UIComponent</code> to be instantiated. The method {@link #getType()} provides a {@link ComponentType}
 * descriptor that is capable of providing a {@link com.sun.jsftemplating.component.factory.ComponentFactory} to perform
 * the actual instantiation. This class also stores properties and facets (children) to be set on a newly instantiated
 * instance.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutComponent extends LayoutElementBase implements LayoutElement {
    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * Constructor.
     * </p>
     */
    public LayoutComponent(LayoutElement parent, String id, ComponentType type) {
        super(parent, id);
        _type = type;
    }

    /**
     * <p>
     * Accessor for type.
     * </p>
     */
    public ComponentType getType() {
        return _type;
    }

    /**
     * <p>
     * Determines if this component should be created even if there is already an existing <code>UIComponent</code>. It will
     * "overwrite" the existing component if this property is true.
     * </p>
     */
    public void setOverwrite(boolean value) {
        _overwrite = value;
    }

    /**
     * <p>
     * Determines if this component should be created even if there is already an existing <code>UIComponent</code>. It will
     * "overwrite" the existing component if this property is true.
     * </p>
     */
    public boolean isOverwrite() {
        return _overwrite;
    }

    /**
     * <p>
     * This method adds an option to the LayoutComponent. Options may be useful in constructing the LayoutComponent.
     * </p>
     *
     * @param name The name of the option
     * @param value The value of the option (may be List or String)
     */
    public void addOption(String name, Object value) {
        _options.put(name, value);
    }

    /**
     * <p>
     * This method adds all the options in the given Map to the {@link LayoutComponent}. Options may be useful in
     * constructing the {@link LayoutComponent}.
     * </p>
     *
     * @param map The map of options to add.
     */
    public void addOptions(Map<String, Object> map) {
        _options.putAll(map);
    }

    /**
     * <p>
     * Accessor method for an option. This method does not evaluate expressions.
     * </p>
     *
     * @param name The option name to retrieve.
     *
     * @return The option value (List or String), or null if not found.
     *
     * @see #getEvaluatedOption(FacesContext, String, UIComponent)
     */
    public Object getOption(String name) {
        return _options.get(name);
    }

    /**
     * <p>
     * Accessor method for an option. This method evaluates our own expressions (not JSF expressions).
     * </p>
     *
     * @param ctx The <code>FacesContext</code>.
     * @param name The option name to retrieve.
     * @param component The <code>UIComponent</code> (may be null).
     *
     * @return The option value (List or String), or null if not found.
     *
     * @see #getOption(String)
     */
    public Object getEvaluatedOption(FacesContext ctx, String name, UIComponent component) {
        // Get the option value
        Object value = getOption(name);

        // Invoke our own EL. This is needed b/c JSF's EL is designed for
        // Bean getters only. It does not get CONSTANTS or pull data from
        // other sources (such as session, request attributes, etc., etc.)
        // Resolve our variables now because we cannot depend on the
        // individual components to do this. We may want to find a way to
        // make this work as a regular ValueExpression... but for
        // now, we'll just resolve it here.
        return VariableResolver.resolveVariables(ctx, this, component, value);
    }

    /**
     * <p>
     * This method returns true/false based on whether the given option name has been set.
     * </p>
     *
     * @param name The option name to look for.
     *
     * @return true/false depending on whether the options exists.
     */
    public boolean containsOption(String name) {
        return _options.containsKey(name);
    }

    /**
     * <p>
     * This method sets the Map of options.
     * </p>
     *
     * @param options <code>Map</code> of options.
     */
    public void setOptions(Map<String, Object> options) {
        _options = options;
    }

    /**
     * <p>
     * This method returns the options as a Map. This method does not evaluate expressions.
     * </p>
     *
     * @return Map of options.
     */
    public Map<String, Object> getOptions() {
        return _options;
    }

    /**
     * <p>
     * This method is overriden so that the correct UIComponent can be passed into the events. This is important so that
     * correct component is searched for "instance" handlers.
     * </p>
     *
     * @param context The <code>FacesContext</code>.
     * @param parent The <code>UIComponent</code>.
     */
    @Override
    public void encode(FacesContext context, UIComponent parent) throws IOException {
        if (!this.getClass().getName().equals(CLASS_NAME)) {
            // The sub-classes of this component shouldn't use this method,
            // this is a hack to allow them to use LayoutElementBase.encode
            super.encode(context, parent);
            return;
        }

        // If overwrite...
        if (isOverwrite()) {
// FIXME: shouldn't this do a replace, not a remove?  Otherwise the order may change
            String id = getId(context, parent);
            if (parent.getFacets().remove(id) == null) {
                UIComponent child = ComponentUtil.getInstance(context).findChild(parent, id, null);
                if (child != null) {
                    // Not a facet, try child...
                    parent.getChildren().remove(child);
                }
            }
        }

        // Display this UIComponent
        // First find the UIComponent
        UIComponent childComponent = null;
        if (parent instanceof ChildManager) {
            // If we have a ChildManager, take advantage of it...
            childComponent = ((ChildManager) parent).getChild(context, this);
        } else {
            // Use local util method for finding / creating child component...
            childComponent = getChild(context, parent);
        }

        dispatchHandlers(context, BEFORE_ENCODE, new BeforeEncodeEvent(childComponent));

        // Add child components... (needs to be done here, LE's can't do it)
        // Use check for instance of TC. If present we must instantiate its
        // children as they were skipped when the tree was initially created.
        if (parent instanceof TemplateComponent) {
            // Only do this for TemplateRenderer use-cases (LayoutViewHandler
            // does this for pages)
            LayoutViewHandler.buildUIComponentTree(context, childComponent, this);
        }

        // Render the child UIComponent
        encodeChild(context, childComponent);

        // Invoke "after" handlers
        dispatchHandlers(context, AFTER_ENCODE, new AfterEncodeEvent(childComponent));
    }

    /**
     * <p>
     * Although this method is part of the interface, it is not used b/c I overrode the encode() method which calls this
     * method. This method does nothing except satisfy the compiler.
     * </p>
     */
    @Override
    public boolean encodeThis(FacesContext context, UIComponent parent) throws IOException {
        return false;
    }

    /**
     * <p>
     * This method will find or create a <code>UIComponent</code> as described by this <code>LayoutComponent</code>
     * descriptor. If the component already exists as a child or facet, it will be returned. If it creates a new
     * <code>UIComponent</code>, it will typically be added to the given parent <code>UIComponent</code> as a facet (this
     * actually depends on the factory that instantiates the <code>UIComponent</code>).
     * </p>
     *
     * @param context The <code>FacesContext</code>
     * @param parent The <code>UIComponent</code> to serve as the parent to search and to store the new
     * <code>UIComponent</code>.
     *
     * @return The <code>UIComponent</code> requested (found or newly created)
     */
    public UIComponent getChild(FacesContext context, UIComponent parent) {
        UIComponent childComponent = null;

        // First pull off the id from the descriptor
        String id = this.getId(context, parent);

        // We have an id, use it to search for an already-created child
        ComponentUtil compUtil = ComponentUtil.getInstance(context);
        childComponent = compUtil.findChild(parent, id, id);
        if (childComponent != null) {
            return childComponent;
        }

        // Invoke "beforeCreate" handlers
        this.beforeCreate(context, parent);

        // Create UIComponent
        childComponent = compUtil.createChildComponent(context, this, parent);

        // Invoke "afterCreate" handlers
        this.afterCreate(context, childComponent);

        // Return the newly created UIComponent
        return childComponent;
    }

    /**
     * <p>
     * This method retrieves the Handlers for the requested type. But also includes any handlers that are associated with
     * the instance (i.e. the UIComponent).
     * </p>
     *
     * @param type The type of <code>Handler</code>s to retrieve.
     * @param comp The associated <code>UIComponent</code> (or null).
     *
     * @return A List of Handlers.
     */
    @Override
    public List<Handler> getHandlers(String type, UIComponent comp) {
        // 1st get list of handlers for definition of this LayoutElement
        List<Handler> handlers = null;

        // Now check to see if there are any on the UIComponent
        if (comp != null) {
            List<Handler> instHandlers = (List<Handler>) comp.getAttributes().get(type);
            if (instHandlers != null && instHandlers.size() > 0) {
                // NOTE: Copy b/c this is <i>instance</i> + static
                // Add the UIComponent instance handlers
                handlers = new ArrayList<>(instHandlers);

                List<Handler> defHandlers = getHandlers(type);
                if (defHandlers != null) {
                    // Add the LayoutElement "definition" handlers, if any
                    handlers.addAll(getHandlers(type));
                }
            }
        }
        if (handlers == null) {
            handlers = getHandlers(type);
        }

        return handlers;
    }

    /**
     * <p>
     * This method is invoked before the Component described by this LayoutComponent is created. This allows handlers
     * registered for "beforeCreate" functionality to be invoked.
     * </p>
     *
     * @param context The FacesContext
     *
     * @return The result of invoking the handlers (null by default)
     */
    public Object beforeCreate(FacesContext context, UIComponent parent) {
        // Invoke "beforeCreate" handlers
        return dispatchHandlers(context, BEFORE_CREATE, new BeforeCreateEvent(parent));
    }

    /**
     * <p>
     * This method is invoked after the Component described by this LayoutComponent is created. This allows handlers
     * registered for "afterCreate" functionality to be invoked.
     * </p>
     *
     * @param context The FacesContext
     *
     * @return The result of invoking the handlers (null by default)
     */
    public Object afterCreate(FacesContext context, UIComponent component) {
        // Invoke "afterCreate" handlers
        return dispatchHandlers(context, AFTER_CREATE, new AfterCreateEvent(component));
    }

    /**
     * <p>
     * This method returns true if the child should be added to the parent component as a facet. Otherwise, it returns false
     * indicating that it should exist as a real child.
     * </p>
     *
     * <p>
     * This value is calculated every time this call is made to allow for the context in which the LayoutComponent exists to
     * determine its value. If a {@link LayoutFacet} exists as a parent {@link LayoutElement}, or a <code>UIViewRoot</code>
     * or {@link TemplateComponent} exists as the immediate parent, it will return the facet name that should be used.
     * Otherwise, it will return <code>null</code>.
     * </p>
     *
     * @param parent This is the parent UIComponent.
     *
     * @return The facet name if the UIComponent should be added as a facet.
     */
    public String getFacetName(UIComponent parent) {
        String name = null;

        // First check to see if this LC specifies a different facet name...
        name = (String) getOption(FACET_NAME);
        if (name != null && name.equals(getUnevaluatedId())) {
            // No special facet name supplied, don't assume this is a facet yet
            name = null;
        }

        // Next check to see if we are inside a LayoutFacet
        if (name == null) {
            LayoutElement parentElt = getParent();
            while (parentElt != null) {
                if (parentElt instanceof LayoutFacet) {
                    // Inside a LayoutFacet, use its name... only if this facet
                    // is a child of a LayoutComponent (otherwise, it is a
                    // layout facet used for layout, not for defining a facet
                    // of a UIComponent)
                    if (LayoutElementUtil.isLayoutComponentChild(parentElt)) {
                        name = parentElt.getUnevaluatedId();
                    } else {
                        name = getUnevaluatedId();
                    }
                    if (name == null) {
                        name = "_noname";
                    }
                    break;
                }
                if (parentElt instanceof LayoutComponent) {
                    // No need to process further, this is not a facet child
                    return null;
                }
                parentElt = parentElt.getParent();
            }
        }

        // If not found yet, check to see if we're at the top...
        if (name == null) {
            if (parent instanceof TemplateComponent) {
                // We don't know if we are adding a child of a
                // TemplateComponent from a page, or if the TemplateComponent
                // itself has a child... if the TemplateComponent is driving
                // the rendering process, then we want this to be a facet. If
                // the page is adding a child to a TemplateComponent, we do
                // not want this to be a facet.

                // Look to see if the parent LayoutDefinition == the current
                // LayoutDefinition. If so, we're "inside" a
                // TemplateComponent, not a page.
                FacesContext ctx = FacesContext.getCurrentInstance();
                if (((TemplateComponent) parent).getLayoutDefinition(ctx) == getLayoutDefinition()) {
                    name = getUnevaluatedId();
                }
            } else if (parent instanceof UIViewRoot && ViewRootUtil.getLayoutDefinition((UIViewRoot) parent) != null) {

                // NOTE: Only set the name if its a JSFT ViewRoot
                name = getUnevaluatedId();
            }
        }

        // Return the result
        return name;
    }

    /**
     * <p>
     * This method returns a flag that indicates if this <code>LayoutComponent</code> is nested (directly or indirectly)
     * inside another <code>LayoutComponent</code>. This flag is used for such purposes as deciding if "instance" handlers
     * are appropriate.
     * </p>
     *
     * @return <code>true</code> if component is nested.
     */
    public boolean isNested() {
        return _nested;
    }

    /**
     * <p>
     * This method sets the nested flag for this <code>LayoutComponent</code>. This method is commonly only called from code
     * that constructs the tree of {@link LayoutElement} components.
     * </p>
     *
     * @param value The boolean value.
     */
    public void setNested(boolean value) {
        _nested = value;
    }

    /**
     * <p>
     * Component type
     * </p>
     */
    private ComponentType _type = null;

    /**
     * <p>
     * Determines if this component should be created even if there is already an existing <code>UIComponent</code>. It will
     * "overwrite" the existing component if this property is true. Usually only applies when this is used within the
     * context of a <code>Renderer</code>.
     * </p>
     */
    private boolean _overwrite = false;

    /**
     * <p>
     * Map of options.
     * </p>
     */
    private Map<String, Object> _options = new HashMap<>();

    /**
     * <p>
     * This is the "type" for handlers to be invoked to handle "afterCreate" functionality for this element.
     * </p>
     */
    public static final String AFTER_CREATE = "afterCreate";

    /**
     * <p>
     * This is the "type" for handlers to be invoked to handle "beforeCreate" functionality for this element.
     * </p>
     */
    public static final String BEFORE_CREATE = "beforeCreate";

    /**
     * <p>
     * This is the "type" for handlers to be invoked to handle "command" functionality for this element.
     * </p>
     */
    public static final String COMMAND = "command";

    /**
     * <p>
     * This defines the property key for specifying the facet name in which the component should be stored under in its
     * parent UIComponent.
     * </p>
     */
    public static final String FACET_NAME = "_facetName";

    /**
     * <p>
     * This defines the attribute name on the tag that flags that duplicate ID's should not be checked for this component.
     * If specified on a component, the check will be skipped, regardless of the value of the attribute. The attribute name
     * is ("skipIdCheck").
     * </p>
     */
    public static final String SKIP_ID_CHECK = "skipIdCheck";

    public static final String CLASS_NAME = LayoutComponent.class.getName();

    /**
     * <p>
     * The value of the nested property.
     * </p>
     */
    private boolean _nested = false;
}
