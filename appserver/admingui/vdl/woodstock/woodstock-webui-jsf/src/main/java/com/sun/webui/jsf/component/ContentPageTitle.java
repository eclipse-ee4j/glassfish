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
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;

/**
 * The ContentPageTitle component is used to display a page title.
 */
@Component(type = "com.sun.webui.jsf.ContentPageTitle",
        family = "com.sun.webui.jsf.ContentPageTitle",
        displayName = "Content Area",
        instanceName = "contentArea",
        tagName = "contentPageTitle",
        //CHECKSTYLE:OFF
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_content_page_title",
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_content_page_title_props")
        //CHECKSTYLE:ON
public final class ContentPageTitle extends UIComponentBase
        implements NamingContainer {

    /**
     * Content bottom separator.
     */
    public static final String CONTENT_BOTTOM_SEPARATOR = "pageSeparator";


    /**
     * The help text to display just below the page title.
     */
    @Property(name = "helpText",
            displayName = "Help Text",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String helpText = null;

    /**
     * Indicates that the page title separator should be displayed, when set to
     * true. The separator is a thin line that displays by default when bottom
     * buttons are used. Set this attribute to false if the separator should not
     * be displayed. This attribute also determines whether to display the
     * pageSeparator facet.
     */
    @Property(name = "separator", displayName = "Separator")
    private boolean separator = false;

    /**
     * Separator set flag.
     */
    private boolean separatorSet = false;

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
            // CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StyleClassPropertyEditor")
            //CHECKSTYLE:ON
    private String styleClass = null;

    /**
     * The text to display as the page title.
     */
    @Property(name = "title",
            displayName = "Title",
            category = "Appearance",
            // CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            // CHECKSTYLE:ON
    private String title = null;

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     */
    @Property(name = "visible", displayName = "Visible")
    private boolean visible = false;

    /**
     * visible set flag.
     */
    private boolean visibleSet = false;

    /**
     * Default Constructor.
     */
    public ContentPageTitle() {
        super();
        setRendererType("com.sun.webui.jsf.ContentPageTitle");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.ContentPageTitle";
    }

    /**
     * Return a component that implements an page separator image. If a facet
     * named {@code pageSeparator} is found that component is returned. If a
     * facet is not found a {@code PageSeparator} component instance is returned
     * with the id {@code getId() + "_pageSeparator"}.
     * <p>
     * If a facet is not defined then the returned {@code Icon} component is
     * created every time this method is called.
     * </p>
     *
     * @return - pageSeparator facet or a PageSeparator instance
     */
    public UIComponent getBottomPageSeparator() {
        // First check if a buttons facet was defined
        UIComponent bottomFacet = getFacet(CONTENT_BOTTOM_SEPARATOR);
        if (bottomFacet != null) {
            return bottomFacet;
        }

        bottomFacet = new PageSeparator();
        bottomFacet.setId(ComponentUtilities.createPrivateFacetId(this,
                CONTENT_BOTTOM_SEPARATOR));
        bottomFacet.setParent(this);

        return bottomFacet;
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
     * The help text to display just below the page title.
     * @return String
     */
    public String getHelpText() {
        if (this.helpText != null) {
            return this.helpText;
        }
        ValueExpression vb = getValueExpression("helpText");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The help text to display just below the page title.
     *
     * @see #getHelpText()
     * @param newHelpText helpText
     */
    public void setHelpText(final String newHelpText) {
        this.helpText = newHelpText;
    }

    /**
     * Indicates that the page title separator should be displayed, when set to
     * true. The separator is a thin line that displays by default when bottom
     * buttons are used. Set this attribute to false if the separator should not
     * be displayed. This attribute also determines whether to display the
     * pageSeparator facet.
     * @return {@code boolean}
     */
    public boolean isSeparator() {
        if (this.separatorSet) {
            return this.separator;
        }
        ValueExpression vb = getValueExpression("separator");
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
     * Indicates that the page title separator should be displayed, when set to
     * true. The separator is a thin line that displays by default when bottom
     * buttons are used. Set this attribute to false if the separator should not
     * be displayed. This attribute also determines whether to display the
     * pageSeparator facet.
     *
     * @see #isSeparator()
     * @param newSeparator separator
     */
    public void setSeparator(final boolean newSeparator) {
        this.separator = newSeparator;
        this.separatorSet = true;
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
        this.helpText = (String) values[1];
        this.separator = ((Boolean) values[2]);
        this.separatorSet = ((Boolean) values[3]);
        this.style = (String) values[4];
        this.styleClass = (String) values[5];
        this.title = (String) values[6];
        this.visible = ((Boolean) values[7]);
        this.visibleSet = ((Boolean) values[8]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[9];
        values[0] = super.saveState(context);
        values[1] = this.helpText;
        if (this.separator) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.separatorSet) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        values[4] = this.style;
        values[5] = this.styleClass;
        values[6] = this.title;
        if (this.visible) {
            values[7] = Boolean.TRUE;
        } else {
            values[7] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] = Boolean.FALSE;
        }
        return values;
    }
}
