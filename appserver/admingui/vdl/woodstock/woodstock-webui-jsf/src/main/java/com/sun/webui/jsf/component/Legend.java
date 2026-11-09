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
import jakarta.faces.component.UIOutput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;

/**
 * The Legend component displays a legend to explain icons used in a page.
 */
@Component(type = "com.sun.webui.jsf.Legend",
        family = "com.sun.webui.jsf.Legend",
        displayName = "Legend", tagName = "legend",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_legend",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_legend_props")
        //CHECKSTYLE:ON
public final class Legend extends UIOutput implements NamingContainer {

    /**
     * Facet name.
     */
    public static final String LEGEND_IMAGE_FACET = "legendImage";

    /**
     * Specifies the position of the legend. Valid values are: "right" (the
     * default) and "left".
     */
    @Property(name = "position", displayName = "Legend Position")
    private String position;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style", displayName = "CSS Style(s)")
    private String style;

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "styleClass", displayName = "CSS Style Class(es)")
    private String styleClass;

    /**
     * The explanatory text that is displayed in the legend. If not specified,
     * the required field legend text is displayed.
     */
    @Property(name = "text", displayName = "Legend Text")
    private String text;

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
    private boolean visible;

    /**
     * visible set flag.
     */
    private boolean visibleSet;

    /**
     * Default constructor.
     */
    public Legend() {
        super();
        setRendererType("com.sun.webui.jsf.Legend");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Legend";
    }

    /**
     * Return a component that implements a legend image. If a facet named
     * {@code legendImage} is found that component is returned. If a facet is
     * not found an {@code Icon} component instance is returned with the id
     * {@code getId() + "_legendImage"}. The {@code Icon} instance returned is
     * ThemeImages.LABEL_REQUIRED_ICON.
     * <p>
     * If a facet is not defined then the returned {@code Icon} component is
     * created every time this method is called.
     * </p>
     *
     * @return - legendImage facet or an Icon instance
     */
    public UIComponent getLegendImage() {
        // First check if an image facet was specified
        UIComponent imageFacet = getFacet(LEGEND_IMAGE_FACET);
        // If not create one with the default icon.
        if (imageFacet != null) {
            return imageFacet;
        }

        Theme theme = ThemeUtilities
                .getTheme(FacesContext.getCurrentInstance());
        Icon icon = ThemeUtilities.getIcon(theme,
                ThemeImages.LEGEND_REQUIRED_ICON);
        icon.setId(
                ComponentUtilities.
                        createPrivateFacetId(this, LEGEND_IMAGE_FACET));
        icon.setParent(this);

        return icon;
    }

    @Property(name = "id")
    @Override
    public void setId(final String id) {
        super.setId(id);
    }

    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean newRendered) {
        super.setRendered(newRendered);
    }

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
     * Specifies the position of the legend.Valid values are: "right" (the
     * default) and "left".
     *
     * @return String
     */
    public String getPosition() {
        if (this.position != null) {
            return this.position;
        }
        ValueExpression vb = getValueExpression("position");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Specifies the position of the legend.Valid values are: "right" (the
     * default) and "left".
     *
     * @param newPosition position
     * @see #getPosition()
     */
    public void setPosition(final String newPosition) {
        this.position = newPosition;
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
     * The explanatory text that is displayed in the legend. If not specified,
     * the required field legend text is displayed.
     * @return String
     */
    public String getText() {
        if (this.text != null) {
            return this.text;
        }
        ValueExpression vb = getValueExpression("text");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The explanatory text that is displayed in the legend. If not specified,
     * the required field legend text is displayed.
     *
     * @see #getText()
     * @param newText text
     */
    public void setText(final String newText) {
        this.text = newText;
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
        this.position = (String) values[1];
        this.style = (String) values[2];
        this.styleClass = (String) values[3];
        this.text = (String) values[4];
        this.visible = ((Boolean) values[5]);
        this.visibleSet = ((Boolean) values[6]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[7];
        values[0] = super.saveState(context);
        values[1] = this.position;
        values[2] = this.style;
        values[3] = this.styleClass;
        values[4] = this.text;
        if (this.visible) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] = Boolean.FALSE;
        }
        return values;
    }
}
