/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: ScrollerTag.java,v 1.4 2004/11/14 07:33:16 tcfujii Exp $
 */

package components.taglib;

import components.components.ScrollerComponent;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.MethodBinding;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.webapp.UIComponentTag;

/**
 * <p> ScrollerTag is the tag handler class for <code>ScrollerComponent.
 */
public class ScrollerTag extends UIComponentTag {

    protected String actionListener = null;
    protected String navFacetOrientation = null;
    protected String forValue = null;


    /**
     * method reference to handle an action event generated as a result of
     * clicking on a link that points a particular page in the result-set.
     */
    public void setActionListener(String actionListener) {
        this.actionListener = actionListener;
    }


    /*
     * When rendering a widget representing "page navigation" where
     * should the facet markup be rendered in relation to the page
     * navigation widget?  Values are "NORTH", "SOUTH", "EAST", "WEST".
     * Case insensitive. This can be value or a value binding
     * reference expression.
     */
    public void setNavFacetOrientation(String navFacetOrientation) {
        this.navFacetOrientation = navFacetOrientation;
    }


    /*
     * The data grid component for which this acts as a scroller.
     * This can be value or a value binding reference expression.
     */
    public void setFor(String newForValue) {
        forValue = newForValue;
    }


    public String getComponentType() {
        return ("Scroller");
    }


    public String getRendererType() {
        return (null);
    }


    public void release() {
        super.release();
        this.navFacetOrientation = null;
    }


    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        FacesContext context = FacesContext.getCurrentInstance();
        ValueBinding vb = null;

        if (actionListener != null) {
            if (isValueReference(actionListener)) {
                Class args[] = {ActionEvent.class};
                MethodBinding mb = FacesContext.getCurrentInstance()
                    .getApplication()
                    .createMethodBinding(actionListener, args);
                ((ScrollerComponent) component).setActionListener(mb);
            } else {
                Object params [] = {actionListener};
                throw new jakarta.faces.FacesException();
            }
        }

        // if the attributes are values set them directly on the component, if
        // not set the ValueBinding reference so that the expressions can be
        // evaluated lazily.
        if (navFacetOrientation != null) {
            if (isValueReference(navFacetOrientation)) {
                vb =
                    context.getApplication().createValueBinding(
                        navFacetOrientation);
                component.setValueBinding("navFacetOrientation", vb);
            } else {
                component.getAttributes().put("navFacetOrientation",
                                              navFacetOrientation);
            }
        }

        if (forValue != null) {
            if (isValueReference(forValue)) {
                vb = context.getApplication().createValueBinding(forValue);
                component.setValueBinding("for", vb);
            } else {
                component.getAttributes().put("for", forValue);
            }
        }
    }
}
