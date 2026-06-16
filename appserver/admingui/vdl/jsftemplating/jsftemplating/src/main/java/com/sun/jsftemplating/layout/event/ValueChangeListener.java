/*
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.layout.event;

import com.sun.jsftemplating.layout.LayoutDefinitionException;
import com.sun.jsftemplating.layout.LayoutDefinitionManager;
import com.sun.jsftemplating.layout.descriptors.ComponentType;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.layout.descriptors.LayoutDefinition;
import com.sun.jsftemplating.layout.descriptors.LayoutElement;
import com.sun.jsftemplating.layout.descriptors.handler.Handler;
import com.sun.jsftemplating.util.LogUtil;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ValueChangeEvent;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * This class provides a <code>ValueChangeListener</code> that can delegate to handlers (that are likely defined via the
 * template). It is safe to register this class as a managed bean at the Application scope. Or to use it directly as a
 * <code>ValueChangeListener</code>.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class ValueChangeListener implements jakarta.faces.event.ValueChangeListener, Serializable {

    /**
     * <p>
     * Constructor. It is not recommended this constructor be used, however, it is available so that it may be used as a
     * managed bean. Instead call {@link #getInstance()}.
     * </p>
     */
    public ValueChangeListener() {
        super();
    }

    /**
     * <p>
     * This delegates to {@link #getInstance(FacesContext)}.
     * </p>
     */
    public static ValueChangeListener getInstance() {
        return getInstance(FacesContext.getCurrentInstance());
    }

    /**
     * <p>
     * This is the preferred way to obtain an instance of this object.
     * </p>
     */
    public static ValueChangeListener getInstance(FacesContext ctx) {
        if (ctx == null) {
            ctx = FacesContext.getCurrentInstance();
        }
        ValueChangeListener instance = null;
        if (ctx != null) {
            instance = (ValueChangeListener) ctx.getExternalContext().getApplicationMap().get(VCL_INSTANCE);
        }
        if (instance == null) {
            instance = new ValueChangeListener();
            if (ctx != null) {
                ctx.getExternalContext().getApplicationMap().put(VCL_INSTANCE, instance);
            }
        }
        return instance;
    }

    /**
     * <p>
     * This method is invoked, when used directly as a <code>ValueChangeListener</code>.
     */
    @Override
    public void processValueChange(ValueChangeEvent event) {
        invokeValueChangeHandlers(event);
    }

    /**
     * <p>
     * This is an ValueChangeListener that delegates to handlers to process the action.
     * </p>
     */
    public void invokeValueChangeHandlers(ValueChangeEvent event) {
        // Get the UIComponent (evh) source associated w/ this valueChangeEvent
        UIComponent evh = (UIComponent) event.getSource();
        if (evh == null) {
            throw new IllegalArgumentException("ValueChange invoked, however, no source was given!");
        }

        // Get the FacesContext
        FacesContext context = FacesContext.getCurrentInstance();

        // Look on the UIComponent for the ValueChangeHandlers
        LayoutElement desc = null;
        List<Handler> handlers = (List<Handler>) evh.getAttributes().get(VALUE_CHANGE);
        if (handlers != null && handlers.size() > 0) {
            // This is needed for components that don't have corresponding
            // LayoutElements, it is also useful for dynamically defining
            // Handlers (advanced and not recommended unless you have a good
            // reason). May also happen if "id" for any component in
            // hierarchy is not a simple String.

            // No parent (null) or ComponentType, just pass (null)
            desc = new LayoutComponent((LayoutElement) null, evh.getId(), (ComponentType) null);
        } else {
            // Attempt to find LayoutElement based on evh's client id
            // "desc" may be null
            String viewId = getViewId(evh);
            desc = findLayoutElementByClientId(context, viewId, evh.getClientId(context));
            if (desc == null) {
                // Do a brute force search for the LE
                desc = findLayoutElementById(context, viewId, evh.getId());
            }
        }

        // If We still don't have a desc, we're stuck
        if (desc == null) {
            throw new IllegalArgumentException("Unable to locate handlers for '" + evh.getClientId(context) + "'.");
        }

        // Dispatch the Handlers from the LayoutElement
        desc.dispatchHandlers(context, VALUE_CHANGE, event);
    }

    /**
     * <p>
     * This method returns the "viewId" of the <code>ViewRoot</code> given a <code>UIComponent</code> that is part of that
     * <code>ViewRoot</code>.
     * </p>
     */
    public static String getViewId(UIComponent comp) {
// FIXME: This should be a util method. (Same as CommandActionListener)
        String result = null;
        while (comp != null && !(comp instanceof UIViewRoot)) {
            // Searching for the UIViewRoot...
            comp = comp.getParent();
        }
        if (comp != null) {
            // Found the UIViewRoot, get its "ViewId"
            result = ((UIViewRoot) comp).getViewId();
        }
        // Return the result (or null)
        return result;
    }

    /**
     * <p>
     * This method searches for the LayoutComponent that matches the given client ID. Although this is often possible, it
     * won't work all the time. This is because there is no way to ensure a 1-to-1 mapping between the UIComponent and the
     * LayoutComponent tree. A given LayoutComponent may create multiple UIComponent, the LayoutComponent tree may itself be
     * dynamic, and the UIComponent tree may change after it is initially created from the LayoutComponent tree. For these
     * reasons, this method may fail. In these circumstances, it is critical to store the necessary information with the
     * UIComponent itself.
     * </p>
     */
    public static LayoutElement findLayoutElementByClientId(FacesContext ctx, String layoutDefKey, String clientId) {
// FIXME: This should be a util method. (Same as CommandActionListener)
        LayoutElement result = null;
        try {
            result = findLayoutElementByClientId(LayoutDefinitionManager.getLayoutDefinition(ctx, layoutDefKey), clientId);
        } catch (LayoutDefinitionException ex) {
            if (LogUtil.configEnabled()) {
                LogUtil.config("Unable to resolve client id '" + clientId + "' for LayoutDefinition key: '" + layoutDefKey + "'.", ex);
            }
        }
        return result;
    }

    public static LayoutElement findLayoutElementByClientId(LayoutDefinition def, String clientId) {
// FIXME: This should be a util method. (Same as CommandActionListener)
//
// FIXME: TBD...
// FIXME: Walk LE tree, ignore non-LayoutComponent entries (this may cause a problem itself b/c of conditional statements & loops)
// FIXME: Handle LayoutCompositions / LayoutInserts
        return null;
    }

    /**
     * <p>
     * This method simply searches the LayoutElement tree using the given id. As soon as it matches any LayoutElement in the
     * tree w/ the given id, it returns it. This method does *not* respect NamingContainers as the only information given to
     * this method is a simple "id".
     * </p>
     */
    public static LayoutElement findLayoutElementById(FacesContext ctx, String layoutDefKey, String id) {
// FIXME: This should be a util method. (Same as CommandActionListener)
        // Sanity check
        if (id == null) {
            return null;
        }

        LayoutElement result = null;
        try {
            result = findLayoutElementById(LayoutDefinitionManager.getLayoutDefinition(ctx, layoutDefKey), id);
        } catch (LayoutDefinitionException ex) {
            if (LogUtil.configEnabled()) {
                LogUtil.config("Unable to resolve id '" + id + "' for LayoutDefinition key: '" + layoutDefKey + "'.", ex);
            }
        }
        return result;
    }

    /**
     * <p>
     * This method does not evaluate the id field of the LayoutElement when checking for a match, this means it will not
     * find values where the LayoutComponent's id must be evaulated. Store the handlers on the UIComponent in this case.
     * </p>
     */
    public static LayoutElement findLayoutElementById(LayoutElement elt, String id) {
// FIXME: This should be a util method. (Same as CommandActionListener)
        // First check to see if the given LayoutElement is it
        if (elt.getUnevaluatedId().equals(id)) {
            return elt;
        }

        // Iterate over children and recurse (depth first)
        LayoutElement child = null;
        Iterator<LayoutElement> it = elt.getChildLayoutElements().iterator();
        while (it.hasNext()) {
            child = it.next();
// FIXME: Handle LayoutCompositions / LayoutInserts
            if (child instanceof LayoutComponent) {
                child = findLayoutElementById(child, id);
                if (child != null) {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     * Application scope key for an instance of this class.
     */
    private static final String VCL_INSTANCE = "__jsft_ValueChangeListener";

    private static final long serialVersionUID = 2L;

    /**
     * <p>
     * This is the "event type" for handlers to be invoked to handle value change event for this component.
     * </p>
     */
    public static final String VALUE_CHANGE = "valueChange";

}
