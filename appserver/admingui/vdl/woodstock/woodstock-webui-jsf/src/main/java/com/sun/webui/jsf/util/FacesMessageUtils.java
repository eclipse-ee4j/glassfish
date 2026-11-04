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

import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Utilities for retrieving messages from FacesMessages.
 * FIXME: Move to a superclass for Message and MessageGroup only
 */
public final class FacesMessageUtils {

    /**
     * Cannot be instanciated.
     */
    private FacesMessageUtils() {
    }

    /**
     * Return a iterator that can be used to retrieve messages from
     * FacesContext.
     *
     * @param context The FacesContext of the request
     * @param forComponentId The component associated with the message(s)
     * @param msgComponent The Message, MessageGroup component
     *
     * @return an Iterator over FacesMessages that are queued.
     */
    public static Iterator getMessageIterator(final FacesContext context,
            final String forComponentId, final UIComponent msgComponent) {

        Iterator messageIterator;

        // Return messages for the specified component
        if (forComponentId != null) {
            if (forComponentId.length() == 0) {
                // Return global messages - not associated with any component.
                messageIterator = context.getMessages(null);
            } else {
                // Get messages for the specified component only.
                UIComponent forComponent = getForComponent(context,
                        forComponentId,
                        msgComponent);
                if (forComponent != null) {
                    String clientId = forComponent.getClientId(context);
                    messageIterator = context.getMessages(clientId);
                } else {
                    messageIterator = Collections.EMPTY_LIST.iterator();
                }
            }
        } else {
            // No component specified return all messages.
            messageIterator = context.getMessages();
        }

        return messageIterator;
    }

    /**
     * Walk the component tree looking for the specified component.
     *
     * @param context The FacesContext of the request
     * @param forComponentId The component to look for
     * @param msgComponent The Message, MessageGroup component to start the
     * search.
     *
     * @return the matching component, or null if no match is found.
     */
    private static UIComponent getForComponent(final FacesContext context,
            final String forComponentId, final UIComponent msgComponent) {

        if (forComponentId == null || forComponentId.length() == 0) {
            return null;
        }

        UIComponent forComponent = null;
        UIComponent currentParent = msgComponent;
        try {
            // Check the naming container of the current
            // component for the forComponent
            while (currentParent != null) {
                // If the current component is a NamingContainer,
                // see if it contains what we're looking for.
                forComponent = currentParent.findComponent(forComponentId);
                if (forComponent != null) {
                    break;
                }
                // if not, start checking further up in the view
                currentParent = currentParent.getParent();
            }
            // Note that this following clause will never happen
            // because JSF throws IllegalArgumentException
            // and therefore if the component isn't found goes
            // right to the "catch". So what was the point ?

            // no hit from above, scan for a NamingContainer
            // that contains the component we're looking for from the root.
            if (forComponent == null) {
                forComponent = findUIComponentBelow(context.getViewRoot(),
                                forComponentId);
            }
        } catch (Throwable t) {
            // Keep this looking like jsf
            // ignore and log message below
        }

        if (forComponent == null) {
            // Log a message.
            if (LogUtil.warningEnabled(FacesMessageUtils.class)) {
                LogUtil.warning(FacesMessageUtils.class,
                        "FacesMesageUtils.componentNotFound",
                        forComponentId);
            }
        }
        return forComponent;
    }

    /**
     * Recursively searches for NamingContainers from the top of the tree
     * looking for the specified component.
     *
     * @param startComponent UI component
     * @param forComponentId the component to search for
     *
     * @return the matching component, or null if no match is found.
     */
    private static UIComponent findUIComponentBelow(
            final UIComponent startComponent, final String forComponentId) {

        UIComponent forComponent = null;
        List children = startComponent.getChildren();

        for (int i = 0, size = children.size(); i < size; i++) {
            UIComponent comp = (UIComponent) children.get(i);

            if (comp == null) {
                continue;
            }

            if (comp instanceof NamingContainer) {
                forComponent = comp.findComponent(forComponentId);
            }

            if (forComponent == null) {
                if (comp.getChildCount() > 0) {
                    forComponent = findUIComponentBelow(comp, forComponentId);
                }
            }

            if (forComponent != null) {
                break;
            }
        }
        return forComponent;
    }
}
