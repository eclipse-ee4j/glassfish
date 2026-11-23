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
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.util.ComponentUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;

/**
 * The Alert component is used to render an inline alert message.
 */
@Component(type = "com.sun.webui.jsf.Alert",
        family = "com.sun.webui.jsf.Alert",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_alert",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_alert_props")
        //CHECKSTYLE:ON
public final class Alert extends UIOutput implements NamingContainer {

    /**
     * Facet name for alert image.
     */
    public static final String ALERT_IMAGE_FACET = "alertImage";

    /**
     * Facet name for the alert link.
     */
    public static final String ALERT_LINK_FACET = "alertLink";

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
    private String alt;

    /**
     * Optional detailed message text for the alert. This message might include
     * more information about the alert and instructions for what to do about
     * the alert.
     */
    @Property(name = "detail",
            displayName = "Detail Message",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String detail;

    /**
     * The linkActionExpression attribute is used to specify the action to take
     * when the embedded hyperlink component is activated by the user. The value
     * of the linkActionExpression attribute must be one of the following: <br>
     * <ul>
     * <li>
     * an outcome string, used to indicate which page to display next, as
     * defined by a navigation rule in the application configuration resource
     * file{@code (faces-config.xml)}.
     * </li>
     * <li>a JavaServer Faces EL expression that resolves to a backing bean
     * method. The method must take no parameters and return an outcome string.
     * The class that defines the method must implement the<br>
     * {@code java.io.Serializable} interface or
     * {@code jakarta.faces.component.StateHolder} interface.</li>
     * </ul>
     */
    @Property(name = "linkActionExpression",
            isHidden = true,
            displayName = "Link Action Method Expression",
            category = "Navigation")
    @Property.Method(signature = "java.lang.String action()")
    private jakarta.el.MethodExpression linkActionExpression;

    /**
     * The window (target) in which to load the link that is specified with
     * linkText.
     */
    @Property(name = "linkTarget",
            displayName = "Link Target",
            category = "Navigation",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.FrameTargetsEditor")
            //CHECKSTYLE:ON
    private String linkTarget;

    /**
     * The text for an optional link that is appended to the detail message.
     */
    @Property(name = "linkText",
            displayName = "Link Text",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String linkText;

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the link
     * that is specified with linkText.
     */
    @Property(name = "linkToolTip",
            displayName = "Link Tooltip",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String linkToolTip;

    /**
     * Absolute, relative, or context relative (starting with "/") URL to the
     * resource to navigate to when the link that is specified with linkText is
     * selected.
     */
    @Property(name = "linkURL",
            displayName = "Link URL",
            category = "Navigation",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.SunWebUrlPropertyEditor")
            //CHECKSTYLE:ON
    private String linkURL;

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
     * Summary message text for the alert. This brief message is prominently
     * displayed next to the icon.
     */
    @Property(name = "summary",
            displayName = "Summary Message",
            category = "Appearance",
            isDefault = true,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String summary;

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
     * The type or category of alert. This type can be set to "information",
     * "success", "warning" or "error". The type specified determines which icon
     * is rendered for the alert.
     */
    @Property(name = "type",
            displayName = "Alert Type",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.AlertTypesEditor")
            //CHECKSTYLE:ON
    private String type;

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     */
    @Property(name = "visible", displayName = "Visible", category = "Behavior")
    private boolean visible;

    /**
     * visible set flag.
     */
    private boolean visibleSet;

    /**
     * Default Constructor.
     */
    public Alert() {
        super();
        setRendererType("com.sun.webui.jsf.Alert");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Alert";
    }

    /**
     * Return a component that implements an alert image. If a facet named
     * {@code alertImage} is found that component is returned. If a facet is not
     * found an {@code Icon} component instance is returned with the id
     * {@code alertImage}. The {@code Icon} instance is initialized with the
     * values from
     * <ul>
     * <li>{@code getType()} if a valid type, else
     * {@code ThemeImages.ALERT_ERROR_LARGE}</li>
     * <li>{@code getAlt()} if not null</li>
     * </ul>
     *
     *
     * If a facet is not defined then the returned {@code Icon} component is
     * created every time this method is called.
     *
     * @return alertImage facet or an Icon instance
     */
    public UIComponent getAlertIcon() {

        UIComponent imageFacet = getFacet(ALERT_IMAGE_FACET);
        if (imageFacet != null) {
            return imageFacet;
        }

        // No need to save it as a facet.
        // Make sure to set parent, for clientId.
        Theme theme = ThemeUtilities
                .getTheme(FacesContext.getCurrentInstance());
        String iconidentifier = getIconIdentifier();
        Icon icon = ThemeUtilities.getIcon(theme, iconidentifier);

        icon.setId(ALERT_IMAGE_FACET);
        icon.setParent(this);

        String iconalt = getAlt();
        if (iconalt != null) {
            icon.setAlt(iconalt);
        }
        return icon;
    }

    /**
     * Return a component that implements an alert link. If a facet named
     * {@code alertLink} is found that component is returned. If a facet is not
     * defined and {@code getLinkText()} returns non null, an
     * {@code IconHyperlink} component instance is created with the id
     * {@code alertLink} and initialized with the values from:
     * <ul>
     * <li>{@code getLinkTarget()}</li>
     * <li>{@code getLinkText()}</li>
     * <li>{@code getLinkToolTip()}</li>
     * <li>{@code getLinkURL()}</li>
     * <li>{@code getLinkAction()}</li>
     * <li>{@code ThemeImages.HREF_LINK}</li>
     * </ul>
     * If {@code getLinkText()} returns null, null is returned.
     *
     * If a facet is not defined and if a {@code IconHyperlink} component is
     * created, it is reinitialized every time this method is called.
     *
     * @return alertLink facet or a IconHyperlink instance or null
     */
    public UIComponent getAlertLink() {
        UIComponent linkFacet = getFacet(ALERT_LINK_FACET);
        if (linkFacet != null) {
            return linkFacet;
        }

        // Get the private facet.
        IconHyperlink h = (IconHyperlink) ComponentUtilities.getPrivateFacet(
                this, ALERT_LINK_FACET, false);

        // If getLinkText() returns null, null is returned.
        String linktext = getLinkText();
        if (linktext == null) {
            // Remove the previously created private facet.
            if (h != null) {
                ComponentUtilities.removePrivateFacet(this, ALERT_LINK_FACET);
            }
            return null;
        }

        // Return the private facet or create one, but initialize
        // it every time, except for the id.
        if (h == null) {
            h = new IconHyperlink();
            h.setId(ALERT_LINK_FACET);
            ComponentUtilities.putPrivateFacet(this, ALERT_LINK_FACET, h);
        }

        h.setIcon(ThemeImages.HREF_LINK);
        h.setTarget(getLinkTarget());
        h.setText(linktext);
        h.setToolTip(getLinkToolTip());
        h.setUrl(getLinkURL());

        MethodExpression action = getLinkActionExpression();
        if (action != null) {
            h.setActionExpression(action);
        }

        return h;
    }

    /**
     * Get the icon image for the alert type.
     *
     * @return String
     */
    private String getIconIdentifier() {
        String alertType = getType();
        if (alertType != null) {
            String lower = alertType.toLowerCase();
            if (lower.startsWith("warn")) {
                return ThemeImages.ALERT_WARNING_LARGE;
            } else if (lower.startsWith("ques")) {
                return ThemeImages.ALERT_HELP_LARGE;
            } else if (lower.startsWith("info")) {
                return ThemeImages.ALERT_INFO_LARGE;
            } else if (lower.startsWith("succ")) {
                return ThemeImages.ALERT_SUCCESS_LARGE;
            }
        }
        return ThemeImages.ALERT_ERROR_LARGE;
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

    // Hide converter
    @Property(name = "converter", isHidden = true, isAttribute = false)
    @Override
    public Converter getConverter() {
        return super.getConverter();
    }

    // Hide value
    @Property(name = "value", isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    /**
     * Alternative textual description of the image rendered by this
     * component.The alt text can be used by screen readers and in tool tips,
     * and when image display is turned off in the web browser.
     *
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
     * Alternative textual description of the image rendered by this
     * component.The alt text can be used by screen readers and in tool tips,
     * and when image display is turned off in the web browser.
     *
     * @param newAlt alternative text
     * @see #getAlt()
     */
    public void setAlt(final String newAlt) {
        this.alt = newAlt;
    }

    /**
     * Optional detailed message text for the alert.This message might include
     * more information about the alert and instructions for what to do about
     * the alert.
     *
     * @return String
     */
    public String getDetail() {
        if (this.detail != null) {
            return this.detail;
        }
        ValueExpression vb = getValueExpression("detail");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Optional detailed message text for the alert.This message might include
     * more information about the alert and instructions for what to do about
     * the alert.
     *
     * @param newDetails detailed message
     * @see #getDetail()
     */
    public void setDetail(final String newDetails) {
        this.detail = newDetails;
    }

    /**
     * The linkActionExpression attribute is used to specify the action to take
     * when the embedded hyperlink component is activated by the user.The value
     * of the linkActionExpression attribute must be one of the following:
     * <ul>
     * <li>
     * an outcome string, used to indicate which page to display next, as
     * defined by a navigation rule in the application configuration resource
     * file{@code (faces-config.xml)}.</li>
     * <li>a JavaServer Faces EL expression that resolves to a backing bean
     * method. The method must take no parameters and return an outcome string.
     * The class that defines the method must implement the<br>
     * {@code java.io.Serializable} interface or
     * {@code jakarta.faces.component.StateHolder} interface.</li>
     * </ul>
     *
     * @return {@code jakarta.el.MethodExpression}
     */
    public jakarta.el.MethodExpression getLinkActionExpression() {
        if (this.linkActionExpression != null) {
            return this.linkActionExpression;
        }
        ValueExpression vb = getValueExpression("linkActionExpression");
        if (vb != null) {
            return (jakarta.el.MethodExpression) vb
                    .getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The linkActionExpression attribute is used to specify the action to take
     * when the embedded hyperlink component is activated by the user.The value
     * of the linkActionExpression attribute must be one of the following: <br>
     * <ul>
     * <li>
     * an outcome string, used to indicate which page to display next, as
     * defined by a navigation rule in the application configuration resource
     * file{@code (faces-config.xml)}.</li>
     * <li>a JavaServer Faces EL expression that resolves to a backing bean
     * method. The method must take no parameters and return an outcome string.
     * The class that defines the method must implement the<br>
     * {@code java.io.Serializable} interface or
     * {@code jakarta.faces.component.StateHolder} interface.</li>
     * </ul>
     *
     * @param newLinkActionExpression method expression
     * @see #getLinkActionExpression()
     */
    public void setLinkActionExpression(
            final jakarta.el.MethodExpression newLinkActionExpression) {

        this.linkActionExpression = newLinkActionExpression;
    }

    /**
     * The window (target) in which to load the link that is specified with
     * linkText.
     *
     * @return String
     */
    public String getLinkTarget() {
        if (this.linkTarget != null) {
            return this.linkTarget;
        }
        ValueExpression vb = getValueExpression("linkTarget");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The window (target) in which to load the link that is specified with
     * linkText.
     *
     * @param newLinkTarget linkTarget
     * @see #getLinkTarget()
     */
    public void setLinkTarget(final String newLinkTarget) {
        this.linkTarget = newLinkTarget;
    }

    /**
     * The text for an optional link that is appended to the detail message.
     *
     * @return String
     */
    public String getLinkText() {
        if (this.linkText != null) {
            return this.linkText;
        }
        ValueExpression vb = getValueExpression("linkText");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The text for an optional link that is appended to the detail message.
     *
     * @param newLinkText linkText
     * @see #getLinkText()
     */
    public void setLinkText(final String newLinkText) {
        this.linkText = newLinkText;
    }

    /**
     * Sets the value of the title attribute for the HTML element.The specified
     * text will display as a tool tip if the mouse cursor hovers over the link
     * that is specified with linkText.
     *
     * @return String
     */
    public String getLinkToolTip() {
        if (this.linkToolTip != null) {
            return this.linkToolTip;
        }
        ValueExpression vb = getValueExpression("linkToolTip");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Sets the value of the title attribute for the HTML element.The specified
     * text will display as a tool tip if the mouse cursor hovers over the link
     * that is specified with linkText.
     *
     * @param newLinkToolTip linkToolTip
     * @see #getLinkToolTip()
     */
    public void setLinkToolTip(final String newLinkToolTip) {
        this.linkToolTip = newLinkToolTip;
    }

    /**
     * Absolute, relative, or context relative (starting with "/") URL to the
     * resource to navigate to when the link that is specified with linkText is
     * selected.
     *
     * @return String
     */
    public String getLinkURL() {
        if (this.linkURL != null) {
            return this.linkURL;
        }
        ValueExpression vb = getValueExpression("linkURL");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Absolute, relative, or context relative (starting with "/") URL to the
     * resource to navigate to when the link that is specified with linkText is
     * selected.
     *
     * @param newLinkURL linkURL
     * @see #getLinkURL()
     */
    public void setLinkURL(final String newLinkURL) {
        this.linkURL = newLinkURL;
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
     * @param newStyle style
     * @see #getStyle()
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
     * @param newStyleClass styleClass
     * @see #getStyleClass()
     */
    public void setStyleClass(final String newStyleClass) {
        this.styleClass = newStyleClass;
    }

    /**
     * Summary message text for the alert.This brief message is prominently
     * displayed next to the icon.
     *
     * @return String
     */
    public String getSummary() {
        if (this.summary != null) {
            return this.summary;
        }
        ValueExpression vb = getValueExpression("summary");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Summary message text for the alert. This brief message is prominently
     * displayed next to the icon.
     *
     * @see #getSummary()
     * @param newSummary summary
     */
    public void setSummary(final String newSummary) {
        this.summary = newSummary;
    }

    /**
     * Position of this element in the tabbing order of the current
     * document.Tabbing order determines the sequence in which elements receive
     * focus when the tab key is pressed. The value must be an integer between 0
     * and 32767.
     *
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
     * Position of this element in the tabbing order of the current
     * document.Tabbing order determines the sequence in which elements receive
     * focus when the tab key is pressed. The value must be an integer between 0
     * and 32767.
     *
     * @param newTabIndex tabIndex
     * @see #getTabIndex()
     */
    public void setTabIndex(final int newTabIndex) {
        this.tabIndex = newTabIndex;
        this.tabIndexSet = true;
    }

    /**
     * The type or category of alert. This type can be set to "information",
     * "success", "warning" or "error". The type specified determines which icon
     * is rendered for the alert.
     *
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
     * The type or category of alert. This type can be set to "information",
     * "success", "warning" or "error". The type specified determines which icon
     * is rendered for the alert.
     *
     * @see #getType()
     * @param newType type
     */
    public void setType(final String newType) {
        this.type = newType;
    }

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page.If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     *
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
     * viewable by the user in the rendered HTML page.If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     *
     * @param newVisible visible
     * @see #isVisible()
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
        this.detail = (String) values[2];
        this.linkActionExpression = (jakarta.el.MethodExpression)
                restoreAttachedState(context, values[3]);
        this.linkTarget = (String) values[4];
        this.linkText = (String) values[5];
        this.linkToolTip = (String) values[6];
        this.linkURL = (String) values[7];
        this.style = (String) values[8];
        this.styleClass = (String) values[9];
        this.summary = (String) values[10];
        this.tabIndex = ((Integer) values[11]);
        this.tabIndexSet = ((Boolean) values[12]);
        this.type = (String) values[13];
        this.visible = ((Boolean) values[14]);
        this.visibleSet = ((Boolean) values[15]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[16];
        values[0] = super.saveState(context);
        values[1] = this.alt;
        values[2] = this.detail;
        values[3] = saveAttachedState(context, linkActionExpression);
        values[4] = this.linkTarget;
        values[5] = this.linkText;
        values[6] = this.linkToolTip;
        values[7] = this.linkURL;
        values[8] = this.style;
        values[9] = this.styleClass;
        values[10] = this.summary;
        values[11] = this.tabIndex;
        if (this.tabIndexSet) {
            values[12] = Boolean.TRUE;
        } else {
            values[12] = Boolean.FALSE;
        }
        values[13] = this.type;
        if (this.visible) {
            values[14] = Boolean.TRUE;
        } else {
            values[14] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[15] = Boolean.TRUE;
        } else {
            values[15] = Boolean.FALSE;
        }
        return values;
    }
}
