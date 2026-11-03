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
 * The FrameSet component defines a set of frames.
 */
@Component(type = "com.sun.webui.jsf.FrameSet",
        family = "com.sun.webui.jsf.FrameSet",
        displayName = "Frame Set", tagName = "frameSet",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_frame_set",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_frame_set_props")
        //CHECKSTYLE:ON
public final class FrameSet extends UIComponentBase {

    /**
     * The width, in pixels, of the space around frames. The frameSpacing
     * attribute and the border attribute set the same property in different
     * browsers. Set frameSpacing and border to the same value.
     */
    @Property(name = "border",
            displayName = "border",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int border = Integer.MIN_VALUE;

    /**
     * border set flag.
     */
    private boolean borderSet = false;

    /**
     * The {@code bordercolor} attribute allows you to set the color of the
     * frame borders using a hex value or a color name.
     */
    @Property(name = "borderColor",
            displayName = "Border Color",
            category = "Appearance")
    private String borderColor = null;

    /**
     * Defines the number and size of columns in a frame set. The size can be
     * specified in pixels, percentage of the page width, or with an asterisk
     * (*). Specifying * causes the columns to use available space. See the HTML
     * specification for the frame set element for more details.
     */
    @Property(name = "cols",
            displayName = "Number of Columns",
            category = "Appearance",
            isDefault = true)
    private String cols = null;

    /**
     * Flag indicating whether frames should have borders or not. If frameBorder
     * is true, decorative borders are drawn. If frameBorder is false, a space
     * between frames shows up as the background color of the page. To show no
     * border or space between frames, you should set frameBorder to false, and
     * set frameSpacing and border to 0.
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
     * The width, in pixels, of the space around frames. The frameSpacing
     * attribute and the border attribute set the same property in different
     * browsers. Set frameSpacing and border to the same value.
     */
    @Property(name = "frameSpacing",
            displayName = "Frame Spacing",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int frameSpacing = Integer.MIN_VALUE;

    /**
     * frameSpacing set flag.
     */
    private boolean frameSpacingSet = false;

    /**
     * Defines the number and size of rows in a frame set. The size can be
     * specified in pixels, percentage of the page length, or with an asterisk
     * (*). Specifying * causes the rows to use available space. See the HTML
     * specification for the frame set element for more details.
     */
    @Property(name = "rows",
            displayName = "Number of Rows",
            category = "Appearance")
    private String rows = null;

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
     * Construct a new {@code FrameSet}.
     */
    public FrameSet() {
        super();
        setRendererType("com.sun.webui.jsf.FrameSet");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.FrameSet";
    }

    @Property(name = "id")
    @Override
    public void setId(final String id) {
        super.setId(id);
    }

    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    /**
     * The width, in pixels, of the space around frames. The frameSpacing
     * attribute and the border attribute set the same property in different
     * browsers. Set frameSpacing and border to the same value.
     * @return int
     */
    public int getBorder() {
        if (this.borderSet) {
            return this.border;
        }
        ValueExpression vb = getValueExpression("border");
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
     * The width, in pixels, of the space around frames. The frameSpacing
     * attribute and the border attribute set the same property in different
     * browsers.  Set frameSpacing and border to the same value.
     * @see #getBorder()
     * @param newBorder border
     */
    public void setBorder(final int newBorder) {
        this.border = newBorder;
        this.borderSet = true;
    }

    /**
     * The {@code bordercolor} attribute allows you to set the color of the
     * frame borders using a hex value or a color name.
     *
     * @return String
     */
    public String getBorderColor() {
        if (this.borderColor != null) {
            return this.borderColor;
        }
        ValueExpression vb = getValueExpression("borderColor");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The {@code bordercolor} attribute allows you to set the color of the
     * frame borders using a hex value or a color name.
     *
     * @see #getBorderColor()
     * @param newBorderColor borderColor
     */
    public void setBorderColor(final String newBorderColor) {
        this.borderColor = newBorderColor;
    }

    /**
     * Defines the number and size of columns in a frame set. The size can be
     * specified in pixels, percentage of the page width, or with an asterisk
     * (*). Specifying * causes the columns to use available space. See the HTML
     * specification for the frame set element for more details.
     *
     * @return String
     */
    public String getCols() {
        if (this.cols != null) {
            return this.cols;
        }
        ValueExpression vb = getValueExpression("cols");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Defines the number and size of columns in a frame set. The size can be
     * specified in pixels, percentage of the page width, or with an asterisk
     * (*). Specifying * causes the columns to use available space. See the HTML
     * specification for the frame set element for more details.
     *
     * @see #getCols()
     * @param newCols cols
     */
    public void setCols(final String newCols) {
        this.cols = newCols;
    }

    /**
     * Flag indicating whether frames should have borders or not. If frameBorder
     * is true, decorative borders are drawn. If frameBorder is false, a space
     * between frames shows up as the background color of the page. To show no
     * border or space between frames, you should set frameBorder to false, and
     * set frameSpacing and border to 0.
     *
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
     * Flag indicating whether frames should have borders or not. If frameBorder
     * is true, decorative borders are drawn. If frameBorder is false, a space
     * between frames shows up as the background color of the page. To show no
     * border or space between frames, you should set frameBorder to false, and
     * set frameSpacing and border to 0.
     *
     * @see #isFrameBorder()
     * @param newFrameBorder frameBorder
     */
    public void setFrameBorder(final boolean newFrameBorder) {
        this.frameBorder = newFrameBorder;
        this.frameBorderSet = true;
    }

    /**
     * The width, in pixels, of the space around frames. The frameSpacing
     * attribute and the border attribute set the same property in different
     * browsers. Set frameSpacing and border to the same value.
     *
     * @return int
     */
    public int getFrameSpacing() {
        if (this.frameSpacingSet) {
            return this.frameSpacing;
        }
        ValueExpression vb = getValueExpression("frameSpacing");
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
     * The width, in pixels, of the space around frames. The frameSpacing
     * attribute and the border attribute set the same property in different
     * browsers. Set frameSpacing and border to the same value.
     *
     * @see #getFrameSpacing()
     * @param newFrameSpacing frameSpacing
     */
    public void setFrameSpacing(final int newFrameSpacing) {
        this.frameSpacing = newFrameSpacing;
        this.frameSpacingSet = true;
    }

    /**
     * Defines the number and size of rows in a frame set. The size can be
     * specified in pixels, percentage of the page length, or with an asterisk
     * (*). Specifying * causes the rows to use available space. See the HTML
     * specification for the frames et element for more details.
     *
     * @return String
     */
    public String getRows() {
        if (this.rows != null) {
            return this.rows;
        }
        ValueExpression vb = getValueExpression("rows");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Defines the number and size of rows in a frame set. The size can be
     * specified in pixels, percentage of the page length, or with an asterisk
     * (*). Specifying * causes the rows to use available space. See the HTML
     * specification for the frame set element for more details.
     *
     * @see #getRows()
     * @param newRows rows
     */
    public void setRows(final String newRows) {
        this.rows = newRows;
    }

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     *
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
     *
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
     * Sets the value of the title attribute for the HTML element.The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     *
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

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.border = ((Integer) values[1]);
        this.borderSet = ((Boolean) values[2]);
        this.borderColor = (String) values[3];
        this.cols = (String) values[4];
        this.frameBorder = ((Boolean) values[5]);
        this.frameBorderSet = ((Boolean) values[6]);
        this.frameSpacing = ((Integer) values[7]);
        this.frameSpacingSet = ((Boolean) values[8]);
        this.rows = (String) values[9];
        this.style = (String) values[10];
        this.styleClass = (String) values[11];
        this.toolTip = (String) values[12];
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[13];
        values[0] = super.saveState(context);
        values[1] = this.border;
        if (this.borderSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] =  Boolean.FALSE;
        }
        values[3] = this.borderColor;
        values[4] = this.cols;
        if (this.frameBorder) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] =  Boolean.FALSE;
        }
        if (this.frameBorderSet) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] =  Boolean.FALSE;
        }
        values[7] = this.frameSpacing;
        if (this.frameSpacingSet) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] =  Boolean.FALSE;
        }
        values[9] = this.rows;
        values[10] = this.style;
        values[11] = this.styleClass;
        values[12] = this.toolTip;
        return values;
    }
}
