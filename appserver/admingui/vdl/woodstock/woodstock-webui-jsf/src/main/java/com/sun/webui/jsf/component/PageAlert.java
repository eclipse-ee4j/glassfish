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

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;

/**
 * The PageAlert component displays a full page alert.
 */
@Component(type = "com.sun.webui.jsf.PageAlert",
        family = "com.sun.webui.jsf.PageAlert",
        displayName = "Page Alert",
        tagName = "pageAlert",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_page_alert",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_page_alert_props")
        //CHECKSTYLE:ON
public final class PageAlert extends UIComponentBase
        implements NamingContainer {

    /**
     * Page alert input facet.
     */
    public static final String PAGEALERT_INPUT_FACET = "pageAlertInput";

    /**
     * Page alert title facet.
     */
    public static final String PAGEALERT_TITLE_FACET = "pageAlertTitle";

    /**
     * Page alert buttons facet.
     */
    public static final String PAGEALERT_BUTTONS_FACET = "pageAlertButtons";

    /**
     * Page alert separator facet.
     */
    public static final String PAGEALERT_SEPARATOR_FACET =
            "pageAlertSeparator";

    /**
     * Page alert image facet.
     */
    public static final String PAGEALERT_IMAGE_FACET = "pageAlertImage";

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
     * Detailed message text for the alert. This message might include more
     * information about the alert and instructions for what to do about the
     * alert.
     */
    @Property(name = "detail",
            displayName = "Detail Message",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String detail;

    /**
     * Flag indicating that the message text should be escaped so that it is not
     * interpreted by the browser.
     */
    @Property(name = "escape",
            displayName = "Escape",
            category = "Data")
    private boolean escape;

    /**
     * escape set flag.
     */
    private boolean escapeSet;

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
     * <b>Deprecated.</b>
     * <i>Use the title attribute to display the message summary in the page
     * title.</i>
     */
    @Property(name = "summary",
            displayName = "Summary Message",
            category = "Appearance",
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
     * The text to display as the page title.
     */
    @Property(name = "title",
            displayName = "Title",
            category = "Appearance", isDefault = true,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String title;

    /**
     * The type or category of alert. The type attribute can be set to one of
     * the following: "question", "information", "warning" or "error". The
     * default type is error.
     */
    @Property(name = "type",
            displayName = "Alert Type",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.PageAlertTypesEditor")
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
    public PageAlert() {
        super();
        setRendererType("com.sun.webui.jsf.PageAlert");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.PageAlert";
    }

    /**
     * Get the page alert input facet.
     *
     * @return A Back button (or a facet with buttons).
     */
    public UIComponent getPageAlertInput() {
        return getFacet(PAGEALERT_INPUT_FACET);
    }

    /**
     * Return a component that implements the page alert's title text. If a
     * facet named {@code pageAlertTitle} is found that component is
     * returned.
     * If a facet is not found an {@code StaticText} component instance is
     * returned with the id
     * {@code getId() + "_pageAlertTitle"}. The {@code StaticText}
     * instance is * initialized with the values from
     *
     * <ul>
     * <li>{@code getSafeTitle()}
     * </ul>
     *
     * <p>
     * If a facet is not defined then the returned {@code StaticText}
     * component is created every time this method is called.
     * </p>
     *
     * @return pageAlertTitle facet or a StaticText instance
     */
    public UIComponent getPageAlertTitle() {
        UIComponent titleFacet = getFacet(PAGEALERT_TITLE_FACET);
        if (titleFacet != null) {
            return titleFacet;
        }

        StaticText alertTitle = new StaticText();
        alertTitle.setId(
                ComponentUtilities.createPrivateFacetId(this,
                        PAGEALERT_TITLE_FACET));
        alertTitle.setParent(this);
        alertTitle.setText(getSafeTitle());
        return alertTitle;
    }

    /**
     * Get buttons for the Page Alert. Return a set of buttons if they were
     * specified in the facet
     *
     * @return A Back button (or a facet with buttons).
     */
    public UIComponent getPageAlertButtons() {
        // First check if a buttons facet was defined
        UIComponent buttonFacet = getFacet(PAGEALERT_BUTTONS_FACET);
        return buttonFacet;
    }

    /**
     * Return a component that implements a page separator. If a facet named
     * {@code pageAlertSeparator} is found that component is returned.
     * If a facet is not found a {@code PageSeparator} component instance
     * is returned with the id
     * {@code getId() + "_pageAlertSeparator"}.
     * <p>
     * If a facet is not defined then the returned {@code PageSeparator}
     * component is created every time this method is called.
     * </p>
     *
     * @return pageAlertSeparator facet or a PageSeparator instance
     */
    public UIComponent getPageAlertSeparator() {
        // First check if a pageAlertSeparator facet was defined
        UIComponent separatorFacet = getFacet(PAGEALERT_SEPARATOR_FACET);
        if (separatorFacet != null) {
            return separatorFacet;
        }

        PageSeparator separator = new PageSeparator();
        separator.setId(ComponentUtilities.createPrivateFacetId(this,
                PAGEALERT_SEPARATOR_FACET));
        separator.setParent(this);
        return separator;
    }

    /**
     * Return a component that implements a page alert image. If a facet named
     * {@code pageAlertImage} is found that component is returned.
     * If a facet is not found an {@code Icon} component instance is
     * returned with the id
     * {@code getId() + "_pageAlertImage"}. The {@code Icon} instance
     * returned is determined from the value of {@code getType()}. If the
     * returned value is not a recognized value,
     * {@code ThemeImages.ALERT_ERROR_LARGE} is used. The {@code Icon}
     * instance is initialized with the value of {@code getAlt()}
     * <p>
     * If a facet is not defined then the returned {@code Icon} component
     * is created every time this method is called.
     * </p>
     *
     * @return - pageAlertImage facet or an Icon instance
     */
    public UIComponent getPageAlertImage() {
        // First check if a PAGEALERT_IMAGE_FACET  facet was defined
        UIComponent imageFacet = getFacet(PAGEALERT_IMAGE_FACET);
        if (imageFacet != null) {
            return imageFacet;
        }

        Icon icon = ThemeUtilities.getIcon(getTheme(), getIconIdentifier());
        String imageAlt = getAlt();
        if (imageAlt != null) {
            icon.setAlt(imageAlt);
        }
        icon.setId(ComponentUtilities.createPrivateFacetId(this,
                        PAGEALERT_IMAGE_FACET));
        icon.setParent(this);
        return icon;
    }

    /**
     * Get safe title.
     * @return String
     */
    public String getSafeTitle() {
        String alertTitle = getTitle();
        if (alertTitle == null) {
            alertTitle = getAlt();
            if (alertTitle == null) {
                alertTitle = "";
            }
        }
        return alertTitle;
    }

    /**
     * Get the alert icon identifier.
     * @return String
     */
    private String getIconIdentifier() {
        String alertType = getType();
        if (alertType != null) {
            alertType = alertType.toLowerCase();
            if (alertType.startsWith("warn")) {
                return ThemeImages.ALERT_WARNING_LARGE;
            } else if (alertType.startsWith("ques")) {
                return ThemeImages.ALERT_HELP_LARGE;
            } else if (alertType.startsWith("info")) {
                return ThemeImages.ALERT_INFO_LARGE;
            }
        }
        return ThemeImages.ALERT_ERROR_LARGE;
    }

    /**
     * Utility to get theme.
     * @return Theme
     */
    private Theme getTheme() {
        return ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
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
     * Detailed message text for the alert. This message might include more
     * information about the alert and instructions for what to do about the
     * alert.
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
     * Detailed message text for the alert. This message might include more
     * information about the alert and instructions for what to do about the
     * alert.
     *
     * @see #getDetail()
     * @param newDetail detail
     */
    public void setDetail(final String newDetail) {
        this.detail = newDetail;
    }

    /**
     * Flag indicating that the message text should be escaped so that it is not
     * interpreted by the browser.
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
     * Flag indicating that the message text should be escaped so that it is not
     * interpreted by the browser.
     *
     * @see #isEscape()
     * @param newEscape escape
     */
    public void setEscape(final boolean newEscape) {
        this.escape = newEscape;
        this.escapeSet = true;
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
     * <b>Deprecated.</b>
     *
     * <i>Use the title attribute to display the message
     * summary in the page title.</i>
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
     * <b>Deprecated.</b>
     *
     * <i>Use the title attribute to display the message summary in the page
     * title.</i>
     *
     * @see #getSummary()
     * @param newSummary summary
     */
    public void setSummary(final String newSummary) {
        this.summary = newSummary;
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
     * The text to display as the page title.
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
     * The text to display as the page title.
     *
     * @see #getTitle()
     * @param newTitle title
     */
    public void setTitle(final String newTitle) {
        this.title = newTitle;
    }

    /**
     * The type or category of alert. The type attribute can be set to one of
     * the following: "question", "information", "warning" or "error". The
     * default type is error.
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
        return "error";
    }

    /**
     * The type or category of alert. The type attribute can be set to one of
     * the following: "question", "information", "warning" or "error". The
     * default type is error.
     *
     * @see #getType()
     * @param newType type
     */
    public void setType(final String newType) {
        this.type = newType;
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
        this.detail = (String) values[2];
        this.escape = ((Boolean) values[3]);
        this.escapeSet = ((Boolean) values[4]);
        this.style = (String) values[5];
        this.styleClass = (String) values[6];
        this.summary = (String) values[7];
        this.tabIndex = ((Integer) values[8]);
        this.tabIndexSet = ((Boolean) values[9]);
        this.title = (String) values[10];
        this.type = (String) values[11];
        this.visible = ((Boolean) values[12]);
        this.visibleSet = ((Boolean) values[13]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[14];
        values[0] = super.saveState(context);
        values[1] = this.alt;
        values[2] = this.detail;
        if (this.escape) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.escapeSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        values[5] = this.style;
        values[6] = this.styleClass;
        values[7] = this.summary;
        values[8] = this.tabIndex;
        if (this.tabIndexSet) {
            values[9] = Boolean.TRUE;
        } else {
            values[9] = Boolean.FALSE;
        }
        values[10] = this.title;
        values[11] = this.type;
        if (this.visible) {
            values[12] = Boolean.TRUE;
        } else {
            values[12] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[13] = Boolean.TRUE;
        } else {
            values[13] = Boolean.FALSE;
        }
        return values;
    }
}
