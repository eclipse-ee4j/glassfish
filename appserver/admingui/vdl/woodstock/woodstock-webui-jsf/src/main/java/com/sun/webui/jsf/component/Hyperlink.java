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
 * The Hyperlink component is used to create a link.
 */
@Component(type = "com.sun.webui.jsf.Hyperlink",
        family = "com.sun.webui.jsf.Hyperlink",
        displayName = "Hyperlink",
        tagName = "hyperlink",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_hyperlink",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_hyperlink_props")
        //CHECKSTYLE:ON
public class Hyperlink extends WebuiCommand implements ComplexComponent {

    /**
     * Flag indicating that activation of this component by the user is not
     * currently permitted. In this component library, the disabled attribute
     * also causes the hyperlink to be rendered as formatted text in an HTMLq
     * &lt;span&gt; tag. The hyperlink cannot be enabled from the client because
     * this is a server side only feature. You cannot disable an anchor.
     */
    @Property(name = "disabled",
            displayName = "Disabled",
            category = "Behavior")
    private boolean disabled;

    /**
     * disabled set flag.
     */
    private boolean disabledSet;

    /**
     * Scripting code executed when this element loses focus.
     */
    @Property(name = "onBlur",
            displayName = "Blur Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onBlur;

    /**
     * Scripting code executed when a mouse click occurs over this component. If
     * the component submits the form (by using the action attribute), the
     * script that you use with the onClick attribute should not return from the
     * function. When the action attribute is used, the component handles the
     * return with a script that is appended to the anchor element's
     * {@code onclick} property. When you supply an onClick attribute, this
     * return script is appended after your script in the anchor's
     * {@code onclick}. It is OK to return from your script to abort the submit
     * process if necessary.
     */
    @Property(name = "onClick",
            displayName = "Click Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onClick;

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     */
    @Property(name = "onDblClick",
            displayName = "Double Click Script",
            category = "Javascript",
            isHidden = true,
            isAttribute = false,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onDblClick;

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
    private String onFocus;

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
    private String onKeyDown;

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
    private String onKeyPress;

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
    private String onKeyUp;

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
    private String onMouseDown;

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
    private String onMouseMove;

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
    private String onMouseOut;

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
    private String onMouseOver;

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
    private String onMouseUp;

    /**
     * The shape of the hot spot on the screen (for use in client-side image
     * maps). Valid values are: default (entire region); rect (rectangular
     * region); circle (circular region); and poly (polygonal region).
     */
    @Property(name = "shape",
            displayName = "Shape",
            category = "Advanced",
            isHidden = true,
            isAttribute = false)
    private String shape;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)",
            category = "Appearance",
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
    private String style;

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
    private String styleClass;

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
    private boolean tabIndexSet;

    /**
     * The resource at the specified URL is displayed in the frame that is
     * specified with the target attribute. Values such as "_blank" that are
     * valid for the target attribute of a HTML anchor element are also valid
     * for this attribute in this component
     */
    @Property(name = "target",
            displayName = "Target",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.FrameTargetsEditor")
            //CHECKSTYLE:ON
    private String target;

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     */
    @Property(name = "toolTip",
            displayName = "Tool Tip", category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String toolTip;

    /**
     * The MIME content type of the resource specified by this component.
     */
    @Property(name = "type",
            displayName = "Type",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.MimeTypesEditor")
            //CHECKSTYLE:ON
    private String type;

    /**
     * Absolute, relative, or context relative (starting with "/") URL to the
     * resource selected by this hyperlink. If the URL attribute is specified,
     * clicking this hyperlink sends the browser to the new location. If the
     * action attribute is specified, the form is submitted. If both are
     * specified, the url attribute takes precedence.
     */
    @Property(name = "url",
            displayName = "URL",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.SunWebUrlPropertyEditor")
            //CHECKSTYLE:ON
    private String url;

    /**
     * The language code of the resource designated by this hyperlink.
     */
    @Property(name = "urlLang",
            displayName = "URL Lang",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.LanguagesEditor")
            //CHECKSTYLE:ON
    private String urlLang;

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
    private boolean visible;

    /**
     * visible set flag.
     */
    private boolean visibleSet;

    /**
     * Default constructor.
     */
    public Hyperlink() {
        super();
        setRendererType("com.sun.webui.jsf.Hyperlink");
    }

    /**
     * This implementation returns {@code "com.sun.webui.jsf.Hyperlink"}.
     * @return String
     */
    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Hyperlink";
    }

    /**
     * Implement this method so that it returns the DOM ID of the HTML element
     * which should receive focus when the component receives focus, and to
     * which a component label should apply. Usually, this is the first element
     * that accepts input.
     *
     * @param context The FacesContext for the request
     * @return The client id, also the JavaScript element id
     *
     * @deprecated
     * @see #getLabeledElementId
     * @see #getFocusElementId
     */
    @Override
    public String getPrimaryElementID(final FacesContext context) {
        return getLabeledElementId(context);
    }

    /**
     * Returns the absolute ID of an HTML element suitable for use as the value
     * of an HTML LABEL element's {@code for} attribute. If the
     * {@code ComplexComponent} has sub-components, and one of the
     * sub-components is the target of a label, if that sub-component is a
     * {@code ComplexComponent}, then {@code getLabeledElementId} must
     * called on the sub-component and the value returned. The value returned by
     * this method call may or may not resolve to a component instance.
     *
     * @param context The FacesContext used for the request
     * @return An abolute id suitable for the value of an HTML LABEL element's
     * {@code for} attribute.
     */
    @Override
    public String getLabeledElementId(final FacesContext context) {
        return getClientId(context);
    }

    /**
     * Returns the id of an HTML element suitable to receive the focus. If the
     * {@code ComplexComponent} has sub-components, and one of the
     * sub-components is to receive the focus, if that sub-component is a
     * {@code ComplexComponent}, then {@code getFocusElementId} must
     * called on the sub-component and the value returned. The value returned by
     * this method call may or may not resolve to a component instance.
     * <p>
     * This implementation returns the value of
     * {@code getLabeledElementId}.
     * </p>
     * @param context The FacesContext used for the request
     */
    @Override
    public String getFocusElementId(final FacesContext context) {
        // For now return the labeled component.
        return getLabeledElementId(context);
    }

    /**
     * This implementation returns the class name and id.
     * @return String
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.getClass().getName());
        buffer.append(" id: ");
        buffer.append(getId());
        return buffer.toString();
    }

    /**
     * Return the {@code ValueExpression} stored for the specified name (if
     * any), respecting any property aliases.
     *
     * @param name Name of value binding expression to retrieve
     * @return ValueExpression
     */
    @Override
    public ValueExpression getValueExpression(final String name) {
        if (name.equals("text")) {
            return super.getValueExpression("value");
        }
        return super.getValueExpression(name);
    }

    /**
     * Set the {@code ValueExpression} stored for the specified name (if
     * any), respecting any property aliases.
     *
     * @param name Name of value binding to set
     * @param binding ValueExpression to set, or null to remove
     */
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
     * Flag indicating that activation of this component by the user is not
     * currently permitted. In this component library, the disabled attribute
     * also causes the hyperlink to be rendered as formatted text in an HTML
     * &lt;span&gt; tag. The hyperlink cannot be enabled from the client because
     * this is a server side only feature. You cannot disable an anchor.
     * @return Object
     */
    @Property(isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    /**
     * Flag indicating that activation of this component by the user is not
     * currently permitted. In this component library, the disabled attribute
     * also causes the hyperlink to be rendered as formatted text in an HTML
     * &lt;span&gt; tag. The hyperlink cannot be enabled from the client because
     * this is a server side only feature. You cannot disable an anchor.
     *
     * @return {@code bolean}
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
     * Flag indicating that activation of this component by the user is not
     * currently permitted. In this component library, the disabled attribute
     * also causes the hyperlink to be rendered as formatted text in an HTML
     * &lt;span&gt; tag. The hyperlink cannot be enabled from the client because
     * this is a server side only feature. You cannot disable an anchor.
     *
     * @see #isDisabled()
     * @param newDisabled disabled
     */
    public void setDisabled(final boolean newDisabled) {
        this.disabled = newDisabled;
        this.disabledSet = true;
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
     * Scripting code executed when a mouse click occurs over this component. If
     * the component submits the form (by using the action attribute), the
     * script that you use with the onClick attribute should not return from the
     * function. When the action attribute is used, the component handles the
     * return with a script that is appended to the anchor element's
     * {@code onclick} property. When you supply an onClick attribute, this
     * return script is appended after your script in the anchor's
     * {@code onclick}. It is OK to return from your script to abort the submit
     * process if necessary.
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
     * Scripting code executed when a mouse click occurs over this component. If
     * the component submits the form (by using the action attribute), the
     * script that you use with the onClick attribute should not return from the
     * function. When the action attribute is used, the component handles the
     * return with a script that is appended to the anchor element's
     * {@code onclick} property. When you supply an onClick attribute, this
     * return script is appended after your script in the anchor's
     * {@code onclick}. It is OK to return from your script to abort the submit
     * process if necessary.
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
     * @param newOnKeyDown onKeyDown
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
     * The shape of the hot spot on the screen (for use in client-side image
     * maps). Valid values are: default (entire region); rect (rectangular
     * region); circle (circular region); and poly (polygonal region).
     * @return String
     */
    public String getShape() {
        if (this.shape != null) {
            return this.shape;
        }
        ValueExpression vb = getValueExpression("shape");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The shape of the hot spot on the screen (for use in client-side image
     * maps). Valid values are: default (entire region); rect (rectangular
     * region); circle (circular region); and poly (polygonal region).
     *
     * @see #getShape()
     * @param newShape shape
     */
    public void setShape(final String newShape) {
        this.shape = newShape;
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
     * The resource at the specified URL is displayed in the frame that is
     * specified with the target attribute. Values such as "_blank" that are
     * valid for the target attribute of a HTML anchor element are also valid
     * for this attribute in this component
     * @return String
     */
    public String getTarget() {
        if (this.target != null) {
            return this.target;
        }
        ValueExpression vb = getValueExpression("target");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The resource at the specified URL is displayed in the frame that is
     * specified with the target attribute. Values such as "_blank" that are
     * valid for the target attribute of a HTML anchor element are also valid
     * for this attribute in this component
     *
     * @see #getTarget()
     * @param newTarget target
     */
    public void setTarget(final String newTarget) {
        this.target = newTarget;
    }

    /**
     * The text to be displayed for the hyperlink.
     * @return Object
     */
    @Property(name = "text",
            displayName = "text",
            category = "Appearance",
            isDefault = true,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    public Object getText() {
        return getValue();
    }

    /**
     * The text to be displayed for the hyperlink.
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
     * The MIME content type of the resource specified by this component.
     * @return String
     */
    public String getType() {
        if (this.type != null) {
            return this.type;
        }
        ValueExpression vb = getValueExpression("type");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The MIME content type of the resource specified by this component.
     *
     * @see #getType()
     * @param newType type
     */
    public void setType(final String newType) {
        this.type = newType;
    }

    /**
     * Absolute, relative, or context relative (starting with "/") URL to the
     * resource selected by this hyperlink. If the URL attribute is specified,
     * clicking this hyperlink sends the browser to the new location. If the
     * action attribute is specified, the form is submitted. If both are
     * specified, the URL attribute takes precedence.
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
     * Absolute, relative, or context relative (starting with "/") URL to the
     * resource selected by this hyperlink. If the URL attribute is specified,
     * clicking this hyperlink sends the browser to the new location. If the
     * action attribute is specified, the form is submitted. If both are
     * specified, the URL attribute takes precedence.
     *
     * @see #getUrl()
     * @param newUrl URL
     */
    public void setUrl(final String newUrl) {
        this.url = newUrl;
    }

    /**
     * The language code of the resource designated by this hyperlink.
     * @return String
     */
    public String getUrlLang() {
        if (this.urlLang != null) {
            return this.urlLang;
        }
        ValueExpression vb = getValueExpression("urlLang");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The language code of the resource designated by this hyperlink.
     *
     * @see #getUrlLang()
     * @param newUrlLang urlLang
     */
    public void setUrlLang(final String newUrlLang) {
        this.urlLang = newUrlLang;
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
     * This implementation restores the state of all properties.
     * @param context faces context
     * @param state state object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.disabled = ((Boolean) values[1]);
        this.disabledSet = ((Boolean) values[2]);
        this.onBlur = (String) values[3];
        this.onClick = (String) values[4];
        this.onDblClick = (String) values[5];
        this.onFocus = (String) values[6];
        this.onKeyDown = (String) values[7];
        this.onKeyPress = (String) values[8];
        this.onKeyUp = (String) values[9];
        this.onMouseDown = (String) values[10];
        this.onMouseMove = (String) values[11];
        this.onMouseOut = (String) values[12];
        this.onMouseOver = (String) values[13];
        this.onMouseUp = (String) values[14];
        this.shape = (String) values[15];
        this.style = (String) values[16];
        this.styleClass = (String) values[17];
        this.tabIndex = ((Integer) values[18]);
        this.tabIndexSet = ((Boolean) values[19]);
        this.target = (String) values[20];
        this.toolTip = (String) values[21];
        this.type = (String) values[22];
        this.url = (String) values[23];
        this.urlLang = (String) values[24];
        this.visible = ((Boolean) values[25]);
        this.visibleSet = ((Boolean) values[26]);
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
        if (this.disabled) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.disabledSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        values[3] = this.onBlur;
        values[4] = this.onClick;
        values[5] = this.onDblClick;
        values[6] = this.onFocus;
        values[7] = this.onKeyDown;
        values[8] = this.onKeyPress;
        values[9] = this.onKeyUp;
        values[10] = this.onMouseDown;
        values[11] = this.onMouseMove;
        values[12] = this.onMouseOut;
        values[13] = this.onMouseOver;
        values[14] = this.onMouseUp;
        values[15] = this.shape;
        values[16] = this.style;
        values[17] = this.styleClass;
        values[18] = this.tabIndex;
        if (this.tabIndexSet) {
            values[19] = Boolean.TRUE;
        } else {
            values[19] = Boolean.FALSE;
        }
        values[20] = this.target;
        values[21] = this.toolTip;
        values[22] = this.type;
        values[23] = this.url;
        values[24] = this.urlLang;
        if (this.visible) {
            values[25] = Boolean.TRUE;
        } else {
            values[25] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[26] = Boolean.TRUE;
        } else {
            values[26] = Boolean.FALSE;
        }
        return values;
    }
}
