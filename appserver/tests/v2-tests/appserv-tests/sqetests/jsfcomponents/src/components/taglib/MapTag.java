/*
 * Copyright (c) 2004, 2018 Oracle and/or its affiliates. All rights reserved.
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

package components.taglib;


import components.components.MapComponent;
import components.renderkit.Util;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.MethodBinding;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.webapp.UIComponentTag;


/**
 * <p>{@link UIComponentTag} for an image map.</p>
 */

public class MapTag extends UIComponentTag {


    private String current = null;


    public void setCurrent(String current) {
        this.current = current;
    }


    private String actionListener = null;


    public void setActionListener(String actionListener) {
        this.actionListener = actionListener;
    }


    private String action = null;


    public void setAction(String action) {
        this.action = action;
    }


    private String immediate = null;


    public void setImmediate(String immediate) {
        this.immediate = immediate;
    }


    private String styleClass = null;


    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }


    public String getComponentType() {
        return ("DemoMap");
    }


    public String getRendererType() {
        return ("DemoMap");
    }


    public void release() {
        super.release();
        current = null;
        styleClass = null;
        actionListener = null;
        action = null;
        immediate = null;
        styleClass = null;
    }


    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        MapComponent map = (MapComponent) component;
        //        if (current != null) {
        //            map.setCurrent(current);
        //        }
        if (styleClass != null) {
            if (isValueReference(styleClass)) {
                ValueBinding vb = FacesContext.getCurrentInstance()
                    .getApplication().
                    createValueBinding(styleClass);
                map.setValueBinding("styleClass", vb);
            } else {
                map.getAttributes().put("styleClass", styleClass);
            }
        }
        if (actionListener != null) {
            if (isValueReference(actionListener)) {
                Class args[] = {ActionEvent.class};
                MethodBinding mb = FacesContext.getCurrentInstance()
                    .getApplication()
                    .createMethodBinding(actionListener, args);
                map.setActionListener(mb);
            } else {
                Object params [] = {actionListener};
                throw new jakarta.faces.FacesException();
            }
        }

        if (action != null) {
            if (isValueReference(action)) {
                MethodBinding vb = FacesContext.getCurrentInstance()
                    .getApplication()
                    .createMethodBinding(action, null);
                map.setAction(vb);
            } else {
                map.setAction(Util.createConstantMethodBinding(action));
            }
        }
        if (immediate != null) {
            if (isValueReference(immediate)) {
                ValueBinding vb = FacesContext.getCurrentInstance()
                    .getApplication().
                    createValueBinding(immediate);
                map.setValueBinding("immediate", vb);
            } else {
                boolean _immediate = new Boolean(immediate).booleanValue();
                map.setImmediate(_immediate);
            }
        }

    }


}
