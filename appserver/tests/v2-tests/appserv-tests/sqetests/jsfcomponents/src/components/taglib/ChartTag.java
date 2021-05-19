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

import components.components.ChartComponent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import jakarta.faces.el.ValueBinding;
import jakarta.faces.webapp.UIComponentTag;


/**
 * <p><strong>ChartTag</strong> is the tag handler that processes the
 * <code>chart</code> custom tag.</p>
 */

public class ChartTag extends UIComponentTag {


    /**
     * <p>The width of the chart
     */
    private String width = null;
    /**
     * <p>Set the width of the chart
     */
    public void setWidth(String width) {
        this.width = width;
    }

    /**
     * <p>The height of the chart
     */
    private String height = null;
    /**
     * <p>Set the height of the chart
     */
    public void setHeight(String height) {
        this.height = height;
    }

    /**
     * <p>The layout of the chart.  This attribute is applicable to bar
     * charts, and the value can be "horizontal" or "vertical".</p>
     */
    private String orientation = null;
    /**
     * <p>Set the orientation of the chart
     */
    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    private String value = null;
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * <p>The type of chart.  Values can be "bar" or "pie".
     */
    private String type = null;
    /**
     * <p>Set the type of the chart
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * <p>The title of the chart
     */
    private String title = null;
    /**
     * <p>Set the title of the chart
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * <p>The label for the x-axis of the bar chart
     */
    private String xlabel = null;
    /**
     * <p>Set the x-axis label for the bar chart
     */
    public void setXlabel(String xlabel) {
        this.xlabel = xlabel;
    }

    /**
     * <p>The label for the y-axis of the bar chart
     */
    private String ylabel = null;
    /**
     * <p>Set the y-axis label for the bar chart
     */
    public void setYlabel(String ylabel) {
        this.ylabel = ylabel;
    }

    /**
     * <p>Return the type of the component.
     */
    public String getComponentType() {
        return ("Chart");
    }


    /**
     * <p>Return the renderer type (if any)
     */
    public String getRendererType() {
        return (null);
    }


    /**
     * <p>Release any resources used by this tag handler
     */
    public void release() {
        super.release();
        width = null;
        height = null;
        orientation = null;
        title = null;
        xlabel = null;
        ylabel = null;
        type = null;
    }


    /**
     * <p>Set the component properties
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        ChartComponent chart = (ChartComponent) component;

        if (width != null) {
            if (isValueReference(width)) {
                ValueBinding vb = FacesContext.getCurrentInstance()
                    .getApplication().createValueBinding(width);
                chart.setValueBinding("width", vb);
            } else {
                chart.setWidth(width);
            }
        }

        if (height != null) {
            if (isValueReference(height)) {
                ValueBinding vb = FacesContext.getCurrentInstance()
                    .getApplication().createValueBinding(height);
                chart.setValueBinding("height", vb);
            } else {
                chart.setHeight(height);
            }
        }

        if (orientation != null) {
            if (isValueReference(orientation)) {
                ValueBinding vb = FacesContext.getCurrentInstance()
                    .getApplication().createValueBinding(orientation);
                chart.setValueBinding("orientation", vb);
            } else {
                chart.setOrientation(orientation);
            }
        }

        if (type != null) {
            if (isValueReference(type)) {
                ValueBinding vb = FacesContext.getCurrentInstance()
                    .getApplication().createValueBinding(type);
                chart.setValueBinding("type", vb);
            } else {
                chart.setType(type);
            }
        }

        if (value != null) {
            if (isValueReference(value)) {
                ValueBinding vb = FacesContext.getCurrentInstance()
                    .getApplication().createValueBinding(value);
                chart.setValueBinding("value", vb);
            } else {
                chart.setValue(value);
            }
        }

        if (title != null) {
            if (isValueReference(title)) {
                ValueBinding vb = FacesContext.getCurrentInstance()
                    .getApplication().createValueBinding(title);
                chart.setValueBinding("title", vb);
            } else {
                chart.setTitle(title);
            }
        }

        if (xlabel != null) {
            if (isValueReference(xlabel)) {
                ValueBinding vb = FacesContext.getCurrentInstance()
                    .getApplication().createValueBinding(xlabel);
                chart.setValueBinding("xlabel", vb);
            } else {
                chart.setXlabel(xlabel);
            }
        }

        if (ylabel != null) {
            if (isValueReference(ylabel)) {
                ValueBinding vb = FacesContext.getCurrentInstance()
                    .getApplication().createValueBinding(ylabel);
                chart.setValueBinding("ylabel", vb);
            } else {
                chart.setYlabel(ylabel);
            }
        }
    }
}
