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
 * The Button component is used to display an input button.
 */
@Component(type = "com.sun.webui.jsf.Button",
        family = "com.sun.webui.jsf.Button",
        tagRendererType = "com.sun.webui.jsf.Button",
        displayName = "Button",
        tagName = "button",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_button",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_button_props")
        //CHECKSTYLE:ON
public final class Button extends WebuiCommand implements ComplexComponent {

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
     * Indicates that activation of this component by the user is not currently
     * permitted. In this component library, the disabled attribute also causes
     * the button to be rendered using a particular style.
     */
    @Property(name = "disabled",
            displayName = "Disabled",
            category = "Behavior")
    private boolean disabled = false;

    /**
     * disabled set flag.
     */
    private boolean disabledSet = false;

    /**
     * Escape the HTML text so it won't be interpreted by the browser as HTML.
     * When the {@code escape} value is set to false, an HTML {@code button}
     * element is rendered, instead of an HTML {@code input} element. And the
     * {@code alt} attribute does not apply.
     */
    @Property(name = "escape",
            displayName = "Escape",
            category = "Appearance")
    private boolean escape = false;

    /**
     * escape set flag.
     */
    private boolean escapeSet = false;

    /**
     * The identifier key of a theme image to be used for the button.
     */
    @Property(name = "icon",
            displayName = "Icon",
            category = "Appearance",
            isHidden = true,
            isAttribute = true,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.ThemeIconsEditor")
            //CHECKSTYLE:ON
    private String icon = null;

    /**
     * Resource path of an image to be displayed to create the visual appearance
     * of this button instead of the standard button image. Either the
     * {@code imageURL} or {@code text} attributes must be specified. When an
     * {@code imageURL} value is given, the button type is set to {@code image}.
     */
    @Property(name = "imageURL",
            displayName = "Image URL",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.ImageUrlPropertyEditor")
            //CHECKSTYLE:ON
    private String imageURL = null;

    /**
     * Indicates that the button should be rendered using a different style than
     * normal buttons. By default, a button that specifies the mini attribute
     * looks the same as a normal button. You must set your own CSS style to
     * render a mini button.
     */
    @Property(name = "mini",
            displayName = "Is Mini",
            category = "Appearance")
    private boolean mini = false;

    /**
     * mini set flag.
     */
    private boolean miniSet = false;

    /**
     * Indicates that padding should not be applied to the button text. By
     * default, whitespace characters are padded to button text greater than or
     * equal to 4 characters in length. If the value is set to true, no padding
     * is applied.
     */
    @Property(name = "noTextPadding",
            displayName = "No Text Padding",
            category = "Appearance")
    private boolean noTextPadding = false;

    /**
     * noTextPadding set flag.
     */
    private boolean noTextPaddingSet = false;

    /**
     * Scripting code executed when this element loses focus.
     */
    @Property(name = "onBlur",
            displayName = "Blur Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onBlur = null;

    /**
     * Scripting code executed when a mouse click occurs over this component.
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
     * Scripting code executed when this component receives focus. An element
     * receives focus when the user selects the element by pressing the tab key
     * or clicking the mouse.
     */
    @Property(name = "onFocus",
            displayName = "Focus Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onFocus = null;

    /**
     * Scripting code executed when the user presses down on a key while the
     * component has focus.
     */
    @Property(name = "onKeyDown",
            displayName = "Key Down Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onKeyDown = null;

    /**
     * Scripting code executed when the user presses and releases a key while
     * the component has focus.
     */
    @Property(name = "onKeyPress",
            displayName = "Key Press Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onKeyPress = null;

    /**
     * Scripting code executed when the user releases a key while the component
     * has focus.
     */
    @Property(name = "onKeyUp",
            displayName = "Key Up Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onKeyUp = null;

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
     * Scripting code executed when the user moves the mouse pointer off this
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
     * Indicates that the button is the most commonly used button within a
     * group.
     */
    @Property(name = "primary",
            displayName = "Is Primary",
            category = "Appearance")
    private boolean primary = false;

    /**
     * primary set flag.
     */
    private boolean primarySet = false;

    /**
     * Indicates that the button should be a HTML reset button. By default, this
     * value is false and the button is created as a submit button. If the value
     * is set to true, no action listener will be invoked.
     */
    @Property(name = "reset",
            displayName = "Is Reset",
            category = "Behavior")
    private boolean reset = false;

    /**
     * reset set flag.
     */
    private boolean resetSet = false;

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
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The value must be an integer between 0 and
     * 32767.
     */
    @Property(name = "tabIndex",
            displayName = "Tab Index",
            category = "Accessibility",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int tabIndex = Integer.MIN_VALUE;

    /**
     * tabIndex set flag.
     */
    private boolean tabIndexSet = false;

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
     * Default constructor.
     */
    public Button() {
        super();
        setRendererType("com.sun.webui.jsf.Button");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Button";
    }

    @Override
    public String getLabeledElementId(final FacesContext context) {
        return getClientId(context);
    }

    @Override
    public String getFocusElementId(final FacesContext context) {
        return getLabeledElementId(context);
    }

    @Override
    public String getPrimaryElementID(final FacesContext context) {
        return getLabeledElementId(context);
    }

    // Overwrite value annotation
    @Property(name = "value", isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    @Override
    public ValueExpression getValueExpression(final String name) {
        if (name.equals("text")) {
            return super.getValueExpression("value");
        }
        return super.getValueExpression(name);
    }

    @Override
    public void setValueExpression(final String name,
            final ValueExpression binding) {

        if (name.equals("text")) {
            super.setValueExpression("value", binding);
            return;
        }
        super.setValueExpression(name, binding);
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
     * Indicates that activation of this component by the user is not currently
     * permitted. In this component library, the disabled attribute also causes
     * the button to be rendered using a particular style.
     * @return {@code boolean}
     */
    public boolean isDisabled() {
        if (this.disabledSet) {
            return this.disabled;
        }
        ValueExpression vb = getValueExpression("disabled");
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
     * Indicates that activation of this component by the user is not currently
     * permitted. In this component library, the disabled attribute also causes
     * the button to be rendered using a particular style.
     *
     * @see #isDisabled()
     * @param newDisabled disabled
     */
    public void setDisabled(final boolean newDisabled) {
        this.disabled = newDisabled;
        this.disabledSet = true;
    }

    /**
     * Escape the HTML text so it won't be interpreted by the browser as HTML.
     * When the {@code escape} value is set to false, an HTML
     * {@code button} element is rendered, instead of an HTML
     * {@code input} element. And the {@code alt} attribute does not
     * apply.
     * @return {@code boolean}
     */
    public boolean isEscape() {
        if (this.escapeSet) {
            return this.escape;
        }
        ValueExpression vb = getValueExpression("escape");
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
     * Escape the HTML text so it won't be interpreted by the browser as HTML.
     * When the {@code escape} value is set to false, an HTML
     * {@code button} element is rendered, instead of an HTML
     * {@code input} element. And the {@code alt} attribute does not
     * apply.
     *
     * @see #isEscape()
     * @param newEscape escape
     */
    public void setEscape(final boolean newEscape) {
        this.escape = newEscape;
        this.escapeSet = true;
    }

    /**
     * The identifier key of a theme image to be used for the button.
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
     * The identifier key of a theme image to be used for the button.
     *
     * @see #getIcon()
     * @param newIcon icon
     */
    public void setIcon(final String newIcon) {
        this.icon = newIcon;
    }

    /**
     * Resource path of an image to be displayed to create the visual appearance
     * of this button instead of the standard button image. Either the
     * {@code imageURL} or {@code text} attributes must be specified.
     * When an {@code imageURL} value is given, the button type is set to
     * {@code image}.
     * @return String
     */
    public String getImageURL() {
        if (this.imageURL != null) {
            return this.imageURL;
        }
        ValueExpression vb = getValueExpression("imageURL");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Resource path of an image to be displayed to create the visual appearance
     * of this button instead of the standard button image. Either the
     * {@code imageURL} or {@code text} attributes must be specified.
     * When an {@code imageURL} value is given, the button type is set to
     * {@code image}.
     *
     * @see #getImageURL()
     * @param newImageURL imageURL
     */
    public void setImageURL(final String newImageURL) {
        this.imageURL = newImageURL;
    }

    /**
     * Indicates that the button should be rendered using a different style than
     * normal buttons. By default, a button that specifies the mini attribute
     * looks the same as a normal button. You must set your own CSS style to
     * render a mini button.
     * @return {@code boolean}
     */
    public boolean isMini() {
        if (this.miniSet) {
            return this.mini;
        }
        ValueExpression vb = getValueExpression("mini");
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
     * Indicates that the button should be rendered using a different style than
     * normal buttons. By default, a button that specifies the mini attribute
     * looks the same as a normal button. You must set your own CSS style to
     * render a mini button.
     *
     * @see #isMini()
     * @param newMini mini
     */
    public void setMini(final boolean newMini) {
        this.mini = newMini;
        this.miniSet = true;
    }

    /**
     * Indicates that padding should not be applied to the button text. By
     * default, whitespace characters are padded to button text greater than or
     * equal to 4 characters in length. If the value is set to true, no padding
     * is applied.
     * @return {@code boolean}
     */
    public boolean isNoTextPadding() {
        if (this.noTextPaddingSet) {
            return this.noTextPadding;
        }
        ValueExpression vb = getValueExpression("noTextPadding");
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
     * Indicates that padding should not be applied to the button text. By
     * default, whitespace characters are padded to button text greater than or
     * equal to 4 characters in length. If the value is set to true, no padding
     * is applied.
     *
     * @see #isNoTextPadding()
     * @param newNoTextPadding noTextPadding
     */
    public void setNoTextPadding(final boolean newNoTextPadding) {
        this.noTextPadding = newNoTextPadding;
        this.noTextPaddingSet = true;
    }

    /**
     * Scripting code executed when this element loses focus.
     * @return String
     */
    public String getOnBlur() {
        if (this.onBlur != null) {
            return this.onBlur;
        }
        ValueExpression vb = getValueExpression("onBlur");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when this element loses focus.
     *
     * @see #getOnBlur()
     * @param newOnBlur onBlur
     */
    public void setOnBlur(final String newOnBlur) {
        this.onBlur = newOnBlur;
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
     * Scripting code executed when this component receives focus. An element
     * receives focus when the user selects the element by pressing the tab key
     * or clicking the mouse.
     * @return String
     */
    public String getOnFocus() {
        if (this.onFocus != null) {
            return this.onFocus;
        }
        ValueExpression vb = getValueExpression("onFocus");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when this component receives focus. An element
     * receives focus when the user selects the element by pressing the tab key
     * or clicking the mouse.
     *
     * @see #getOnFocus()
     * @param newOnFocus onFocus
     */
    public void setOnFocus(final String newOnFocus) {
        this.onFocus = newOnFocus;
    }

    /**
     * Scripting code executed when the user presses down on a key while the
     * component has focus.
     * @return String
     */
    public String getOnKeyDown() {
        if (this.onKeyDown != null) {
            return this.onKeyDown;
        }
        ValueExpression vb = getValueExpression("onKeyDown");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user presses down on a key while the
     * component has focus.
     *
     * @see #getOnKeyDown()
     * @param newOnKeyDown v
     */
    public void setOnKeyDown(final String newOnKeyDown) {
        this.onKeyDown = newOnKeyDown;
    }

    /**
     * Scripting code executed when the user presses and releases a key while
     * the component has focus.
     * @return String
     */
    public String getOnKeyPress() {
        if (this.onKeyPress != null) {
            return this.onKeyPress;
        }
        ValueExpression vb = getValueExpression("onKeyPress");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user presses and releases a key while
     * the component has focus.
     *
     * @see #getOnKeyPress()
     * @param newOnKeyPress onKeyPress
     */
    public void setOnKeyPress(final String newOnKeyPress) {
        this.onKeyPress = newOnKeyPress;
    }

    /**
     * Scripting code executed when the user releases a key while the component
     * has focus.
     * @return String
     */
    public String getOnKeyUp() {
        if (this.onKeyUp != null) {
            return this.onKeyUp;
        }
        ValueExpression vb = getValueExpression("onKeyUp");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user releases a key while the component
     * has focus.
     *
     * @see #getOnKeyUp()
     * @param newOnKeyUp onKeyUp
     */
    public void setOnKeyUp(final String newOnKeyUp) {
        this.onKeyUp = newOnKeyUp;
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
     * Scripting code executed when the user moves the mouse pointer off this
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
     * Scripting code executed when the user moves the mouse pointer off this
     * component.
     *
     * @see #getOnMouseOut()
     * @param newOnMouseOut onMouseOut
     */
    public void setOnMouseOut(final String newOnMouseOut) {
        this.onMouseOut = newOnMouseOut;
    }

    /**
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
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
     * Indicates that the button is the most commonly used button within a
     * group.
     * @return {@code boolean}
     */
    public boolean isPrimary() {
        if (this.primarySet) {
            return this.primary;
        }
        ValueExpression vb = getValueExpression("primary");
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
     * Indicates that the button is the most commonly used button within a
     * group.
     *
     * @see #isPrimary()
     * @param newPrimary primary
     */
    public void setPrimary(final boolean newPrimary) {
        this.primary = newPrimary;
        this.primarySet = true;
    }

    /**
     * Indicates that the button should be a HTML reset button. By default, this
     * value is false and the button is created as a submit button. If the value
     * is set to true, no action listener will be invoked.
     * @return {@code boolean}
     */
    public boolean isReset() {
        if (this.resetSet) {
            return this.reset;
        }
        ValueExpression vb = getValueExpression("reset");
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
     * Indicates that the button should be a HTML reset button. By default, this
     * value is false and the button is created as a submit button. If the value
     * is set to true, no action listener will be invoked.
     *
     * @see #isReset()
     * @param newReset reset
     */
    public void setReset(final boolean newReset) {
        this.reset = newReset;
        this.resetSet = true;
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
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The value must be an integer between 0 and
     * 32767.
     * @return int
     */
    public int getTabIndex() {
        if (this.tabIndexSet) {
            return this.tabIndex;
        }
        ValueExpression vb = getValueExpression("tabIndex");
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
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The value must be an integer between 0 and
     * 32767.
     *
     * @see #getTabIndex()
     * @param newTabIndex tabIndex
     */
    public void setTabIndex(final int newTabIndex) {
        this.tabIndex = newTabIndex;
        this.tabIndexSet = true;
    }

    /**
     * Text to display on the button. Either the {@code imageURL} or
     * {@code text} attributes must be specified. When an
     * {@code imageURL} value is given, the button type is set to
     * {@code image}.
     * @return Object
     */
    @Property(name = "text",
            displayName = "Button Text",
            category = "Appearance",
            isDefault = true,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    public Object getText() {
        return getValue();
    }

    /**
     * Text to display on the button. Either the {@code imageURL} or
     * {@code text} attributes must be specified. When an
     * {@code imageURL} value is given, the button type is set to
     * {@code image}.
     *
     * @see #getText()
     * @param text text
     */
    public void setText(final Object text) {
        setValue(text);
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

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.alt = (String) values[1];
        this.disabled = ((Boolean) values[2]);
        this.disabledSet = ((Boolean) values[3]);
        this.escape = ((Boolean) values[4]);
        this.escapeSet = ((Boolean) values[5]);
        this.icon = (String) values[6];
        this.imageURL = (String) values[7];
        this.mini = ((Boolean) values[8]);
        this.miniSet = ((Boolean) values[9]);
        this.noTextPadding = ((Boolean) values[10]);
        this.noTextPaddingSet = ((Boolean) values[11]);
        this.onBlur = (String) values[12];
        this.onClick = (String) values[13];
        this.onDblClick = (String) values[14];
        this.onFocus = (String) values[15];
        this.onKeyDown = (String) values[16];
        this.onKeyPress = (String) values[17];
        this.onKeyUp = (String) values[18];
        this.onMouseDown = (String) values[19];
        this.onMouseMove = (String) values[20];
        this.onMouseOut = (String) values[21];
        this.onMouseOver = (String) values[22];
        this.onMouseUp = (String) values[23];
        this.primary = ((Boolean) values[24]);
        this.primarySet = ((Boolean) values[25]);
        this.reset = ((Boolean) values[26]);
        this.resetSet = ((Boolean) values[27]);
        this.style = (String) values[28];
        this.styleClass = (String) values[29];
        this.tabIndex = ((Integer) values[30]);
        this.tabIndexSet = ((Boolean) values[31]);
        this.toolTip = (String) values[32];
        this.visible = ((Boolean) values[33]);
        this.visibleSet = ((Boolean) values[34]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[35];
        values[0] = super.saveState(context);
        values[1] = this.alt;
        if (this.disabled) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.disabledSet) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.escape) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        if (this.escapeSet) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        values[6] = this.icon;
        values[7] = this.imageURL;
        if (this.mini) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] = Boolean.FALSE;
        }
        if (this.miniSet) {
            values[9] = Boolean.TRUE;
        } else {
            values[9] = Boolean.FALSE;
        }
        if (this.noTextPadding) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        if (this.noTextPaddingSet) {
            values[11] = Boolean.TRUE;
        } else {
            values[11] = Boolean.FALSE;
        }
        values[12] = this.onBlur;
        values[13] = this.onClick;
        values[14] = this.onDblClick;
        values[15] = this.onFocus;
        values[16] = this.onKeyDown;
        values[17] = this.onKeyPress;
        values[18] = this.onKeyUp;
        values[19] = this.onMouseDown;
        values[20] = this.onMouseMove;
        values[21] = this.onMouseOut;
        values[22] = this.onMouseOver;
        values[23] = this.onMouseUp;
        if (this.primary) {
            values[24] = Boolean.TRUE;
        } else {
            values[24] = Boolean.FALSE;
        }
        if (this.primarySet) {
            values[25] = Boolean.TRUE;
        } else {
            values[25] = Boolean.FALSE;
        }
        if (this.reset) {
            values[26] = Boolean.TRUE;
        } else {
            values[26] = Boolean.FALSE;
        }
        if (this.resetSet) {
            values[27] = Boolean.TRUE;
        } else {
            values[27] = Boolean.FALSE;
        }
        values[28] = this.style;
        values[29] = this.styleClass;
        values[30] = this.tabIndex;
        if (this.tabIndexSet) {
            values[31] = Boolean.TRUE;
        } else {
            values[31] = Boolean.FALSE;
        }
        values[32] = this.toolTip;
        if (this.visible) {
            values[33] = Boolean.TRUE;
        } else {
            values[33] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[34] = Boolean.TRUE;
        } else {
            values[34] = Boolean.FALSE;
        }
        return values;
    }
}
