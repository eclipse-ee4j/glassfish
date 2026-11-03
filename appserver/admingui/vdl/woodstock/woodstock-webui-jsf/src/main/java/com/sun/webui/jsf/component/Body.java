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
import com.sun.webui.jsf.util.FocusManager;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;

/**
 * The Body component is used to contain the other components of the page.
 */
@Component(type = "com.sun.webui.jsf.Body",
        family = "com.sun.webui.jsf.Body",
        displayName = "Body",
        tagName = "body",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_body",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_body_props")
        //CHECKSTYLE:ON
public final class Body extends UIComponentBase {

    /**
     * @deprecated
     */
    public static final String FOCUS_PARAM
            = "com.sun.webui.jsf_body_focusComponent";

    /**
     * @deprecated
     */
    public static final String JAVASCRIPT_OBJECT = "_jsObject";

    /**
     * Specify the ID of the component that should receive focus when the page
     * is loaded. If the focus attribute is not set, or if the value is null, no
     * component has focus when the page is initially rendered. If the page is
     * submitted and reloaded, the component that submitted the page receives
     * focus. By setting the focus attribute, you can ensure that a particular
     * component receives focus each time.
     */
    @Property(name = "focus",
            displayName = "Component to receive focus",
            category = "Behavior",
            isDefault = true,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.EventClientIdsEditor")
            //CHECKSTYLE:ON
    private String focus = null;

    /**
     * The path to an image to be used as a background for the page.
     */
    @Property(name = "imageURL",
            displayName = "Image URL",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.ImageUrlPropertyEditor")
            //CHECKSTYLE:ON
    private String imageURL = null;

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
     * Scripting code executed when when this page is loaded in a browser.
     */
    @Property(name = "onLoad",
            displayName = "Onload Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onLoad = null;

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
     * Scripting code executed when this page is unloaded from a browser as a
     * user exits the page.
     */
    @Property(name = "onUnload",
            displayName = "Unload Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onUnload = null;

    /**
     * Use the preserveFocus attribute to indicate whether the last element to
     * have the focus, receives the focus the next time the page is rendered. If
     * set to {@code true} the focus is preserved; if set to
     * {@code false} it is not. If set to {@code false} and the
     * {@code focus} attribute is set then the element identified by that
     * id will receive the focus. The default value is true.
     */
    @Property(name = "preserveFocus",
            displayName = "Preserve Focus",
            category = "Behavior")
    private boolean preserveFocus = true;

    /**
     * preserveFocus set flag.
     */
    private boolean preserveFocusSet = false;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
            //CHECKSTYLE:OFF
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
    public Body() {
        super();
        setRendererType("com.sun.webui.jsf.Body");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Body";
    }

    /**
     * @deprecated @see #getFocus
     * @param context faces context
     * @return String
     */
    public String getFocusID(final FacesContext context) {

        // Note that this code is duplicated in
        // Body renderer because we don't want to
        // reference a deprecated method.
        String id = getFocus();
        if (id != null || id.length() == 0) {
            // Need absolute id.
            String absid = id;
            char separatorChar = UINamingContainer.getSeparatorChar(context);

            if (id.charAt(0) != separatorChar) {
                absid = String.valueOf(separatorChar).concat(id);
            }
            try {
                // Since a developer using setFocus may not be able to
                // identify a sub component of a ComplexComponent, that
                // must be done here.
                UIComponent comp = findComponent(absid);
                if (comp != null && comp instanceof ComplexComponent) {
                    id = ((ComplexComponent) comp).getFocusElementId(context);
                }
            } catch (Exception e) {
                if (LogUtil.finestEnabled()) {
                    LogUtil.finest("Body.getFocusId, couldn't find "
                            + " component with id " + id);
                }
            }
        } else {
            // Get client id cached in request map -- bugtraq #6316565.
            // Note: This must be a client Id to identify table children.
            // This interface is expected to be the actual id and does
            // not require checking for ComplexComponent.
            id = FocusManager.getRequestFocusElementId(context);
        }
        return id;
    }

    /**
     * @deprecated
     *
     * @param context faces context
     * @return String
     */
    public String getJavaScriptObjectName(final FacesContext context) {
        return getClientId(context).replace(':', '_').concat(JAVASCRIPT_OBJECT);
    }

    /**
     * The component identifier for this component. This value must be unique
     * within the closest parent component that is a naming container.
     * @param id id
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
     * @param rendered rendered
     */
    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    /**
     * Specify the ID of the component that should receive focus when the page
     * is loaded. If the focus attribute is not set, or if the value is null, no
     * component has focus when the page is initially rendered. If the page is
     * submitted and reloaded, the component that submitted the page receives
     * focus. By setting the focus attribute, you can ensure that a particular
     * component receives focus each time.
     * @return String
     */
    public String getFocus() {
        if (this.focus != null) {
            return this.focus;
        }
        ValueExpression vb = getValueExpression("focus");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Specify the ID of the component that should receive focus when the page
     * is loaded. If the focus attribute is not set, or if the value is null, no
     * component has focus when the page is initially rendered. If the page is
     * submitted and reloaded, the component that submitted the page receives
     * focus. By setting the focus attribute, you can ensure that a particular
     * component receives focus each time.
     *
     * @see #getFocus()
     * @param newFocus focus
     */
    public void setFocus(final String newFocus) {
        this.focus = newFocus;
    }

    /**
     * The path to an image to be used as a background for the page.
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
     * The path to an image to be used as a background for the page.
     *
     * @see #getImageURL()
     * @param newImageURL imageURL
     */
    public void setImageURL(final String newImageURL) {
        this.imageURL = newImageURL;
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
     * Scripting code executed when when this page is loaded in a browser.
     * @return String
     */
    public String getOnLoad() {
        if (this.onLoad != null) {
            return this.onLoad;
        }
        ValueExpression vb = getValueExpression("onLoad");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when when this page is loaded in a browser.
     *
     * @see #getOnLoad()
     * @param newOnLoad onLoad
     */
    public void setOnLoad(final String newOnLoad) {
        this.onLoad = newOnLoad;
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
     * Scripting code executed when this page is unloaded from a browser as a
     * user exits the page.
     * @return String
     */
    public String getOnUnload() {
        if (this.onUnload != null) {
            return this.onUnload;
        }
        ValueExpression vb = getValueExpression("onUnload");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when this page is unloaded from a browser as a
     * user exits the page.
     *
     * @see #getOnUnload()
     * @param newOnUnload onUnload
     */
    public void setOnUnload(final String newOnUnload) {
        this.onUnload = newOnUnload;
    }

    /**
     * Use the preserveFocus attribute to indicate whether the last element to
     * have the focus, receives the focus the next time the page is rendered. If
     * set to {@code true} the focus is preserved; if set to
     * {@code false} it is not. If set to {@code false} and the
     * {@code focus} attribute is set then the element identified by that
     * id will receive the focus. The default value is true.
     * @return {@code boolean}
     */
    public boolean isPreserveFocus() {
        if (this.preserveFocusSet) {
            return this.preserveFocus;
        }
        ValueExpression vb = getValueExpression("preserveFocus");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        // Get the default behavior from the theme.
        String defaultPreserveFocus;
        Theme theme = ThemeUtilities
                .getTheme(FacesContext.getCurrentInstance());
        try {
            defaultPreserveFocus = theme.getMessage("body.preserveFocus");
            return Boolean.parseBoolean(defaultPreserveFocus);
        } catch (Exception e) {
        }
        return this.preserveFocus;
    }

    /**
     * Use the preserveFocus attribute to indicate whether the last element to
     * have the focus, receives the focus the next time the page is rendered. If
     * set to {@code true} the focus is preserved; if set to
     * {@code false} it is not. If set to {@code false} and the
     * {@code focus} attribute is set then the element identified by that
     * id will receive the focus. The default value is true.
     * @param newPreserveFocus preserveFocus
     */
    public void setPreserveFocus(final boolean newPreserveFocus) {
        this.preserveFocus = newPreserveFocus;
        this.preserveFocusSet = true;
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
        this.focus = (String) values[1];
        this.imageURL = (String) values[2];
        this.onBlur = (String) values[3];
        this.onClick = (String) values[4];
        this.onDblClick = (String) values[5];
        this.onFocus = (String) values[6];
        this.onKeyDown = (String) values[7];
        this.onKeyPress = (String) values[8];
        this.onKeyUp = (String) values[9];
        this.onLoad = (String) values[10];
        this.onMouseDown = (String) values[11];
        this.onMouseMove = (String) values[12];
        this.onMouseOut = (String) values[13];
        this.onMouseOver = (String) values[14];
        this.onMouseUp = (String) values[15];
        this.onUnload = (String) values[16];
        this.style = (String) values[17];
        this.styleClass = (String) values[18];
        this.visible = ((Boolean) values[19]);
        this.visibleSet = ((Boolean) values[20]);
        this.preserveFocus = ((Boolean) values[21]);
        this.preserveFocusSet = ((Boolean) values[22]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[23];
        values[0] = super.saveState(context);
        values[1] = this.focus;
        values[2] = this.imageURL;
        values[3] = this.onBlur;
        values[4] = this.onClick;
        values[5] = this.onDblClick;
        values[6] = this.onFocus;
        values[7] = this.onKeyDown;
        values[8] = this.onKeyPress;
        values[9] = this.onKeyUp;
        values[10] = this.onLoad;
        values[11] = this.onMouseDown;
        values[12] = this.onMouseMove;
        values[13] = this.onMouseOut;
        values[14] = this.onMouseOver;
        values[15] = this.onMouseUp;
        values[16] = this.onUnload;
        values[17] = this.style;
        values[18] = this.styleClass;
        if (this.visible) {
            values[19] = Boolean.TRUE;
        } else {
            values[19] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[20] = Boolean.TRUE;
        } else {
            values[20] = Boolean.FALSE;
        }
        if (this.preserveFocus) {
            values[21] = Boolean.TRUE;
        } else {
            values[21] = Boolean.FALSE;
        }
        if (this.preserveFocusSet) {
            values[22] = Boolean.TRUE;
        } else {
            values[22] = Boolean.FALSE;
        }
        return values;
    }
}
