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
package com.sun.webui.jsf.component;

import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;

/**
 * The Iframe component is used to create an inline frame.
 */
@Component(type = "com.sun.webui.jsf.IFrame",
        family = "com.sun.webui.jsf.IFrame",
        displayName = "Iframe",
        instanceName = "iFrame",
        tagName = "iframe",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_i_frame",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_i_frame_props")
        //CHECKSTYLE:ON
public final class IFrame extends Frame {

    /**
     * Specifies how to align the iframe according to the surrounding text. One
     * of the following: left, right, top, middle, bottom
     */
    @Property(name = "align",
            displayName = "Align",
            category = "Appearance")
    private String align;

    /**
     * Defines the height of the iframe in pixels or as a percentage of it's
     * container.
     */
    @Property(name = "height",
            displayName = "Height",
            category = "Appearance")
    private String height;

    /**
     * Set the value of the noResize attribute to "true" when user is not
     * allowed to resize the frame.
     */
    @Property(name = "noResize",
            displayName = "No Resize",
            category = "Appearance",
            isHidden = true,
            isAttribute = false)
    private boolean noResize;

    /**
     * noResize set flag.
     */
    private boolean noResizeSet;

    /**
     * Defines the width of the iframe in pixels or as a percentage of it's
     * container.
     */
    @Property(name = "width",
            displayName = "Width",
            category = "Appearance")
    private String width;

    /**
     * Construct a new {@code IFrame}.
     */
    public IFrame() {
        super();
        setRendererType("com.sun.webui.jsf.IFrame");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.IFrame";
    }

    /**
     * The component identifier for this component. This value must be unique
     * within the closest parent component that is a naming container.
     */
    @Property(name = "id")
    @Override
    public void setId(final String id) {
        super.setId(id);
    }

    /**
     * Use the rendered attribute to indicate whether the HTML code for the
     * component should be included in the rendered HTML page. If set to false,
     * the rendered HTML page does not include the HTML for the component. If
     * the component is not rendered, it is also not processed on any subsequent
     * form submission.
     */
    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    /**
     * Specifies how to align the iframe according to the surrounding text. One
     * of the following: left, right, top, middle, bottom
     * @return String
     */
    public String getAlign() {
        if (this.align != null) {
            return this.align;
        }
        ValueExpression vb = getValueExpression("align");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Specifies how to align the iframe according to the surrounding text. One
     * of the following: left, right, top, middle, bottom
     *
     * @see #getAlign()
     * @param newAlign align
     */
    public void setAlign(final String newAlign) {
        this.align = newAlign;
    }

    /**
     * Defines the height of the iframe in pixels or as a percentage of it's
     * container.
     * @return String
     */
    public String getHeight() {
        if (this.height != null) {
            return this.height;
        }
        ValueExpression vb = getValueExpression("height");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Defines the height of the iframe in pixels or as a percentage of it's
     * container.
     *
     * @see #getHeight()
     * @param newHeight height
     */
    public void setHeight(final String newHeight) {
        this.height = newHeight;
    }

    /**
     * Set the value of the noResize attribute to "true" when user is not
     * allowed to resize the frame.
     * @return {@code boolean}
     */
    @Override
    public boolean isNoResize() {
        if (this.noResizeSet) {
            return this.noResize;
        }
        ValueExpression vb = getValueExpression("noResize");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return false;
    }

    /**
     * Set the value of the noResize attribute to "true" when user is not
     * allowed to resize the frame.
     *
     * @see #isNoResize()
     * @param newRoResize noResize
     */
    @Override
    public void setNoResize(final boolean newRoResize) {
        this.noResize = newRoResize;
        this.noResizeSet = true;
    }

    /**
     * Defines the width of the iframe in pixels or as a percentage of it's
     * container.
     * @return String
     */
    public String getWidth() {
        if (this.width != null) {
            return this.width;
        }
        ValueExpression vb = getValueExpression("width");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Defines the width of the iframe in pixels or as a percentage of it's
     * container.
     *
     * @see #getWidth()
     * @param newWidth width
     */
    public void setWidth(final String newWidth) {
        this.width = newWidth;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.align = (String) values[1];
        this.height = (String) values[2];
        this.noResize = ((Boolean) values[3]);
        this.noResizeSet = ((Boolean) values[4]);
        this.width = (String) values[5];
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[6];
        values[0] = super.saveState(context);
        values[1] = this.align;
        values[2] = this.height;
        if (this.noResize) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.noResizeSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        values[5] = this.width;
        return values;
    }
}
