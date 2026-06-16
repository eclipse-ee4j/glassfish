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

import com.sun.jsftemplating.component.TemplateComponent;
import com.sun.jsftemplating.layout.descriptors.handler.Handler;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerDefinition;
import com.sun.jsftemplating.layout.event.DecodeEvent;
import com.sun.jsftemplating.layout.event.InitPageEvent;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This represents the top-level {@link LayoutElement}, it is the container for every other {@link LayoutElement}. By
 * itself, it has no functionality. Its purpose in life is to group all top-level child {@link LayoutElement}s.
 * LayoutDefintion objects can be registered with the {@link com.sun.jsftemplating.layout.LayoutDefinitionManager}.
 * </p>
 *
 * <p>
 * This class provide a helper method
 * {@link #getChildLayoutElementById(FacesContext, String, LayoutElement, UIComponent)} which will search recursively
 * for the given child LayoutElement by id.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutDefinition extends LayoutElementBase {
    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * Constructor.
     * </p>
     */
    public LayoutDefinition(String id) {
        // LayoutDefinition objects do not have a parent
        super(null, id);

        // Set the default StaticText ComponentType
        addComponentType(new ComponentType(STATIC_TEXT_TYPE, STATIC_TEXT_FACTORY_CLASS_NAME));
    }

    /**
     * <p>
     * This method returns the <code>Map</code> containing the {@link ComponentType}s. It ensures that the <code>Map</code>
     * is not <code>null</code>.
     * </p>
     */
    protected Map<String, ComponentType> getComponentTypes() {
        if (_types == null) {
            _types = new HashMap<>();
        }
        return _types;
    }

    /**
     * <p>
     * Retrieve a {@link ComponentType} by typeID.
     * </p>
     *
     * @param typeID The key used to retrieve the ComponentType
     *
     * @return The requested ComponentType or null
     *
     * @deprecated Reader's should use global {@link ComponentType}s (see
     * {@link com.sun.jsftemplating.layout.LayoutDefinitionManager#getGlobalComponentType(FacesContext, String)}) or should
     * cache {@link ComponentType}s locally, this information is not needed in the <code>LayoutDefinition</code>.
     */
    @Deprecated
    public ComponentType getComponentType(String typeID) {
        return getComponentTypes().get(typeID);
    }

    /**
     * <p>
     * This will add the given <code>ComponentType</code> to the map of registered <code>ComponentType</code>'s. It will use
     * the <code>ComponentType</code> ID as the key to the <code>Map</code>. This means that if a <code>ComponentType</code>
     * with the same ID had previously been registered, it will be replaced with the <code>ComponentType</code> passed in.
     * </p>
     *
     * @param type The <code>ComponentType</code>.
     *
     * @deprecated Reader's should use global {@link ComponentType}s (see
     * {@link com.sun.jsftemplating.layout.LayoutDefinitionManager#addGlobalComponentType(FacesContext, ComponentType)}) or
     * should cache {@link ComponentType}s locally, this information is not needed in the <code>LayoutDefinition</code>.
     */
    @Deprecated
    public void addComponentType(ComponentType type) {
        getComponentTypes().put(type.getId(), type);
    }

    /**
     * <p>
     * This method adds a {@link Resource}. These resources should be added to the request scope when this component is
     * used. This is mainly used for <code>ResourceBundle<code>s (at this time).</p>
     *
     *    @param    res The {@link Resource} to associate with the
     *            <code>LayoutDefinition</code>.
     */
    public void addResource(Resource res) {
        _resources.add(res);
    }

    /**
     * <p>
     * This method returns a List of {@link Resource} objects.
     * </p>
     *
     * @return This method returns a List of {@link Resource} objects.
     */
    public List<Resource> getResources() {
        return _resources;
    }

    /**
     * <p>
     * This method allows the <code>List</code> of {@link Resource}s to be set.
     * </p>
     *
     * @param resources <code>List</code> to {@link Resource}s.
     */
    public void setResources(List<Resource> resources) {
        _resources = resources;
    }

    /**
     * <p>
     * This method searches for the requested {@link LayoutComponent} by id.
     * </p>
     *
     * @param context <code>FacesContext</code>
     * @param id id to look for
     * @param parent Search starts from this {@link LayoutElement}
     * @param parentComponent Parent <code>UIComponent</code>
     *
     * @return The matching {@link LayoutElement} if found, null otherwise.
     */
    public static LayoutElement getChildLayoutElementById(FacesContext context, String id, LayoutElement parent, UIComponent parentComponent) {
        // NOTE: I may want to optimize this by putting all values in a Map so
        // NOTE: that I don't have to do this search.

        // Make sure this isn't what we're looking for
        if (parent.getId(context, parentComponent).equals(id)) {
            return parent;
        }

        // Not 'this' so lets check the children
        Iterator<LayoutElement> it = parent.getChildLayoutElements().iterator();
        LayoutElement elt = null;
        while (it.hasNext()) {
            elt = getChildLayoutElementById(context, id, it.next(), parentComponent);
            if (elt != null) {
                // Found it!
                return elt;
            }
        }

        // Not found...
        return null;
    }

    /**
     * <p>
     * Retrieve an attribute by key.
     * </p>
     *
     * @param key The key used to retrieve the attribute
     *
     * @return The requested attribute or null. public Object getAttribute(String key) { return _attributes.get(key); }
     */

    /**
     * <p>
     * Associate the given key with the given Object as an attribute.
     * </p>
     *
     * @param key The key associated with the given object (if this key is already in use, it will replace the previously
     * set attribute object).
     *
     * @param value The Object to store. public void setAttribute(String key, Object value) { _attributes.put(key, value); }
     */

    /**
     * <p>
     * This method sets a locally defined {@link HandlerDefinition}.
     * </p>
     */
    public void setHandlerDefinition(String key, HandlerDefinition value) {
        _attributes.put(key, value);
    }

    /**
     * <p>
     * This method accesses a locally defined {@link HandlerDefinition}.
     * </p>
     */
    public HandlerDefinition getHandlerDefinition(String key) {
        return _attributes.get(key);
    }

    /**
     * <p>
     * This function overrides the superclass in order to call encodeBegin / encodeEnd on the UIViewRoot (and only for
     * UIViewRoot instances). This is especially important for Ajax requests.
     * </p>
     */
    @Override
    public void encode(FacesContext context, UIComponent component) throws IOException {
        if (component instanceof UIViewRoot) {
            component.encodeBegin(context);
// FIXME: For now I am treating "@all" Ajax requests as normal requests...
// FIXME: Otherwise the partialviewcontext tries to render the whole view, and
// FIXME: fails b/c JSFT may use Facets for top-level components.  Need to find
// FIXME: a better way to handle this.
            if (context.getPartialViewContext().isPartialRequest() && !context.getPartialViewContext().isRenderAll()) {
                // JSF is now overriding this, so this is required...
                component.encodeChildren(context);
            } else {
                // This is not an ajax request... behave normal
                super.encode(context, component);
            }
            component.encodeEnd(context);
        } else {
            super.encode(context, component);
        }
    }

    /**
     * <p>
     * The <code>LayoutDefinition</code> does not encode anything for itself, this method simply returns true.
     * </p>
     *
     * @param context The <code>FacesContext</code>.
     * @param component The <code>UIComponent</code>.
     *
     * @return true.
     */
    @Override
    protected boolean encodeThis(FacesContext context, UIComponent component) throws IOException {
        return true;
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

        // Now check to see if there are any on the UIComponent (NOTE: We do
        // not pull off handlers if the parent is a TemplateComponent b/c it
        // is the responsibility of the parent class to invoke handlers via
        // its LayoutComponent. If we do it here, it will happen 2x.)
        if (comp != null && !(comp.getParent() instanceof TemplateComponent)) {
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
     * This decode method invokes any registered {@link #DECODE} handlers.
     * </p>
     *
     * @param context The FacesContext.
     * @param component The <code>UIComponent</code>.
     */
    public void decode(FacesContext context, UIComponent component) {
        // Invoke "decode" handlers
        dispatchHandlers(context, DECODE, new DecodeEvent(component));
    }

    /**
     * <p>
     * This method is responsible for dispatching the "initPage" handlers associated with this <code>LayoutDefinition</code>
     * (if any).
     * </p>
     *
     * <p>
     * The <code>source</code> passed in should be the <code>UIViewRoot</code>. However, it is expected that in most cases
     * this will not be available. It is reasonable for this to be <code>null</code>.
     * </p>
     *
     * <p>
     * If the <code>FacesContext</code> provided is null, this method will simply return.
     * </p>
     */
    public void dispatchInitPageHandlers(FacesContext ctx, Object source) {
        // Sanity check (this may happen if invoked outside JSF)...
        // Check to see if we've already done this...
        if ((ctx == null) || isInitPageExecuted(ctx)) {
            // We've already init'd this request, do nothing
            return;
        }

        // Dispatch Handlers
        dispatchHandlers(ctx, INIT_PAGE, new InitPageEvent(source));

        // Flag request as having processed the initPage handlers
        setInitPageExecuted(ctx, Boolean.TRUE);
    }

    /**
     * <p>
     * This method checks to see if the initPage event has fired yet for this request.
     * </p>
     */
    public boolean isInitPageExecuted(FacesContext ctx) {
        Map<String, Object> reqAtts = ctx.getExternalContext().getRequestMap();
        String key = INIT_PAGE_PREFIX + getId(ctx, (UIComponent) null);
        return Boolean.TRUE.equals(reqAtts.get(key));
    }

    /**
     * <p>
     * This method marks the initPage event as fired for this request.
     * </p>
     */
    public void setInitPageExecuted(FacesContext ctx, boolean value) {
        String key = INIT_PAGE_PREFIX + getId(ctx, (UIComponent) null);
        ctx.getExternalContext().getRequestMap().put(key, value);
    }

    /**
     *
     */
    private static final String INIT_PAGE_PREFIX = "__ip";

    /**
     * <p>
     * This is the "type" for handlers to be invoked to handle "decode" functionality for this element.
     * </p>
     */
    public static final String DECODE = "decode";

    /**
     * <p>
     * This is the "type" for handlers to be invoked to handle "initPage" functionality for this element.
     * </p>
     */
    public static final String INIT_PAGE = "initPage";

    /**
     * <p>
     * This is a hard-coded LayoutComponent type. By default it corresponds to
     * {@link com.sun.jsftemplating.component.factory.basic.StaticTextFactory}.
     * </p>
     */
    public static final String STATIC_TEXT_TYPE = "staticText";

    /**
     * <p>
     * This is the full classname of the default StaticTextFactory.
     * </p>
     */
    public static final String STATIC_TEXT_FACTORY_CLASS_NAME = "com.sun.jsftemplating.component.factory.basic.StaticTextFactory";

    /**
     * <p>
     * This is a list of Resource objects. These resources are to be added to the Request scope when this
     * <code>LayoutDefinition</code> is used.
     * </p>
     */
    private List<Resource> _resources = new ArrayList<>();

    /**
     * <p>
     * Map of types. This information is needed to instantiate UIComponents.
     * </p>
     */
    private Map<String, ComponentType> _types = null;

    /**
     * <p>
     * Map of attributes. Attributes can be used to store extra information about the <code>LayoutDefinition</code>.
     * </p>
     */
    private Map<String, HandlerDefinition> _attributes = new HashMap<>();
}
