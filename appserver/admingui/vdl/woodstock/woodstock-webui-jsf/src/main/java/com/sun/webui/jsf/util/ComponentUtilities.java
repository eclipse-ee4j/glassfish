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

package com.sun.webui.jsf.util;

import jakarta.faces.FactoryFinder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;
import jakarta.servlet.http.HttpServletRequest;

import java.beans.Beans;
import java.util.Iterator;

/**
 * Methods for general component manipulation.
 */
public final class ComponentUtilities {

    /**
     * Underscore character.
     */
    private static final String USCORE = "_";

    /**
     * Creates a new instance of ComponentUtilities.
     */
    private ComponentUtilities() {
    }

    /**
     * Store an internally created component utilizing the
     * internal facet naming convention by mapping the facet
     * to the name returned by {@code createPrivateFacetName()}.
     * Add the component to the parent's facets map.
     *
     * @param parent the component that created the facet
     * @param facetName the public facet name
     * @param facet the private facet component instance
     */
    public static void putPrivateFacet(final UIComponent parent,
            final String facetName, final UIComponent facet) {

        if (parent == null || facet == null || facetName == null) {
            return;
        }
        parent.getFacets().put(createPrivateFacetName(facetName), facet);
    }

    /**
     * Remove an internally created component utilizing the
     * internal facet naming convention by mapping the facet
     * to the name returned by {@code createPrivateFacetName()}.
     * Remove the component from the parent's facets map.
     *
     * @param parent the component that created the facet
     * @param facetName the public facet name
     */
    public static void removePrivateFacet(final UIComponent parent,
            final String facetName) {

        if (parent == null || facetName == null) {
            return;
        }
        parent.getFacets().remove(createPrivateFacetName(facetName));
    }

    /**
     * Return a private facet from the the parent component's facet map.Look for
     * a private facet name by calling {@code createPrivateFacetName()} on the
     * facetName parameter.
     * If the matchId parameter is true, verify that the facet that is found has
     * an id that matches the value of
     * {@code getPrivateFacetId(parent.getId(), facetName)}. If the id's do not
     * match return null and remove the existing facet.<br>
     * If matchId is false, return the facet if found or null.
     *
     * @param parent the component that contains the facet
     * @param facetName the public facet name
     * @param matchId verify a the id of the facet
     * @return a UIComponent if the facet is found else null.
     */
    public static UIComponent getPrivateFacet(final UIComponent parent,
            final String facetName, final boolean matchId) {

        if (parent == null || facetName == null) {
            return null;
        }

        String pfacetName = createPrivateFacetName(facetName);
        UIComponent facet = (UIComponent) parent.getFacets().get(pfacetName);
        if (facet == null || !matchId) {
            return facet;
        }

        // Will never be null as long as facetName is not null.
        String id = createPrivateFacetId(parent, facetName);
        if (!id.equals(facet.getId())) {
            parent.getFacets().remove(pfacetName);
            return null;
        }
        return facet;
    }

    /**
     * Prefix the facetName parameter with an "_".
     *
     * @param facetName the public facet name
     * @return a private facet name
     */
    public static String createPrivateFacetName(final String facetName) {
        return USCORE.concat(facetName);
    }

    /**
     * Return an id using the convention:
     * {@code parent.getId() + "_" + facetName}. If {@code parent.getId()} is
     * {@code null}, {@code "_" + facetName }
     * is returned.
     *
     * @param parent the component that contains the facet
     * @param facetName the public facet name
     * @return an id for a private facet.
     */
    public static String createPrivateFacetId(final UIComponent parent,
            final String facetName) {

        String pfacetName = createPrivateFacetName(facetName);
        String id = parent.getId();
        if (id != null) {
            pfacetName = id.concat(pfacetName);
        }
        return pfacetName;
    }

    /**
     * Return a child with the specified component id from the specified
     * component. If not found, return {@code null}.
     *
     * This method will NOT create a new {@code UIComponent}.
     *
     * @param parent {@code UIComponent} to be searched
     * @param id Component id (or facet name) to search for
     *
     * @return The child {@code UIComponent} if it exists, null otherwise.
     */
    public static UIComponent getChild(final UIComponent parent,
            final String id) {

        return findChild(parent, id, id);
    }

    /**
     * Return a child with the specified component id (or facetName) from the
     * specified component. If not found, return {@code null}.
     * {@code facetName} or {@code id} may be null to avoid searching
     * the facet Map or the {@code parent}'s children.
     *
     * This method will NOT create a new {@code UIComponent}.
     *
     * @param parent {@code UIComponent} to be searched
     * @param id id to search for
     * @param facetName Facet name to search for
     *
     * @return The child {@code UIComponent} if it exists, null otherwise.
     */
    public static UIComponent findChild(final UIComponent parent,
            final String id, final String facetName) {

        // Sanity Check
        if (parent == null) {
            return null;
        }

        // First search for facet
        UIComponent child;
        if (facetName != null) {
            child = (UIComponent) parent.getFacets().get(facetName);
            if (child != null) {
                return child;
            }
        }

        // Search for component by id
        if (id != null) {
            Iterator it = parent.getChildren().iterator();
            while (it.hasNext()) {
                child = (UIComponent) it.next();
                if (id.equals(child.getId())) {
                    return (child);
                }
            }
        }

        // Not found, return null
        return null;
    }

    /**
     * Helper method to obtain containing {@code UIForm}.
     *
     * @param context {@code FacesContext} for the request we are
     * processing.
     * @param component {@code UIComponent} to find form from.
     *
     * @return Returns the {@code UIForm} component that contains this
     * element
     */
    public static UIComponent getForm(final FacesContext context,
            final UIComponent component) {

        //make sure component is not null
        if (component != null) {
            //make sure we don't already have a form
            if (component instanceof UIForm) {
                return component;
            }

            UIComponent form = component;
            do {
                form = form.getParent();
                if (form != null && form instanceof UIForm) {
                    return form;
                }
            } while (form != null);
        }
        return null;
    }

    /**
     * Gets the form id from the containing {@code UIForm}.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to find form from.
     *
     * @return Returns the id of the {@code UIForm} that contains this
     * element.
     */
    public static String getFormName(final FacesContext context,
            final UIComponent component) {

        UIComponent form = getForm(context, component);
        if (form != null) {
            return form.getClientId(context);
        }
        return null;
    }

    /**
     * Return the base URI for the view identifier of the current view.
     *
     * @param context {@code FacesContext} for the current request
     * @return String
     */
    public static String getBase(final FacesContext context) {
        return getContext(context) + context.getViewRoot().getViewId();
    }

    /**
     * Return an absolute URL to our server and context path.
     *
     * @param context {@code FacesContext} for the current request
     * @return String
     */
    @SuppressWarnings({
        "checkstyle:emptystatement",
        "checkstyle:magicnumber",
        "checkstyle:emptyblock"
    })
    public static String getContext(final FacesContext context) {

        // FIXME - design time FacesContext needs better instrumentaiton?
        if (Beans.isDesignTime()) {
            return "http://localhost:18080/myapp";
        }

        //FIXME - portlet environment variation?
        HttpServletRequest request = (HttpServletRequest) context
                .getExternalContext().getRequest();
        StringBuilder sb = new StringBuilder(request.getScheme());
        sb.append("://");
        sb.append(request.getServerName());
        if ("http".equals(request.getScheme())
                && (80 == request.getServerPort())) {
            ;
        } else if ("https".equals(request.getScheme())
                && (443 == request.getServerPort())) {
        } else {
            sb.append(":").append(request.getServerPort());
        }
        sb.append(request.getContextPath());
        return sb.toString();
    }

    /**
     * Get the action URL.
     * @param context faces context
     * @param url input URL
     * @return String
     */
    public static String getActionURL(final FacesContext context,
            final String url) {

        return context.getApplication().getViewHandler()
                .getActionURL(context, url);
    }

    /**
     * Add a PhaseListener.
     *
     * @param phaseListener PhaseListener instance.
     */
    public static void addPhaseListener(final PhaseListener phaseListener) {
        LifecycleFactory factory = (LifecycleFactory) FactoryFinder
                .getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle = factory
                .getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
        lifecycle.addPhaseListener(phaseListener);
    }

    /**
     * Remove a PhaseListener.
     *
     * @param phaseListener PhaseListener instance.
     */
    public static void removePhaseListener(final PhaseListener phaseListener) {
        LifecycleFactory factory = (LifecycleFactory) FactoryFinder
                .getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle = factory
                .getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
        lifecycle.removePhaseListener(phaseListener);
    }
}
