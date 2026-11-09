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
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIOutput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;

/**
 * The VersionPage component is used to display a version page.
 */
@Component(type = "com.sun.webui.jsf.VersionPage",
        family = "com.sun.webui.jsf.VersionPage",
        displayName = "Version Page",
        tagName = "versionPage",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_version_page",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_version_page_props")
        //CHECKSTYLE:ON
public final class VersionPage extends UIOutput implements NamingContainer {

    /**
     * The application copyright information. This data is not escaped. If this
     * is user-provided information (not common), the developer is responsible
     * for escaping this property to prevent XSS attacks.
     */
    @Property(name = "copyrightString",
            displayName = "Copyright String",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String copyrightString;

    /**
     * The description to use for the Product Name Image displayed in the
     * version page.
     */
    @Property(name = "productImageDescription",
            displayName = "Product Image Description",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String productImageDescription;

    /**
     * The height to use for the Product Name Image.
     */
    @Property(name = "productImageHeight",
            displayName = "Product Image Height",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int productImageHeight = Integer.MIN_VALUE;

    /**
     * productImageHeight set flag.
     */
    private boolean productImageHeightSet;

    /**
     * The URL to use for the Product Name Image.
     */
    @Property(name = "productImageURL",
            displayName = "Product Image URL",
            category = "Navigation",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.SunWebUrlPropertyEditor")
            //CHECKSTYLE:ON
    private String productImageURL;

    /**
     * The width to use for the Product Name Image.
     */
    @Property(name = "productImageWidth",
            displayName = "Product Image Width",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int productImageWidth = Integer.MIN_VALUE;

    /**
     * productImageWidth set flag.
     */
    private boolean productImageWidthSet;

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
     * The name of version information file containing the formatted application
     * version and copyright message.
     */
    @Property(name = "versionInformationFile",
            displayName = "Version Information File",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor",
            //CHECKSTYLE:ON
            isHidden = true, isAttribute = false)
    private String versionInformationFile;

    /**
     * The application version.
     */
    @Property(name = "versionString",
            displayName = "Version String",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String versionString;

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
            displayName = "Visible")
    private boolean visible;

    /**
     * visible set flag.
     */
    private boolean visibleSet;

    /**
     * Construct a new {@code VersionPage}.
     */
    public VersionPage() {
        super();
        setRendererType("com.sun.webui.jsf.VersionPage");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.VersionPage";
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

    // Hide converter
    @Property(isHidden = true, isAttribute = false)
    @Override
    public Converter getConverter() {
        return super.getConverter();
    }

    // Hide value
    @Property(isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    /**
     * The application copyright information. This data is not escaped. If this
     * is user-provided information (not common), the developer is responsible
     * for escaping this property to prevent XSS attacks.
     * @return String
     */
    public String getCopyrightString() {
        if (this.copyrightString != null) {
            return this.copyrightString;
        }
        ValueExpression vb = getValueExpression("copyrightString");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The application copyright information.
     *
     * @see #getCopyrightString()
     * @param newCopyrightString copyrightString
     */
    public void setCopyrightString(final String newCopyrightString) {
        this.copyrightString = newCopyrightString;
    }

    /**
     * The description to use for the Product Name Image displayed in the
     * version page.
     * @return String
     */
    public String getProductImageDescription() {
        if (this.productImageDescription != null) {
            return this.productImageDescription;
        }
        ValueExpression vb = getValueExpression("productImageDescription");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The description to use for the Product Name Image displayed in the
     * version page.
     *
     * @see #getProductImageDescription()
     * @param newProductImageDescription productImageDescription
     */
    public void setProductImageDescription(
            final String newProductImageDescription) {

        this.productImageDescription = newProductImageDescription;
    }

    /**
     * The height to use for the Product Name Image.
     * @return int
     */
    public int getProductImageHeight() {
        if (this.productImageHeightSet) {
            return this.productImageHeight;
        }
        ValueExpression vb = getValueExpression("productImageHeight");
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
     * The height to use for the Product Name Image.
     *
     * @see #getProductImageHeight()
     * @param newProductImageHeight productImageHeight
     */
    public void setProductImageHeight(final int newProductImageHeight) {
        this.productImageHeight = newProductImageHeight;
        this.productImageHeightSet = true;
    }

    /**
     * The URL to use for the Product Name Image.
     * @return String
     */
    public String getProductImageURL() {
        if (this.productImageURL != null) {
            return this.productImageURL;
        }
        ValueExpression vb = getValueExpression("productImageURL");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The URL to use for the Product Name Image.
     *
     * @see #getProductImageURL()
     * @param newProductImageURL productImageURL
     */
    public void setProductImageURL(final String newProductImageURL) {
        this.productImageURL = newProductImageURL;
    }

    /**
     * The width to use for the Product Name Image.
     * @return int
     */
    public int getProductImageWidth() {
        if (this.productImageWidthSet) {
            return this.productImageWidth;
        }
        ValueExpression vb = getValueExpression("productImageWidth");
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
     * The width to use for the Product Name Image.
     *
     * @see #getProductImageWidth()
     * @param newProductImageWidth productImageWidth
     */
    public void setProductImageWidth(final int newProductImageWidth) {
        this.productImageWidth = newProductImageWidth;
        this.productImageWidthSet = true;
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
     * The name of version information file containing the formatted application
     * version and copyright message.
     * @return String
     */
    public String getVersionInformationFile() {
        if (this.versionInformationFile != null) {
            return this.versionInformationFile;
        }
        ValueExpression vb = getValueExpression("versionInformationFile");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The name of version information file containing the formatted application
     * version and copyright message.
     *
     * @see #getVersionInformationFile()
     * @param newVersionInformationFile versionInformationFile
     */
    public void setVersionInformationFile(
            final String newVersionInformationFile) {

        this.versionInformationFile = newVersionInformationFile;
    }

    /**
     * The application version.
     * @return String
     */
    public String getVersionString() {
        if (this.versionString != null) {
            return this.versionString;
        }
        ValueExpression vb = getValueExpression("versionString");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The application version.
     *
     * @see #getVersionString()
     * @param newVersionString versionString
     */
    public void setVersionString(final String newVersionString) {
        this.versionString = newVersionString;
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
        this.copyrightString = (String) values[1];
        this.productImageDescription = (String) values[2];
        this.productImageHeight = ((Integer) values[3]);
        this.productImageHeightSet = ((Boolean) values[4]);
        this.productImageURL = (String) values[5];
        this.productImageWidth = ((Integer) values[6]);
        this.productImageWidthSet = ((Boolean) values[7]);
        this.style = (String) values[8];
        this.styleClass = (String) values[9];
        this.versionInformationFile = (String) values[10];
        this.versionString = (String) values[11];
        this.visible = ((Boolean) values[12]);
        this.visibleSet = ((Boolean) values[13]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[14];
        values[0] = super.saveState(context);
        values[1] = this.copyrightString;
        values[2] = this.productImageDescription;
        values[3] = this.productImageHeight;
        if (this.productImageHeightSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        values[5] = this.productImageURL;
        values[6] = this.productImageWidth;
        if (this.productImageWidthSet) {
            values[7] = Boolean.TRUE;
        } else {
            values[7] = Boolean.FALSE;
        }
        values[8] = this.style;
        values[9] = this.styleClass;
        values[10] = this.versionInformationFile;
        values[11] = this.versionString;
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
