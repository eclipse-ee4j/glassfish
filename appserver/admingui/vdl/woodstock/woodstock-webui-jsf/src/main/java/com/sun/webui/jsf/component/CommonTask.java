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
import com.sun.webui.jsf.util.ComponentUtilities;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * The CommonTask component is used to create a single task within a
 * CommonTasksSection or CommonTasksGroup component.
 */
@Component(type = "com.sun.webui.jsf.CommonTask",
        family = "com.sun.webui.jsf.CommonTask",
        displayName = "Common Task",
        instanceName = "commonTask",
        tagName = "commonTask")
public final class CommonTask extends com.sun.webui.jsf.component.WebuiCommand
        implements NamingContainer {

    /**
     * Common Task facet identifier.
     */
    public static final String COMMONTASK_FACET = "taskAction";

    /**
     * Info panel facet identifier.
     */
    public static final String INFOPANEL_FACET = "infoPanel";

    /**
     * Info link facet identifier.
     */
    public static final String INFOLINK_FACET = "infoLink";

    /**
     * Disabled.
     */
    @Property(name = "disabled",
            displayName = "Disabled",
            isHidden = true,
            isAttribute = false)
    private boolean disabled = false;

    /**
     * disabled set flag.
     */
    private boolean disabledSet = false;

    /**
     * Specify a theme key for an image to be displayed in front of the text for
     * the task. The key {@code CTS_OVERVIEW} will generate an image that
     * can be used to mark tasks that are for overview information about the
     * task.
     */
    @Property(name = "icon",
            displayName = "icon",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.ThemeIconsEditor")
            //CHECKSTYLE:ON
    private String icon = null;

    /**
     * Specify the height in pixels of the image that is specified with the
     * imageUrl attribute.
     */
    @Property(name = "imageHeight",
            displayName = "imageHeight",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int imageHeight = Integer.MIN_VALUE;

    /**
     * imageHeight set flag.
     */
    private boolean imageHeightSet = false;

    /**
     * The path to an image to be displayed in front of the text for the task.
     * If both icon and imageUrl are provided, the icon takes precedence over
     * the path specified for the image.
     */
    @Property(name = "imageUrl",
            displayName = "imageURL",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.ImageUrlPropertyEditor")
            //CHECKSTYLE:ON
    private String imageUrl = null;

    /**
     * Specify the width in pixels of the image that is specified with the
     * imageUrl attribute.
     */
    @Property(name = "imageWidth",
            displayName = "imageWidth",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int imageWidth = Integer.MIN_VALUE;

    /**
     * imageWidth set flag.
     */
    private boolean imageWidthSet = false;

    /**
     * Immediate.
     */
    @Property(name = "immediate",
            displayName = "Immediate",
            isHidden = true,
            isAttribute = false)
    private boolean immediate = false;

    /**
     * immediate set flag.
     */
    private boolean immediateSet = false;

    /**
     * Specify the text for the link that is displayed at the bottom of the
     * task's information panel.
     */
    @Property(name = "infoLinkText",
            displayName = "infoLinkText",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String infoLinkText = null;

    /**
     * Specify the URL for the link that is displayed at the bottom of the
     * task's information panel.
     */
    @Property(name = "infoLinkUrl",
            displayName = "infoLinkUrl",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.SunWebUrlPropertyEditor")
            //CHECKSTYLE:ON
    private String infoLinkUrl = null;

    /**
     * Specify the text to be displayed in the information panel for this task.
     */
    @Property(name = "infoText",
            displayName = "infoText",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String infoText = null;

    /**
     * Specify the title text to be displayed in the information panel for this
     * task.
     */
    @Property(name = "infoTitle",
            displayName = "infoTitle",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String infoTitle = null;

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
     * Scripting code executed when the user moves the mouse pointer while it is
     * over the component.
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
     * The resource at the specified URL is displayed in the frame that is
     * specified with the target attribute. Values such as "_blank" that are
     * valid for the target attribute of a HTML anchor element are also valid
     * for this attribute in this component.
     */
    @Property(name = "target",
            displayName = "Target",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.FrameTargetsEditor")
            //CHECKSTYLE:ON
    private String target = null;

    /**
     * Title.
     */
    @Property(name = "title",
            displayName = "Title",
            isHidden = true,
            isAttribute = false)
    private String title = null;

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool-tip if the mouse cursor hovers over the HTML
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
     * Creates a new instance of Task.
     */
    public CommonTask() {
        super();
        setRendererType("com.sun.webui.jsf.CommonTask");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.CommonTask";
    }

    // Hide value
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
     * Indicates that activation of this component by the user is not currently
     * permitted.
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
     * permitted.
     * @param newDisabled disabled
     */
    public void setDisabled(final boolean newDisabled) {
        this.disabled = newDisabled;
        this.disabledSet = true;
    }

    /**
     * Specify a theme key for an image to be displayed in front of the text for
     * the task. The key {@code CTS_OVERVIEW} will generate an image that
     * can be used to mark tasks that are for overview information about the
     * task.
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
     * Specify a theme key for an image to be displayed in front of the text for
     * the task. The key {@code CTS_OVERVIEW} will generate an image that
     * can be used to mark tasks that are for overview information about the
     * task.
     *
     * @see #getIcon()
     * @param newIcon icon
     */
    public void setIcon(final String newIcon) {
        this.icon = newIcon;
    }

    /**
     * Specify the height in pixels of the image that is specified with the
     * imageUrl attribute.
     * @return int
     */
    public int getImageHeight() {
        if (this.imageHeightSet) {
            return this.imageHeight;
        }
        ValueExpression vb = getValueExpression("imageHeight");
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
     * Specify the height in pixels of the image that is specified with the
     * imageUrl attribute.
     *
     * @see #getImageHeight()
     * @param newImageHeight imageHeight
     */
    public void setImageHeight(final int newImageHeight) {
        this.imageHeight = newImageHeight;
        this.imageHeightSet = true;
    }

    /**
     * The path to an image to be displayed in front of the text for the task.
     * If both icon and imageUrl are provided, the icon takes precedence over
     * the path specified for the image.
     * @return String
     */
    public String getImageUrl() {
        if (this.imageUrl != null) {
            return this.imageUrl;
        }
        ValueExpression vb = getValueExpression("imageUrl");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The path to an image to be displayed in front of the text for the task.
     * If both icon and imageUrl are provided, the icon takes precedence over
     * the path specified for the image.
     *
     * @see #getImageUrl()
     * @param newImageUrl imageUrl
     */
    public void setImageUrl(final String newImageUrl) {
        this.imageUrl = newImageUrl;
    }

    /**
     * Specify the width in pixels of the image that is specified with the
     * imageUrl attribute.
     * @return int
     */
    public int getImageWidth() {
        if (this.imageWidthSet) {
            return this.imageWidth;
        }
        ValueExpression vb = getValueExpression("imageWidth");
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
     * Specify the width in pixels of the image that is specified with the
     * imageUrl attribute.
     *
     * @see #getImageWidth()
     * @param newImageWidth imageWidth
     */
    public void setImageWidth(final int newImageWidth) {
        this.imageWidth = newImageWidth;
        this.imageWidthSet = true;
    }

    @Override
    public boolean isImmediate() {
        if (this.immediateSet) {
            return this.immediate;
        }
        ValueExpression vb = getValueExpression("immediate");
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

    @Override
    public void setImmediate(final boolean newImmediate) {
        this.immediate = newImmediate;
        this.immediateSet = true;
    }

    /**
     * Specify the text for the link that is displayed at the bottom of the
     * task's information panel.
     * @return String
     */
    public String getInfoLinkText() {
        if (this.infoLinkText != null) {
            return this.infoLinkText;
        }
        ValueExpression vb = getValueExpression("infoLinkText");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Specify the text for the link that is displayed at the bottom of the
     * task's information panel.
     *
     * @see #getInfoLinkText()
     * @param newInfoLinkText infoLinkText
     */
    public void setInfoLinkText(final String newInfoLinkText) {
        this.infoLinkText = newInfoLinkText;
    }

    /**
     * Specify the URL for the link that is displayed at the bottom of the
     * task's information panel.
     * @return String
     */
    public String getInfoLinkUrl() {
        if (this.infoLinkUrl != null) {
            return this.infoLinkUrl;
        }
        ValueExpression vb = getValueExpression("infoLinkUrl");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Specify the URL for the link that is displayed at the bottom of the
     * task's information panel.
     *
     * @see #getInfoLinkUrl()
     * @param newInfoLinkUrl infoLinkUrl
     */
    public void setInfoLinkUrl(final String newInfoLinkUrl) {
        this.infoLinkUrl = newInfoLinkUrl;
    }

    /**
     * Specify the text to be displayed in the information panel for this task.
     * @return String
     */
    public String getInfoText() {
        if (this.infoText != null) {
            return this.infoText;
        }
        ValueExpression vb = getValueExpression("infoText");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Specify the text to be displayed in the information panel for this task.
     *
     * @see #getInfoText()
     * @param newInfoText infoText
     */
    public void setInfoText(final String newInfoText) {
        this.infoText = newInfoText;
    }

    /**
     * Specify the title text to be displayed in the information panel for this
     * task.
     * @return String
     */
    public String getInfoTitle() {
        if (this.infoTitle != null) {
            return this.infoTitle;
        }
        ValueExpression vb = getValueExpression("infoTitle");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Specify the title text to be displayed in the information panel for this
     * task.
     *
     * @see #getInfoTitle()
     * @param newInfoTitle infoTitle
     */
    public void setInfoTitle(final String newInfoTitle) {
        this.infoTitle = newInfoTitle;
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
     * Scripting code executed when the user moves the mouse pointer while it is
     * over the component.
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
     * Scripting code executed when the user moves the mouse pointer while it is
     * over the component.
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
     * for this attribute in this component.
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
     * for this attribute in this component.
     *
     * @see #getTarget()
     * @param newTarget target
     */
    public void setTarget(final String newTarget) {
        this.target = newTarget;
    }

    /**
     * The text to be displayed for the task.
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
     * The text to be displayed for the task.
     *
     * @see #getText()
     * @param text text
     */
    public void setText(final Object text) {
        setValue(text);
    }

    /**
     * The title.
     * @return String
     */
    public String getTitle() {
        if (this.title != null) {
            return this.title;
        }
        ValueExpression vb = getValueExpression("title");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The title.
     * @param newTitle title
     */
    public void setTitle(final String newTitle) {
        this.title = newTitle;
    }

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool-tip if the mouse cursor hovers over the HTML
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
     * text will display as a tool-tip if the mouse cursor hovers over the HTML
     * element.
     *
     * @see #getToolTip()
     * @param newToolTip tool-tip
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
        this.disabled = ((Boolean) values[1]);
        this.disabledSet = ((Boolean) values[2]);
        this.icon = (String) values[3];
        this.imageHeight = ((Integer) values[4]);
        this.imageHeightSet = ((Boolean) values[5]);
        this.imageUrl = (String) values[6];
        this.imageWidth = ((Integer) values[7]);
        this.imageWidthSet = ((Boolean) values[8]);
        this.immediate = ((Boolean) values[9]);
        this.immediateSet = ((Boolean) values[10]);
        this.infoLinkText = (String) values[11];
        this.infoLinkUrl = (String) values[12];
        this.infoText = (String) values[13];
        this.infoTitle = (String) values[14];
        this.onBlur = (String) values[15];
        this.onClick = (String) values[16];
        this.onDblClick = (String) values[17];
        this.onFocus = (String) values[18];
        this.onKeyDown = (String) values[19];
        this.onKeyPress = (String) values[20];
        this.onKeyUp = (String) values[21];
        this.onMouseDown = (String) values[22];
        this.onMouseMove = (String) values[23];
        this.onMouseOut = (String) values[24];
        this.onMouseOver = (String) values[25];
        this.onMouseUp = (String) values[26];
        this.style = (String) values[27];
        this.styleClass = (String) values[28];
        this.tabIndex = ((Integer) values[29]);
        this.tabIndexSet = ((Boolean) values[30]);
        this.target = (String) values[31];
        this.title = (String) values[32];
        this.toolTip = (String) values[33];
        this.visible = ((Boolean) values[34]);
        this.visibleSet = ((Boolean) values[35]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[36];
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
        values[3] = this.icon;
        values[4] = this.imageHeight;
        if (this.imageHeightSet) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        values[6] = this.imageUrl;
        values[7] = this.imageWidth;
        if (this.imageHeightSet) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] = Boolean.FALSE;
        }
        if (this.immediate) {
            values[9] = Boolean.TRUE;
        } else {
            values[9] = Boolean.FALSE;
        }
        if (this.immediateSet) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        values[11] = this.infoLinkText;
        values[12] = this.infoLinkUrl;
        values[13] = this.infoText;
        values[14] = this.infoTitle;
        values[15] = this.onBlur;
        values[16] = this.onClick;
        values[17] = this.onDblClick;
        values[18] = this.onFocus;
        values[19] = this.onKeyDown;
        values[20] = this.onKeyPress;
        values[21] = this.onKeyUp;
        values[22] = this.onMouseDown;
        values[23] = this.onMouseMove;
        values[24] = this.onMouseOut;
        values[25] = this.onMouseOver;
        values[26] = this.onMouseUp;
        values[27] = this.style;
        values[28] = this.styleClass;
        values[29] = this.tabIndex;
        if (this.tabIndexSet) {
            values[30] = Boolean.TRUE;
        } else {
            values[30] = Boolean.FALSE;
        }
        values[31] = this.target;
        values[32] = this.title;
        values[33] = this.toolTip;
        if (this.visible) {
            values[34] = Boolean.TRUE;
        } else {
            values[34] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[35] = Boolean.TRUE;
        } else {
            values[35] = Boolean.FALSE;
        }
        return values;
    }

    /**
     * Returns a component that represents the action element for the
     * {@link CommonTask}. When the user clicks on this action element, it takes
     * him to the corresponding task. In the default case, when no facet is
     * specified, an hyperlink is created, with the default styles applied to it
     * and is returned back to the invoking function.
     *
     * @return The commonTask action component.
     */
    public UIComponent getTaskAction() {
        UIComponent comp = getFacet(COMMONTASK_FACET);
        if (comp != null) {
            return comp;
        }
        return null;
    }

    /**
     * Checks whether a facet has been specified for the
     * {@link com.sun.webui.jsf.component.Hyperlink} inside the info panel. If
     * not, it checks whether the infoLinkUrl and infoLinkText attributes have
     * been specified. If these attributes of the component have been specified,
     * it creates an hyperlink with these attributes. Otherwise, it returns null
     *
     * @return The hyperlink present at the bottom of the info panel.
     */
    public UIComponent getInfoLink() {
        UIComponent comp = getFacet(INFOLINK_FACET);
        if (comp != null) {
            return comp;
        }
        comp = ComponentUtilities.getPrivateFacet(this, INFOLINK_FACET, true);
        if (getInfoLinkText() != null && getInfoLinkUrl() != null) {
            if (comp == null) {
                Hyperlink link = new Hyperlink();
                link.setId(ComponentUtilities
                        .createPrivateFacetId(this, INFOLINK_FACET));
                ComponentUtilities.putPrivateFacet(this, INFOLINK_FACET, link);
                comp = link;
            }
            try {
                Hyperlink link = (Hyperlink) comp;
                link.setUrl(getInfoLinkUrl());
                link.setTarget("_blank");
                link.setText(getInfoLinkText());
            } catch (ClassCastException e) {
                // The comp object did not contain a hyperlink.
            }
        }
        return comp;
    }

    /**
     * Checks whether a facet has been specified for the infoPanel. FIXME: Is it
     * possible to create the default info panel here instead of doing it in the
     * renderer. There is a lot of HTML to be generated between the components
     * present in the panel which is making the creation of this facet over here
     * impossible.
     *
     * @return A component which represents the info panel for the common task.
     */
    public UIComponent getInfoPanel() {
        UIComponent comp = getFacet(INFOPANEL_FACET);
        return comp;
    }
}
