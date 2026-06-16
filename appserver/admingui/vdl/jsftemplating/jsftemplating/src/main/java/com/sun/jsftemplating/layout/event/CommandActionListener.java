/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2006, 2022 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * The purpose of this class is to provide an <code>ActionListener</code> that can delegate to handlers (that are likely
 * defined via XML). It is safe to register this class as a managed bean at the Application scope. Or to use it directly
 * as an <code>ActionListener</code>.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class CommandActionListener implements ActionListener, Serializable {

    /**
     * <p>
     * Constructor. It is not recommended this constructor be used, however, it is available so that it may be used as a
     * managed bean. Instead call {@link #getInstance()}.
     * </p>
     */
    public CommandActionListener() {
        super();
    }

    /**
     * <p>
     * This delegates to {@link #getInstance(FacesContext)}.
     * </p>
     */
    @Produces
    @ApplicationScoped
    @Named("lfCommand")
    public static CommandActionListener getInstance() {
        return getInstance(FacesContext.getCurrentInstance());
    }

    /**
     * <p>
     * This is the preferred way to obtain an instance of this object.
     * </p>
     */
    public static CommandActionListener getInstance(FacesContext ctx) {
        if (ctx == null) {
            ctx = FacesContext.getCurrentInstance();
        }
        CommandActionListener instance = null;
        if (ctx != null) {
            instance = (CommandActionListener) ctx.getExternalContext().getApplicationMap().get(CAL_INSTANCE);
        }
        if (instance == null) {
            instance = new CommandActionListener();
            if (ctx != null) {
                ctx.getExternalContext().getApplicationMap().put(CAL_INSTANCE, instance);
            }
        }
        return instance;
    }

    /**
     * <p>
     * This method is invoked, when used directly as an <code>ActionListener</code>.
     */
    @Override
    public void processAction(ActionEvent event) {
        invokeCommandHandlers(event);
    }

    /**
     * <p>
     * This is an ActionListener that delegates to handlers to process the action.
     * </p>
     */
    public void invokeCommandHandlers(ActionEvent event) {
        // Get the UIComponent source associated w/ this command
        UIComponent command = (UIComponent) event.getSource();
        if (command == null) {
            throw new IllegalArgumentException("Action invoked, however, no source was given!");
        }

        // Get the FacesContext
        FacesContext context = FacesContext.getCurrentInstance();

        // Look on the UIComponent for the CommandHandlers
        LayoutElement desc = null;
        List<Handler> handlers = (List<Handler>) command.getAttributes().get(LayoutComponent.COMMAND);
        if (handlers != null && handlers.size() > 0) {
            // This is needed for components that don't have corresponding
            // LayoutElements, it is also useful for dynamically defining
            // Handlers (advanced and not recommended unless you have a good
            // reason). May also happen if "id" for any component in
            // hierarchy is not a simple String.

            // No parent (null) or ComponentType, just pass (null)
            desc = new LayoutComponent((LayoutElement) null, command.getId(), (ComponentType) null);
        } else {
            // Attempt to find LayoutElement based on command's client id
            // "desc" may be null
            String viewId = getViewId(command);
            desc = findLayoutElementByClientId(context, viewId, command.getClientId(context));
            if (desc == null) {
                // Do a brute force search for the LE
                desc = findLayoutElementById(context, viewId, command.getId());
            }
        }

        // If We still don't have a desc, we're stuck
        if (desc == null) {
            throw new IllegalArgumentException("Unable to locate handlers for '" + command.getClientId(context) + "'.");
        }

        // Dispatch the Handlers from the LayoutElement
        desc.dispatchHandlers(context, COMMAND_EVENT_TYPE, event);
    }

    /**
     * <p>
     * This method returns the "viewId" of the <code>ViewRoot</code> given a <code>UIComponent</code> that is part of that
     * <code>ViewRoot</code>.
     * </p>
     */
    public static String getViewId(UIComponent comp) {
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
    private static final String CAL_INSTANCE = "__jsft_CommandActionListener";

    private static final long serialVersionUID = 2L;

    /**
     * <p>
     * The "command" event type. ("command")
     * </p>
     */
    public static final String COMMAND_EVENT_TYPE = "command";
}
