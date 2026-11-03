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
import jakarta.faces.component.UIGraphic;
import jakarta.faces.context.FacesContext;

/**
 * The ImageComponent is used to display inline graphic image.
 */
@Component(type = "com.sun.webui.jsf.Image",
        family = "com.sun.webui.jsf.Image",
        displayName = "Image",
        tagName = "image", instanceName = "image",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_image_component",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_image_component_props")
        //CHECKSTYLE:ON
public class ImageComponent extends UIGraphic {

    /**
     * Specifies the position of the image with respect to its context. Valid
     * values are: bottom (the default); middle; top; left; right.
     */
    @Property(name = "align",
            displayName = "Align",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.HtmlAlignEditor")
            //CHECKSTYLE:ON
    private String align = null;

    /**
     * Alternative textual description of the image rendered by this component.
     * The alt text can be used by screen readers and in tool tips, and when
     * image display is turned off in the web browser.
     */
    @Property(name = "alt",
            displayName = "Alt Text",
            category = "Accessibility",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String alt = null;

    /**
     * Specifies the width of the img border in pixels. The default value for
     * this attribute depends on the web browser.
     */
    @Property(name = "border",
            displayName = "Border",
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
     * Image height override. When specified, the width and height attributes
     * tell web browsers to override the natural image or object size in favor
     * of these values, specified in pixels. Some browsers might not support
     * this behavior.
     */
    @Property(name = "height",
            displayName = "Height",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int height = Integer.MIN_VALUE;

    /**
     * height set flag.
     */
    private boolean heightSet = false;

    /**
     * Specifies the amount of white space in pixels to be inserted to the left
     * and right of the image. The default value is not specified but is
     * generally a small, non-zero size.
     */
    @Property(name = "hspace",
            displayName = "Horizontal Space",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int hspace = Integer.MIN_VALUE;

    /**
     * hspace set flag.
     */
    private boolean hspaceSet = false;

    /**
     * The identifier of the desired theme image.
     */
    @Property(name = "icon",
            displayName = "Icon",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.ThemeIconsEditor")
            //CHECKSTYLE:ON
    private String icon = null;

    /**
     * A verbose description of this image.
     */
    @Property(name = "longDesc",
            displayName = "Long Description",
            category = "Accessibility",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String longDesc = null;

    /**
     * Scripting code executed when a mouse click occurs over this
     * component.
     */
    @Property(name = "onClick",
            displayName = "Click Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onClick = null;

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     */
    @Property(name = "onDblClick",
            displayName = "Double Click Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onDblClick = null;

    /**
     * Scripting code executed when the user presses a mouse button while the
     * mouse pointer is on the component.
     */
    @Property(name = "onMouseDown",
            displayName = "Mouse Down Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseDown = null;

    /**
     * Scripting code executed when the user moves the mouse pointer while over
     * the component.
     */
    @Property(name = "onMouseMove",
            displayName = "Mouse Move Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseMove = null;

    /**
     * Scripting code executed when a mouse out movement occurs over this
     * component.
     */
    @Property(name = "onMouseOut",
            displayName = "Mouse Out Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseOut = null;

    /**
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
     */
    @Property(name = "onMouseOver",
            displayName = "Mouse In Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseOver = null;

    /**
     * Scripting code executed when the user releases a mouse button while the
     * mouse pointer is on the component.
     */
    @Property(name = "onMouseUp",
            displayName = "Mouse Up Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseUp = null;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
            //CHECKSTYLE:ON
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
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     */
    @Property(name = "visible",
            displayName = "Visible",
            category = "Behavior")
    private boolean visible = false;

    /**
     * visible set flag.
     */
    private boolean visibleSet = false;

    /**
     * Specifies the amount of white space in pixels to be inserted above and
     * below the image. The default value is not specified but is generally a
     * small, non-zero size.
     */
    @Property(name = "vspace",
            displayName = "Vertical Space",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int vspace = Integer.MIN_VALUE;

    /**
     * vspace set flag.
     */
    private boolean vspaceSet = false;

    /**
     * Image width override. When specified, the width and height attributes
     * tell web browsers to override the natural image or object size in favor
     * of these values, specified in pixels. Some browsers might not support
     * this behavior.
     */
    @Property(name = "width",
            displayName = "Width",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int width = Integer.MIN_VALUE;

    /**
     * width set flag.
     */
    private boolean widthSet = false;

    /**
     * Construct a new {@code ImageComponent}.
     */
    public ImageComponent() {
        super();
        setRendererType("com.sun.webui.jsf.Image");
    }

    /**
     * This implementation returns {@code "com.sun.webui.jsf.Image"}.
     * @return String
     */
    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Image";
    }

    /**
     * This implementation invokes {@code super.setId}.
     * @param id id
     */
    @Property(name = "id")
    @Override
    public void setId(final String id) {
        super.setId(id);
    }

    /**
     * This implementation invokes {@code super.setRendered}.
     * @param rendered rendered
     */
    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    /**
     * This implementation aliases {@code url} with {@code value} and invokes
     * {@code super.getValueExpression}.
     * @param name name
     * @return ValueExpression
     */
    @Override
    public ValueExpression getValueExpression(final String name) {
        if (name.equals("url")) {
            return super.getValueExpression("value");
        }
        return super.getValueExpression(name);
    }

    /**
     * This implementation aliases {@code url} with {@code value} and invokes
     * {@code super.setValueExpression}.
     * @param name name
     */
    @Override
    public void setValueExpression(final String name,
            final ValueExpression binding) {

        if (name.equals("url")) {
            super.setValueExpression("value", binding);
            return;
        }
        super.setValueExpression(name, binding);
    }

    /**
     * This implementation invokes {@code super.getValue}.
     * @return Object
     */
    // Hide value
    @Property(name = "value", isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    /**
     * Specifies the position of the image with respect to its context. Valid
     * values are: bottom (the default); middle; top; left; right.
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
     * Specifies the position of the image with respect to its context. Valid
     * values are: bottom (the default); middle; top; left; right.
     *
     * @see #getAlign()
     * @param newAlign align
     */
    public void setAlign(final String newAlign) {
        this.align = newAlign;
    }

    /**
     * Alternative textual description of the image rendered by this component.
     * The alt text can be used by screen readers and in tool tips, and when
     * image display is turned off in the web browser.
     * @return String
     */
    public String getAlt() {
        if (this.alt != null) {
            return this.alt;
        }
        ValueExpression vb = getValueExpression("alt");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Alternative textual description of the image rendered by this component.
     * The alt text can be used by screen readers and in tool tips, and when
     * image display is turned off in the web browser.
     *
     * @see #getAlt()
     * @param newAlt alt
     */
    public void setAlt(final String newAlt) {
        this.alt = newAlt;
    }

    /**
     * Specifies the width of the img border in pixels. The default value for
     * this attribute depends on the web browser.
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
        return 0;
    }

    /**
     * Specifies the width of the img border in pixels. The default value for
     * this attribute depends on the web browser.
     *
     * @see #getBorder()
     * @param newBorder border
     */
    public void setBorder(final int newBorder) {
        this.border = newBorder;
        this.borderSet = true;
    }

    /**
     * Image height override. When specified, the width and height attributes
     * tell web browsers to override the natural image or object size in favor
     * of these values, specified in pixels. Some browsers might not support
     * this behavior.
     * @return int
     */
    public int getHeight() {
        if (this.heightSet) {
            return this.height;
        }
        ValueExpression vb = getValueExpression("height");
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
     * <p>
     * Image height override. When specified, the width and height attributes
     * tell web browsers to override the natural image or object size in favor
     * of these values, specified in pixels. Some browsers might not support
     * this behavior.</p>
     *
     * @see #getHeight()
     * @param newHeight height
     */
    public void setHeight(final int newHeight) {
        this.height = newHeight;
        this.heightSet = true;
    }

    /**
     * Specifies the amount of white space in pixels to be inserted to the left
     * and right of the image. The default value is not specified but is
     * generally a small, non-zero size.
     * @return int
     */
    public int getHspace() {
        if (this.hspaceSet) {
            return this.hspace;
        }
        ValueExpression vb = getValueExpression("hspace");
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
     * Specifies the amount of white space in pixels to be inserted to the left
     * and right of the image. The default value is not specified but is
     * generally a small, non-zero size.
     *
     * @see #getHspace()
     * @param newHspace hspace
     */
    public void setHspace(final int newHspace) {
        this.hspace = newHspace;
        this.hspaceSet = true;
    }

    /**
     * The identifier of the desired theme image.
     * @return String
     */
    public String getIcon() {
        if (this.icon != null) {
            return this.icon;
        }
        ValueExpression vb = getValueExpression("icon");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The identifier of the desired theme image.
     *
     * @see #getIcon()
     * @param newIcon icon
     */
    public void setIcon(final String newIcon) {
        this.icon = newIcon;
    }

    /**
     * A verbose description of this image.
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
     * A verbose description of this image.
     *
     * @see #getLongDesc()
     * @param newLongDesc longDesc
     */
    public void setLongDesc(final String newLongDesc) {
        this.longDesc = newLongDesc;
    }

    /**
     * Scripting code executed when a mouse click occurs over this
     * component.
     * @return String
     */
    public String getOnClick() {
        if (this.onClick != null) {
            return this.onClick;
        }
        ValueExpression vb = getValueExpression("onClick");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when a mouse click occurs over this
     * component.
     *
     * @see #getOnClick()
     * @param newOnClick onClick
     */
    public void setOnClick(final String newOnClick) {
        this.onClick = newOnClick;
    }

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     * @return String
     */
    public String getOnDblClick() {
        if (this.onDblClick != null) {
            return this.onDblClick;
        }
        ValueExpression vb = getValueExpression("onDblClick");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     *
     * @see #getOnDblClick()
     * @param newOnDblClick onDblClick
     */
    public void setOnDblClick(final String newOnDblClick) {
        this.onDblClick = newOnDblClick;
    }

    /**
     * Scripting code executed when the user presses a mouse button while the
     * mouse pointer is on the component.
     * @return String
     */
    public String getOnMouseDown() {
        if (this.onMouseDown != null) {
            return this.onMouseDown;
        }
        ValueExpression vb = getValueExpression("onMouseDown");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user presses a mouse button while the
     * mouse pointer is on the component.
     *
     * @see #getOnMouseDown()
     * @param newOnMouseDown onMouseDown
     */
    public void setOnMouseDown(final String newOnMouseDown) {
        this.onMouseDown = newOnMouseDown;
    }

    /**
     * Scripting code executed when the user moves the mouse pointer while over
     * the component.
     * @return String
     */
    public String getOnMouseMove() {
        if (this.onMouseMove != null) {
            return this.onMouseMove;
        }
        ValueExpression vb = getValueExpression("onMouseMove");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user moves the mouse pointer while over
     * the component.
     *
     * @see #getOnMouseMove()
     * @param newOnMouseMove onMouseMove
     */
    public void setOnMouseMove(final String newOnMouseMove) {
        this.onMouseMove = newOnMouseMove;
    }

    /**
     * Scripting code executed when a mouse out movement occurs over this
     * component.
     * @return String
     */
    public String getOnMouseOut() {
        if (this.onMouseOut != null) {
            return this.onMouseOut;
        }
        ValueExpression vb = getValueExpression("onMouseOut");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when a mouse out movement occurs over this
     * component.
     *
     * @see #getOnMouseOut()
     * @param newOnMouseOut onMouseOut
     */
    public void setOnMouseOut(final String newOnMouseOut) {
        this.onMouseOut = newOnMouseOut;
    }

    /**
     * <p>
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.</p>
     * @return String
     */
    public String getOnMouseOver() {
        if (this.onMouseOver != null) {
            return this.onMouseOver;
        }
        ValueExpression vb = getValueExpression("onMouseOver");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
     *
     * @see #getOnMouseOver()
     * @param newOnMouseOver onMouseOver
     */
    public void setOnMouseOver(final String newOnMouseOver) {
        this.onMouseOver = newOnMouseOver;
    }

    /**
     * Scripting code executed when the user releases a mouse button while the
     * mouse pointer is on the component.
     * @return String
     */
    public String getOnMouseUp() {
        if (this.onMouseUp != null) {
            return this.onMouseUp;
        }
        ValueExpression vb = getValueExpression("onMouseUp");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user releases a mouse button while the
     * mouse pointer is on the component.
     *
     * @see #getOnMouseUp()
     * @param newOnMouseUp onMouseUp
     */
    public void setOnMouseUp(final String newOnMouseUp) {
        this.onMouseUp = newOnMouseUp;
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
     * Absolute or relative URL to the image to be rendered.
     * @return String
     */
    @Property(name = "url",
            displayName = "Url",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.ImageUrlPropertyEditor")
            //CHECKSTYLE:ON
    @Override
    public String getUrl() {
        return (String) getValue();
    }

    /**
     * Absolute or relative URL to the image to be rendered.
     *
     * @see #getUrl()
     */
    @Override
    public void setUrl(final String url) {
        setValue((Object) url);
    }

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     * @return {@code boolean}
     */
    public boolean isVisible() {
        if (this.visibleSet) {
            return this.visible;
        }
        ValueExpression vb = getValueExpression("visible");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return true;
    }

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     *
     * @see #isVisible()
     * @param newVisible visible
     */
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    /**
     * Specifies the amount of white space in pixels to be inserted above and
     * below the image. The default value is not specified but is generally a
     * small, non-zero size.
     * @return int
     */
    public int getVspace() {
        if (this.vspaceSet) {
            return this.vspace;
        }
        ValueExpression vb = getValueExpression("vspace");
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
     * Specifies the amount of white space in pixels to be inserted above and
     * below the image. The default value is not specified but is generally a
     * small, non-zero size.
     *
     * @see #getVspace()
     * @param newVspace vspace
     */
    public void setVspace(final int newVspace) {
        this.vspace = newVspace;
        this.vspaceSet = true;
    }

    /**
     * Image width override. When specified, the width and height attributes
     * tell web browsers to override the natural image or object size in favor
     * of these values, specified in pixels. Some browsers might not support
     * this behavior.
     * @return int
     */
    public int getWidth() {
        if (this.widthSet) {
            return this.width;
        }
        ValueExpression vb = getValueExpression("width");
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
     * Image width override. When specified, the width and height attributes
     * tell web browsers to override the natural image or object size in favor
     * of these values, specified in pixels. Some browsers might not support
     * this behavior.
     *
     * @see #getWidth()
     * @param newWidth width
     */
    public void setWidth(final int newWidth) {
        this.width = newWidth;
        this.widthSet = true;
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
        this.align = (String) values[1];
        this.alt = (String) values[2];
        this.border = ((Integer) values[3]);
        this.borderSet = ((Boolean) values[4]);
        this.height = ((Integer) values[5]);
        this.heightSet = ((Boolean) values[6]);
        this.hspace = ((Integer) values[7]);
        this.hspaceSet = ((Boolean) values[8]);
        this.icon = (String) values[9];
        this.longDesc = (String) values[10];
        this.onClick = (String) values[11];
        this.onDblClick = (String) values[12];
        this.onMouseDown = (String) values[13];
        this.onMouseMove = (String) values[14];
        this.onMouseOut = (String) values[15];
        this.onMouseOver = (String) values[16];
        this.onMouseUp = (String) values[17];
        this.style = (String) values[18];
        this.styleClass = (String) values[19];
        this.toolTip = (String) values[20];
        this.visible = ((Boolean) values[21]);
        this.visibleSet = ((Boolean) values[22]);
        this.vspace = ((Integer) values[23]);
        this.vspaceSet = ((Boolean) values[24]);
        this.width = ((Integer) values[25]);
        this.widthSet = ((Boolean) values[26]);
    }

    /**
     * This implementation saves the state of all properties.
     * @param context faces context
     * @return Object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[27];
        values[0] = super.saveState(context);
        values[1] = this.align;
        values[2] = this.alt;
        values[3] = this.border;
        if (this.borderSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        values[5] = this.height;
        if (this.heightSet) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] = Boolean.FALSE;
        }
        values[7] = this.hspace;
        if (this.hspaceSet) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] = Boolean.FALSE;
        }
        values[9] = this.icon;
        values[10] = this.longDesc;
        values[11] = this.onClick;
        values[12] = this.onDblClick;
        values[13] = this.onMouseDown;
        values[14] = this.onMouseMove;
        values[15] = this.onMouseOut;
        values[16] = this.onMouseOver;
        values[17] = this.onMouseUp;
        values[18] = this.style;
        values[19] = this.styleClass;
        values[20] = this.toolTip;
        if (this.visible) {
            values[21] = Boolean.TRUE;
        } else {
            values[21] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[22] = Boolean.TRUE;
        } else {
            values[22] = Boolean.FALSE;
        }
        values[23] = this.vspace;
        if (this.vspaceSet) {
            values[24] = Boolean.TRUE;
        } else {
            values[24] = Boolean.FALSE;
        }
        values[25] = this.width;
        if (this.widthSet) {
            values[26] = Boolean.TRUE;
        } else {
            values[26] = Boolean.FALSE;
        }
        return values;
    }
}
