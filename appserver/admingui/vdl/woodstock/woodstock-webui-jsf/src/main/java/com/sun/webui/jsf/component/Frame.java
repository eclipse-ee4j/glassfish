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
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;

/**
 * The Frame component is used inside a FrameSet component to denote a frame.
 */
@Component(type = "com.sun.webui.jsf.Frame",
        family = "com.sun.webui.jsf.Frame",
        displayName = "Frame", tagName = "frame",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_frame",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_frame_props")
        //CHECKSTYLE:ON
public class Frame extends UIComponentBase {

    /**
     * Set the value of the frameBorder attribute to "true" when a border is
     * needed around the frame.
     */
    @Property(name = "frameBorder",
            displayName = "Frame Border",
            category = "Appearance")
    private boolean frameBorder = false;

    /**
     * frameBorder set flag.
     */
    private boolean frameBorderSet = false;

    /**
     * A URL to a long description of the frame contents. Use it for browsers
     * that do not support frames
     */
    @Property(name = "longDesc",
            displayName = "Long Description",
            category = "Appearance")
    private String longDesc = null;

    /**
     * Defines the top and bottom margins in the frame.
     */
    @Property(name = "marginHeight",
            displayName = "Margin Height",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int marginHeight = Integer.MIN_VALUE;

    /**
     * maginHeight set flag.
     */
    private boolean marginHeightSet = false;

    /**
     * Defines the left and right margins in the frame.
     */
    @Property(name = "marginWidth",
            displayName = "Margin Width",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int marginWidth = Integer.MIN_VALUE;

    /**
     * maginWidth set flag.
     */
    private boolean marginWidthSet = false;

    /**
     * Defines a unique name for the frame (to use in scripts).
     */
    @Property(name = "name",
            displayName = "Name",
            category = "Appearance",
            isDefault = true)
    private String name = null;

    /**
     * Set the value of the noResize attribute to "true" when user is not
     * allowed to resize the frame.
     */
    @Property(name = "noResize",
            displayName = "No Resize",
            category = "Appearance",
            isHidden = true,
            isAttribute = true)
    private boolean noResize = false;

    /**
     * noResize set flag.
     */
    private boolean noResizeSet = false;

    /**
     * Determines scroll bar action (valid values are: yes, no, auto).
     */
    @Property(name = "scrolling",
            displayName = "Scrolling",
            category = "Appearance")
    private String scrolling = null;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)",
            category = "Appearance",
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
    private String style = null;

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "styleClass",
            displayName = "CSS Style Class(es)",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StyleClassPropertyEditor")
            //CHECKSTYLE:ON
    private String styleClass = null;

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     */
    @Property(name = "toolTip",
            displayName = "Tool Tip",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String toolTip = null;

    /**
     * Defines the URL of the file to show in the frame.
     */
    @Property(name = "url",
            displayName = "URL",
            category = "Appearance")
    private String url = null;

    /**
     * Construct a new {@code Frame}.
     */
    public Frame() {
        super();
        setRendererType("com.sun.webui.jsf.Frame");
    }

    /**
     * This implementation returns {@code "com.sun.webui.jsf.Frame"}.
     * @return String
     */
    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Frame";
    }

    /**
     * The component identifier for this component. This value must be unique
     * within the closest parent component that is a naming container.
     * This implementation invokes {@code super.setId}.
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
     * This implementation invokes {@code super.setRendered}.
     */
    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    /**
     * Set the value of the frameBorder attribute to "true" when a border is
     * needed around the frame.
     * @return {@code boolean}
     */
    public boolean isFrameBorder() {
        if (this.frameBorderSet) {
            return this.frameBorder;
        }
        ValueExpression vb = getValueExpression("frameBorder");
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
     * Set the value of the frameBorder attribute to "true" when a border is
     * needed around the frame.
     *
     * @see #isFrameBorder()
     * @param newFrameBorder frameBorder
     */
    public void setFrameBorder(final boolean newFrameBorder) {
        this.frameBorder = newFrameBorder;
        this.frameBorderSet = true;
    }

    /**
     * A URL to a long description of the frame contents. Use it for browsers
     * that do not support frames
     * @return String
     */
    public String getLongDesc() {
        if (this.longDesc != null) {
            return this.longDesc;
        }
        ValueExpression vb = getValueExpression("longDesc");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * A URL to a long description of the frame contents. Use it for browsers
     * that do not support frames
     *
     * @see #getLongDesc()
     * @param newLongDesc longDesc
     */
    public void setLongDesc(final String newLongDesc) {
        this.longDesc = newLongDesc;
    }

    /**
     * Defines the top and bottom margins in the frame.
     * @return int
     */
    public int getMarginHeight() {
        if (this.marginHeightSet) {
            return this.marginHeight;
        }
        ValueExpression vb = getValueExpression("marginHeight");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Defines the top and bottom margins in the frame.
     *
     * @see #getMarginHeight()
     * @param newMarginHeight marginHeight
     */
    public void setMarginHeight(final int newMarginHeight) {
        this.marginHeight = newMarginHeight;
        this.marginHeightSet = true;
    }

    /**
     * Defines the left and right margins in the frame.
     * @return int
     */
    public int getMarginWidth() {
        if (this.marginWidthSet) {
            return this.marginWidth;
        }
        ValueExpression vb = getValueExpression("marginWidth");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Defines the left and right margins in the frame.
     *
     * @see #getMarginWidth()
     * @param newMarginWidth marginWidth
     */
    public void setMarginWidth(final int newMarginWidth) {
        this.marginWidth = newMarginWidth;
        this.marginWidthSet = true;
    }

    /**
     * Defines a unique name for the frame (to use in scripts).
     * @return String
     */
    public String getName() {
        if (this.name != null) {
            return this.name;
        }
        ValueExpression vb = getValueExpression("name");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Defines a unique name for the frame (to use in scripts).
     *
     * @see #getName()
     * @param newName name
     */
    public void setName(final String newName) {
        this.name = newName;
    }

    /**
     * Set the value of the noResize attribute to "true" when user is not
     * allowed to resize the frame.
     * @return {@code boolean}
     */
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
     * @param newNoResize noResize
     */
    public void setNoResize(final boolean newNoResize) {
        this.noResize = newNoResize;
        this.noResizeSet = true;
    }

    /**
     * Determines scroll bar action (valid values are: yes, no, auto).
     * @return String
     */
    public String getScrolling() {
        if (this.scrolling != null) {
            return this.scrolling;
        }
        ValueExpression vb = getValueExpression("scrolling");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Determines scrol lbar action (valid values are: yes, no, auto).
     *
     * @see #getScrolling()
     * @param newScrolling scrolling
     */
    public void setScrolling(final String newScrolling) {
        this.scrolling = newScrolling;
    }

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     * @return String
     */
    public String getStyle() {
        if (this.style != null) {
            return this.style;
        }
        ValueExpression vb = getValueExpression("style");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     *
     * @see #getStyle()
     * @param newStyle style
     */
    public void setStyle(final String newStyle) {
        this.style = newStyle;
    }

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     * @return String
     */
    public String getStyleClass() {
        if (this.styleClass != null) {
            return this.styleClass;
        }
        ValueExpression vb = getValueExpression("styleClass");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     *
     * @see #getStyleClass()
     * @param newStyleClass styleClass
     */
    public void setStyleClass(final String newStyleClass) {
        this.styleClass = newStyleClass;
    }

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     * @return String
     */
    public String getToolTip() {
        if (this.toolTip != null) {
            return this.toolTip;
        }
        ValueExpression vb = getValueExpression("toolTip");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     *
     * @see #getToolTip()
     * @param newToolTip tool tip
     */
    public void setToolTip(final String newToolTip) {
        this.toolTip = newToolTip;
    }

    /**
     * Defines the URL of the file to show in the frame.
     * @return String
     */
    public String getUrl() {
        if (this.url != null) {
            return this.url;
        }
        ValueExpression vb = getValueExpression("url");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Defines the URL of the file to show in the frame.
     *
     * @see #getUrl()
     * @param newUrl URL
     */
    public void setUrl(final String newUrl) {
        this.url = newUrl;
    }

    /**
     * This implementation restores the state of all properties.
     * @param context faces context
     * @param state state object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.frameBorder = ((Boolean) values[1]);
        this.frameBorderSet = ((Boolean) values[2]);
        this.longDesc = (String) values[3];
        this.marginHeight = ((Integer) values[4]);
        this.marginHeightSet = ((Boolean) values[5]);
        this.marginWidth = ((Integer) values[6]);
        this.marginWidthSet = ((Boolean) values[7]);
        this.name = (String) values[8];
        this.noResize = ((Boolean) values[9]);
        this.noResizeSet = ((Boolean) values[10]);
        this.scrolling = (String) values[11];
        this.style = (String) values[12];
        this.styleClass = (String) values[13];
        this.toolTip = (String) values[14];
        this.url = (String) values[15];
    }

    /**
     * This implementation saves the state of all properties.
     * @param context faces context
     * @return Object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[16];
        values[0] = super.saveState(context);
        if (this.frameBorder) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.frameBorderSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        values[3] = this.longDesc;
        values[4] = this.marginHeight;
        if (this.marginHeightSet) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        values[6] = this.marginWidth;
        if (this.marginWidthSet) {
            values[7] = Boolean.TRUE;
        } else {
            values[7] = Boolean.FALSE;
        }
        values[8] = this.name;
        if (this.noResize) {
            values[9] = Boolean.TRUE;
        } else {
            values[9] = Boolean.FALSE;
        }
        if (this.noResizeSet) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        values[11] = this.scrolling;
        values[12] = this.style;
        values[13] = this.styleClass;
        values[14] = this.toolTip;
        values[15] = this.url;
        return values;
    }
}
